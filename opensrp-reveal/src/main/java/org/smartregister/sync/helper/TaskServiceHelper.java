package org.smartregister.sync.helper;

import static org.smartregister.AllConstants.COUNT;
import static org.smartregister.AllConstants.PerformanceMonitoring.ACTION;
import static org.smartregister.AllConstants.PerformanceMonitoring.FETCH;
import static org.smartregister.AllConstants.PerformanceMonitoring.PUSH;
import static org.smartregister.AllConstants.PerformanceMonitoring.TASK_SYNC;
import static org.smartregister.AllConstants.PerformanceMonitoring.TEAM;
import static org.smartregister.reveal.api.RevealService.ADD_TASK_URL;
import static org.smartregister.reveal.api.RevealService.SYNC_TASK_URL;
import static org.smartregister.reveal.api.RevealService.UPDATE_STATUS_URL;
import static org.smartregister.util.PerformanceMonitoringUtils.addAttribute;
import static org.smartregister.util.PerformanceMonitoringUtils.clearTraceAttributes;
import static org.smartregister.util.PerformanceMonitoringUtils.initTrace;
import static org.smartregister.util.PerformanceMonitoringUtils.startTrace;
import static org.smartregister.util.PerformanceMonitoringUtils.stopTrace;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.VisibleForTesting;
import com.google.firebase.perf.metrics.Trace;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.smartregister.AllConstants;
import org.smartregister.CoreLibrary;
import org.smartregister.commonregistry.CommonPersonObject;
import org.smartregister.commonregistry.CommonRepository;
import org.smartregister.domain.Response;
import org.smartregister.domain.SyncEntity;
import org.smartregister.domain.SyncProgress;
import org.smartregister.domain.Task;
import org.smartregister.domain.TaskUpdate;
import org.smartregister.exception.NoHttpResponseException;
import org.smartregister.repository.AllSharedPreferences;
import org.smartregister.repository.BaseRepository;
import org.smartregister.repository.TaskRepository;
import org.smartregister.reveal.application.RevealApplication;
import org.smartregister.reveal.model.PersonName;
import org.smartregister.reveal.model.PersonRequest;
import org.smartregister.service.HTTPAgent;
import org.smartregister.util.DateTimeTypeConverter;
import org.smartregister.util.Utils;
import timber.log.Timber;

public class TaskServiceHelper extends BaseHelper {

    private final AllSharedPreferences allSharedPreferences;

    protected final Context context;

    private TaskRepository taskRepository;

    public static final String TASK_LAST_SYNC_DATE = "TASK_LAST_SYNC_DATE";

    private static final String TASKS_NOT_PROCESSED = "Tasks with identifiers not processed: ";

    public static final Gson taskGson = new GsonBuilder()
            .registerTypeAdapter(DateTime.class, new DateTimeTypeConverter("yyyy-MM-dd'T'HHmm")).create();

    protected static TaskServiceHelper instance;

    private boolean syncByGroupIdentifier = true;

    private Trace taskSyncTrace;

    private String team;

    private long totalRecords;

    private SyncProgress syncProgress;

    private TaskServiceProcessor taskServiceProcessor;

    private CommonRepository commonRepository;

    /**
     * If set to false tasks will sync by owner otherwise defaults to sync by group identifier
     *
     * @param syncByGroupIdentifier flag for determining if tasks should be synced by group identifier
     *                              or owner (username)
     */
    public void setSyncByGroupIdentifier(boolean syncByGroupIdentifier) {
        this.syncByGroupIdentifier = syncByGroupIdentifier;
    }

    public boolean isSyncByGroupIdentifier() {
        return syncByGroupIdentifier;
    }

    public static TaskServiceHelper getInstance() {
        if (instance == null) {
            instance = new TaskServiceHelper(CoreLibrary.getInstance().context().getTaskRepository());
        }
        return instance;
    }

    @VisibleForTesting
    public TaskServiceHelper(TaskRepository taskRepository) {
        this.context = CoreLibrary.getInstance().context().applicationContext();
        this.taskRepository = taskRepository;
        this.allSharedPreferences = CoreLibrary.getInstance().context().allSharedPreferences();
        this.taskSyncTrace = initTrace(TASK_SYNC);
        String providerId = allSharedPreferences.fetchRegisteredANM();
        team = allSharedPreferences.fetchDefaultTeam(providerId);
        this.taskServiceProcessor = TaskServiceProcessor.getInstance();
    }

    public List<Task> syncTasks() {
        List<Task> fetchedTasks = fetchTasksFromServer();
        taskServiceProcessor.processDuplicateTasks();
        syncCreatedTaskToServer();
        syncTaskStatusToServer();
        return fetchedTasks;
    }

    protected List<String> getLocationIds() {
        return CoreLibrary.getInstance().context().getLocationRepository().getAllLocationIds();
    }

    protected Set<String> getPlanDefinitionIds() {
        return CoreLibrary.getInstance().context().getPlanDefinitionRepository()
                .findAllPlanDefinitionIds();
    }

    public List<Task> fetchTasksFromServer() {
        Set<String> planDefinitions = getPlanDefinitionIds();
        List<String> groups = getLocationIds();

        syncProgress = new SyncProgress();
        syncProgress.setSyncEntity(SyncEntity.TASKS);
        syncProgress.setTotalRecords(totalRecords);

        List<Task> tasks = batchFetchTasksFromServer(planDefinitions, groups, new ArrayList<>(), true);

        syncProgress.setPercentageSynced(Utils.calculatePercentage(totalRecords, tasks.size()));
        sendSyncProgressBroadcast(syncProgress, context);

        return tasks;
    }

    private List<Task> batchFetchTasksFromServer(Set<String> planDefinitions, List<String> groups,
            List<Task> batchFetchedTasks, boolean returnCount) {
        long serverVersion = 0;
        try {
            serverVersion = Long.parseLong(allSharedPreferences.getPreference(TASK_LAST_SYNC_DATE));
        } catch (NumberFormatException e) {
            Timber.e(e, "EXCEPTION %s", e.toString());
        }
        if (serverVersion > 0) {
            serverVersion += 1;
        }
        try {
            long maxServerVersion = 0L;
            String tasksResponse = fetchTasks(planDefinitions, groups, serverVersion, returnCount);
            startTaskTrace(FETCH, 0);
            List<Task> tasks = taskGson.fromJson(tasksResponse, new TypeToken<List<Task>>() {
            }.getType());

            addAttribute(taskSyncTrace, COUNT, String.valueOf(tasks.size()));
            stopTrace(taskSyncTrace);
            if (tasks != null && tasks.size() > 0) {
                for (Task task : tasks) {
                    try {
                        task.setSyncStatus(BaseRepository.TYPE_Synced);
                        task.setLastModified(new DateTime());
                        taskRepository.addOrUpdate(task);
                    } catch (Exception e) {
                        Timber.e(e, "Error saving task %s", task.getIdentifier());
                    }
                }
            }
            if (!Utils.isEmptyCollection(tasks)) {
                allSharedPreferences.savePreference(TASK_LAST_SYNC_DATE,
                        String.valueOf(getTaskMaxServerVersion(tasks, maxServerVersion)));
                // retry fetch since there were items synced from the server
                syncProgress.setPercentageSynced(Utils.calculatePercentage(totalRecords, tasks.size()));
                sendSyncProgressBroadcast(syncProgress, context);
                return batchFetchTasksFromServer(planDefinitions, groups, tasks, false);
            }
        } catch (Exception e) {
            Timber.e(e, "Error fetching tasks from server");
        }
        return batchFetchedTasks;
    }

    private String fetchTasks(Set<String> plan, List<String> group, Long serverVersion, boolean returnCount)
            throws Exception {
        HTTPAgent httpAgent = getHttpAgent();
        String baseUrl = CoreLibrary.getInstance().context().configuration().dristhiBaseURL();
        String endString = "/";

        if (baseUrl.endsWith(endString)) {
            baseUrl = baseUrl.substring(0, baseUrl.lastIndexOf(endString));
        }

        JSONObject request = isSyncByGroupIdentifier() ? getSyncTaskRequest(plan, group, serverVersion) :
                getSyncTaskRequest(plan, getOwner(), serverVersion);
        request.put(AllConstants.RETURN_COUNT, returnCount);

        if (httpAgent == null) {
            throw new IllegalArgumentException(SYNC_TASK_URL + " http agent is null");
        }

        Response resp = httpAgent.post(MessageFormat.format("{0}{1}", baseUrl, SYNC_TASK_URL),
                request.toString());

        if (resp.isFailure()) {
            throw new NoHttpResponseException(SYNC_TASK_URL + " not returned data");
        }

        if (returnCount) {
            totalRecords = resp.getTotalRecords();
        }

        return resp.payload().toString();
    }

    protected String getOwner() {
        return allSharedPreferences.fetchRegisteredANM();
    }

    @NonNull
    private JSONObject getSyncTaskRequest(Set<String> plan, List<String> group, Long serverVersion)
            throws JSONException {
        JSONObject request = new JSONObject();
        request.put("plan", new JSONArray(plan));
        request.put("group", new JSONArray(group));
        request.put("serverVersion", serverVersion);
        return request;
    }

    @NonNull
    private JSONObject getSyncTaskRequest(Set<String> plan, String owner, Long serverVersion) throws JSONException {
        JSONObject request = new JSONObject();
        request.put("plan", new JSONArray(plan));
        request.put("owner", owner);
        request.put("serverVersion", serverVersion);
        return request;
    }

    private long getTaskMaxServerVersion(List<Task> tasks, long maxServerVersion) {
        for (Task task : tasks) {
            long serverVersion = task.getServerVersion();
            if (serverVersion > maxServerVersion) {
                maxServerVersion = serverVersion;
            }
        }

        return maxServerVersion;
    }

    public void syncTaskStatusToServer() {
        HTTPAgent httpAgent = getHttpAgent();
        List<TaskUpdate> updates = taskRepository.getUnSyncedTaskStatus();
        if (!updates.isEmpty()) {
            String jsonPayload = new Gson().toJson(updates);

            String baseUrl = CoreLibrary.getInstance().context().configuration().dristhiBaseURL();
            Response<String> response = httpAgent.postWithJsonResponse(
                    MessageFormat.format("{0}/{1}",
                            baseUrl,
                            UPDATE_STATUS_URL),
                    jsonPayload);

            if (response.isFailure()) {
                Timber.e("Update Status failedd: %s", response.payload());
                return;
            }

            if (response.payload() != null) {
                try {
                    JSONObject idObject = new JSONObject(response.payload());
                    JSONArray updatedIds = idObject.optJSONArray("task_ids");
                    if (updatedIds != null) {
                        for (int i = 0; i < updatedIds.length(); i++) {
                            taskRepository.markTaskAsSynced(updatedIds.get(i).toString());
                        }
                    }
                } catch (JSONException e) {
                    Timber.e(e, "Error processing the tasks payload: %s", response.payload());
                }
            }
        }
    }

    public void syncCreatedTaskToServer() {
        HTTPAgent httpAgent = getHttpAgent();
        List<Task> tasks = taskRepository.getAllUnsynchedCreatedTasks();
        String[] personIdentifiers = tasks.stream().map(task -> task.getForEntity())
                .toArray(String[]::new);
        commonRepository = RevealApplication.getInstance().getContext()
                .commonrepository("ec_family_member");
        List<CommonPersonObject> commonPersonObjects = commonRepository
                .findByBaseEntityIds(personIdentifiers);

        commonPersonObjects.stream().forEach(commonPersonObject -> {
            String baseEntityId = commonPersonObject.getColumnmaps().get("base_entity_id");
            List<Task> personTasks = tasks.stream()
                    .filter(task -> task.getForEntity().equals(baseEntityId)).collect(Collectors.toList());
            String firstName = commonPersonObject.getColumnmaps().get("first_name");
            String lastName = commonPersonObject.getColumnmaps().get("last_name");
            String gender = commonPersonObject.getColumnmaps().get("gender").toUpperCase();
            PersonName personName = PersonName.builder()
                    .use("OFFICIAL").text(firstName).family(lastName).given("").prefix("").suffix("")
                    .build();
            PersonRequest personRequest = PersonRequest.builder().identifier(UUID.fromString(baseEntityId))
                    .name(personName).gender(gender).build();
            personTasks.forEach(task -> task.setPersonRequest(personRequest));
        });
        if (!tasks.isEmpty()) {
            startTaskTrace(PUSH, tasks.size());
            String jsonPayload = taskGson.toJson(tasks);
            String baseUrl = CoreLibrary.getInstance().context().configuration().dristhiBaseURL();
            Response<String> response = httpAgent.postWithJsonResponse(
                    MessageFormat.format("{0}/{1}",
                            baseUrl,
                            ADD_TASK_URL),
                    jsonPayload);
            stopTrace(taskSyncTrace);
            if (response.isFailure()) {
                Timber.e("Failed to create new tasks on server.: %s", response.payload());
                return;
            }
            Set<String> unprocessedIds = new HashSet<>();
            if (StringUtils.isNotBlank(response.payload())) {
                if (response.payload().startsWith(TASKS_NOT_PROCESSED)) {
                    unprocessedIds.addAll(Arrays
                            .asList(response.payload().substring(TASKS_NOT_PROCESSED.length()).split(",")));
                }
                for (Task task : tasks) {
                    if (!unprocessedIds.contains(task.getIdentifier())) {
                        taskRepository.markTaskAsSynced(task.getIdentifier());
                    }
                }
            }

        }
    }

    private HTTPAgent getHttpAgent() {
        return CoreLibrary.getInstance().context().getHttpAgent();
    }

    private void startTaskTrace(String action, int count) {
        clearTraceAttributes(taskSyncTrace);
        addAttribute(taskSyncTrace, TEAM, team);
        addAttribute(taskSyncTrace, ACTION, action);
        addAttribute(taskSyncTrace, COUNT, String.valueOf(count));
        startTrace(taskSyncTrace);
    }
}


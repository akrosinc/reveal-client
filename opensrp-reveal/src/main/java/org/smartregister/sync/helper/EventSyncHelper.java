package org.smartregister.sync.helper;

import static org.smartregister.AllConstants.COUNT;
import static org.smartregister.AllConstants.PerformanceMonitoring.EVENT_SYNC;
import static org.smartregister.AllConstants.RETURN_COUNT;
import static org.smartregister.reveal.api.RevealService.EVENT_ADD_URL;
import static org.smartregister.reveal.api.RevealService.EVENT_SYNC_URL;
import static org.smartregister.util.PerformanceMonitoringUtils.addAttribute;
import static org.smartregister.util.PerformanceMonitoringUtils.initTrace;
import static org.smartregister.util.PerformanceMonitoringUtils.startTrace;
import static org.smartregister.util.PerformanceMonitoringUtils.stopTrace;

import android.content.Context;
import android.util.Log;
import android.util.Pair;

import com.google.firebase.perf.metrics.Trace;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.smartregister.AllConstants;
import org.smartregister.CoreLibrary;
import org.smartregister.SyncConfiguration;
import org.smartregister.domain.Response;
import org.smartregister.domain.SyncEntity;
import org.smartregister.domain.SyncProgress;
import org.smartregister.domain.db.EventClient;
import org.smartregister.repository.AllSharedPreferences;
import org.smartregister.repository.EventClientRepository;
import org.smartregister.reveal.util.FirebaseLogger;
import org.smartregister.service.HTTPAgent;
import org.smartregister.util.NetworkUtils;
import org.smartregister.util.SyncUtils;
import org.smartregister.util.Utils;
import org.smartregister.view.activity.DrishtiApplication;

import java.text.MessageFormat;
import java.util.List;
import java.util.Map;

import timber.log.Timber;

/**
 * Clean, callable Events sync helper with SyncProgress tracking,
 * Firebase Performance telemetry, and error logging.
 */
public class EventSyncHelper extends BaseHelper {

    protected static final int EVENT_PULL_LIMIT = 500;
    protected static final int EVENT_PUSH_LIMIT = 50;
    private static final String TAG = "EventSyncHelper";

    protected static EventSyncHelper instance;

    private final Context context;
    private final HTTPAgent httpAgent;
    private final SyncUtils syncUtils;
    private final AllSharedPreferences allSharedPreferences;
    private final Trace eventSyncTrace;

    private long totalRecords;
    private int fetchedRecords;
    private boolean foundNewData;
    private SyncProgress syncProgress;

    public static synchronized EventSyncHelper getInstance() {
        if (instance == null) {
            instance = new EventSyncHelper();
        }
        return instance;
    }

    private EventSyncHelper() {
        this.context = CoreLibrary.getInstance().context().applicationContext();
        this.httpAgent = CoreLibrary.getInstance().context().getHttpAgent();
        this.syncUtils = new SyncUtils(context);
        this.allSharedPreferences = CoreLibrary.getInstance().context().allSharedPreferences();
        this.eventSyncTrace = initTrace(EVENT_SYNC);
    }

    public synchronized boolean syncEvents() {
        fetchedRecords = 0;
        totalRecords = 0;
        foundNewData = false;

        // Initialize progress broadcast for UI listeners
        syncProgress = new SyncProgress();
        syncProgress.setSyncEntity(SyncEntity.EVENTS);
        syncProgress.setTotalRecords(0);
        syncProgress.setPercentageSynced(0);
        sendSyncProgressBroadcast(syncProgress, context);

        if (!NetworkUtils.isNetworkAvailable()) {
            Log.w("SYNC_TRACE_RVL", "EVENTS_SYNC_NO_NETWORK");
            return false;
        }

        try {
            startTrace(eventSyncTrace);

            boolean hasValidAuthorization = syncUtils.verifyAuthorization();
            boolean isSuccessfulPushSync = false;
            if (hasValidAuthorization || !CoreLibrary.getInstance().getSyncConfiguration().disableSyncToServerIfUserIsDisabled()) {
                isSuccessfulPushSync = pushToServer();
            }

            if (!hasValidAuthorization) {
                syncUtils.logoutUser();
                stopTrace(eventSyncTrace);
                return false;
            } else if (!syncUtils.isAppVersionAllowed()) {
                if (isSuccessfulPushSync) {
                    syncUtils.logoutUser();
                }
                stopTrace(eventSyncTrace);
                return false;
            } else {
                pullECFromServer();

                // Finalize trace telemetry
                addAttribute(eventSyncTrace, COUNT, String.valueOf(fetchedRecords));
                stopTrace(eventSyncTrace);

                // Broadcast final 100% if records were pulled or none were needed
                if (totalRecords == 0 || fetchedRecords >= totalRecords) {
                    syncProgress.setPercentageSynced(100);
                    sendSyncProgressBroadcast(syncProgress, context);
                }

                return foundNewData;
            }
        } catch (Exception e) {
            Timber.tag("Reveal Exception").w(e, "Error syncing events");
            stopTrace(eventSyncTrace);
            return false;
        }
    }

    private void pullECFromServer() {
        fetchRetry(0, true);
    }

    private synchronized void fetchRetry(final int count, boolean returnCount) {
        Log.d("SYNC_TRACE_RVL", "EVENTS_FETCH_RETRY_START t=" + System.currentTimeMillis() + " count=" + count);
        try {
            SyncConfiguration configs = CoreLibrary.getInstance().getSyncConfiguration();
            if (configs.getSyncFilterParam() == null || StringUtils.isBlank(configs.getSyncFilterValue())) {
                Log.w("SYNC_TRACE_RVL", "EVENTS_FETCH_RETRY_BLANK_FILTER_FAIL");
                return;
            }

            final ECSyncHelper ecSyncUpdater = ECSyncHelper.getInstance(context);
            String baseUrl = getFormattedBaseUrl();
            Long lastSyncDatetime = ecSyncUpdater.getLastSyncTimeStamp();

            if (httpAgent == null) {
                Log.w("SYNC_TRACE_RVL", "EVENTS_HTTP_AGENT_NULL");
                return;
            }

            String url = baseUrl + EVENT_SYNC_URL;
            Response resp;
            String requestPayload = "";

            if (configs.isSyncUsingPost()) {
                JSONObject syncParams = new JSONObject();
                syncParams.put(configs.getSyncFilterParam().value(), configs.getSyncFilterValue());
                syncParams.put("serverVersion", lastSyncDatetime);
                syncParams.put("limit", EVENT_PULL_LIMIT);
                syncParams.put(RETURN_COUNT, returnCount);
                requestPayload = syncParams.toString();
                resp = httpAgent.postWithJsonResponse(url, requestPayload);
            } else {
                url += "?" + configs.getSyncFilterParam().value() + "=" + configs.getSyncFilterValue()
                        + "&serverVersion=" + lastSyncDatetime + "&limit=" + EVENT_PULL_LIMIT;
                resp = httpAgent.fetch(url);
            }

            if (resp.isUrlError() || resp.isTimeoutError()) {
                Log.w("SYNC_TRACE_RVL", "EVENTS_FETCH_FAILED status=" + resp.status());
                FirebaseLogger.logApiFailures(requestPayload, resp);
                return;
            }

            if (resp.isFailure()) {
                FirebaseLogger.logApiFailures(requestPayload, resp);
                fetchFailed(count);
                return;
            }

            if (returnCount) {
                totalRecords = resp.getTotalRecords();
                syncProgress.setTotalRecords(totalRecords);
            }

            processFetchedEvents(resp, ecSyncUpdater, count);

        } catch (Exception e) {
            Timber.tag("Reveal Exception").w(e, "Events fetch retry exception");
            fetchFailed(count);
        }
    }

    private void processFetchedEvents(Response resp, ECSyncHelper ecSyncUpdater, final int count) throws JSONException {
        int eCount;
        JSONObject jsonObject = new JSONObject();
        if (resp.payload() == null) {
            eCount = 0;
        } else {
            jsonObject = new JSONObject((String) resp.payload());
            eCount = fetchNumberOfEvents(jsonObject);
        }

        if (eCount == 0) {
            Log.d("SYNC_TRACE_RVL", "EVENTS_NOTHING_NEW");
            syncProgress.setPercentageSynced(100);
            sendSyncProgressBroadcast(syncProgress, context);
        } else if (eCount < 0) {
            fetchFailed(count);
        } else {
            foundNewData = true;
            Pair<Long, Long> serverVersionPair = getMinMaxServerVersions(jsonObject);
            long lastServerVersion = serverVersionPair.second - 1;
            if (eCount < EVENT_PULL_LIMIT) {
                lastServerVersion = serverVersionPair.second;
            }

            boolean isSaved = ecSyncUpdater.saveAllClientsAndEvents(jsonObject);
            if (isSaved) {
                processClient(serverVersionPair);
                ecSyncUpdater.updateLastSyncTimeStamp(lastServerVersion);
            }

            fetchedRecords += eCount;

            // Broadcast real-time batch progress
            int percentage = totalRecords > 0
                    ? Utils.calculatePercentage((int) totalRecords, fetchedRecords)
                    : 100;
            syncProgress.setPercentageSynced(percentage);
            sendSyncProgressBroadcast(syncProgress, context);

            Log.d("SYNC_TRACE_RVL", "EVENTS_FETCHED t=" + System.currentTimeMillis()
                    + " count=" + fetchedRecords + "/" + totalRecords + " (" + percentage + "%)");

            fetchRetry(0, false);
        }
    }

    private void fetchFailed(int count) {
        if (count < CoreLibrary.getInstance().getSyncConfiguration().getSyncMaxRetries()) {
            fetchRetry(count + 1, false);
        } else {
            Log.w("SYNC_TRACE_RVL", "EVENTS_FETCH_FAILED_MAX_RETRIES");
        }
    }

    private void processClient(Pair<Long, Long> serverVersionPair) {
        try {
            ECSyncHelper ecUpdater = ECSyncHelper.getInstance(context);
            List<EventClient> events = ecUpdater.allEventClients(serverVersionPair.first - 1, serverVersionPair.second);
            DrishtiApplication.getInstance().getClientProcessor().processClient(events);
        } catch (Exception e) {
            Timber.tag("Reveal Exception").w(e, "Process client exception");
        }
    }

    private boolean pushToServer() {
        return pushECToServer(CoreLibrary.getInstance().context().getEventClientRepository()) &&
                (!CoreLibrary.getInstance().context().hasForeignEvents()
                        || pushECToServer(CoreLibrary.getInstance().context().getForeignEventClientRepository()));
    }

    private boolean pushECToServer(EventClientRepository db) {
        boolean isSuccessfulPushSync = true;
        String baseUrl = getFormattedBaseUrl();

        for (int i = 0; i < syncUtils.getNumOfSyncAttempts(); i++) {
            Map<String, Object> pendingEvents = db.getUnSyncedEvents(EVENT_PUSH_LIMIT);
            if (pendingEvents.isEmpty()) {
                break;
            }
            JSONObject request = new JSONObject();
            try {
                if (pendingEvents.containsKey(AllConstants.KEY.CLIENTS)) {
                    request.put(AllConstants.KEY.CLIENTS, pendingEvents.get(AllConstants.KEY.CLIENTS));
                }
                if (pendingEvents.containsKey(AllConstants.KEY.EVENTS)) {
                    request.put(AllConstants.KEY.EVENTS, pendingEvents.get(AllConstants.KEY.EVENTS));
                }
            } catch (JSONException e) {
                Timber.tag("Reveal Exception").w(e);
            }

            Response<String> response = httpAgent.post(
                    MessageFormat.format("{0}/{1}", baseUrl, EVENT_ADD_URL),
                    request.toString());

            if (response.isFailure()) {
                Timber.tag("Reveal Exception").w("Events push failed.");
                FirebaseLogger.logApiFailures(request.toString(), response);
                isSuccessfulPushSync = false;
            } else {
                db.markEventsAsSynced(pendingEvents);
                break;
            }
        }
        return isSuccessfulPushSync;
    }

    private int fetchNumberOfEvents(JSONObject jsonObject) {
        int count = -1;
        try {
            if (jsonObject != null && jsonObject.has("no_of_events")) {
                count = jsonObject.getInt("no_of_events");
            }
        } catch (JSONException e) {
            Timber.tag("Reveal Exception").w(e);
        }
        return count;
    }

    private Pair<Long, Long> getMinMaxServerVersions(JSONObject jsonObject) {
        try {
            if (jsonObject != null && jsonObject.has("events")) {
                JSONArray events = jsonObject.getJSONArray("events");
                long maxV = Long.MIN_VALUE, minV = Long.MAX_VALUE;
                for (int i = 0; i < events.length(); i++) {
                    Object o = events.get(i);
                    if (o instanceof JSONObject && ((JSONObject) o).has("serverVersion")) {
                        long v = ((JSONObject) o).getLong("serverVersion");
                        if (v > maxV) maxV = v;
                        if (v < minV) minV = v;
                    }
                }
                return Pair.create(minV, maxV);
            }
        } catch (Exception e) {
            Timber.tag("Reveal Exception").w(e);
        }
        return Pair.create(0L, 0L);
    }

    public String getFormattedBaseUrl() {
        String baseUrl = CoreLibrary.getInstance().context().configuration().dristhiBaseURL();
        if (baseUrl.endsWith("/")) {
            baseUrl = baseUrl.substring(0, baseUrl.lastIndexOf("/"));
        }
        return baseUrl;
    }

    public int getTotalRecords() {
        return (int) totalRecords;
    }

    public int getFetchedRecords() {
        return fetchedRecords;
    }
}

//package org.smartregister.sync.helper;
//
//import static org.smartregister.AllConstants.RETURN_COUNT;
//import static org.smartregister.reveal.api.RevealService.EVENT_ADD_URL;
//import static org.smartregister.reveal.api.RevealService.EVENT_SYNC_URL;
//
//import android.content.Context;
//import android.util.Log;
//import android.util.Pair;
//
//import org.apache.commons.lang3.StringUtils;
//import org.joda.time.DateTime;
//import org.json.JSONArray;
//import org.json.JSONException;
//import org.json.JSONObject;
//import org.smartregister.AllConstants;
//import org.smartregister.CoreLibrary;
//import org.smartregister.SyncConfiguration;
//import org.smartregister.domain.Response;
//import org.smartregister.domain.db.EventClient;
//import org.smartregister.repository.AllSharedPreferences;
//import org.smartregister.repository.EventClientRepository;
//import org.smartregister.reveal.application.RevealApplication;
//import org.smartregister.service.HTTPAgent;
//import org.smartregister.util.NetworkUtils;
//import org.smartregister.util.SyncUtils;
//import org.smartregister.view.activity.DrishtiApplication;
//
//import java.text.MessageFormat;
//import java.util.List;
//import java.util.Map;
//
//import timber.log.Timber;
//
///**
// * Clean, callable Events sync helper — extracted from SyncIntentService so it can
// * be invoked directly from LocationTaskIntentService.doSync() as a pipeline stage,
// * instead of via a separate IntentService with its own unconditional job-scheduling
// * side effects.
// */
//public class EventSyncHelper {
//
//    protected static final int EVENT_PULL_LIMIT = 500; // matches RevealSyncIntentService override
//    protected static final int EVENT_PUSH_LIMIT = 50;
//    private static final String TAG = "EventSyncHelper";
//
//    protected static EventSyncHelper instance;
//
//    private final Context context;
//    private final HTTPAgent httpAgent;
//    private final SyncUtils syncUtils;
//    private final AllSharedPreferences allSharedPreferences;
//
//    private long totalRecords;
//    private int fetchedRecords;
//    private boolean foundNewData;
//
//    public static EventSyncHelper getInstance() {
//        if (instance == null) {
//            instance = new EventSyncHelper();
//        }
//        return instance;
//    }
//
//    private EventSyncHelper() {
//        this.context = CoreLibrary.getInstance().context().applicationContext();
//        this.httpAgent = CoreLibrary.getInstance().context().getHttpAgent();
//        this.syncUtils = new SyncUtils(context);
//        this.allSharedPreferences = CoreLibrary.getInstance().context().allSharedPreferences();
//    }
//
//    /**
//     * Runs a full Events sync cycle (push then pull) and returns whether new data
//     * was actually found/processed. Resets per-cycle counters at the start, so this
//     * is safe to call repeatedly across separate sync cycles without state leaking
//     * between calls (the bug that caused Events progress to exceed 100% previously).
//     */
//    public boolean syncEvents() {
//        fetchedRecords = 0;
//        totalRecords = 0;
//        foundNewData = false;
//
//        if (!NetworkUtils.isNetworkAvailable()) {
//            Log.w("SYNC_TRACE_RVL", "EVENTS_SYNC_NO_NETWORK");
//            return false;
//        }
//
//        try {
//            boolean hasValidAuthorization = syncUtils.verifyAuthorization();
//            boolean isSuccessfulPushSync = false;
//            if (hasValidAuthorization || !CoreLibrary.getInstance().getSyncConfiguration().disableSyncToServerIfUserIsDisabled()) {
//                isSuccessfulPushSync = pushToServer();
//            }
//
//            if (!hasValidAuthorization) {
//                syncUtils.logoutUser();
//                return false;
//            } else if (!syncUtils.isAppVersionAllowed()) {
//                if (isSuccessfulPushSync) {
//                    syncUtils.logoutUser();
//                }
//                return false;
//            } else {
//                pullECFromServer();
//                return foundNewData; // true only if real events were fetched, not just "ran cleanly"
//            }
//        } catch (Exception e) {
//            Timber.tag("Reveal Exception").w(e, "Error syncing events");
//            return false;
//        }
//    }
//
//    private void pullECFromServer() {
//        fetchRetry(0, true);
//    }
//
//    private synchronized void fetchRetry(final int count, boolean returnCount) {
//        Log.d("SYNC_TRACE_RVL", "EVENTS_FETCH_RETRY_START t=" + System.currentTimeMillis() + " count=" + count);
//        try {
//            SyncConfiguration configs = CoreLibrary.getInstance().getSyncConfiguration();
//            if (configs.getSyncFilterParam() == null || StringUtils.isBlank(configs.getSyncFilterValue())) {
//                Log.w("SYNC_TRACE_RVL", "EVENTS_FETCH_RETRY_BLANK_FILTER_FAIL");
//                return;
//            }
//
//            final ECSyncHelper ecSyncUpdater = ECSyncHelper.getInstance(context);
//            String baseUrl = getFormattedBaseUrl();
//            Long lastSyncDatetime = ecSyncUpdater.getLastSyncTimeStamp();
//
//            if (httpAgent == null) {
//                Log.w("SYNC_TRACE_RVL", "EVENTS_HTTP_AGENT_NULL");
//                return;
//            }
//
//            String url = baseUrl + EVENT_SYNC_URL;
//            Response resp;
//            if (configs.isSyncUsingPost()) {
//                JSONObject syncParams = new JSONObject();
//                syncParams.put(configs.getSyncFilterParam().value(), configs.getSyncFilterValue());
//                syncParams.put("serverVersion", lastSyncDatetime);
//                syncParams.put("limit", EVENT_PULL_LIMIT);
//                syncParams.put(RETURN_COUNT, returnCount);
//                resp = httpAgent.postWithJsonResponse(url, syncParams.toString());
//            } else {
//                url += "?" + configs.getSyncFilterParam().value() + "=" + configs.getSyncFilterValue()
//                        + "&serverVersion=" + lastSyncDatetime + "&limit=" + EVENT_PULL_LIMIT;
//                resp = httpAgent.fetch(url);
//            }
//
//            if (resp.isUrlError() || resp.isTimeoutError()) {
//                Log.w("SYNC_TRACE_RVL", "EVENTS_FETCH_FAILED status=" + resp.status());
//                return;
//            }
//
//            if (resp.isFailure()) {
//                fetchFailed(count);
//                return;
//            }
//
//            if (returnCount) {
//                totalRecords = resp.getTotalRecords();
//            }
//
//            processFetchedEvents(resp, ecSyncUpdater, count);
//
//        } catch (Exception e) {
//            Timber.tag("Reveal Exception").w(e, "Events fetch retry exception");
//            fetchFailed(count);
//        }
//    }
//
//    private void processFetchedEvents(Response resp, ECSyncHelper ecSyncUpdater, final int count) throws JSONException {
//        int eCount;
//        JSONObject jsonObject = new JSONObject();
//        if (resp.payload() == null) {
//            eCount = 0;
//        } else {
//            jsonObject = new JSONObject((String) resp.payload());
//            eCount = fetchNumberOfEvents(jsonObject);
//        }
//
//        if (eCount == 0) {
//            Log.d("SYNC_TRACE_RVL", "EVENTS_NOTHING_NEW");
//        } else if (eCount < 0) {
//            fetchFailed(count);
//        } else {
//            foundNewData = true;
//            Pair<Long, Long> serverVersionPair = getMinMaxServerVersions(jsonObject);
//            long lastServerVersion = serverVersionPair.second - 1;
//            if (eCount < EVENT_PULL_LIMIT) {
//                lastServerVersion = serverVersionPair.second;
//            }
//
//            boolean isSaved = ecSyncUpdater.saveAllClientsAndEvents(jsonObject);
//            if (isSaved) {
//                processClient(serverVersionPair);
//                ecSyncUpdater.updateLastSyncTimeStamp(lastServerVersion);
//            }
//            fetchedRecords += eCount;
//            Log.d("SYNC_TRACE_RVL", "EVENTS_FETCHED t=" + System.currentTimeMillis()
//                    + " count=" + fetchedRecords + "/" + totalRecords);
//            fetchRetry(0, false);
//        }
//    }
//
//    private void fetchFailed(int count) {
//        if (count < CoreLibrary.getInstance().getSyncConfiguration().getSyncMaxRetries()) {
//            fetchRetry(count + 1, false);
//        } else {
//            Log.w("SYNC_TRACE_RVL", "EVENTS_FETCH_FAILED_MAX_RETRIES");
//        }
//    }
//
//    private void processClient(Pair<Long, Long> serverVersionPair) {
//        try {
//            ECSyncHelper ecUpdater = ECSyncHelper.getInstance(context);
//            List<EventClient> events = ecUpdater.allEventClients(serverVersionPair.first - 1, serverVersionPair.second);
//            DrishtiApplication.getInstance().getClientProcessor().processClient(events);
//        } catch (Exception e) {
//            Timber.tag("Reveal Exception").w(e, "Process client exception");
//        }
//    }
//
//    private boolean pushToServer() {
//        return pushECToServer(CoreLibrary.getInstance().context().getEventClientRepository()) &&
//                (!CoreLibrary.getInstance().context().hasForeignEvents()
//                        || pushECToServer(CoreLibrary.getInstance().context().getForeignEventClientRepository()));
//    }
//
//    private boolean pushECToServer(EventClientRepository db) {
//        boolean isSuccessfulPushSync = true;
//        String baseUrl = getFormattedBaseUrl();
//
//        for (int i = 0; i < syncUtils.getNumOfSyncAttempts(); i++) {
//            Map<String, Object> pendingEvents = db.getUnSyncedEvents(EVENT_PUSH_LIMIT);
//            if (pendingEvents.isEmpty()) {
//                break;
//            }
//            JSONObject request = new JSONObject();
//            try {
//                if (pendingEvents.containsKey(AllConstants.KEY.CLIENTS)) {
//                    request.put(AllConstants.KEY.CLIENTS, pendingEvents.get(AllConstants.KEY.CLIENTS));
//                }
//                if (pendingEvents.containsKey(AllConstants.KEY.EVENTS)) {
//                    request.put(AllConstants.KEY.EVENTS, pendingEvents.get(AllConstants.KEY.EVENTS));
//                }
//            } catch (JSONException e) {
//                Timber.tag("Reveal Exception").w(e);
//            }
//
//            Response<String> response = httpAgent.post(
//                    MessageFormat.format("{0}/{1}", baseUrl, EVENT_ADD_URL),
//                    request.toString());
//
//            if (response.isFailure()) {
//                Timber.tag("Reveal Exception").w("Events push failed.");
//                isSuccessfulPushSync = false;
//            } else {
//                db.markEventsAsSynced(pendingEvents);
//                break;
//            }
//        }
//        return isSuccessfulPushSync;
//    }
//
//    private int fetchNumberOfEvents(JSONObject jsonObject) {
//        int count = -1;
//        try {
//            if (jsonObject != null && jsonObject.has("no_of_events")) {
//                count = jsonObject.getInt("no_of_events");
//            }
//        } catch (JSONException e) {
//            Timber.tag("Reveal Exception").w(e);
//        }
//        return count;
//    }
//
//    private Pair<Long, Long> getMinMaxServerVersions(JSONObject jsonObject) {
//        try {
//            if (jsonObject != null && jsonObject.has("events")) {
//                JSONArray events = jsonObject.getJSONArray("events");
//                long maxV = Long.MIN_VALUE, minV = Long.MAX_VALUE;
//                for (int i = 0; i < events.length(); i++) {
//                    Object o = events.get(i);
//                    if (o instanceof JSONObject && ((JSONObject) o).has("serverVersion")) {
//                        long v = ((JSONObject) o).getLong("serverVersion");
//                        if (v > maxV) maxV = v;
//                        if (v < minV) minV = v;
//                    }
//                }
//                return Pair.create(minV, maxV);
//            }
//        } catch (Exception e) {
//            Timber.tag("Reveal Exception").w(e);
//        }
//        return Pair.create(0L, 0L);
//    }
//
//    private String getFormattedBaseUrl() {
//        String baseUrl = CoreLibrary.getInstance().context().configuration().dristhiBaseURL();
//        if (baseUrl.endsWith("/")) {
//            baseUrl = baseUrl.substring(0, baseUrl.lastIndexOf("/"));
//        }
//        return baseUrl;
//    }
//
//    public int getTotalRecords() {
//        return (int) totalRecords;
//    }
//
//    public int getFetchedRecords() {
//        return fetchedRecords;
//    }
//}
package org.smartregister.sync.helper;

import static org.smartregister.AllConstants.BATCH_SIZE;
import static org.smartregister.AllConstants.COUNT;
import static org.smartregister.AllConstants.PerformanceMonitoring.HDSS_SYNC;
import static org.smartregister.reveal.api.RevealService.HDSS_PUSH_URL;
import static org.smartregister.reveal.api.RevealService.HDSS_SYNC_URL;
import static org.smartregister.reveal.api.RevealService.LOCATION_STRUCTURE_URL;
import static org.smartregister.util.PerformanceMonitoringUtils.addAttribute;
import static org.smartregister.util.PerformanceMonitoringUtils.initTrace;
import static org.smartregister.util.PerformanceMonitoringUtils.startTrace;
import static org.smartregister.util.PerformanceMonitoringUtils.stopTrace;

import android.content.Context;

import com.google.firebase.perf.metrics.Trace;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;
import org.smartregister.AllConstants;
import org.smartregister.CoreLibrary;
import org.smartregister.domain.HdssCompoundObj;
import org.smartregister.domain.HdssIndividualHouseHoldCompound;
import org.smartregister.domain.Location;
import org.smartregister.domain.Response;
import org.smartregister.domain.SyncEntity;
import org.smartregister.domain.SyncProgress;
import org.smartregister.exception.NoHttpResponseException;
import org.smartregister.repository.AllSharedPreferences;
import org.smartregister.repository.HdssRepository;
import org.smartregister.reveal.application.RevealApplication;
import org.smartregister.reveal.util.FirebaseLogger;
import org.smartregister.reveal.util.PreferencesUtil;
import org.smartregister.service.HTTPAgent;
import org.smartregister.util.Utils;

import java.text.MessageFormat;
import java.util.List;

import timber.log.Timber;

public class HdssServiceHelper extends BaseHelper {

    private static final String USERID = "user_id";
    private static final String CURRENT_OPERATIONAL_AREA_ID = "operational_area_id";


    protected static HdssServiceHelper instance;
    protected final Context context;

    public static Gson hdssGson = new GsonBuilder().create();
    private AllSharedPreferences allSharedPreferences = CoreLibrary.getInstance().context().allSharedPreferences();
    private HdssRepository hdssRepository;
    private Trace hdssSyncTrace;
    private String team;
    private long totalRecords = 0;
    private SyncProgress syncProgress;

    public HdssServiceHelper(HdssRepository hdssRepository) {
        this.context = CoreLibrary.getInstance().context().applicationContext();
        this.hdssRepository = hdssRepository;
        this.hdssSyncTrace = initTrace(HDSS_SYNC);
        String providerId = allSharedPreferences.fetchRegisteredANM();
        team = allSharedPreferences.fetchDefaultTeam(providerId);
    }

    public static HdssServiceHelper getInstance() {
        return RevealApplication.getInstance().getContext().hdssServiceHelper();
    }

    public void syncHdssDetails() {


        syncProgress = new SyncProgress();
        syncProgress.setSyncEntity(SyncEntity.HDSS);
        syncProgress.setTotalRecords(totalRecords);

        HdssRepository.createCompoundTable(hdssRepository.getWritableDatabase());
        HdssRepository.createHouseholdTable(hdssRepository.getWritableDatabase());
        HdssRepository.createCompoundHouseholdTable(hdssRepository.getWritableDatabase());
        HdssRepository.createHouseholdStructureTable(hdssRepository.getWritableDatabase());
        HdssRepository.createHouseholdIndividualTable(hdssRepository.getWritableDatabase());
        HdssRepository.createIndividualTable(hdssRepository.getWritableDatabase());

        HdssRepository.createIndividualTableIndex(hdssRepository.getWritableDatabase());
        HdssRepository.createHouseholdIndividualTableIndex(hdssRepository.getWritableDatabase());
        HdssRepository.createHouseholdStructureTableIndex(hdssRepository.getWritableDatabase());
        HdssRepository.createCompoundHouseholdTableIndex(hdssRepository.getWritableDatabase());


        try {
            pushHdssEntities();
        } catch (Exception e) {
            Timber.tag("Reveal Exception").e("cannot push down hdss items");
        }


        batchhdssEntities();
    }

    private void pushHdssEntities() throws Exception {
        long minServerVersion = hdssRepository.getMinServerVersionFromMaxOfAllHdssTables();
        List<HdssIndividualHouseHoldCompound> itemsGreateThanServerVersion = hdssRepository.getItemsGreateThanServerVersion(minServerVersion);

        String json = hdssGson.toJson(itemsGreateThanServerVersion);

        pushHdssEntities(json);

    }

    private void batchhdssEntities() {
        long serverVersion = PreferencesUtil.getInstance().getHdssMaxServerVersion();
        String providerId = allSharedPreferences.fetchRegisteredANM();

        try {

            startTrace(hdssSyncTrace);
            boolean isEmpty = false;

            int totalCount = 0;
            int totalSumCount = 0;
            do {
                String hdssResponse = fetchHdssEntities(providerId, serverVersion, 500);
                HdssCompoundObj hdssCompounds = hdssGson.fromJson(hdssResponse, HdssCompoundObj.class);

                isEmpty = hdssCompounds.getEmpty();
                if (!isEmpty) {
                    totalCount = hdssCompounds.getTotalRecords();
                    serverVersion = hdssCompounds.getServerVersion();
                    hdssRepository.addOrUpdateCompoundsBatched(hdssCompounds.getAllCompounds());
                    hdssRepository.addOrUpdateHousehold(hdssCompounds.getAllHouseholds());
                    hdssRepository.addOrUpdateCompoundHouseholdsBatched(hdssCompounds.getCompoundHouseHolds());
                    hdssRepository.addOrUpdateHouseholdStructureBatched(hdssCompounds.getAllHouseholdStructure());
                    hdssRepository.addOrUpdateHouseholdIndividualBatched(hdssCompounds.getAllHouseholdIndividual());
                    hdssRepository.addOrUpdateIndividualBatched(hdssCompounds.getAllIndividuals());
                    hdssRepository.deleteFromHouseholdIndividualByIndividualIds(hdssCompounds.getAllHouseholdIndividualToDelete());
                    hdssRepository.deleteFromCompoundHouseholdByHouseholdIds(hdssCompounds.getAllCompoundHouseholdToDelete());
                    hdssRepository.deleteFromHouseholdStructureByHouseholdIds(hdssCompounds.getAllCompoundHouseholdToDelete());

                    PreferencesUtil.getInstance().setHdssMaxServerVersion(hdssCompounds.getServerVersion());

                    int countOfIndividuals = hdssRepository.getCountOfIndividuals();

                    totalSumCount = countOfIndividuals;

                    if (hdssCompounds.getAllIndividuals()!=null){
                        Timber.tag("TotalCount").i("a hdssCompounds.getAllIndividuals() %s",hdssCompounds.getAllIndividuals().size());
                    }
                    Timber.tag("TotalCount").i("a countOfIndividuals %s",countOfIndividuals);
                    Timber.tag("TotalCount").i("a totalCount %s",totalCount);

                    syncProgress.setPercentageSynced(Utils.calculatePercentage(totalCount, countOfIndividuals));
                    sendSyncProgressBroadcast(syncProgress, context);
                } else {

                    if (hdssCompounds.getTotalRecords() > 0) {
                        int countOfIndividuals = hdssRepository.getCountOfIndividuals();
                        totalCount = hdssCompounds.getTotalRecords();


                        Timber.tag("TotalCount").i("b countOfIndividuals %s",countOfIndividuals);
                        Timber.tag("TotalCount").i("b totalCount %s",totalCount);

                        syncProgress.setPercentageSynced(Utils.calculatePercentage(totalCount, countOfIndividuals));
                        sendSyncProgressBroadcast(syncProgress, context);
                    } else {
                    }
                }

            } while (!isEmpty);

            addAttribute(hdssSyncTrace, COUNT, String.valueOf(totalSumCount));
            Timber.tag("TotalCount").i("end of syncing batch");
            stopTrace(hdssSyncTrace);

        } catch (Exception e) {
            Timber.tag("Reveal Exception").w(e, "EXCEPTION %s", e.toString());
            Timber.tag("TotalCount").e("exception %s",e.getMessage());

        }
    }

    private String pushHdssEntities(String json) throws Exception {

        HTTPAgent httpAgent = getHttpAgent();
        if (httpAgent == null) {
            throw new IllegalArgumentException(HDSS_PUSH_URL + " http agent is null");
        }

        String baseUrl = getFormattedBaseUrl();

        Response<String> resp;

        resp = httpAgent.post(MessageFormat.format("{0}{1}", baseUrl, HDSS_PUSH_URL),
                json);

        if (resp.isFailure()) {
            FirebaseLogger.logApiFailures(json, resp);
            throw new NoHttpResponseException(HDSS_PUSH_URL + " not returned data");
        }

        return resp.payload();
    }


    private String fetchHdssEntities(String userId, Long serverVersion, int batchSize) throws Exception {

        Timber.tag("TotalCount").i("requesting");

        HTTPAgent httpAgent = getHttpAgent();
        if (httpAgent == null) {
            throw new IllegalArgumentException(HDSS_SYNC_URL + " http agent is null");
        }

        String baseUrl = getFormattedBaseUrl();

        Response<String> resp;

        JSONObject request = new JSONObject();
        request.put(USERID, userId);
        request.put(AllConstants.SERVER_VERSION, serverVersion);
        request.put(BATCH_SIZE, batchSize);
        Timber.tag("TotalCount").i("a request %s",request);
        resp = httpAgent.post(MessageFormat.format("{0}{1}", baseUrl, HDSS_SYNC_URL),
                request.toString());

        if (resp.isFailure()) {
            FirebaseLogger.logApiFailures(request.toString(), resp);
            throw new NoHttpResponseException(LOCATION_STRUCTURE_URL + " not returned data");
        }


//        totalRecords = resp.getTotalRecords();


        return resp.payload();
    }

    private String getMaxServerVersion(List<Location> locations) {
        long maxServerVersion = 0;
        for (Location location : locations) {
            long serverVersion = location.getServerVersion();
            if (serverVersion > maxServerVersion) {
                maxServerVersion = serverVersion;
            }
        }
        return String.valueOf(maxServerVersion);
    }


    public HTTPAgent getHttpAgent() {
        return CoreLibrary.getInstance().context().getHttpAgent();
    }

    @NotNull
    public String getFormattedBaseUrl() {
        String baseUrl = CoreLibrary.getInstance().context().configuration().dristhiBaseURL();
        String endString = "/";
        if (baseUrl.endsWith(endString)) {
            baseUrl = baseUrl.substring(0, baseUrl.lastIndexOf(endString));
        }
        return baseUrl;
    }

}


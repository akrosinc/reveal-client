package org.smartregister.sync.helper;

import static org.smartregister.AllConstants.BATCH_SIZE;
import static org.smartregister.AllConstants.COUNT;
import static org.smartregister.AllConstants.PerformanceMonitoring.HDSS_SYNC;
import static org.smartregister.reveal.api.RevealService.HDSS_FILE_URL;
import static org.smartregister.reveal.api.RevealService.HDSS_PUSH_URL;
import static org.smartregister.reveal.api.RevealService.HDSS_SYNC_URL;
import static org.smartregister.reveal.api.RevealService.LOCATION_STRUCTURE_URL;
import static org.smartregister.util.PerformanceMonitoringUtils.addAttribute;
import static org.smartregister.util.PerformanceMonitoringUtils.initTrace;
import static org.smartregister.util.PerformanceMonitoringUtils.startTrace;
import static org.smartregister.util.PerformanceMonitoringUtils.stopTrace;

import android.content.Context;

import androidx.security.crypto.EncryptedFile;
import androidx.security.crypto.MasterKeys;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.firebase.perf.metrics.Trace;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;
import org.smartregister.AllConstants;
import org.smartregister.CoreLibrary;
import org.smartregister.domain.HdssCompound;
import org.smartregister.domain.HdssCompoundHousehold;
import org.smartregister.domain.HdssCompoundObj;
import org.smartregister.domain.HdssHousehold;
import org.smartregister.domain.HdssHouseholdIndividual;
import org.smartregister.domain.HdssHouseholdStructure;
import org.smartregister.domain.HdssIndividual;
import org.smartregister.domain.HdssIndividualHouseHoldCompound;
import org.smartregister.domain.HttpResponseWrapper;
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
  private AllSharedPreferences allSharedPreferences =
      CoreLibrary.getInstance().context().allSharedPreferences();
  private HdssRepository hdssRepository;
  private Trace hdssSyncTrace;
  private String team;
  private long totalRecords = 0;
  private SyncProgress syncProgress;
  private SyncProgress offlineSyncProgress;
  private SyncProgress fileSyncProgress;

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

    offlineSyncProgress = new SyncProgress();
    offlineSyncProgress.setSyncEntity(SyncEntity.HDSS_OFFLINE);

    fileSyncProgress = new SyncProgress();
    fileSyncProgress.setSyncEntity(SyncEntity.HDSS_FILE);

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


    boolean hdssFileProcessed = PreferencesUtil.getInstance().getHdssFileProcessed();
    if (!hdssFileProcessed){
      batchhdssEntitiesFile();
    } else {
      batchhdssEntities();
    }

  }

  private void pushHdssEntities() throws Exception {
    long minServerVersion = hdssRepository.getMinServerVersionFromMaxOfAllHdssTables();
    List<HdssIndividualHouseHoldCompound> itemsGreateThanServerVersion =
        hdssRepository.getItemsGreateThanServerVersion(minServerVersion);

    String json = hdssGson.toJson(itemsGreateThanServerVersion);

    pushHdssEntities(json);
  }

  private void fetchUsersFile() {}

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
          hdssRepository.addOrUpdateCompoundHouseholdsBatched(
              hdssCompounds.getCompoundHouseHolds());
          hdssRepository.addOrUpdateHouseholdStructureBatched(
              hdssCompounds.getAllHouseholdStructure());
          hdssRepository.addOrUpdateHouseholdIndividualBatched(
              hdssCompounds.getAllHouseholdIndividual());
          hdssRepository.addOrUpdateIndividualBatched(hdssCompounds.getAllIndividuals());
          hdssRepository.deleteFromHouseholdIndividualByIndividualIds(
              hdssCompounds.getAllHouseholdIndividualToDelete());
          hdssRepository.deleteFromCompoundHouseholdByHouseholdIds(
              hdssCompounds.getAllCompoundHouseholdToDelete());
          hdssRepository.deleteFromHouseholdStructureByHouseholdIds(
              hdssCompounds.getAllCompoundHouseholdToDelete());

          PreferencesUtil.getInstance().setHdssMaxServerVersion(hdssCompounds.getServerVersion());

          int countOfIndividuals = hdssRepository.getCountOfIndividuals();

          totalSumCount = countOfIndividuals;

          if (hdssCompounds.getAllIndividuals() != null) {
            Timber.tag("TotalCount")
                .i(
                    "a hdssCompounds.getAllIndividuals() %s",
                    hdssCompounds.getAllIndividuals().size());
          }
          Timber.tag("TotalCount").i("a countOfIndividuals %s", countOfIndividuals);
          Timber.tag("TotalCount").i("a totalCount %s", totalCount);

          syncProgress.setPercentageSynced(
              Utils.calculatePercentage(totalCount, countOfIndividuals));
          sendSyncProgressBroadcast(syncProgress, context);
        } else {

          if (hdssCompounds.getTotalRecords() > 0) {
            int countOfIndividuals = hdssRepository.getCountOfIndividuals();
            totalCount = hdssCompounds.getTotalRecords();

            Timber.tag("TotalCount").i("b countOfIndividuals %s", countOfIndividuals);
            Timber.tag("TotalCount").i("b totalCount %s", totalCount);

            syncProgress.setPercentageSynced(
                Utils.calculatePercentage(totalCount, countOfIndividuals));
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
      Timber.tag("TotalCount").e("exception %s", e.getMessage());
    }
  }

  private void batchhdssEntitiesFile() {
    long serverVersion = PreferencesUtil.getInstance().getHdssMaxServerVersion();
    String providerId = allSharedPreferences.fetchRegisteredANM();

    try {

      startTrace(hdssSyncTrace);

      Timber.tag("SecureStore").i("file fetched? %s",PreferencesUtil.getInstance().getHdssFileFetched());


      if (!PreferencesUtil.getInstance().getHdssFileFetched()) {
        fileSyncProgress.setPercentageSynced(0);
        sendSyncProgressBroadcast(fileSyncProgress,context);
        PreferencesUtil.getInstance().setHdssFileProcessingLineNumber(0);
        String filename = fetchHdssEntitiesFile(providerId, serverVersion, 500);

        Timber.tag("SecureStore").i("file download complete: %s", filename);
        PreferencesUtil.getInstance().setHdssFileFetched(true);
        Timber.tag("SecureStore").i("save file fetched %s",PreferencesUtil.getInstance().getHdssFileFetched());

      }

      boolean isEmpty = false;
      int totalCount = 0;
      int totalSumCount = 0;


      if (PreferencesUtil.getInstance().getHdssFileFetched()) {
        Timber.tag("SecureStore").i("processing file? %s",PreferencesUtil.getInstance().getHdssFileFetched());

        PreferencesUtil.getInstance().setHdssFileProcessed(false);
        readAndProcessHdssFile(context);
        PreferencesUtil.getInstance().setHdssFileProcessed(true);
      }

      addAttribute(hdssSyncTrace, COUNT, String.valueOf(totalSumCount));
      Timber.tag("SecureStore").i("end of syncing batch");
      stopTrace(hdssSyncTrace);

    } catch (Exception e) {
      Timber.tag("Reveal Exception").w(e, "EXCEPTION %s", e.toString());
      Timber.tag("SecureStore").e("exception %s", e.getMessage());
    }
  }

  private void readAndProcessHdssFile(Context context) {
    try {
      int hdssFileProcessingLineNumber =
          PreferencesUtil.getInstance().getHdssFileProcessingLineNumber();

      Timber.tag("SecureStore").i("hdssFileProcessingLineNumber %s",hdssFileProcessingLineNumber);
      int hdssFileTotalRecordCount = PreferencesUtil.getInstance().getHdssFileTotalRecordCount();
      Timber.tag("SecureStore").i("hdssFileTotalRecordCount %s",hdssFileTotalRecordCount);

      String fileName = "secure_export.csv";
      File file = new File(context.getFilesDir(), fileName);

      String masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC);
      EncryptedFile encryptedFile =
          new EncryptedFile.Builder(
                  file,
                  context,
                  masterKeyAlias,
                  EncryptedFile.FileEncryptionScheme.AES256_GCM_HKDF_4KB)
              .build();

      try (InputStream inputStream = encryptedFile.openFileInput();
          BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {

        String line;
        int currentLine = 0;
        int batchCount = 0;

        HdssCompoundObj batch = HdssCompoundObj.builder().build();

        // Skip previously processed lines
        while (currentLine < hdssFileProcessingLineNumber && (line = reader.readLine()) != null) {
          currentLine++;
        }

        while ((line = reader.readLine()) != null) {
          String[] fields = line.split(";", -1); // include empty fields

          for (int i = 0; i < fields.length; i++) {
            fields[i] = unquote(fields[i].trim());
          }

          String type = fields.length > 0 ? fields[0] : "";
//          Timber.tag("SecureStore").i("fields %s", Arrays.toString(fields));

          long maxServerVersion = 0;
          switch (type) {
            case "allCompounds":
              long serverVersion = safeParseLong(fields[2]);
              if (serverVersion>maxServerVersion){
                maxServerVersion = serverVersion;
              }
              batch.getAllCompounds().add(
                  HdssCompound.builder()
                      .compoundId(fields[1])
                      .serverVersion(safeParseLong(fields[2]))
                      .build());
              break;

            case "compoundHouseHolds":
              serverVersion = safeParseLong(fields[3]);
              batch.getCompoundHouseHolds().add(
                  HdssCompoundHousehold.builder()
                      .compoundId(fields[1])
                      .householdId(fields[2])
                      .serverVersion(safeParseLong(fields[3]))
                      .build());
              break;

            case "allHouseholdIndividual":
              serverVersion = safeParseLong(fields[3]);
              batch.getAllHouseholdIndividual().add(
                  HdssHouseholdIndividual.builder()
                      .individualId(fields[1])
                      .householdId(fields[2])
                      .serverVersion(safeParseLong(fields[3]))
                      .build());
              break;

            case "allHouseholdStructure":
              serverVersion = safeParseLong(fields[3]);
              batch.getAllHouseholdStructure().add(
                  HdssHouseholdStructure.builder()
                      .structureId(fields[1])
                      .householdId(fields[2])
                      .serverVersion(safeParseLong(fields[3]))
                      .build());
              break;

            case "allIndividuals":
              serverVersion = safeParseLong(fields[10]);
              batch.getAllIndividuals().add(
                  HdssIndividual.builder()
                      .identifier(fields[1])
                      .individualId(fields[2])
                      .name(fields[3])
                      .dob(fields[4])
                      .gender(fields[5])
                      .cluster(fields[6])
                      .floatingLocationId(fields[7])
                      .floatingLocationName(fields[8])
                      .floatingLocationGeographicLevel(fields[9])
                      .serverVersion(safeParseLong(fields[10]))
                      .build());
              break;

            case "allHouseholds":
              serverVersion = safeParseLong(fields[3]);
              batch.getAllHouseholds().add(
                  HdssHousehold.builder()
                      .householdId(fields[1])
                      .floatingHouseholdLocationName(fields[2])
                      .serverVersion(safeParseLong(fields[3]))
                      .build());
              break;

            case "allHouseholdIndividualToDelete":
              batch.getAllHouseholdIndividualToDelete().add(fields[1]);
              break;

            case "allCompoundHouseholdToDelete":
              batch.getAllCompoundHouseholdToDelete().add(fields[1]);
              break;

            default:
              Timber.tag("SecureStore").w("Unknown type: %s", type);
              break;
          }

          batchCount++;
          currentLine++;
          PreferencesUtil.getInstance().setHdssFileProcessingLineNumber(currentLine);

          if (batchCount >= 800) {
            writeBatchToDb(batch);
            Timber.tag("SecureStore").i("percentage 1: %s / %s",currentLine,PreferencesUtil.getInstance().getHdssFileTotalRecordCount());

            int percentageSynced = (int) Math.round((double) currentLine / (double) PreferencesUtil.getInstance().getHdssFileTotalRecordCount() * 100);
            Timber.tag("SecureStore").i("percentage 1 calculated: %s",percentageSynced);

            offlineSyncProgress.setPercentageSynced(
                percentageSynced);
            sendSyncProgressBroadcast(offlineSyncProgress, context);
            Timber.tag("SecureStore").i("Wrote batch to DB at line: %d", currentLine);
            batch = HdssCompoundObj.builder().build(); // reset
            batchCount = 0;
          }
        }

        // Write final batch
        if (batchCount > 0) {
          writeBatchToDb(batch);
          Timber.tag("SecureStore").i("percentage 2: %s / %s",currentLine,PreferencesUtil.getInstance().getHdssFileTotalRecordCount());
          int percentageSynced = (int) Math.round((double) currentLine / (double) PreferencesUtil.getInstance().getHdssFileTotalRecordCount() * 100);

          Timber.tag("SecureStore").i("percentage 2 calculated: %s",percentageSynced);

          syncProgress.setPercentageSynced(
              percentageSynced);
          sendSyncProgressBroadcast(syncProgress, context);

          Timber.tag("SecureStore").i("Wrote final batch to DB at line: %d", currentLine);
        }
      }

    } catch (Exception e) {
      Timber.tag("HDSSProcessor").e(e, "Error reading HDSS file");
    }
  }
  private long safeParseLong(String s) {
    try {
      return Long.parseLong(s);
    } catch (Exception e) {
      return 0L;
    }
  }
  private static String unquote(String s) {
    if (s != null && s.length() >= 2 && s.startsWith("\"") && s.endsWith("\"")) {
      return s.substring(1, s.length() - 1);
    }
    return s;
  }
  private void writeBatchToDb(HdssCompoundObj hdssCompounds) {
    try {
//      Timber.tag("SecureStore").i("writing to getAllCompounds %s",hdssCompounds.getAllCompounds()!=null?hdssCompounds.getAllCompounds().size():0);
      hdssRepository.addOrUpdateCompoundsBatched(hdssCompounds.getAllCompounds());
//      Timber.tag("SecureStore").i("wrote to getAllCompounds ");

    } catch (Exception e) {
      Timber.tag("Reveal Exception").e(e, "Failed to add or update compounds");
    }

    try {
//      Timber.tag("SecureStore").i("writing to getAllHouseholds %s",hdssCompounds.getAllHouseholds()!=null?hdssCompounds.getAllHouseholds().size():0);

      hdssRepository.addOrUpdateHousehold(hdssCompounds.getAllHouseholds());
//      Timber.tag("SecureStore").i("wrote to getAllHouseholds ");

    } catch (Exception e) {
      Timber.tag("Reveal Exception").e(e, "Failed to add or update households");
    }

    try {
//      Timber.tag("SecureStore").i("writing to getCompoundHouseHolds %s",hdssCompounds.getCompoundHouseHolds()!=null?hdssCompounds.getCompoundHouseHolds().size():0);

      hdssRepository.addOrUpdateCompoundHouseholdsBatched(hdssCompounds.getCompoundHouseHolds());
//      Timber.tag("SecureStore").i("wrote to getCompoundHouseHolds ");

    } catch (Exception e) {
      Timber.tag("Reveal Exception").e(e, "Failed to add or update compound households");
    }

    try {
//      Timber.tag("SecureStore").i("writing to getAllHouseholdStructure %s",hdssCompounds.getAllHouseholdStructure()!=null?hdssCompounds.getAllHouseholdStructure().size():0);
      hdssRepository.addOrUpdateHouseholdStructureBatched(hdssCompounds.getAllHouseholdStructure());
//      Timber.tag("SecureStore").i("wrote to getAllHouseholdStructure ");

    } catch (Exception e) {
      Timber.tag("Reveal Exception").e(e, "Failed to add or update household structures");
    }

    try {
//      Timber.tag("SecureStore").i("writing to getAllHouseholdIndividual %s",hdssCompounds.getAllHouseholdIndividual()!=null?hdssCompounds.getAllHouseholdIndividual().size():0);
      hdssRepository.addOrUpdateHouseholdIndividualBatched(
          hdssCompounds.getAllHouseholdIndividual());
//      Timber.tag("SecureStore").i("wrote to getAllHouseholdIndividual ");

    } catch (Exception e) {
      Timber.tag("Reveal Exception").e(e, "Failed to add or update household individuals");
    }

    try {
//      Timber.tag("SecureStore").i("writing to getAllIndividuals %s",hdssCompounds.getAllIndividuals()!=null?hdssCompounds.getAllIndividuals().size():0);
      hdssRepository.addOrUpdateIndividualBatched(hdssCompounds.getAllIndividuals());
//      Timber.tag("SecureStore").i("wrote to getAllIndividuals ");

    } catch (Exception e) {
      Timber.tag("Reveal Exception").e(e, "Failed to add or update individuals");
    }

    try {
//      Timber.tag("SecureStore").i("writing to getAllHouseholdIndividualToDelete %s",hdssCompounds.getAllHouseholdIndividualToDelete()!=null?hdssCompounds.getAllHouseholdIndividualToDelete().size():0);
      hdssRepository.deleteFromHouseholdIndividualByIndividualIds(
          hdssCompounds.getAllHouseholdIndividualToDelete());
//      Timber.tag("SecureStore").i("wrote to getAllHouseholdIndividualToDelete ");

    } catch (Exception e) {
      Timber.tag("Reveal Exception").e(e, "Failed to delete household individuals");
    }

    try {
//      Timber.tag("SecureStore").i("writing to getAllCompoundHouseholdToDelete %s",hdssCompounds.getAllCompoundHouseholdToDelete()!=null?hdssCompounds.getAllCompoundHouseholdToDelete().size():0);

      hdssRepository.deleteFromCompoundHouseholdByHouseholdIds(
          hdssCompounds.getAllCompoundHouseholdToDelete());
//      Timber.tag("SecureStore").i("wrote to getAllCompoundHouseholdToDelete ");

    } catch (Exception e) {
      Timber.tag("Reveal Exception").e(e, "Failed to delete compound households");
    }

//    try {
//      Timber.tag("SecureStore").i("writing to getAllCompoundHouseholdToDelete %s",hdssCompounds.getAllCompoundHouseholdToDelete()!=null?hdssCompounds.getAllCompoundHouseholdToDelete().size():0);
//
//      hdssRepository.deleteFromHouseholdStructureByHouseholdIds(
//          hdssCompounds.getAllCompoundHouseholdToDelete());
//    } catch (Exception e) {
//      Timber.tag("Reveal Exception").e(e, "Failed to delete household structures");
//    }
  }

  private String pushHdssEntities(String json) throws Exception {

    HTTPAgent httpAgent = getHttpAgent();
    if (httpAgent == null) {
      throw new IllegalArgumentException(HDSS_PUSH_URL + " http agent is null");
    }

    String baseUrl = getFormattedBaseUrl();

    Response<String> resp;

    resp = httpAgent.post(MessageFormat.format("{0}{1}", baseUrl, HDSS_PUSH_URL), json);

    if (resp.isFailure()) {
      FirebaseLogger.logApiFailures(json, resp);
      throw new NoHttpResponseException(HDSS_PUSH_URL + " not returned data");
    }

    return resp.payload();
  }

  private String fetchHdssEntitiesFile(String userId, Long serverVersion, int batchSize)
      throws Exception {

    PreferencesUtil.getInstance().setHdssFileTotalRecordCount(0);
    Timber.tag("SecureStore").i("requesting");

    HTTPAgent httpAgent = getHttpAgent();
    if (httpAgent == null) {
      throw new IllegalArgumentException(HDSS_FILE_URL + " http agent is null");
    }

    String baseUrl = getFormattedBaseUrl();

    JSONObject request = new JSONObject();
    request.put(USERID, userId);
    request.put(AllConstants.SERVER_VERSION, serverVersion);
    request.put(BATCH_SIZE, batchSize);
    Timber.tag("SecureStore").i("a request %s", request);

    HttpResponseWrapper httpResponseWrapper =
        httpAgent.postForInputStream(
            MessageFormat.format("{0}{1}", baseUrl, HDSS_FILE_URL), request.toString());

    storeEncryptedFile(context, httpResponseWrapper.getInputStream(), "secure_export.csv");

    Map<String, List<String>> headers = httpResponseWrapper.getHeaders();
    String metadataJson = null;
    if (headers != null && headers.entrySet() != null) {
      Timber.tag("SecureStore").i("header not null");
      for (Map.Entry<String, List<String>> entry : headers.entrySet()) {
        Timber.tag("SecureStore").i("header entry.getKey() %s",entry.getKey());

        if ("x-file-metadata".equalsIgnoreCase(entry.getKey())) {
          Timber.tag("SecureStore").i("x-file-metadata found %s",entry.getValue().get(0));

          metadataJson = entry.getValue().get(0); // assuming single value
          break;
        }
      }
      if (metadataJson != null) {
        Timber.tag("SecureStore").i("metadataJson not null");

        ObjectMapper objectMapper = new ObjectMapper();

        Map<String, Integer> metadataMap =
            objectMapper.readValue(metadataJson, new TypeReference<Map<String, Integer>>() {});

        if (metadataMap != null) {
          Timber.tag("SecureStore").i("metadataMap not null");
          int compoundCount = safeGetCount(metadataMap, "allCompounds");
          int compoundHouseholdCount = safeGetCount(metadataMap, "compoundHouseHolds");
          int householdIndividualCount = safeGetCount(metadataMap, "allHouseholdIndividual");
          int householdStructureCount = safeGetCount(metadataMap, "allHouseholdStructure");
          int individualCount = safeGetCount(metadataMap, "allIndividuals");
          int householdCount = safeGetCount(metadataMap, "allHouseholds");
          int householdIndividualToDeleteCount =
              safeGetCount(metadataMap, "allHouseholdIndividualToDelete");
          int compoundHouseholdToDeleteCount =
              safeGetCount(metadataMap, "allCompoundHouseholdToDelete");

          int totalCount =
              compoundCount
                  + compoundHouseholdCount
                  + householdIndividualCount
                  + householdStructureCount
                  + individualCount
                  + householdCount
                  + householdIndividualToDeleteCount
                  + compoundHouseholdToDeleteCount;
          Timber.tag("SecureStore").i("totalCount %s",totalCount);
          PreferencesUtil.getInstance().setHdssFileTotalRecordCount(totalCount);
        }
      }
    }
    return "secure_export.csv";
  }
  private int safeGetCount(Map<String, Integer> map, String key) {
    Integer value = map.get(key);
    return value != null ? value : 0;
  }

  private void storeEncryptedFile(Context context, InputStream inputStream, String fileName)
      throws Exception {
    String masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC);

    File file = new File(context.getFilesDir(), fileName);

    EncryptedFile encryptedFile =
        new EncryptedFile.Builder(
                file,
                context,
                masterKeyAlias,
                EncryptedFile.FileEncryptionScheme.AES256_GCM_HKDF_4KB)
            .build();

    try (OutputStream outputStream = encryptedFile.openFileOutput()) {
      byte[] buffer = new byte[4096];
      int bytesRead;
      while ((bytesRead = inputStream.read(buffer)) != -1) {
        outputStream.write(buffer, 0, bytesRead);
      }
    }

    Timber.tag("SecureStore").i("File stored securely: %s", file.getAbsolutePath());
  }

  private String fetchHdssEntities(String userId, Long serverVersion, int batchSize)
      throws Exception {

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
    Timber.tag("TotalCount").i("a request %s", request);
    resp =
        httpAgent.post(MessageFormat.format("{0}{1}", baseUrl, HDSS_SYNC_URL), request.toString());

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

package org.smartregister.reveal.searchbox;

import static org.smartregister.reveal.api.RevealService.HDSS_SEARCH_URL;
import static org.smartregister.reveal.api.RevealService.LOCATION_STRUCTURE_URL;


import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.text.MessageFormat;
import java.util.List;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.json.JSONObject;
import org.smartregister.CoreLibrary;
import org.smartregister.domain.HdssIndividualHouseHoldCompound;
import org.smartregister.domain.Response;
import org.smartregister.exception.NoHttpResponseException;
import org.smartregister.repository.HdssRepository;
import org.smartregister.reveal.searchbox.HdssSearchBoxFactory.SearchResponse;
import org.smartregister.reveal.util.FirebaseLogger;
import org.smartregister.service.HTTPAgent;
import timber.log.Timber;

public class HdssSearchTask {

  private final HdssRepository hdssRepository;
  private final Gson gson = new Gson();

  public HdssSearchTask(HdssRepository repository) {
    this.hdssRepository = repository;
    HdssRepository.createSearchResultsTable(repository.getWritableDatabase());
  }

  public SearchTaskResult execute(HdssSearchRequest request) {

    String searchString = request.getSearchString();
    String gender = request.getGender();
    String dob = request.getDob();
    String name = request.getNameString();
    String cluster = request.getCluster();
    String startAge = request.getStartAge();
    String endAge = request.getEndAge();

    boolean useAgeRange = request.isUseAgeRange();
    boolean useExactDate = request.isUseExactDate();
    boolean searchOnline = request.isSearchOnline();

    int batchSize = request.getBatchSize();
    int batchNumber = request.getBatchNumber();

    if (searchOnline){
      try {
        hdssRepository.deleteSearchResultsData();

        String s = searchHdssEntities(searchString, gender, dob,name,startAge,endAge,cluster,useAgeRange,useExactDate);

        List<SearchResponse> resultList = gson.fromJson(s, new TypeToken<List<SearchResponse>>() {}.getType());

        hdssRepository.addOrUpdateSearchResults(resultList);

        List<HdssSearchBoxFactory.SearchResponse> searchResultsInBatches = hdssRepository.getSearchResultsInBatches(batchSize, batchNumber*batchSize);

        String output = gson.toJson(searchResultsInBatches);

        return SearchTaskResult.success(output);
      } catch (IllegalStateException e) {
        return SearchTaskResult.failure("Too much data returned, please narrow your search");
      } catch (Exception e) {
        return SearchTaskResult.failure("Unknown error");
      }
    } else {
      try {
        hdssRepository.deleteSearchResultsData();

        String dobFormatted = null;
        if (dob!=null){
          DateTime dateTime = DateTime.parse(dob, DateTimeFormat.forPattern("dd-MM-yyyy"));
          dobFormatted = dateTime.toString("yyyy-MM-dd");
        }


        List<HdssIndividualHouseHoldCompound> hdssIndividualHouseHoldCompounds = hdssRepository.searchHouseholdIndividual(searchString, gender,dobFormatted, name, cluster,startAge, endAge,useAgeRange,useExactDate);
        Timber.tag("searching").e("searched results %s",hdssIndividualHouseHoldCompounds.size());

        hdssRepository.addOrUpdateLocalSearchResults(hdssIndividualHouseHoldCompounds);
        List<HdssSearchBoxFactory.SearchResponse> searchResultsInBatches = hdssRepository.getSearchResultsInBatches(batchSize, batchNumber * batchSize);
        Timber.tag("searching").e("retrieved results %s",searchResultsInBatches.size());
        String output = gson.toJson(searchResultsInBatches);

        return SearchTaskResult.success(output);
      } catch (IllegalStateException e) {
        return SearchTaskResult.failure("Too much data returned, please narrow your search");
      } catch (Exception e) {
        Timber.tag("searching").e(e,"error");
        return SearchTaskResult.failure("Unknown error");
      }
    }


  }

  private String searchHdssEntities(String searchString, String gender, String dob, String name, String startAge, String endAge,String cluster,boolean useAgeRange, boolean useExactDate) throws Exception {

    HTTPAgent httpAgent = getHttpAgent();
    if (httpAgent == null) {
      throw new IllegalArgumentException(HDSS_SEARCH_URL + " http agent is null");
    }

    String baseUrl = getFormattedBaseUrl();

    Response<String> resp;

    JSONObject request = new JSONObject();
    request.put("searchString", searchString);

    if (gender != null) {
      request.put("gender", gender);
    }

    if (name != null) {
      request.put("name", name);
    }
    if (cluster != null) {
      request.put("cluster", cluster);
    }

    if (useAgeRange){
      if (startAge!=null && endAge!=null){
        request.put("startAge", startAge);
        request.put("endAge", endAge);
      }
    }
    if (useExactDate){
      if (dob != null) {
        request.put("dob", dob);
      }
    }

    resp = httpAgent.post(MessageFormat.format("{0}{1}", baseUrl, HDSS_SEARCH_URL),
        request.toString());

    if (resp.isFailure()) {
      FirebaseLogger.logApiFailures(request.toString(), resp);
      throw new NoHttpResponseException(LOCATION_STRUCTURE_URL + " not returned data");
    }

    return resp.payload();
  }

  public HTTPAgent getHttpAgent() {
    return CoreLibrary.getInstance().context().getHttpAgent();
  }

  public String getFormattedBaseUrl() {
    String baseUrl = CoreLibrary.getInstance().context().configuration().dristhiBaseURL();
    String endString = "/";
    if (baseUrl.endsWith(endString)) {
      baseUrl = baseUrl.substring(0, baseUrl.lastIndexOf(endString));
    }
    return baseUrl;
  }

}

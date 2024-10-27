package org.smartregister.reveal.searchbox;

import static org.smartregister.reveal.api.RevealService.HDSS_SEARCH_URL;
import static org.smartregister.reveal.api.RevealService.LOCATION_STRUCTURE_URL;
import static org.smartregister.reveal.searchbox.HdssSearchBoxFactory.BATCH_NUMBER;
import static org.smartregister.reveal.searchbox.HdssSearchBoxFactory.BATCH_SIZE;
import static org.smartregister.reveal.searchbox.HdssSearchBoxFactory.DOB;
import static org.smartregister.reveal.searchbox.HdssSearchBoxFactory.GENDER;
import static org.smartregister.reveal.searchbox.HdssSearchBoxFactory.SEARCH_STRING;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.Data;

import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONObject;
import org.smartregister.CoreLibrary;
import org.smartregister.domain.Response;
import org.smartregister.exception.NoHttpResponseException;
import org.smartregister.repository.HdssRepository;
import org.smartregister.reveal.util.FirebaseLogger;
import org.smartregister.service.HTTPAgent;

import java.text.MessageFormat;
import java.util.List;

public class HdssSearchWorker extends Worker {

    private HdssRepository hdssRepository;

    public HdssSearchWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
        this.hdssRepository = CoreLibrary.getInstance().context().getHdssRepository();
        HdssRepository.createSearchResultsTable(hdssRepository.getWritableDatabase());
    }

    @NonNull
    @Override
    public Result doWork() {

        Data data = getInputData();

        String searchString = data.getString(SEARCH_STRING);
        String gender = data.getString(GENDER);
        String dob = data.getString(DOB);

        int batchSize = data.getInt(BATCH_SIZE,0);
        int batchNumber = data.getInt(BATCH_NUMBER,0);


        try {
            hdssRepository.deleteSearchResultsData();

            String s = searchHdssEntities(searchString, gender, dob);

            Gson gson = new Gson();

            List<HdssSearchBoxFactory.SearchResponse> resultList = gson.fromJson(s, new TypeToken<List<HdssSearchBoxFactory.SearchResponse>>() {}.getType());

            hdssRepository.addOrUpdateSearchResults(resultList);

            List<HdssSearchBoxFactory.SearchResponse> searchResultsInBatches = hdssRepository.getSearchResultsInBatches(batchSize, batchNumber*batchSize);

            String json = gson.toJson(searchResultsInBatches);

            Data output = new Data.Builder()
                    .putString("result",json)
                    .build();

            return Result.success(output);
        } catch (IllegalStateException e) {
            Data output = new Data.Builder()
                    .putString("error", "Too much data returned, please narrow your search")
                    .build();
            return Result.failure(output);
        } catch (Exception e) {
            return Result.failure();
        }

    }

    private String searchHdssEntities(String searchString, String gender, String dob) throws Exception {

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

        if (dob != null) {
            request.put("dob", dob);
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

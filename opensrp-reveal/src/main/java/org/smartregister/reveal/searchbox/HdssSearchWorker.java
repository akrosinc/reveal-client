package org.smartregister.reveal.searchbox;

import static org.smartregister.reveal.api.RevealService.HDSS_SEARCH_URL;
import static org.smartregister.reveal.api.RevealService.LOCATION_STRUCTURE_URL;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.Data;

import androidx.work.Worker;
import androidx.work.WorkerParameters;

import org.json.JSONObject;
import org.smartregister.CoreLibrary;
import org.smartregister.domain.Response;
import org.smartregister.exception.NoHttpResponseException;
import org.smartregister.reveal.util.FirebaseLogger;
import org.smartregister.service.HTTPAgent;

import java.text.MessageFormat;

public class HdssSearchWorker extends Worker {

    public HdssSearchWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {

        Data data = getInputData();

        String searchString = data.getString("searchString");
        String gender = data.getString("gender");

        try {

            String s = searchHdssEntities(searchString, gender);
            Data output = new Data.Builder()
                    .putString("result", s)
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

    private String searchHdssEntities(String searchString, String gender) throws Exception {

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

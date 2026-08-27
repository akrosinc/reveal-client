package org.smartregister.sync.helper;

import static org.smartregister.reveal.api.RevealService.DB_PULL_UPDATE_URL;
import static org.smartregister.reveal.api.RevealService.DB_PULL_URL;
import static org.smartregister.reveal.util.Constants.DBNAME;

import android.content.Context;

import com.google.gson.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.smartregister.CoreLibrary;
import org.smartregister.domain.DBPullConfig;
import org.smartregister.domain.FetchStatus;
import org.smartregister.domain.Response;
import org.smartregister.exception.NoHttpResponseException;
import org.smartregister.repository.AllSharedPreferences;
import org.smartregister.repository.DBPullRepository;
import org.smartregister.service.HTTPAgent;
import org.smartregister.util.DateTimeTypeConverter;
import org.smartregister.util.DateTypeConverter;
import org.smartregister.util.Utils;

import java.text.MessageFormat;
import java.util.List;

import timber.log.Timber;

/**
 * Created by Vincent Karuri on 08/05/2019
 */
public class DataIntentServiceHelper extends BaseHelper {

    private final AllSharedPreferences allSharedPreferences;

    private final DBPullRepository dbPullRepository;

    protected static Gson gson = new GsonBuilder()
            .registerTypeAdapter(DateTime.class, new DateTimeTypeConverter("yyyy-MM-dd"))
            .registerTypeAdapter(LocalDate.class, new DateTypeConverter())
            .disableHtmlEscaping()
            .create();

    protected final Context context;

    protected static DataIntentServiceHelper instance;

    public static DataIntentServiceHelper getInstance() {
        if (instance == null) {
            instance = new DataIntentServiceHelper();
        }
        return instance;
    }

    private DataIntentServiceHelper() {
        this.context = CoreLibrary.getInstance().context().applicationContext();
        this.dbPullRepository = CoreLibrary.getInstance().context().getDbPullRepository();
        this.allSharedPreferences = CoreLibrary.getInstance().context().allSharedPreferences();
    }

    private String fetchDBUserConfig()
            throws Exception {
        HTTPAgent httpAgent = getHttpAgent();
        String baseUrl = CoreLibrary.getInstance().context().configuration().dristhiBaseURL();
        String endString = "/";
        if (baseUrl.endsWith(endString)) {
            baseUrl = baseUrl.substring(0, baseUrl.lastIndexOf(endString));
        }


        Response resp = httpAgent.fetch(
                MessageFormat.format("{0}{1}",
                        baseUrl,
                        DB_PULL_URL));

        if (resp.isFailure()) {
            context.sendBroadcast(Utils.completeSync(FetchStatus.nothingFetched));
            throw new NoHttpResponseException(DB_PULL_URL + " did not return any data");
        }

        return resp.payload().toString();
    }

    private void submitDB(String username) {
        HTTPAgent httpAgent = getHttpAgent();
        String baseUrl = CoreLibrary.getInstance().context().configuration().dristhiBaseURL();
        String endString = "/";
        if (baseUrl.endsWith(endString)) {
            baseUrl = baseUrl.substring(0, baseUrl.lastIndexOf(endString));
        }


        httpAgent.httpDBPost(
                MessageFormat.format("{0}{1}",
                        baseUrl,
                        DB_PULL_UPDATE_URL
                                .concat("/")
                                .concat(allSharedPreferences.fetchRegisteredANM()))
                , context.getDatabasePath(DBNAME).getPath());


    }

    public void getDBUserConfig() {
        try {
            dbPullRepository.removeAllUsers();
            List<DBPullConfig> dbPullUserConfigs = gson.fromJson(fetchDBUserConfig(), new TypeToken<List<DBPullConfig>>() {
            }.getType());

            if (dbPullUserConfigs.stream().map(DBPullConfig::getUserName)
                    .anyMatch(username -> username.equals(allSharedPreferences.fetchRegisteredANM()))) {

                dbPullRepository.addUserToDbPullTable(allSharedPreferences.fetchRegisteredANM());
            }
        } catch (Exception e) {
            Timber.w("Cannot get DB Pull details");
        }
    }

    public void pushDBToServer() {
        try {

            List<String> usersInDBPull = dbPullRepository.getUsersInDBPull();

            if (usersInDBPull.stream()
                    .anyMatch(username -> username.equals(allSharedPreferences.fetchRegisteredANM()))) {
                submitDB(allSharedPreferences.fetchRegisteredANM());
            }

        } catch (Exception e) {
            Timber.tag("Reveal Exception").w("Cannot get DB Pull details");
        }
    }

    private HTTPAgent getHttpAgent() {
        return CoreLibrary.getInstance().context().getHttpAgent();
    }
}

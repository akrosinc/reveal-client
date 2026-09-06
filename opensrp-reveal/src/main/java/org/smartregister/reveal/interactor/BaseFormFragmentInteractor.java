package org.smartregister.reveal.interactor;

import androidx.core.util.Pair;

import net.sqlcipher.Cursor;
import net.sqlcipher.database.SQLiteDatabase;

import org.json.JSONArray;
import org.json.JSONObject;
import org.smartregister.commonregistry.CommonPersonObject;
import org.smartregister.commonregistry.CommonRepository;
import org.smartregister.repository.EventClientRepository;
import org.smartregister.reveal.application.RevealApplication;
import org.smartregister.reveal.contract.BaseFormFragmentContract;
import org.smartregister.reveal.util.AppExecutors;
import org.smartregister.reveal.util.InteractorUtils;

import timber.log.Timber;

import static com.vijay.jsonwizard.constants.JsonFormConstants.KEY;
import static com.vijay.jsonwizard.constants.JsonFormConstants.TEXT;
import static org.smartregister.reveal.util.Constants.DatabaseKeys.BASE_ENTITY_ID;
import static org.smartregister.reveal.util.Constants.DatabaseKeys.FIRST_NAME;
import static org.smartregister.reveal.util.Constants.DatabaseKeys.LAST_NAME;
import static org.smartregister.reveal.util.Constants.DatabaseKeys.STRUCTURE_ID;
import static org.smartregister.reveal.util.Constants.Intervention.IRS;

/**
 * Created by samuelgithengi on 6/14/19.
 */
public class BaseFormFragmentInteractor implements BaseFormFragmentContract.Interactor {

    private BaseFormFragmentContract.Presenter presenter;

    private AppExecutors appExecutors;

    private SQLiteDatabase sqLiteDatabase;

    protected EventClientRepository eventClientRepository;

    private InteractorUtils interactorUtils;

    public BaseFormFragmentInteractor(BaseFormFragmentContract.Presenter presenter) {
        this.presenter = presenter;
        appExecutors = RevealApplication.getInstance().getAppExecutors();
        sqLiteDatabase = RevealApplication.getInstance().getRepository().getReadableDatabase();
        eventClientRepository = RevealApplication.getInstance().getContext().getEventClientRepository();
        interactorUtils = new InteractorUtils();
    }

}

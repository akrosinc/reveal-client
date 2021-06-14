package com.revealprecision.reveal.interactor;

import org.smartregister.configurableviews.ConfigurableViewsLibrary;
import org.smartregister.configurableviews.helper.ConfigurableViewsHelper;
import com.revealprecision.reveal.application.RevealApplication;
import com.revealprecision.reveal.contract.BaseContract;
import com.revealprecision.reveal.contract.TaskRegisterContract;
import com.revealprecision.reveal.util.AppExecutors;

import java.util.List;

/**
 * Created by samuelgithengi on 3/14/19.
 */
public class TaskRegisterInteractor extends BaseInteractor implements TaskRegisterContract.Interactor {

    private ConfigurableViewsHelper viewsHelper;

    private AppExecutors appExecutors;

    public TaskRegisterInteractor(BaseContract.BasePresenter presenterCallBack) {
        super(presenterCallBack);
        viewsHelper = ConfigurableViewsLibrary.getInstance().getConfigurableViewsHelper();
        appExecutors = RevealApplication.getInstance().getAppExecutors();
    }

    @Override
    public void registerViewConfigurations(List<String> viewIdentifiers) {
        appExecutors.diskIO().execute(() -> {
            viewsHelper.registerViewConfigurations(viewIdentifiers);
        });
    }

    @Override
    public void unregisterViewConfiguration(List<String> viewIdentifiers) {
        viewsHelper.unregisterViewConfiguration(viewIdentifiers);
    }

    @Override
    public void cleanupResources() {
        viewsHelper = null;
        appExecutors = null;
    }


}

package org.smartregister.reveal.interactor;


import org.smartregister.reveal.application.RevealApplication;
import org.smartregister.reveal.contract.BaseContract;
import org.smartregister.reveal.contract.TaskRegisterContract;
import org.smartregister.reveal.util.AppExecutors;

import java.util.List;

/**
 * Created by samuelgithengi on 3/14/19.
 */
public class TaskRegisterInteractor extends BaseInteractor implements TaskRegisterContract.Interactor {


    private AppExecutors appExecutors;

    public TaskRegisterInteractor(BaseContract.BasePresenter presenterCallBack) {
        super(presenterCallBack);

        appExecutors = RevealApplication.getInstance().getAppExecutors();
    }

    @Override
    public void registerViewConfigurations(List<String> viewIdentifiers) {

    }

    @Override
    public void unregisterViewConfiguration(List<String> viewIdentifiers) {
    }

    @Override
    public void cleanupResources() {

        appExecutors = null;
    }


}

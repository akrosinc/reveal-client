package org.smartregister.reveal.contract;

import org.smartregister.view.contract.BaseRegisterContract;


public interface EventRegisterActivityContract {

    interface View extends BaseRegisterContract.View {

        void saveJsonForm(String json);

    }

    interface Presenter extends BaseContract.BasePresenter {

        void saveJsonForm(String json);

    }

    interface Interactor extends BaseContract.BaseInteractor {

    }
}

package org.smartregister.reveal.interactor;

import org.smartregister.reveal.contract.BaseContract;
import org.smartregister.reveal.contract.EventRegisterActivityContract;


public class EventRegisterInteractor extends BaseInteractor  implements EventRegisterActivityContract.Interactor {

    public EventRegisterInteractor(BaseContract.BasePresenter presenterCallBack) {
        super(presenterCallBack);
    }
}

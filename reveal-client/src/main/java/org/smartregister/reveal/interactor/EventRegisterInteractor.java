package com.revealprecision.reveal.interactor;

import com.revealprecision.reveal.contract.BaseContract;
import com.revealprecision.reveal.contract.EventRegisterActivityContract;

/**
 * Created by Richard Kareko on 7/31/20.
 */

public class EventRegisterInteractor extends BaseInteractor  implements EventRegisterActivityContract.Interactor {

    public EventRegisterInteractor(BaseContract.BasePresenter presenterCallBack) {
        super(presenterCallBack);
    }
}

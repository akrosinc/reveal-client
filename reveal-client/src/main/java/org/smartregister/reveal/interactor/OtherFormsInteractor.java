package com.revealprecision.reveal.interactor;

import com.revealprecision.reveal.contract.BaseContract;
import com.revealprecision.reveal.contract.OtherFormsContract;

public class OtherFormsInteractor extends BaseInteractor implements OtherFormsContract.Interactor {

    public OtherFormsInteractor(BaseContract.BasePresenter presenterCallBack) {
        super(presenterCallBack);
    }
}

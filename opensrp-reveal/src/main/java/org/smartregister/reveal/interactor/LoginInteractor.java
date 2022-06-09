package org.smartregister.reveal.interactor;

import java.lang.ref.WeakReference;
import org.smartregister.CoreLibrary;
import org.smartregister.login.interactor.BaseLoginInteractor;
import org.smartregister.reveal.application.RevealApplication;
import org.smartregister.reveal.job.LocationTaskServiceJob;
import org.smartregister.reveal.util.Utils;
import org.smartregister.view.contract.BaseLoginContract;

public class LoginInteractor extends BaseLoginInteractor implements BaseLoginContract.Interactor {

    public LoginInteractor(BaseLoginContract.Presenter loginPresenter) {
        super(loginPresenter);
    }

    @Override
    protected void scheduleJobsPeriodically() {
        LocationTaskServiceJob.scheduleJob(LocationTaskServiceJob.TAG,
                Utils.getSyncInterval(), getFlexValue((int) Utils.getSyncInterval()));
    }

    @Override
    protected void scheduleJobsImmediately() {
        Utils.startImmediateSync();
    }

    @Override
    public void loginWithLocalFlag(WeakReference<BaseLoginContract.View> view, boolean localLogin, String userName, char[] password) {
        if (!localLogin) {
            RevealApplication.getInstance().getContext().getHttpAgent().setConnectTimeout(CoreLibrary.getInstance().getSyncConfiguration().getConnectTimeout());
            RevealApplication.getInstance().getContext().getHttpAgent().setReadTimeout(CoreLibrary.getInstance().getSyncConfiguration().getReadTimeout());
        }
        super.loginWithLocalFlag(view, localLogin, userName, password);
    }
}

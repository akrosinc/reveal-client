package org.smartregister.sync.intent;

import android.content.Intent;

import org.smartregister.CoreLibrary;
import org.smartregister.repository.HdssRepository;
import org.smartregister.sync.helper.HdssServiceHelper;


public class HdssSyncIntentService extends BaseSyncIntentService {

    private static final String TAG = HdssSyncIntentService.class.getCanonicalName();

    private HdssRepository hdssRepository;
    private HdssServiceHelper hdssServiceHelper;

    public HdssSyncIntentService() {
        super(TAG);

    }

    @Override
    protected void onHandleIntent(Intent intent) {
        HdssRepository.createCompoundTable(hdssRepository.getWritableDatabase());
        HdssRepository.createCompoundHouseholdTable(hdssRepository.getWritableDatabase());
        HdssRepository.createHouseholdStructureTable(hdssRepository.getWritableDatabase());
        HdssRepository.createHouseholdIndividualTable(hdssRepository.getWritableDatabase());
        HdssRepository.createIndividualTable(hdssRepository.getWritableDatabase());

        HdssRepository.createIndividualTableIndex(hdssRepository.getWritableDatabase());
        HdssRepository.createHouseholdIndividualTableIndex(hdssRepository.getWritableDatabase());
        HdssRepository.createHouseholdStructureTableIndex(hdssRepository.getWritableDatabase());
        HdssRepository.createCompoundHouseholdTableIndex(hdssRepository.getWritableDatabase());

        this.hdssServiceHelper.syncHdssDetails();
        CoreLibrary.getInstance().context().setFetchedHdssDetails(true);

    }

    @Override
    public void onCreate() {
        super.onCreate();
        this.hdssRepository = CoreLibrary.getInstance().context().getHdssRepository();
        this.hdssServiceHelper = CoreLibrary.getInstance().context().hdssServiceHelper();
    }

}


package org.smartregister.service;

import android.content.Intent;

import org.smartregister.DristhiConfiguration;
import org.smartregister.domain.FetchStatus;

import org.smartregister.repository.AllSettings;
import org.smartregister.repository.AllSharedPreferences;
import org.smartregister.repository.FormDataRepository;
import org.smartregister.view.activity.DrishtiApplication;


public class FormSubmissionSyncService {


    public FormSubmissionSyncService(FormSubmissionService formSubmissionService, HTTPAgent
            httpAgent, FormDataRepository formDataRepository, AllSettings allSettings,
                                     AllSharedPreferences allSharedPreferences,
                                     DristhiConfiguration configuration) {

    }

    public FetchStatus sync() {
        try {
            Intent intent = new Intent(DrishtiApplication.getInstance().getApplicationContext(),
                    ImageUploadSyncService.class);
            DrishtiApplication.getInstance().getApplicationContext().startService(intent);
            return FetchStatus.fetched;
        } catch (Exception e) {
            return FetchStatus.fetchedFailed;
        }

    }




}

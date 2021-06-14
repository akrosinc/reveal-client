package com.revealprecision.reveal.interactor;

import android.content.Context;

import org.smartregister.domain.db.EventClient;
import org.smartregister.family.domain.FamilyEventClient;
import com.revealprecision.reveal.application.RevealApplication;
import com.revealprecision.reveal.contract.FamilyRegisterContract;
import com.revealprecision.reveal.sync.RevealClientProcessor;
import com.revealprecision.reveal.util.AppExecutors;
import com.revealprecision.reveal.util.TaskUtils;
import com.revealprecision.reveal.util.Utils;
import org.smartregister.sync.ClientProcessorForJava;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by samuelgithengi on 4/15/19.
 */
public class RevealFamilyRegisterInteractor extends org.smartregister.family.interactor.FamilyRegisterInteractor implements FamilyRegisterContract.Interactor {

    private TaskUtils taskUtils;

    private AppExecutors appExecutors;

    private RevealClientProcessor clientProcessor;

    private FamilyRegisterContract.Presenter presenter;

    public RevealFamilyRegisterInteractor(FamilyRegisterContract.Presenter presenter) {
        this.presenter = presenter;
        taskUtils = TaskUtils.getInstance();
        clientProcessor = (RevealClientProcessor) RevealApplication.getInstance().getClientProcessor();
        appExecutors = RevealApplication.getInstance().getAppExecutors();
    }

    @Override
    public ClientProcessorForJava getClientProcessorForJava() {
        return RevealClientProcessor.getInstance(RevealApplication.getInstance().getApplicationContext());
    }

    @Override
    public void generateTasks(List<FamilyEventClient> eventClientList, String structureId, Context context) {
        appExecutors.diskIO().execute(() -> {
            Set<String> generatedIds = new HashSet<>();
            for (FamilyEventClient eventClient : eventClientList) {
                if (eventClient.getClient().getLastName().equals("Family"))
                    continue;
                String entityId = eventClient.getClient().getBaseEntityId();
                if (!generatedIds.contains(entityId)) {
                    generatedIds.add(entityId);
                    if (Utils.isFocusInvestigation())
                        taskUtils.generateBloodScreeningTask(context, entityId, structureId);
                    else if (Utils.isMDA())
                        taskUtils.generateMDADispenseTask(context, entityId, structureId);
                }
            }
            if (Utils.isFocusInvestigation())
                taskUtils.generateBedNetDistributionTask(context, structureId);
            appExecutors.mainThread().execute(() -> presenter.onTasksGenerated(eventClientList));
        });
    }

    @Override
    protected void processClient(List<EventClient> eventClientList) {
        clientProcessor.processClient(eventClientList, true);
    }
}

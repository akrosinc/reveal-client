package com.revealprecision.reveal.interactor;

import org.json.JSONObject;
import org.smartregister.domain.Event;
import org.smartregister.repository.EventClientRepository;
import com.revealprecision.reveal.application.RevealApplication;
import com.revealprecision.reveal.contract.EventRegisterContract;
import com.revealprecision.reveal.util.AppExecutors;

/**
 * Created by Richard Kareko on 7/31/20.
 */

public class EventRegisterFragmentInteractor implements EventRegisterContract.Interactor {

    private EventRegisterContract.Presenter presenter;

    private EventClientRepository eventClientRepository;

    private AppExecutors appExecutors;

    public EventRegisterFragmentInteractor(EventRegisterContract.Presenter presenter) {
        this.presenter = presenter;
        appExecutors = RevealApplication.getInstance().getAppExecutors();
        eventClientRepository = RevealApplication.getInstance().getContext().getEventClientRepository();
    }

    @Override
    public void findEvent(String formSubmissionId) {
        appExecutors.diskIO().execute(() -> {
            JSONObject eventJSON = eventClientRepository.getEventsByFormSubmissionId(formSubmissionId);
            Event event = eventClientRepository.convert(eventJSON.toString(), Event.class);
            appExecutors.mainThread().execute(() -> {
                presenter.onEventFound(event);
            });
        });
    }
}

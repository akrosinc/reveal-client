package org.smartregister.reveal.interactor;

import com.google.gson.Gson;
import org.json.JSONException;
import org.json.JSONObject;
import org.smartregister.domain.Event;
import org.smartregister.repository.EventClientRepository;
import org.smartregister.reveal.application.RevealApplication;
import org.smartregister.reveal.contract.EventRegisterContract;
import org.smartregister.reveal.util.AppExecutors;
import timber.log.Timber;


public class EventRegisterFragmentInteractor implements EventRegisterContract.Interactor {

    public static final String DELETED = "DELETED";

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

    @Override
    public void deleteEvent(final String formSubmissionId) {
        appExecutors.diskIO().execute(() -> {
            JSONObject eventJSON = eventClientRepository.getEventsByFormSubmissionId(formSubmissionId);
            Event event = eventClientRepository.convert(eventJSON.toString(), Event.class);
            event.setStatus(DELETED);
            try {
                Gson gson = new Gson();
                eventClientRepository.addEvent(event.getBaseEntityId(), new JSONObject(gson.toJson(event)));
            } catch (JSONException e) {
                Timber.e(e);
            }
            appExecutors.mainThread().execute(() -> presenter.onEventDeleted());
        });
    }
}

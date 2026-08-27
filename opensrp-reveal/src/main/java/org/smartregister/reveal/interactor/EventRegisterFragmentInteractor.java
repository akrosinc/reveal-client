package org.smartregister.reveal.interactor;

import static org.smartregister.repository.EventClientRepository.DELETED;

import java.util.Collections;
import java.util.List;
import org.json.JSONException;
import org.json.JSONObject;
import org.smartregister.AllConstants;
import org.smartregister.domain.Event;
import org.smartregister.domain.db.EventClient;
import org.smartregister.repository.EventClientRepository;
import org.smartregister.reveal.application.RevealApplication;
import org.smartregister.reveal.contract.EventRegisterContract;
import org.smartregister.reveal.util.AppExecutors;
import org.smartregister.reveal.util.Constants.DatabaseKeys;
import timber.log.Timber;


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

    @Override
    public void deleteEvent(final String formSubmissionId) {
        appExecutors.diskIO().execute(() -> {
            JSONObject eventJSON = eventClientRepository.getEventsByFormSubmissionId(formSubmissionId);
            JSONObject details = eventJSON.optJSONObject(AllConstants.DETAILS);
            String baseEntityId =  eventJSON.optString(DatabaseKeys.BASE_ENTITY_ID);
            try {
                details.put(DatabaseKeys.ENTITY_STATUS, DELETED);
            } catch (JSONException e) {
                Timber.tag("Reveal Exception").w(e);
            }
            eventClientRepository.addEvent(baseEntityId,eventJSON);
            List<EventClient> eventClientList = eventClientRepository.fetchEventClients(Collections.singletonList(formSubmissionId));
            try {
                RevealApplication.getInstance().getClientProcessor().processClient(eventClientList);
            } catch (Exception e) {
                e.printStackTrace();
            }
            appExecutors.mainThread().execute(() -> presenter.onEventDeleted());
        });
    }
}

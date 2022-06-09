package org.smartregister.family.domain;

import org.smartregister.clientandeventmodel.Client;
import org.smartregister.clientandeventmodel.Event;

public class FamilyEventClient {

    private Event event;
    private Client client;

    public FamilyEventClient(Client client, Event event) {
        this.client = client;
        this.event = event;
    }

    public Client getClient() {
        return client;
    }

    public void setClient(Client client) {
        this.client = client;
    }

    public Event getEvent() {
        return event;
    }

    public void setEvent(Event event) {
        this.event = event;
    }
}

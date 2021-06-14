package com.revealprecision.reveal.interactor;

import android.content.Context;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.powermock.reflect.Whitebox;
import org.robolectric.RuntimeEnvironment;
import org.smartregister.cloudant.models.Client;
import org.smartregister.cloudant.models.Event;
import org.smartregister.domain.db.EventClient;
import org.smartregister.family.domain.FamilyEventClient;
import com.revealprecision.reveal.BaseUnitTest;
import com.revealprecision.reveal.contract.FamilyRegisterContract;
import com.revealprecision.reveal.sync.RevealClientProcessor;
import com.revealprecision.reveal.util.AppExecutors;
import com.revealprecision.reveal.util.TaskUtils;
import org.smartregister.sync.ClientProcessorForJava;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Executors;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;

/**
 * Created by samuelgithengi on 4/25/19.
 */
public class RevealFamilyRegisterInteractorTest extends BaseUnitTest {

    @Rule
    public MockitoRule rule = MockitoJUnit.rule();

    @Mock
    private FamilyRegisterContract.Presenter presenter;

    @Mock
    private TaskUtils taskUtils;

    @Mock
    private RevealClientProcessor clientProcessor;

    private RevealFamilyRegisterInteractor interactor;

    private Context context = RuntimeEnvironment.application;

    @Before
    public void setUp() {
        interactor = new RevealFamilyRegisterInteractor(presenter);
        Whitebox.setInternalState(interactor, "taskUtils", taskUtils);
        AppExecutors appExecutors = new AppExecutors(Executors.newSingleThreadExecutor(),
                Executors.newSingleThreadExecutor(), Executors.newSingleThreadExecutor());
        Whitebox.setInternalState(interactor, "appExecutors", appExecutors);
        Whitebox.setInternalState(interactor, "clientProcessor", clientProcessor);
    }

    @Test
    public void testGetClientProcessorForJava() {
        ClientProcessorForJava clientProcessor = interactor.getClientProcessorForJava();
        assertNotNull(clientProcessor);
        assertTrue(clientProcessor instanceof RevealClientProcessor);
    }

    @Test
    public void testGenerateTasks() {
        List<FamilyEventClient> eventClientList = new ArrayList<>();
        String baseEntityId = UUID.randomUUID().toString();
        Client client = new Client();
        client.withLastName("Otala").withBaseEntityId(baseEntityId);
        eventClientList.add(new FamilyEventClient(client, new Event().withBaseEntityId(baseEntityId)));

        String familyId = UUID.randomUUID().toString();
        Client family = new Client();
        family.withLastName("Family").withBaseEntityId(familyId);
        eventClientList.add(new FamilyEventClient(family, new Event().withBaseEntityId(familyId)));

        String structureId = UUID.randomUUID().toString();
        interactor.generateTasks(eventClientList, structureId, context);
        verify(presenter, timeout(ASYNC_TIMEOUT)).onTasksGenerated(eventClientList);
    }

    @Test
    public void testProcessClient() {
        List<EventClient> eventClientList = Collections.singletonList(new EventClient(new org.smartregister.domain.Event()));
        interactor.processClient(eventClientList);
        verify(clientProcessor).processClient(eventClientList, true);
    }


}

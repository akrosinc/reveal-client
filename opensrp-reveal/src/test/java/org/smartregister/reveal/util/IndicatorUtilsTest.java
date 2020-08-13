package org.smartregister.reveal.util;


import net.sqlcipher.MatrixCursor;
import net.sqlcipher.database.SQLiteDatabase;

import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.powermock.reflect.Whitebox;
import org.robolectric.RuntimeEnvironment;
import org.smartregister.domain.Task;
import org.smartregister.reveal.BaseUnitTest;
import org.smartregister.reveal.BuildConfig;
import org.smartregister.reveal.R;
import org.smartregister.reveal.model.IndicatorDetails;
import org.smartregister.reveal.model.TaskDetails;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertNotNull;
import static junit.framework.TestCase.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.powermock.api.mockito.PowerMockito.when;

/**
 * Created by ndegwamartin on 2019-09-27.
 */
public class IndicatorUtilsTest extends BaseUnitTest {

    private Map<String, Set<Task>> taskTestMap = new HashMap<>();
    @Mock
    private SQLiteDatabase sqLiteDatabase;

    @Before
    public void setUp() {

        Set<Task> tasks = new HashSet<>();

        Task task = new Task();
        task.setStructureId("struct-id-1");
        task.setAuthoredOn(new DateTime());
        task.setBusinessStatus(Constants.BusinessStatus.SPRAYED);
        task.setCode(Constants.Intervention.IRS);
        task.setDescription("random descriptions");
        task.setPlanIdentifier("plan-id-1");
        task.setStatus(Task.TaskStatus.COMPLETED);
        task.setForEntity("my-base-entity-id");
        tasks.add(task);


        task = new Task();
        task.setStructureId("struct-id-1");
        task.setAuthoredOn(new DateTime());
        task.setBusinessStatus(Constants.BusinessStatus.NOT_ELIGIBLE);
        task.setCode(Constants.Intervention.FI);
        task.setDescription("random descriptions 2");
        task.setPlanIdentifier("plan-id-1");
        task.setStatus(Task.TaskStatus.CANCELLED);
        task.setForEntity("my-base-entity-id");
        tasks.add(task);
        taskTestMap.put(task.getStructureId(), tasks);

        tasks = new HashSet<>();

        task = new Task();
        task.setStructureId("struct-id-2");
        task.setAuthoredOn(new DateTime());
        task.setBusinessStatus(Constants.BusinessStatus.NOT_VISITED);
        task.setCode(Constants.Intervention.IRS);
        task.setDescription("random descriptions xx");
        task.setPlanIdentifier("plan-id-1");
        task.setStatus(Task.TaskStatus.READY);
        task.setForEntity("my-base-entity-id");
        tasks.add(task);
        taskTestMap.put(task.getStructureId(), tasks);

        task = new Task();
        task.setStructureId("struct-id-3");
        task.setAuthoredOn(new DateTime());
        task.setBusinessStatus(Constants.BusinessStatus.SPRAYED);
        task.setCode(Constants.Intervention.IRS);
        task.setDescription("random descriptions 3");
        task.setPlanIdentifier("plan-id-1");
        task.setStatus(Task.TaskStatus.COMPLETED);
        task.setForEntity("my-base-entity-id");
        tasks.add(task);

        taskTestMap.put(task.getStructureId(), tasks);


        tasks = new HashSet<>();

        task = new Task();
        task.setStructureId("struct-id-4");
        task.setAuthoredOn(new DateTime());
        task.setBusinessStatus(Constants.BusinessStatus.NOT_SPRAYED);
        task.setCode(Constants.Intervention.IRS);
        task.setDescription("random descriptions 4");
        task.setPlanIdentifier("plan-id-1");
        task.setStatus(Task.TaskStatus.READY);
        task.setForEntity("my-base-entity-id");
        tasks.add(task);
        taskTestMap.put(task.getStructureId(), tasks);

        task = new Task();
        task.setStructureId("struct-id-5");
        task.setAuthoredOn(new DateTime());
        task.setBusinessStatus(Constants.BusinessStatus.SPRAYED);
        task.setCode(Constants.Intervention.IRS);
        task.setDescription("random descriptions");
        task.setStatus(Task.TaskStatus.COMPLETED);
        task.setForEntity("my-base-entity-id");
        task.setPlanIdentifier("plan-id-1");
        tasks.add(task);
        taskTestMap.put(task.getStructureId(), tasks);

        tasks = new HashSet<>();

        task = new Task();
        task.setStructureId("struct-id-6");
        task.setAuthoredOn(new DateTime());
        task.setBusinessStatus(Constants.BusinessStatus.NOT_VISITED);
        task.setCode(Constants.Intervention.IRS);
        task.setDescription("random descriptions 5");
        task.setPlanIdentifier("plan-id-1");
        task.setStatus(Task.TaskStatus.READY);
        task.setForEntity("my-base-entity-id");
        tasks.add(task);
        taskTestMap.put(task.getStructureId(), tasks);


        task = new Task();
        task.setStructureId("struct-id-7");
        task.setAuthoredOn(new DateTime());
        task.setBusinessStatus(Constants.BusinessStatus.NOT_ELIGIBLE);
        task.setCode(Constants.Intervention.IRS);
        task.setDescription("random descriptions 7");
        task.setPlanIdentifier("plan-id-1");
        task.setStatus(Task.TaskStatus.READY);
        task.setForEntity("my-base-entity-id");
        tasks.add(task);
        taskTestMap.put(task.getStructureId(), tasks);
    }


    @Test
    public void validateSetupForTaskTestMapCorrectly() {

        assertNotNull(taskTestMap);
        assertTrue(taskTestMap.size() > 0);


    }

    @Test
    public void testProcessTaskDetailsPopulatesTaskDetailsListCorrectly() {

        List<TaskDetails> taskDetailsList = IndicatorUtils.processTaskDetails(taskTestMap);

        assertNotNull(taskDetailsList);
        assertTrue(taskDetailsList.size() > 0);
        assertEquals(14, taskDetailsList.size());

    }

    @Test
    public void testProcessIndicatorsReturnsCorrectIndicatorDetails() {

        List<TaskDetails> taskDetailsList = IndicatorUtils.processTaskDetails(taskTestMap);
        IndicatorDetails indicatorDetails = IndicatorUtils.processIndicators(taskDetailsList);

        assertNotNull(indicatorDetails);

        assertEquals(3, indicatorDetails.getSprayed());
        assertEquals(1, indicatorDetails.getNotSprayed());
        assertEquals(2, indicatorDetails.getNotVisited());
        assertEquals(50, indicatorDetails.getProgress());
        assertEquals(6, indicatorDetails.getTotalStructures());
        assertEquals(1, indicatorDetails.getIneligible());

    }

    @Test
    public void testPopulateSprayIndicatorsPopulatesCorrectly() {
        Country country = BuildConfig.BUILD_COUNTRY;
        Whitebox.setInternalState(BuildConfig.class, BuildConfig.BUILD_COUNTRY, Country.ZAMBIA);
        List<TaskDetails> taskDetailsList = IndicatorUtils.processTaskDetails(taskTestMap);
        IndicatorDetails indicatorDetails = IndicatorUtils.processIndicators(taskDetailsList);
        List<String> sprayIndicatorList = IndicatorUtils.populateSprayIndicators(RuntimeEnvironment.application, indicatorDetails);

        assertNotNull(sprayIndicatorList);

        assertEquals(14, sprayIndicatorList.size());
        assertEquals(getString(R.string.spray_coverage), sprayIndicatorList.get(0));
        assertEquals("50%", sprayIndicatorList.get(1));
        assertEquals(getString(R.string.structures_remaining_90), sprayIndicatorList.get(2));
        assertEquals("2", sprayIndicatorList.get(3));
        assertEquals(getString(R.string.total_structures), sprayIndicatorList.get(4));
        assertEquals("6", sprayIndicatorList.get(5));
        Whitebox.setInternalState(BuildConfig.class, BuildConfig.BUILD_COUNTRY, country);

    }

    @Test
    public void testGetNamibiaIndicatorsShouldReturnCorrectIndicators() {
        String id = UUID.randomUUID().toString();
        MatrixCursor cursor = new MatrixCursor(new String[]{"totStruct", "notVisitedStruct",
                "foundStruct", "sprayedStruct", "notSprayedStruct", "roomCov"});
        when(sqLiteDatabase.rawQuery(anyString(), eq(new String[]{id}))).thenReturn(cursor);
        cursor.addRow(new Object[]{
                76, 1, 75, 74, 1, 93
        });
        IndicatorDetails indicatorDetails = IndicatorUtils.getNamibiaIndicators(id, sqLiteDatabase);
        assertEquals(76, indicatorDetails.getTotalStructures());
        assertEquals(1, indicatorDetails.getNotVisited());
        assertEquals(75, indicatorDetails.getFoundStructures());
        assertEquals(74, indicatorDetails.getSprayed());
        assertEquals(1, indicatorDetails.getNotSprayed());
        assertEquals(93, indicatorDetails.getRoomCoverage());
        assertEquals(97, indicatorDetails.getProgress());
        verify(sqLiteDatabase).rawQuery(anyString(), eq(new String[]{id}));
    }
}

package org.smartregister.reveal.template;

import org.smartregister.domain.HdssIndividual;
import org.smartregister.domain.Task;
import org.smartregister.reveal.R;
import org.smartregister.reveal.util.Constants;

import java.util.Map;

/**
 * GDRS-specific {@link TaskDisplayProvider}.
 *
 * Resolves individual data from a pre-fetched map so the adapter
 * never touches any repository directly.
 *
 * Primary label  → individual ID
 * Secondary label → household ID (from the Task itself)
 * Action label   → varies by task code + business status
 * Colours        → match existing GDRSActivity behaviour
 */
public class GDRSTaskDisplayProvider implements TaskDisplayProvider {

    /** Pre-fetched: individualIdentifier (forEntity) → HdssIndividual */
    private final Map<String, HdssIndividual> individualMap;

    public GDRSTaskDisplayProvider(Map<String, HdssIndividual> individualMap) {
        this.individualMap = individualMap;
    }

    @Override
    public String getPrimaryLabel(Task task) {
        HdssIndividual individual = individualMap.get(task.getForEntity());
        if (individual != null && individual.getIndividualId() != null) {
            return individual.getIndividualId();
        }
        return task.getForEntity(); // fallback to entity ID
    }

    @Override
    public String getSecondaryLabel(Task task) {
        // Household ID lives on the Task directly
        return task.getHouseholdId();
    }

    @Override
    public String getActionLabel(Task task) {
        boolean complete = Constants.BusinessStatus.COMPLETE.equals(task.getBusinessStatus());
        switch (task.getCode()) {
            case Constants.Action.INDEX_CASE_MEMBER:
                return complete ? "Edit Index Case" : "Confirm Index Case";
            case Constants.Action.SECONDARY_INDEX_CASE_MEMBER:
                return complete ? "Edit Secondary" : "Confirm Secondary";
            case Constants.Action.RCD_MEMBER:
            default:
                return complete ? "Edit RACD" : "Record RACD";
        }
    }

    @Override
    public int getActionColourRes(Task task) {
        boolean complete = Constants.BusinessStatus.COMPLETE.equals(task.getBusinessStatus());
        switch (task.getCode()) {
            case Constants.Action.INDEX_CASE_MEMBER:
                return complete ? R.color.purple : R.color.cyan;
            case Constants.Action.SECONDARY_INDEX_CASE_MEMBER:
                return complete ? R.color.pnc_circle_green : R.color.not_visited_yellow;
            case Constants.Action.RCD_MEMBER:
            default:
                return complete ? R.color.pnc_circle_green : R.color.not_visited_yellow;
        }
    }

    @Override
    public int getActionTextColourRes(Task task) {
        boolean complete = Constants.BusinessStatus.COMPLETE.equals(task.getBusinessStatus());
        switch (task.getCode()) {
            case Constants.Action.INDEX_CASE_MEMBER:
                return complete ? R.color.cyan : android.R.color.black;
            case Constants.Action.SECONDARY_INDEX_CASE_MEMBER:
                return complete ? R.color.purple : R.color.cyan;
            default:
                return android.R.color.black;
        }
    }
}

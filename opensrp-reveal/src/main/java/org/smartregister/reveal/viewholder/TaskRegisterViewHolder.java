package org.smartregister.reveal.viewholder;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.core.util.Pair;
import androidx.recyclerview.widget.RecyclerView;
import org.smartregister.reveal.R;
import org.smartregister.reveal.model.CardDetails;
import org.smartregister.reveal.model.TaskDetails;
import org.smartregister.reveal.util.Constants;
import org.smartregister.reveal.util.Country;
import org.smartregister.reveal.util.PreferencesUtil;
import org.smartregister.reveal.util.Utils;


/**
 * Created by samuelgithengi on 3/12/19.
 */
public class TaskRegisterViewHolder extends RecyclerView.ViewHolder {

    private Context context;

    private ImageView iconView;

    private TextView nameView;

    private TextView distanceView;

    private TextView actionView;

    private TextView taskDetailsView;

    private TextView houseNumberView;

    private PreferencesUtil prefsUtil = PreferencesUtil.getInstance();

    public TaskRegisterViewHolder(@NonNull View itemView) {
        super(itemView);
        context = itemView.getContext();
        iconView = itemView.findViewById(R.id.task_icon);
        nameView = itemView.findViewById(R.id.task_name);
        distanceView = itemView.findViewById(R.id.distance_from_structure);
        taskDetailsView = itemView.findViewById(R.id.task_details);
        actionView = itemView.findViewById(R.id.task_action);
        houseNumberView = itemView.findViewById(R.id.house_number);
    }


    public void setIcon(@DrawableRes int iconResource) {
        iconView.setVisibility(View.VISIBLE);
        iconView.setImageDrawable(context.getResources().getDrawable(iconResource));
    }

    public void setTaskName(String taskName) {
        nameView.setText(taskName);
    }

    public void setDistanceFromStructure(float distance, boolean distanceFromCenter) {
        if (distanceFromCenter) {
            distanceView.setText(context.getString(
                    R.string.distance_from_center, distance, prefsUtil.getCurrentOperationalArea()));
        } else {
            distanceView.setText(context.getString(R.string.distance_from_structure, distance));
        }
    }

    public void hideDistanceFromStructure() {
        distanceView.setVisibility(View.GONE);
    }


    /**
     * Method that handles the populating of action view information on each row of the
     * task register.
     * <p>
     * It handles the text and color displayed on an Action View. Also attaches a clickListener
     * which handles clicks on the action view.
     *
     * @param actionLabel     Text that shows what action to take when action view is clicked
     * @param task            TaskDetails object used to populate info in a particular row
     * @param cardDetails     Object that contains status, status message and status color
     * @param onClickListener Click listener that handles clicks events on the Task Action
     */
    public void setTaskAction(String actionLabel, TaskDetails task, CardDetails cardDetails, View.OnClickListener onClickListener) {
        actionView.setText(actionLabel);
        if ((task.isFamilyRegistered() || task.isFamilyRegTaskExists()) && task.getMdaTasksCount() == 0) {
            actionView.setBackground(context.getResources().getDrawable(R.drawable.family_no_task_registered_bg));
            actionView.setText(R.string.family_member_registered);
        }

        // registered family with multiple tasks
        else if (cardDetails != null && task.getTaskCount() != null) { // task grouping only for FI
            if (task.getTaskCount() > 1) {
                if (task.getTaskCount() != task.getCompleteTaskCount()
                        || (getBuildCountry() == Country.NIGERIA && task.isNoneReceived())) {


                    Pair<Drawable, String> actionViewPair = getActionDrawable(task);
                    actionView.setTextColor(context.getResources().getColor(R.color.text_black));
                    actionView.setBackground(actionViewPair.first);
                    actionView.setText(actionViewPair.second);
                } else if (task.getTaskCount() == task.getCompleteTaskCount()) {
                    showTasksCompleteActionView();
                }
            }

        } else if (cardDetails != null && cardDetails.getStatusColor() != null) {
            actionView.setBackground(context.getResources().getDrawable(R.drawable.no_task_complete_bg));
            actionView.setTextColor(context.getResources().getColor(R.color.text_black));
        } else {
            actionView.setBackground(context.getResources().getDrawable(R.drawable.family_no_task_registered_bg));
            actionView.setTextColor(context.getResources().getColor(R.color.text_black));
            actionView.setText(R.string.family_member_registered);
        }
        actionView.setOnClickListener(onClickListener);
        actionView.setTag(R.id.task_details, task);
    }

    @NonNull
    private Country getBuildCountry() {
        return PreferencesUtil.getInstance().getBuildCountry();
    }

    public void setItemViewListener(TaskDetails task, View.OnClickListener onClickListener) {
        itemView.setOnClickListener(onClickListener);
        itemView.setTag(R.id.task_details, task);
    }

    public void setTaskDetails(String businessStatus, String taskDetails) {
        if (Constants.BusinessStatus.NOT_SPRAYED.equals(businessStatus)) {
            taskDetailsView.setVisibility(View.VISIBLE);
            taskDetailsView.setText(context.getString(R.string.task_reason, taskDetails));
        } else {
            taskDetailsView.setVisibility(View.GONE);
        }
    }

    public void setHouseNumber(String houseNumber) {
        houseNumberView.setText(houseNumber);
    }

    public void showHouseNumber() {
        houseNumberView.setVisibility(View.VISIBLE);
    }
    public void hideHouseNumber() {
        houseNumberView.setVisibility(View.GONE);
    }

    public void hideIcon() {
        iconView.setVisibility(View.GONE);
    }

    private void showTasksCompleteActionView() {
        if (Utils.isFocusInvestigation()) {
            actionView.setBackground(context.getResources().getDrawable(R.drawable.tasks_complete_bg));
        } else if (Utils.isMDA()){
            actionView.setBackground(context.getResources().getDrawable(R.drawable.tasks_complete_bg));
        }
        actionView.setTextColor(context.getResources().getColor(R.color.text_black));
        actionView.setText(context.getText(R.string.tasks_complete));
    }

    private Pair<Drawable, String > getActionDrawable(TaskDetails task) {
        // The assumption is that a register structure task always exists if the structure has
        // atleast one bednet distribution or blood screening task
        boolean familyRegTaskMissingOrFamilyRegComplete = task.isFamilyRegistered() || !task.isFamilyRegTaskExists();
        Drawable actionBg = null;
        String actionText = context.getText(R.string.view_tasks).toString();

        if (Utils.isFocusInvestigation()) {
            if (familyRegTaskMissingOrFamilyRegComplete && task.isBednetDistributed() && task.isBloodScreeningDone()) {
                actionBg = context.getResources().getDrawable(R.drawable.tasks_complete_bg);
                actionText = context.getText(R.string.tasks_complete).toString();
            } else if (familyRegTaskMissingOrFamilyRegComplete && !task.isBednetDistributed() && !task.isBloodScreeningDone()) {
                actionBg = context.getResources().getDrawable(R.drawable.family_registered_bg);
            } else if (familyRegTaskMissingOrFamilyRegComplete && task.isBednetDistributed()) {
                actionBg = context.getResources().getDrawable(R.drawable.bednet_distributed_bg);
            } else if (task.isBloodScreeningDone()) {
                actionBg = context.getResources().getDrawable(R.drawable.blood_screening_complete_bg);
            } else {
                actionBg = context.getResources().getDrawable(R.drawable.no_task_complete_bg);
            }
        } else if (Utils.isMDA()) {
            if (familyRegTaskMissingOrFamilyRegComplete && task.isMdaAdhered() && getBuildCountry() != Country.NIGERIA) {
                actionBg = context.getResources().getDrawable(R.drawable.mda_adhered_bg);
                actionText = context.getText(R.string.tasks_complete).toString();
            } else if (familyRegTaskMissingOrFamilyRegComplete && task.isFullyReceived()) {
                actionBg = context.getResources().getDrawable(R.drawable.mda_partially_received_bg);
            } else if (familyRegTaskMissingOrFamilyRegComplete && task.isPartiallyReceived()) {
                actionBg = context.getResources().getDrawable(R.drawable.mda_partially_received_bg);
            } else if (familyRegTaskMissingOrFamilyRegComplete && task.isNoneReceived()) {
                actionBg = context.getResources().getDrawable(R.drawable.mda_none_received_bg);
            } else if (familyRegTaskMissingOrFamilyRegComplete && task.isNotEligible()) {
                actionBg = context.getResources().getDrawable(R.drawable.family_no_task_registered_bg);
            } else if (familyRegTaskMissingOrFamilyRegComplete) {
                actionBg = context.getResources().getDrawable(R.drawable.family_registered_bg);
            } else {
                actionBg = context.getResources().getDrawable(R.drawable.mda_partially_received_bg);
            }
        }

        return new Pair<>(actionBg, actionText);
    }
}

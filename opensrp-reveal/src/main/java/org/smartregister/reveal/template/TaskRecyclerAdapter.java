package org.smartregister.reveal.template;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.joda.time.DateTime;
import org.smartregister.domain.Task;
import org.smartregister.reveal.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Generic RecyclerView adapter for a task list.
 *
 * <p>Only depends on {@link Task} and the display contracts
 * {@link TaskDisplayProvider} and {@link TaskRowCallbacks}.
 * No HDSS, no plan-specific logic.
 */
public class TaskRecyclerAdapter
        extends RecyclerView.Adapter<TaskRecyclerAdapter.TaskViewHolder> {

    private final Context context;
    private final TaskDisplayProvider displayProvider;
    private final TaskRowCallbacks callbacks;

    private List<TaskRowItem> items     = new ArrayList<>();
    private List<TaskRowItem> allItems  = new ArrayList<>(); // master copy for search

    public TaskRecyclerAdapter(Context context,
                               TaskDisplayProvider displayProvider,
                               TaskRowCallbacks callbacks) {
        this.context         = context;
        this.displayProvider = displayProvider;
        this.callbacks       = callbacks;
    }

    /* ------------------------------------------------------------------ data */

    public void setItems(List<TaskRowItem> newItems) {
        allItems = new ArrayList<>(newItems);
        items    = new ArrayList<>(newItems);
        notifyDataSetChanged();
    }

    /**
     * Filters rows whose primaryLabel or secondaryLabel contains {@code query}.
     * Empty query restores the full list.
     */
    public void filter(String query) {
        if (query == null || query.trim().isEmpty()) {
            items = new ArrayList<>(allItems);
        } else {
            String lower = query.toLowerCase().trim();
            List<TaskRowItem> filtered = new ArrayList<>();
            for (TaskRowItem item : allItems) {
                if (matches(item.getPrimaryLabel(), lower)
                        || matches(item.getSecondaryLabel(), lower)) {
                    filtered.add(item);
                }
            }
            items = filtered;
        }
        notifyDataSetChanged();
    }

    private boolean matches(String value, String query) {
        return value != null && value.toLowerCase().contains(query);
    }

    /* ------------------------------------------------------------------ RecyclerView */

    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_task_row, parent, false);
        return new TaskViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {
        TaskRowItem item = items.get(position);
        Task task        = item.getTask();

        // Primary label
        holder.tvPrimary.setText(item.getPrimaryLabel());

        // Secondary label
        String secondary = item.getSecondaryLabel();
        if (secondary != null && !secondary.isEmpty()) {
            holder.tvSecondary.setText(secondary);
            holder.tvSecondary.setVisibility(View.VISIBLE);
        } else {
            holder.tvSecondary.setVisibility(View.GONE);
        }

        // Created date + old-task warning
        holder.tvCreatedDate.setText(item.getCreatedDate() != null ? item.getCreatedDate() : "");
        if (task.getAuthoredOn() != null
                && task.getAuthoredOn().isBefore(DateTime.now().minusDays(3))) {
            holder.tvOldTaskWarning.setVisibility(View.VISIBLE);
            holder.tvCreatedDate.setTextColor(
                    context.getResources().getColor(android.R.color.holo_red_dark, null));
        } else {
            holder.tvOldTaskWarning.setVisibility(View.GONE);
        }

        // Action button
        holder.btnAction.setText(displayProvider.getActionLabel(task));
        holder.btnAction.setTextColor(
                context.getResources().getColor(displayProvider.getActionTextColourRes(task), null));

        Drawable bg = holder.btnAction.getBackground();
        if (bg instanceof GradientDrawable) {
            ((GradientDrawable) bg).setColor(
                    context.getResources().getColor(displayProvider.getActionColourRes(task), null));
        }

        // Tap — open the task form
        holder.btnAction.setOnClickListener(v -> callbacks.onTaskTap(task));

        // Long-press — reset dialog (only for already-visited tasks)
        if (!isNotVisited(task)) {
            holder.btnAction.setOnLongClickListener(v -> {
                callbacks.onTaskLongPress(task);
                return true;
            });
        } else {
            holder.btnAction.setOnLongClickListener(null);
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    private boolean isNotVisited(Task task) {
        return "Not Visited".equals(task.getBusinessStatus());
    }

    /* ------------------------------------------------------------------ ViewHolder */

    static class TaskViewHolder extends RecyclerView.ViewHolder {

        final TextView tvPrimary;
        final TextView tvSecondary;
        final TextView tvCreatedDate;
        final TextView tvOldTaskWarning;
        final Button   btnAction;

        TaskViewHolder(View itemView) {
            super(itemView);
            tvPrimary        = itemView.findViewById(R.id.tv_primary_label);
            tvSecondary      = itemView.findViewById(R.id.tv_secondary_label);
            tvCreatedDate    = itemView.findViewById(R.id.tv_created_date);
            tvOldTaskWarning = itemView.findViewById(R.id.tv_old_task_warning);
            btnAction        = itemView.findViewById(R.id.btn_task_action);
        }
    }
}

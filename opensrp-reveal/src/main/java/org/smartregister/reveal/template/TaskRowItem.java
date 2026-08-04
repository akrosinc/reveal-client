package org.smartregister.reveal.template;

import org.smartregister.domain.Task;

import lombok.Data;

/**
 * Display model for one row in {@link TaskRecyclerFragment}.
 *
 * <p>Carries the underlying {@link Task} (the only domain object the generic
 * recycler depends on) plus display strings resolved ahead of time by the
 * caller via {@link TaskDisplayProvider}.
 *
 * <p>Keeping the strings pre-resolved means the adapter itself never touches
 * any repository — it is purely a display component.
 */
@Data
public class TaskRowItem {

    /** The underlying task — the single domain dependency. */
    private final Task task;

    /** Pre-resolved primary label (bold top line of the row). */
    private final String primaryLabel;

    /** Pre-resolved optional secondary label. Null/empty → view hidden. */
    private final String secondaryLabel;

    /** Formatted authored-on date string, e.g. "2024-03-15". */
    private final String createdDate;

    /**
     * Builds a {@link TaskRowItem} using a {@link TaskDisplayProvider} to
     * resolve the display strings from the task.
     */
    public static TaskRowItem from(Task task, TaskDisplayProvider provider, String createdDate) {
        return new TaskRowItem(
                task,
                provider.getPrimaryLabel(task),
                provider.getSecondaryLabel(task),
                createdDate
        );
    }
}

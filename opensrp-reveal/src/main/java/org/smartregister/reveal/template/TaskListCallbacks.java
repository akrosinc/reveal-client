package org.smartregister.reveal.template;

import org.smartregister.domain.Task;

/**
 * Callbacks from {@link TaskListFragment} to its host activity.
 *
 * <p>The fragment is purely UI — all persistence logic (cancel, generate, save)
 * is handled by the host via these callbacks.
 */
public interface TaskListCallbacks {

    /**
     * Cancel all existing child tasks, create {@code count} new ones,
     * persist them, and refresh the list.
     *
     * <p>Called when the user taps "Generate" with a valid count.
     *
     * @param count number of child tasks to create
     */
    void onGenerateTasks(int count);

    /**
     * The user tapped the action button on a task row.
     * Open the appropriate form for this task.
     */
    void onTaskTap(Task task);

    /**
     * The user long-pressed a task row (typically to reset it).
     */
    void onTaskLongPress(Task task);
}

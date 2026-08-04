package org.smartregister.reveal.template;

import org.smartregister.domain.Task;

/**
 * Callbacks fired by {@link TaskRecyclerAdapter} when the user interacts
 * with a task row.
 *
 * <p>Implemented by {@link TaskRecyclerFragment}, which forwards the events
 * to its host activity via {@link TaskRecyclerFragment.TaskRecyclerListener}.
 */
public interface TaskRowCallbacks {

    /**
     * The user tapped the action button on a task row.
     * The host decides whether to open a form, show a dialog, etc.
     */
    void onTaskTap(Task task);

    /**
     * The user long-pressed the action button on an already-visited task row.
     * Typically used to show a reset/undo dialog.
     */
    void onTaskLongPress(Task task);
}

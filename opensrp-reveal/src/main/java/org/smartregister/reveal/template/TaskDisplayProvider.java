package org.smartregister.reveal.template;

import org.smartregister.domain.Task;

/**
 * Provides display values for a single {@link Task} row inside
 * {@link org.smartregister.reveal.template.TaskRecyclerFragment}.
 *
 * <p>Implement this per use-case (e.g. GDRSTaskDisplayProvider) to supply
 * domain-specific labels and colours without coupling the generic recycler
 * to any specific repository or domain object beyond {@link Task}.
 */
public interface TaskDisplayProvider {

    /**
     * Main bold label shown at the top of the row.
     * e.g. individual ID, patient name, entity identifier.
     */
    String getPrimaryLabel(Task task);

    /**
     * Optional second line. Return null or empty to hide the view.
     */
    String getSecondaryLabel(Task task);

    /**
     * Text shown on the action button.
     * Typically varies by business status (e.g. "Record", "Edit", "Confirm").
     */
    String getActionLabel(Task task);

    /**
     * Background colour resource id for the action button.
     * Typically varies by business status.
     */
    int getActionColourRes(Task task);

    /**
     * Text colour resource id for the action button.
     */
    int getActionTextColourRes(Task task);
}

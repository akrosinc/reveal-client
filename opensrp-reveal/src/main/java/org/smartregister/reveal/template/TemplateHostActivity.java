package org.smartregister.reveal.template;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import org.smartregister.reveal.activity.RevealJsonFormActivity;
import org.smartregister.reveal.fragment.RevealJsonFormFragment;

import java.util.HashMap;
import java.util.Map;

/**
 * Base activity for all multi-fragment templates.
 *
 * <p>Extends {@link RevealJsonFormActivity} so that any {@link EmbeddedFormFragment}
 * hosted here receives the full Reveal form machinery:
 * <ul>
 *   <li>Location validation (UserLocationView)</li>
 *   <li>Progress dialog</li>
 *   <li>RevealRuleEngineFactory</li>
 *   <li>JsonApi / writeValue / skip-logic / calculations</li>
 * </ul>
 * with zero changes to existing classes.
 *
 * <h3>What this activity adds</h3>
 * <ol>
 *   <li>Collects saved JSON from each {@link EmbeddedFormFragment} via
 *       {@link #onEmbeddedFormSaved(String, String)} instead of finishing.</li>
 *   <li>Provides {@link #onAllFormsSaved(Map)} as the single hook where a
 *       concrete template triggers the actual data persistence.</li>

 * </ol>
 *
 * <h3>Concrete templates extend this class and:</h3>
 * <ol>
 *   <li>Call {@code setContentView()} with their own layout.</li>
 *   <li>Override {@link #initializeFormFragment()} to commit their
 *       {@link EmbeddedFormFragment}(s).</li>
 *   <li>Override {@link #onEmbeddedFormSaved(String, String)} if intermediate
 *       actions are needed after each fragment saves.</li>
 *   <li>Override {@link #onAllFormsSaved(Map)} to persist the data.</li>
 * </ol>
 */
public abstract class TemplateHostActivity extends RevealJsonFormActivity {

    /**
     * Stores JSON collected from each EmbeddedFormFragment keyed by fragmentTag.
     * Concrete templates can inspect this map at any time.
     */
    protected final Map<String, String> savedFormData = new HashMap<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    /* ------------------------------------------------------------------ fragment capture */

    /**
     * Called by {@link EmbeddedFormFragment#finishWithResult} when a form passes
     * validation and the user has saved it.
     *
     * <p>Stores the JSON in {@link #savedFormData} and then calls
     * {@link #onAllFormsSaved} if all required fragments have reported in.
     *
     * @param fragmentTag tag of the fragment that was saved (as committed in FragmentTransaction)
     * @param json        the complete form JSON with all field values
     */
    public void onEmbeddedFormSaved(String fragmentTag, String json) {
        savedFormData.put(fragmentTag, json);
        onFragmentDataReady(fragmentTag, json);
    }

    /**
     * Called each time a single embedded fragment finishes saving.
     * Override in the concrete template to react (e.g. refresh task list, unlock
     * the next section).  Default implementation does nothing.
     *
     * @param fragmentTag tag of the fragment that just saved
     * @param json        the saved form JSON
     */
    protected void onFragmentDataReady(String fragmentTag, String json) {
        // default: no-op — concrete templates override as needed
    }

    /**
     * Called when all required embedded fragments have reported saved data.
     * Override to persist the data, update task status, etc.
     *
     * @param allData map of fragmentTag → saved JSON for every completed fragment
     */
    protected void onAllFormsSaved(Map<String, String> allData) {
        // default: no-op — concrete templates override
    }

    /**
     * Validates all sibling form fragments before allowing a save.
     *
     * <p>Calls only the validation/write portion of the presenter (not the full
     * save flow) on each sibling, then checks the shared invalidFields map.
     *
     * @param excludeTag the tag of the fragment that already validated itself
     * @return true if all other fragments are valid, false if any has errors
     */
    public boolean validateAllFragments(String excludeTag) {
        for (androidx.fragment.app.Fragment f : getSupportFragmentManager().getFragments()) {
            if (f instanceof EmbeddedFormFragment
                    && f.isAdded()
                    && f.getView() != null
                    && !f.getTag().equals(excludeTag)) {
                EmbeddedFormFragment other = (EmbeddedFormFragment) f;
                // Run validation + value writing only (not the full save flow).
                other.getPresenter().validateAndWriteValuess();
                if (!other.getPresenter().isFormValid()) {
                    // Show errors inline — request attention on the first invalid field
                    java.util.Map<String, com.vijay.jsonwizard.utils.ValidationStatus> invalidFields =
                            other.getPresenter().getInvalidFields();
                    for (com.vijay.jsonwizard.utils.ValidationStatus status : invalidFields.values()) {
                        status.requestAttention();
                        break; // scroll to first error only
                    }
                    int errorCount = invalidFields.size();
                    other.showSnackBar(
                            getString(com.vijay.jsonwizard.R.string.json_form_error_msg, errorCount));
                    return false;
                }
            }
        }
        return true;
    }

    /* ------------------------------------------------------------------ loading helpers */


}

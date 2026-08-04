package org.smartregister.reveal.template;

import android.content.Intent;
import android.os.Bundle;

import com.vijay.jsonwizard.constants.JsonFormConstants;

import org.smartregister.reveal.fragment.RevealJsonFormFragment;

/**
 * A {@link RevealJsonFormFragment} that can be embedded inside a host activity
 * alongside other fragments without closing the activity when the form is saved.
 *
 * <h3>What changes vs the base class</h3>
 * <p>Exactly one method is overridden: {@link #finishWithResult(Intent)}.
 *
 * <p>In the base class that method calls {@code getActivity().finish()} which would
 * kill the whole activity.  Here it calls
 * {@link TemplateHostActivity#onEmbeddedFormSaved(String, String)} instead,
 * passing the completed JSON string and this fragment's tag so the host can
 * store / process it without closing the screen.
 *
 * <p>All validation, skip-logic, calculation, location-check, and
 * {@code writeValue} machinery is inherited unchanged from
 * {@link RevealJsonFormFragment} and its presenter.
 *
 * <h3>Usage</h3>
 * <pre>
 *   EmbeddedFormFragment fragment = EmbeddedFormFragment.newInstance("step1", "myTag");
 *   getSupportFragmentManager()
 *       .beginTransaction()
 *       .add(R.id.container_form, fragment, "myTag")
 *       .commit();
 * </pre>
 */
public class EmbeddedFormFragment extends RevealJsonFormFragment {

    private static final String ARG_FRAGMENT_TAG = "embedded_fragment_tag";

    /* ------------------------------------------------------------------ factory */

    /**
     * @param stepName    JSON form step to render (usually {@code "step1"})
     * @param fragmentTag Arbitrary tag identifying this fragment within the host.
     *                    Forwarded to {@link TemplateHostActivity#onEmbeddedFormSaved}
     *                    so the host knows which slot was saved.
     */
    public static EmbeddedFormFragment newInstance(String stepName, String fragmentTag) {
        EmbeddedFormFragment fragment = new EmbeddedFormFragment();
        Bundle args = new Bundle();
        args.putString(JsonFormConstants.JSON_FORM_KEY.STEPNAME, stepName);
        args.putString(ARG_FRAGMENT_TAG, fragmentTag);
        fragment.setArguments(args);
        return fragment;
    }

    /* ------------------------------------------------------------------ key override */

    /**
     * Called by {@link org.smartregister.reveal.presenter.RevealJsonFormFragmentPresenter}
     * after the form passes validation (and optional location/password check).
     *
     * <p>Before notifying the host, validates all sibling form fragments using
     * the engine's full validation. If any sibling has errors, the save is blocked
     * and errors are shown inline on the failing fragment.
     *
     * <p>Instead of finishing the activity, we notify the host so it can save
     * the form data and decide what to do next.
     */
    @Override
    public void finishWithResult(Intent returnIntent) {
        String json        = returnIntent.getStringExtra("json");
        String fragmentTag = getTag();
        if (fragmentTag == null && getArguments() != null) {
            fragmentTag = getArguments().getString(ARG_FRAGMENT_TAG);
        }

        if (getActivity() instanceof TemplateHostActivity) {
            TemplateHostActivity host = (TemplateHostActivity) getActivity();

            // Validate all sibling fragments before allowing save
            if (!host.validateAllFragments(fragmentTag)) {
                // Another fragment has invalid fields — errors shown inline, block save
                return;
            }

            host.onEmbeddedFormSaved(fragmentTag, json);
        }
        // deliberately NOT calling getActivity().finish()
    }
}

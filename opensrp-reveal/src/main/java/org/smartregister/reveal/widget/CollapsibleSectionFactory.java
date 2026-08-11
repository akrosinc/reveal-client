package org.smartregister.reveal.widget;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.vijay.jsonwizard.constants.JsonFormConstants;
import com.vijay.jsonwizard.fragments.JsonFormFragment;
import com.vijay.jsonwizard.interfaces.CommonListener;
import com.vijay.jsonwizard.interfaces.FormWidgetFactory;
import com.vijay.jsonwizard.interfaces.JsonApi;
import com.vijay.jsonwizard.interactors.JsonFormInteractor;

import org.json.JSONArray;
import org.json.JSONObject;
import org.smartregister.reveal.R;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import timber.log.Timber;

/**
 * A collapsible section widget that visually groups fields under an expandable header.
 *
 * <p>The fields themselves are defined at the step's top level (flat) — the section
 * just references their keys via {@code "field_keys"}. This means:
 * <ul>
 *   <li>writeValue, validation, calculations, skip-logic all work unchanged</li>
 *   <li>No runtime mutation of mJSONObject</li>
 *   <li>The section is purely a visual convenience</li>
 * </ul>
 *
 * <h3>JSON usage:</h3>
 * <pre>
 * "fields": [
 *   { "key": "hh_name", "type": "edit_text", "hint": "Head of household", ... },
 *   { "key": "hh_size", "type": "edit_text", "hint": "Household size", ... },
 *   {
 *     "key": "household_section",
 *     "type": "collapsible_section",
 *     "label": "Household Details",
 *     "expanded": true,
 *     "field_keys": ["hh_name", "hh_size"]
 *   }
 * ]
 * </pre>
 *
 * <p>The referenced fields are rendered INSIDE the collapsible container instead of
 * at their normal position. They must be defined BEFORE the section in the fields array
 * (or the factory won't find them in formDataViews).
 *
 * <p>Tapping the header toggles the content visibility (expand/collapse).
 */
public class CollapsibleSectionFactory implements FormWidgetFactory {

    public static final String COLLAPSIBLE_SECTION = "collapsible_section";
    private static final String LABEL = "label";
    private static final String EXPANDED = "expanded";
    private static final String FIELD_KEYS = "field_keys";

    @Override
    public List<View> getViewsFromJson(String stepName, Context context,
                                       JsonFormFragment formFragment, JSONObject jsonObject,
                                       CommonListener listener, boolean popup) throws Exception {

        List<View> views = new ArrayList<>(1);

        // Inflate the collapsible section layout
        LinearLayout rootLayout = (LinearLayout) LayoutInflater.from(context)
                .inflate(R.layout.item_collapsible_section, null);

        // Set up header label
        TextView titleView = rootLayout.findViewById(R.id.tv_section_title);
        String label = jsonObject.optString(LABEL, "Section");
        titleView.setText(label);

        // Content container
        LinearLayout contentContainer = rootLayout.findViewById(R.id.section_content);

        // Initial expanded/collapsed state
        boolean expanded = jsonObject.optBoolean(EXPANDED, true);
        ImageView chevron = rootLayout.findViewById(R.id.iv_chevron);
        setExpandedState(contentContainer, chevron, expanded);

        // Toggle on header click
        View header = rootLayout.findViewById(R.id.section_header);
        header.setOnClickListener(v -> {
            boolean isVisible = contentContainer.getVisibility() == View.VISIBLE;
            setExpandedState(contentContainer, chevron, !isVisible);
        });

        // Get the referenced field keys
        JSONArray fieldKeys = jsonObject.optJSONArray(FIELD_KEYS);
        if (fieldKeys != null) {
            JsonApi jsonApi = (JsonApi) context;
            LinearLayout mainLayout = formFragment.getMainView();

            Set<String> keySet = new HashSet<>();
            for (int i = 0; i < fieldKeys.length(); i++) {
                String k = fieldKeys.optString(i);
                if (k != null && !k.isEmpty()) keySet.add(k);
            }

            // Defer re-parenting until AFTER addFormElements has added all views to mainLayout.
            // Using post() ensures this runs after the current message (addFormElements) completes.
            final Set<String> finalKeySet = keySet;
            final String finalStepName = stepName;
            mainLayout.post(() -> {
                Timber.tag("CollapsibleSection").i("post: mainLayout childCount=%d, keySet=%s",
                        mainLayout.getChildCount(), finalKeySet.toString());

                List<View> viewsToMove = new ArrayList<>();
                Set<View> alreadyFound = new HashSet<>();

                for (int i = 0; i < mainLayout.getChildCount(); i++) {
                    View child = mainLayout.getChildAt(i);
                    for (String fieldKey : finalKeySet) {
                        String address = finalStepName + ":" + fieldKey;
                        View dataView = jsonApi.getFormDataView(address);
                        if (dataView != null && !alreadyFound.contains(child)) {
                            if (isDescendantOf(dataView, child) || dataView == child) {
                                viewsToMove.add(child);
                                alreadyFound.add(child);
                                Timber.tag("CollapsibleSection").i("Found field '%s' in child index %d", fieldKey, i);
                                break;
                            }
                        }
                    }
                }

                for (View viewToMove : viewsToMove) {
                    mainLayout.removeView(viewToMove);
                    contentContainer.addView(viewToMove);
                }

                Timber.tag("CollapsibleSection").i("Moved %d views into section '%s'",
                        viewsToMove.size(), label);
            });
        }

        // Set tags for skip-logic and toggleViewVisibility compatibility
        String key = jsonObject.optString(JsonFormConstants.KEY, "");
        int viewId = View.generateViewId();
        rootLayout.setId(viewId);
        JSONArray canvasIds = new JSONArray();
        canvasIds.put(viewId);
        rootLayout.setTag(com.vijay.jsonwizard.R.id.canvas_ids, canvasIds.toString());
        rootLayout.setTag(com.vijay.jsonwizard.R.id.key, key);
        rootLayout.setTag(com.vijay.jsonwizard.R.id.type, COLLAPSIBLE_SECTION);
        rootLayout.setTag(com.vijay.jsonwizard.R.id.address, stepName + ":" + key);
        rootLayout.setTag(com.vijay.jsonwizard.R.id.extraPopup, popup);

        JSONObject relevanceObj = jsonObject.optJSONObject(JsonFormConstants.RELEVANCE);
        if (relevanceObj != null) {
            rootLayout.setTag(com.vijay.jsonwizard.R.id.relevance, relevanceObj.toString());
            ((JsonApi) context).addSkipLogicView(rootLayout);
        }

        views.add(rootLayout);
        return views;
    }

    @Override
    public List<View> getViewsFromJson(String stepName, Context context,
                                       JsonFormFragment formFragment, JSONObject jsonObject,
                                       CommonListener listener) throws Exception {
        return getViewsFromJson(stepName, context, formFragment, jsonObject, listener, false);
    }

    @NonNull
    @Override
    public Set<String> getCustomTranslatableWidgetFields() {
        Set<String> fields = new HashSet<>();
        fields.add(LABEL);
        return fields;
    }

    private void setExpandedState(View content, ImageView chevron, boolean expanded) {
        content.setVisibility(expanded ? View.VISIBLE : View.GONE);
        chevron.setRotation(expanded ? 180f : 0f);
    }

    /**
     * Checks if {@code child} is a descendant of {@code parent} in the view hierarchy.
     */
    private boolean isDescendantOf(View child, View parent) {
        if (child == parent) return true;
        View current = child;
        while (current != null) {
            if (current == parent) return true;
            if (current.getParent() instanceof View) {
                current = (View) current.getParent();
            } else {
                break;
            }
        }
        return false;
    }
}

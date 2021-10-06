package org.smartregister.reveal.widget;

import android.content.Context;
import android.view.View;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;

import com.vijay.jsonwizard.constants.JsonFormConstants;
import com.vijay.jsonwizard.domain.MultiSelectItem;
import com.vijay.jsonwizard.domain.MultiSelectListAccessory;
import com.vijay.jsonwizard.fragments.JsonFormFragment;
import com.vijay.jsonwizard.interfaces.CommonListener;
import com.vijay.jsonwizard.interfaces.JsonApi;
import com.vijay.jsonwizard.utils.ValidationStatus;
import com.vijay.jsonwizard.views.JsonFormFragmentView;
import com.vijay.jsonwizard.widgets.MultiSelectListFactory;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.smartregister.util.JsonFormUtils;

import java.util.HashMap;
import java.util.List;

public class RevealMultiSelectListFactory  extends MultiSelectListFactory {
    private static HashMap<String, MultiSelectListAccessory> multiSelectListAccessoryHashMap = new HashMap<>();

    @Override
    public List<View> getViewsFromJson(@NonNull String stepName, @NonNull Context context, @NonNull JsonFormFragment formFragment, @NonNull JSONObject jsonObject, @NonNull CommonListener listener, boolean popup) throws Exception {
        List<View> views = super.getViewsFromJson(stepName, context, formFragment, jsonObject, listener, popup);
        RelativeLayout actionView = createActionView(context);
        actionView.setTag(com.vijay.jsonwizard.R.id.address, stepName + ":" + jsonObject.optString(JsonFormConstants.KEY));
        ((JsonApi) context).addFormDataView(actionView);
        return views;
    }

    public static ValidationStatus validate(JsonFormFragmentView fragmentView, RelativeLayout multiselectLayout) {
        String error  = "Required field";
        JSONObject currentJsonState = null;
        try {
           currentJsonState = new JSONObject(fragmentView.getCurrentJsonState());
           JSONArray fields = JsonFormUtils.fields(currentJsonState);
           JSONObject fieldToCheck = null;
            for(int i=0;i<fields.length();i++){
                JSONObject field = fields.getJSONObject(i);
                if(field.optString("type").equals(JsonFormConstants.MULTI_SELECT_LIST)){
                    fieldToCheck = field;
                    break;
                }
            }
            if(fieldToCheck.optString("value").equals("[]")){
                return new ValidationStatus(false, error, fragmentView, multiselectLayout);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return new ValidationStatus(true, error, fragmentView, multiselectLayout);
    }

    private static boolean performValidation(RelativeLayout relativeLayout) {

        boolean isSelected = false;
        String currentAdapterKey = (String) relativeLayout.getTag(com.vijay.jsonwizard.R.id.key);
        MultiSelectListAccessory multiSelectListAccessory = getRevealMultiSelectListAccessoryHashMap().get(currentAdapterKey);
        if (multiSelectListAccessory != null) {
            List<MultiSelectItem> multiSelectItems = multiSelectListAccessory.getSelectedAdapter().getData();

            if (!multiSelectItems.isEmpty()) {
                isSelected = true;
            }
        }
        return isSelected;
    }
    public static HashMap<String, MultiSelectListAccessory> getRevealMultiSelectListAccessoryHashMap() {
        return multiSelectListAccessoryHashMap;
    }

}
package org.smartregister.reveal.widget;

import android.content.Context;
import android.content.res.Configuration;
import android.text.Editable;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;

import android.widget.RelativeLayout;
import com.rengwuxian.materialedittext.MaterialEditText;
import com.rey.material.util.ViewUtil;
import com.vijay.jsonwizard.constants.JsonFormConstants;
import com.vijay.jsonwizard.customviews.GenericTextWatcher;
import com.vijay.jsonwizard.fragments.JsonFormFragment;
import com.vijay.jsonwizard.interfaces.CommonListener;
import com.vijay.jsonwizard.interfaces.JsonApi;
import com.vijay.jsonwizard.utils.FormUtils;
import com.vijay.jsonwizard.widgets.EditTextFactory;

import java.util.ArrayList;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.smartregister.reveal.R;

import java.util.List;

import static org.smartregister.reveal.util.Constants.JsonForm.HINT;
import static org.smartregister.reveal.util.Constants.JsonForm.NO_PADDING;
import static org.smartregister.reveal.util.Constants.JsonForm.SHORTENED_HINT;


public class RevealEditTextFactory extends EditTextFactory {

    private boolean hasNoPadding;
    private final FormUtils formUtils = new FormUtils();

    @Override
    public List<View> getViewsFromJson(String stepName, Context context, JsonFormFragment formFragment, JSONObject jsonObject, CommonListener listener, boolean popup) throws Exception {
        hasNoPadding = new JSONObject(formFragment.getCurrentJsonState()).getJSONObject(stepName).optBoolean(NO_PADDING);
        return super.getViewsFromJson(stepName, context, formFragment, jsonObject, listener, popup);
    }

    @Override
    protected List<View> attachJson(String stepName, Context context, JsonFormFragment formFragment, JSONObject jsonObject,
        CommonListener listener, boolean popup) throws Exception {
        List<View> views = new ArrayList<>(1);

        RelativeLayout rootLayout = getRelativeLayout(context);
        RelativeLayout editTextLayout = rootLayout.findViewById(com.vijay.jsonwizard.R.id.edit_text_layout);
        MaterialEditText editText = editTextLayout.findViewById(com.vijay.jsonwizard.R.id.edit_text);
        ImageView editButton = editTextLayout.findViewById(com.vijay.jsonwizard.R.id.material_edit_text_edit_button);

        FormUtils.setEditButtonAttributes(jsonObject, editText, editButton, listener);
        attachLayout(stepName, context, formFragment, jsonObject, editText, editButton);

        JSONArray canvasIds = new JSONArray();
        rootLayout.setId(ViewUtil.generateViewId());
        canvasIds.put(rootLayout.getId());
        editText.setTag(com.vijay.jsonwizard.R.id.canvas_ids, canvasIds.toString());
        editText.setTag(com.vijay.jsonwizard.R.id.extraPopup, popup);

        attachInfoIcon(stepName, jsonObject, rootLayout, canvasIds, listener);

        ((JsonApi) context).addFormDataView(editText);
        views.add(rootLayout);
        return views;
    }

    private void attachInfoIcon(String stepName, JSONObject jsonObject, RelativeLayout rootLayout, JSONArray canvasIds,
        CommonListener listener) throws JSONException {
        if (jsonObject.has(JsonFormConstants.LABEL_INFO_TEXT) || jsonObject.has(JsonFormConstants.LABEL_INFO_HAS_IMAGE)) {
            ImageView infoIcon = rootLayout.findViewById(com.vijay.jsonwizard.R.id.info_icon);
            formUtils.showInfoIcon(stepName, jsonObject, listener, FormUtils.getInfoDialogAttributes(jsonObject), infoIcon, canvasIds);
        }

    }

    @Override
    protected void attachLayout(String stepName, Context context, JsonFormFragment formFragment, JSONObject jsonObject, MaterialEditText editText, ImageView editButton) throws Exception {
        super.attachLayout(stepName, context, formFragment, jsonObject, editText, editButton);
        Configuration config = context.getResources().getConfiguration();
        int textSize = FormUtils.getValueFromSpOrDpOrPx(jsonObject.optString("text_size", String.valueOf(context.getResources().getDimension(com.vijay.jsonwizard.R
                .dimen.default_text_size))), context);
        if (config.smallestScreenWidthDp < 600) {
            editText.setFloatingLabelTextSize((int) (textSize * 1.6));
            editText.setTextSize(textSize);
        } else {
            editText.setFloatingLabelTextSize(textSize * 3);
            editText.setTextSize(textSize * 2);
        }
        editText.setGravity(Gravity.START);

        // truncate hint when typing
//+---------------------------
    }

    @Override
    protected int getLayout() {
        if (hasNoPadding) {
            return R.layout.padded_item_edit_text;
        } else {
            return super.getLayout();
        }
    }
}

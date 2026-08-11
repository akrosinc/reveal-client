package org.smartregister.reveal.widget;

import static org.smartregister.reveal.util.Constants.JsonForm.NO_PADDING;

import android.content.Context;
import android.content.res.Configuration;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import com.rengwuxian.materialedittext.MaterialEditText;
import com.rey.material.util.ViewUtil;
import com.vijay.jsonwizard.constants.JsonFormConstants;
import com.vijay.jsonwizard.domain.Form;
import com.vijay.jsonwizard.fragments.JsonFormFragment;
import com.vijay.jsonwizard.interfaces.CommonListener;
import com.vijay.jsonwizard.utils.FormUtils;
import com.vijay.jsonwizard.utils.Utils;
import com.vijay.jsonwizard.validators.edittext.RequiredValidator;
import com.vijay.jsonwizard.widgets.DatePickerFactory;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.smartregister.reveal.R;

public class RevealDatePickerFactory extends DatePickerFactory {

    private static final String TAG_DEFAULT_DATE = "default_date_value";

    @Override
    protected void attachLayout(String stepName, final Context context, JsonFormFragment formFragment, JSONObject jsonObject,
        final MaterialEditText editText, final TextView duration) {
        super.attachLayout(stepName, context, formFragment, jsonObject, editText, duration);
        updateEditText(editText, jsonObject, stepName, context, duration);

        // If default is "today", store it and re-apply when field becomes visible
        String defaultValue = jsonObject.optString("default", "");
        if ("today".equalsIgnoreCase(defaultValue)) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy", java.util.Locale.ENGLISH);
            String todayDate = sdf.format(new java.util.Date());
            editText.setTag(R.id.raw_value, todayDate);

            // Listen for visibility changes — when the view becomes VISIBLE and has no text,
            // re-apply the default date
            editText.addOnAttachStateChangeListener(new android.view.View.OnAttachStateChangeListener() {
                @Override
                public void onViewAttachedToWindow(android.view.View v) {}
                @Override
                public void onViewDetachedFromWindow(android.view.View v) {}
            });

            // Use ViewTreeObserver to detect when visibility changes
            editText.getViewTreeObserver().addOnGlobalLayoutListener(() -> {
                if (editText.getVisibility() == android.view.View.VISIBLE
                        && editText.getText().toString().isEmpty()) {
                    String defaultDate = (String) editText.getTag(R.id.raw_value);
                    if (defaultDate != null && !defaultDate.isEmpty()) {
                        editText.setText(defaultDate);
                        editText.setTag(R.id.locale_independent_value, defaultDate);
                    }
                }
            });
        }
    }
    private void updateEditText(MaterialEditText editText, JSONObject jsonObject, String stepName, Context context, TextView duration)  {
        int textSize = FormUtils.getValueFromSpOrDpOrPx(jsonObject.optString("text_size", String.valueOf(context.getResources().getDimension(com.vijay.jsonwizard.R
            .dimen.default_text_size))), context);
        Configuration config = context.getResources().getConfiguration();
        if (config.smallestScreenWidthDp < 600) {
            editText.setFloatingLabelTextSize((int) (textSize * 1.6));
            editText.setTextSize(textSize);
        } else {
            editText.setFloatingLabelTextSize(textSize * 3);
            editText.setTextSize(textSize * 2);
        }
        editText.setGravity(Gravity.START);
    }
}

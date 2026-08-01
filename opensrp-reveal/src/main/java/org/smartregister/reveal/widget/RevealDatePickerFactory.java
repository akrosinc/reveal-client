package org.smartregister.reveal.widget;

import static org.smartregister.reveal.util.Constants.JsonForm.NO_PADDING;

import android.content.Context;
import android.content.res.Configuration;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
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

    @Override
    protected void attachLayout(String stepName, final Context context, JsonFormFragment formFragment, JSONObject jsonObject,
        final MaterialEditText editText, final TextView duration) {
        super.attachLayout(stepName,context,formFragment,jsonObject,editText,duration);
        updateEditText(editText,jsonObject,stepName,context,duration);
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

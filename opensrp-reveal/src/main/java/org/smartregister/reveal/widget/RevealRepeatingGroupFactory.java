package org.smartregister.reveal.widget;

import static android.view.View.GONE;
import static com.vijay.jsonwizard.constants.JsonFormConstants.VALUE;
import static org.smartregister.reveal.util.Constants.JsonForm.ROOMS_ELIGIBLE;
import static org.smartregister.reveal.util.Constants.JsonForm.ROOMS_SPRAYED;
import static org.smartregister.util.JsonFormUtils.STEP1;

import android.content.Context;
import android.view.View;
import android.view.ViewParent;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import android.widget.Toast;
import androidx.annotation.NonNull;

import com.vijay.jsonwizard.domain.WidgetArgs;
import com.vijay.jsonwizard.fragments.JsonFormFragment;
import com.vijay.jsonwizard.interfaces.CommonListener;
import com.vijay.jsonwizard.task.AttachRepeatingGroupTask;
import com.vijay.jsonwizard.widgets.RepeatingGroupFactory;

import java.util.AbstractMap;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;
import lombok.Data;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.smartregister.reveal.activity.RevealJsonFormActivity;
import org.smartregister.reveal.util.Country;
import org.smartregister.reveal.util.PreferencesUtil;

import java.util.List;
import timber.log.Timber;

public class RevealRepeatingGroupFactory extends RepeatingGroupFactory {

  public static final String CONFIRMED_ROOMS_NOT_SPRAYED = "Confirmed # rooms not sprayed";
  public static final String CONFIRMED_ROOMS_NOT_SPRAYED_ERROR =
      "Please correct number of rooms not sprayed ";
  public static final String PLEASE_ENTER_A_VALUE_ERROR_MESSAGE = "Please enter a value";
  public static final String NUMBER_OF_INSECTICIDE_SACHETS_MIXED =
      "Number of insecticide sachets mixed";
  public static final String NUMBER_OF_INSECTICIDE_SACHETS_VALIDATION_ERR_MESSAGE =
      "number of insecticide sachets must be greater than 0";


  @Override
  public List<View> getViewsFromJson(
      String stepName,
      Context context,
      JsonFormFragment formFragment,
      JSONObject jsonObject,
      CommonListener listener,
      boolean popup)
      throws Exception {

    List<View> viewsFromJson =
        super.getViewsFromJson(stepName, context, formFragment, jsonObject, listener, popup);

    return viewsFromJson;
  }

  @Override
  public List<View> getViewsFromJson(
      String stepName,
      Context context,
      JsonFormFragment formFragment,
      JSONObject jsonObject,
      CommonListener listener)
      throws Exception {
    return super.getViewsFromJson(stepName, context, formFragment, jsonObject, listener);
  }

  // TODO: test this method, after upgrading version of opensrp-native-form
  @Override
  protected void addOnDoneAction(TextView textView, ImageButton doneButton, WidgetArgs widgetArgs) {
    Toast.makeText(textView.getContext(), "Click started",Toast.LENGTH_SHORT).show();
    RevealJsonFormActivity activity = (RevealJsonFormActivity) textView.getContext();
    if (Country.SENEGAL.equals(getBuildCountry()) || Country.SENEGAL_EN.equals(getBuildCountry())) {
      String inputText = textView.getText().toString();
      if (inputText.isEmpty()) {
        textView.setError(PLEASE_ENTER_A_VALUE_ERROR_MESSAGE);
        return;
      }

      Integer reasonsOrSachetCount = Integer.parseInt(inputText);
      if (CONFIRMED_ROOMS_NOT_SPRAYED.equals(textView.getHint().toString())) {
        TextView roomSprayedTextView =
            (TextView) activity.getFormDataView(STEP1 + ":" + ROOMS_SPRAYED);
        Integer roomsSprayedCount = Integer.parseInt(roomSprayedTextView.getText().toString());
        TextView roomsEligibleTextView =
            (TextView) activity.getFormDataView(STEP1 + ":" + ROOMS_ELIGIBLE);
        Integer roomsEligible = Integer.parseInt(roomsEligibleTextView.getText().toString());
        if (reasonsOrSachetCount != (roomsEligible - roomsSprayedCount)) {
          textView.setError(CONFIRMED_ROOMS_NOT_SPRAYED_ERROR);
          return;
        }
      } else if (NUMBER_OF_INSECTICIDE_SACHETS_MIXED.equals(textView.getHint().toString())) {
        if (reasonsOrSachetCount < 1) {
          textView.setError(NUMBER_OF_INSECTICIDE_SACHETS_VALIDATION_ERR_MESSAGE);
          return;
        }
      }
    }

    Object tag = textView.getTag(com.vijay.jsonwizard.R.id.address);

    if (tag != null && ((String) tag).equals("step1:rdt_results_number")) {

      try {
        ViewParent parent = textView.getParent();
        View parentView = (View) parent;

        ViewParent parentParent = parentView.getParent();
        View parentParentView = (View) parentParent;

        Map<String, JSONObject> formFieldsMap = activity.getFormFieldsMap();
        JSONObject rdtJsonObject = formFieldsMap.get("step1_rdt_results_number");

        JSONArray repeatingGroupLayout = rdtJsonObject.getJSONArray(VALUE);

        Map<Integer, String> repeatingGroupLayouts = new HashMap<>();
        repeatingGroupLayouts.put(parentParentView.getId(), repeatingGroupLayout.toString());


/////////////////////
        List<String> keyList = new ArrayList<>();
        JSONObject jsonObject = formFieldsMap.get("step1_children_under_5");
        if (jsonObject != null) {
          JSONArray value = jsonObject.getJSONArray("value");
          for (int i = 0; i < value.length(); i++) {
            JSONObject jsonObject1 = value.getJSONObject(i);
            String string = jsonObject1.getString("key");
            keyList.add("step1_".concat(string));
          }
        }
//        Timber.tag("WriteValue").i("keyList %s", keyList);
        Set<String> finalKeyList = new HashSet<>();
        Set<String> strings = formFieldsMap.keySet();
        for (String key : strings) {
          String[] split = key.split("\\|");
          if (split.length > 1) {
            String compareKey = split[0];
            if (keyList.contains(compareKey)) {
              finalKeyList.add(split[1]);
            }
          }
        }
/////////////////////
        List<String> keyListAutoSelect = new ArrayList<>();
        JSONObject jsonObjectAutoSelect = formFieldsMap.get("step1_additional_people_autoselect");
        if (jsonObjectAutoSelect != null) {
          JSONArray value = jsonObjectAutoSelect.getJSONArray("value");
          for (int i = 0; i < value.length(); i++) {
            JSONObject jsonObject1 = value.getJSONObject(i);
            String string = jsonObject1.getString("key");
            keyListAutoSelect.add("step1_".concat(string));
          }
        }
        Set<String> finalKeyListAutoSelect = new HashSet<>();
        Set<String> stringsAutoSelect = formFieldsMap.keySet();
        for (String key : stringsAutoSelect) {
          String[] split = key.split("\\|");
          if (split.length > 1) {
            String compareKey = split[0];
            if (keyListAutoSelect.contains(compareKey)) {
              finalKeyListAutoSelect.add(split[1]);
            }
          }
        }

        List<Entry<String, String>> childNameUniqueIdEntryList = new ArrayList<>();

        for (String key : finalKeyList) {
          JSONObject jsonObject1 = formFieldsMap.get("step1_child_name".concat("|").concat(key));
          if (jsonObject1 != null) {
            String childName = jsonObject1.getString("value");
            Entry<String, String> stringStringSimpleEntry = new SimpleEntry<>(key, childName);
            childNameUniqueIdEntryList.add(stringStringSimpleEntry);
          }
        }

        TextView formDataView = (TextView) activity.getFormDataView(
            STEP1 + ":" + "additional_people_autoselect");
        Object tag1 = formDataView.getTag(com.vijay.jsonwizard.R.id.repeating_group_item_meta);

        for (String key : finalKeyListAutoSelect) {
          if (tag1!=null&&!((String)tag1).isEmpty()){
            String childName = (String)tag1;
            Entry<String, String> stringStringSimpleEntry = new SimpleEntry<>(key, childName);
            childNameUniqueIdEntryList.add(stringStringSimpleEntry);
          }
        }

        addOnDoneActionCustom(
            textView,
            doneButton,
            widgetArgs,
            finalKeyList.size(),
            childNameUniqueIdEntryList,
            repeatingGroupLayouts);

        doneButton.setVisibility(GONE);
      } catch (JSONException e) {
        Timber.tag("WriteValue").e(e, "Error in addOnDoneAction");
      }

    } else if (tag != null && ((String) tag).equals("step1:additional_people_autoselect")) {
      try {
        ViewParent parent = textView.getParent();
        View parentView = (View) parent;

        Timber.tag("WriteValue").i(" parentView %s", parentView.getId());

        ViewParent parentParent = parentView.getParent();
        View parentParentView = (View) parentParent;
        Timber.tag("WriteValue").i(" parentParentView %s", parentParentView.getId());

        Map<String, JSONObject> formFieldsMap = activity.getFormFieldsMap();
        JSONObject rdtJsonObject = formFieldsMap.get("step1_additional_people_autoselect");

        JSONArray repeatingGroupLayout = rdtJsonObject.getJSONArray(VALUE);

        Map<Integer, String> repeatingGroupLayouts = new HashMap<>();
        repeatingGroupLayouts.put(parentParentView.getId(), repeatingGroupLayout.toString());
        Timber.tag("WriteValue").i("step1:additional_people_autoselect text view");

        JSONObject jsonObject = formFieldsMap.get("step1_additional_people");
        List<String> keyList = new ArrayList<>();
        if (jsonObject != null) {
          JSONArray value = jsonObject.getJSONArray("value");

          for (int i = 0; i < value.length(); i++) {
            JSONObject jsonObject1 = value.getJSONObject(i);
            String string = jsonObject1.getString("key");
            keyList.add("step1_".concat(string));
          }
        }
        Timber.tag("WriteValue").i("keyList %s", keyList);

        Set<String> finalKeyList = new HashSet<>();

        Set<String> strings = formFieldsMap.keySet();
        for (String key : strings) {

          String[] split = key.split("\\|");
          if (split.length > 1) {
            String compareKey = split[0];
            if (keyList.contains(compareKey)) {
              finalKeyList.add(split[1]);
            }
          }
        }
        List<Entry<String, String>> childNameUniqueIdEntryList = new ArrayList<>();
        for (String key : finalKeyList) {
          JSONObject jsonObject1 =
              formFieldsMap.get("step1_additional_people_name".concat("|").concat(key));
          if (jsonObject1 != null) {
            String childName = jsonObject1.getString("value");
            Timber.tag("WriteValue").i("childName %s", childName);
            Entry<String, String> stringStringSimpleEntry = new SimpleEntry<>(key, childName);
            childNameUniqueIdEntryList.add(stringStringSimpleEntry);
          }
        }
        if (!childNameUniqueIdEntryList.isEmpty()) {
          List<Entry<String, String>> selectedUniqueIdEntryList = new ArrayList<>();
          Random random = new Random();

          Entry<String, String> stringStringEntry =
              childNameUniqueIdEntryList.get(random.nextInt(childNameUniqueIdEntryList.size()));

          selectedUniqueIdEntryList.add(stringStringEntry);

          addOnDoneActionCustom(
              textView,
              doneButton,
              widgetArgs,
              selectedUniqueIdEntryList.size(),
              selectedUniqueIdEntryList,
              repeatingGroupLayouts);

          doneButton.setVisibility(GONE);
        }

      } catch (JSONException e) {
        Timber.tag("WriteValue").e(e, "Error in addOnDoneAction");
      }
    } else {
      super.addOnDoneAction(textView, doneButton, widgetArgs);
    }
  }

  protected void addOnDoneActionCustom(
      final TextView textView, final ImageButton doneButton, final WidgetArgs widgetArgs,int number, List<Entry<String,String>> childNameUniqueIdEntryList,Map<Integer, String> repeatingGroupLayouts) {
    try {
      InputMethodManager inputMethodManager =
          (InputMethodManager)
              widgetArgs
                  .getFormFragment()
                  .getContext()
                  .getSystemService(Context.INPUT_METHOD_SERVICE);
      inputMethodManager.hideSoftInputFromWindow(textView.getWindowToken(), 0);
      textView.clearFocus();
      attachRepeatingGroupCustom(
          textView.getParent().getParent(),

          doneButton,
          widgetArgs,
          childNameUniqueIdEntryList,
          repeatingGroupLayouts);
    } catch (Exception e) {
      Timber.e(e);
    }
  }

  private void attachRepeatingGroupCustom(
      final ViewParent parent,

      final ImageButton doneButton,
      final WidgetArgs widgetArgs,
      List<Entry<String,String>> childNameUniqueIdEntryList,
      Map<Integer, String> repeatingGroupLayouts) {


    new AttachRepeatingGroupTask(
            parent, repeatingGroupLayouts, widgetArgs, doneButton, childNameUniqueIdEntryList)
        .execute();
  }

  @NonNull
  private Country getBuildCountry() {
    return PreferencesUtil.getInstance().getBuildCountry();
  }
}

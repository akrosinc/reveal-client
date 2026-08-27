package org.smartregister.reveal.model;

import com.vijay.jsonwizard.domain.MultiSelectItem;
import com.vijay.jsonwizard.interfaces.MultiSelectListRepository;

import org.json.JSONException;
import org.json.JSONObject;
import org.smartregister.repository.InterventionAdditionalDetailsRepository;
import org.smartregister.reveal.application.RevealApplication;
import org.smartregister.reveal.util.PreferencesUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import timber.log.Timber;

public class HeadOfHouseholdRepository implements MultiSelectListRepository {
    @Override
    public List<MultiSelectItem> fetchData() {

        InterventionAdditionalDetailsRepository interventionAdditionalDetailsRepository =
                RevealApplication.getInstance().getContext().getInterventionAdditionalDetailsRepository();
        String currentPlanId = PreferencesUtil.getInstance().getCurrentPlanId();

        List<String> hohTypedList = interventionAdditionalDetailsRepository.getStringValuePerFieldCode("hoh_typed", currentPlanId);
        List<MultiSelectItem> multiSelectItems = new ArrayList<>();

        JSONObject property = new JSONObject();
        try {
            property.put("presumed-id","err");
            property.put("confirmed-id","err");

        } catch (JSONException e) {
            Timber.tag("Reveal Exception").w(e);
        }

        if (hohTypedList != null){
            multiSelectItems = hohTypedList.stream().map(hohTyped->{
                MultiSelectItem item = new MultiSelectItem();
                item.setKey(hohTyped);
                item.setText(hohTyped);
                item.setOpenmrsEntityId("");
                item.setOpenmrsEntity("");
                item.setOpenmrsEntityParent("");
                item.setValue(property.toString());
                return item;
            }).collect(Collectors.toList());
        }

        return multiSelectItems;
    }
}

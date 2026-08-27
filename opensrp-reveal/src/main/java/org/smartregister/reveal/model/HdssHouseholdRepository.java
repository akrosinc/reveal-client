package org.smartregister.reveal.model;

import com.vijay.jsonwizard.domain.MultiSelectItem;
import com.vijay.jsonwizard.interfaces.MultiSelectListRepository;

import org.json.JSONException;
import org.json.JSONObject;
import org.smartregister.repository.HdssRepository;
import org.smartregister.reveal.application.RevealApplication;
import org.smartregister.reveal.multiselect.HdssHouseholdMultiSelectItem;
import org.smartregister.reveal.util.PreferencesUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import timber.log.Timber;

public class HdssHouseholdRepository implements MultiSelectListRepository {
    @Override
    public List<MultiSelectItem> fetchData() {

        HdssRepository hdssRepository =
                RevealApplication.getInstance().getContext().getHdssRepository();

        String selectedCompoundID = PreferencesUtil.getInstance().getSelectedCompoundID();
        List<String> householdsByCompoundId = hdssRepository.getHouseholds();

        JSONObject property = new JSONObject();
        try {
            property.put("presumed-id","err");
            property.put("confirmed-id","err");

        } catch (JSONException e) {
            Timber.tag("Reveal Exception").w(e);
        }

        List<MultiSelectItem> multiSelectItems = new ArrayList<>();
        if (selectedCompoundID != null){
            if (householdsByCompoundId != null){
                multiSelectItems = householdsByCompoundId.stream().map(household->{
                    HdssHouseholdMultiSelectItem item = new HdssHouseholdMultiSelectItem();
                    item.setKey(household);
                    item.setText(household);
                    item.setOpenmrsEntityId("");
                    item.setOpenmrsEntity("");
                    item.setOpenmrsEntityParent("");
                    item.setValue(property.toString());
                    return item;
                }).collect(Collectors.toList());
            }

        }
        return multiSelectItems;
    }
}

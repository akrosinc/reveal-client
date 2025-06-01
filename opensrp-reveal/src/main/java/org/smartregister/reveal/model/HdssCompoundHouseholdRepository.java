package org.smartregister.reveal.model;

import com.vijay.jsonwizard.domain.MultiSelectItem;
import com.vijay.jsonwizard.interfaces.MultiSelectListRepository;

import java.util.Set;
import org.json.JSONException;
import org.json.JSONObject;
import org.smartregister.domain.HdssCompoundHousehold;
import org.smartregister.repository.HdssRepository;
import org.smartregister.reveal.application.RevealApplication;
import org.smartregister.reveal.multiselect.HdssCompoundMultiSelectItem;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import timber.log.Timber;

public class HdssCompoundHouseholdRepository implements MultiSelectListRepository {
    @Override
    public List<MultiSelectItem> fetchData() {

        HdssRepository hdssRepository =
                RevealApplication.getInstance().getContext().getHdssRepository();

        Set<HdssCompoundHousehold> individualHouseholdCompounds = hdssRepository.getHouseholdCompound();

        List<MultiSelectItem> multiSelectItems = new ArrayList<>();

        JSONObject property = new JSONObject();
        try {
            property.put("presumed-id","err");
            property.put("confirmed-id","err");

        } catch (JSONException e) {
            Timber.tag("Reveal Exception").w(e);
        }

        if (individualHouseholdCompounds != null){
            multiSelectItems = individualHouseholdCompounds.stream().map(individualHouseholdCompound->{
                HdssCompoundMultiSelectItem item = new HdssCompoundMultiSelectItem();
                item.setKey(individualHouseholdCompound.getHouseholdId());
                item.setText(individualHouseholdCompound.getHouseholdId());
                item.setText2(individualHouseholdCompound.getCompoundId());
                item.setLabel1("Household Id");
                item.setLabel2("Compound Id");
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

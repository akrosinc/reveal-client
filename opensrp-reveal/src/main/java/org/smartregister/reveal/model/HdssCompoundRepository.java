package org.smartregister.reveal.model;

import com.vijay.jsonwizard.domain.MultiSelectItem;
import com.vijay.jsonwizard.interfaces.MultiSelectListRepository;

import org.json.JSONException;
import org.json.JSONObject;
import org.smartregister.repository.HdssRepository;
import org.smartregister.reveal.application.RevealApplication;
import org.smartregister.reveal.multiselect.HdssCompoundMultiSelectItem;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import timber.log.Timber;

public class HdssCompoundRepository implements MultiSelectListRepository {
    @Override
    public List<MultiSelectItem> fetchData() {

        HdssRepository hdssRepository =
                RevealApplication.getInstance().getContext().getHdssRepository();

        List<String> compounds = hdssRepository.getCompounds();

        List<MultiSelectItem> multiSelectItems = new ArrayList<>();

        JSONObject property = new JSONObject();
        try {
            property.put("presumed-id","err");
            property.put("confirmed-id","err");

        } catch (JSONException e) {
            Timber.tag("Reveal Exception").w(e);
        }

        if (compounds != null){
            multiSelectItems = compounds.stream().map(compound->{
                HdssCompoundMultiSelectItem item = new HdssCompoundMultiSelectItem();
                item.setKey(compound);
                item.setText(compound);
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

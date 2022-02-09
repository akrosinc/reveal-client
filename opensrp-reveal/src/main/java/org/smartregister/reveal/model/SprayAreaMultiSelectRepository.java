package org.smartregister.reveal.model;

import com.vijay.jsonwizard.domain.MultiSelectItem;
import com.vijay.jsonwizard.interfaces.MultiSelectListRepository;

import org.json.JSONException;
import org.json.JSONObject;
import org.smartregister.AllConstants;
import org.smartregister.location.helper.LocationHelper;
import org.smartregister.repository.LocationRepository;
import org.smartregister.repository.StructureRepository;
import org.smartregister.reveal.application.RevealApplication;
import org.smartregister.reveal.util.PreferencesUtil;
import org.smartregister.reveal.util.Utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import timber.log.Timber;

public class SprayAreaMultiSelectRepository implements MultiSelectListRepository {
    @Override
    public List<MultiSelectItem> fetchData() {
        List<MultiSelectItem> multiSelectItems = new ArrayList<>();
        List<String> locationNames;
        LocationRepository locationRepository = RevealApplication.getInstance().getLocationRepository();
        StructureRepository structureRepository = RevealApplication.getInstance().getStructureRepository();
        if(Utils.isZambiaIRSFull()){
            //TODO: might just use this for both ZAMBIA and SENeGAL, check preferences first.
            locationNames = Arrays.asList(PreferencesUtil.getInstance().getPreferenceValue(AllConstants.OPERATIONAL_AREAS).split(","));
        } else if(Utils.isZambiaIRSLite()){
            List<String> operationalAreaNames = Arrays.asList(PreferencesUtil.getInstance().getPreferenceValue(AllConstants.OPERATIONAL_AREAS).split(","));
            locationNames = operationalAreaNames.stream()
                                                    .map(name -> locationRepository.getLocationByName(name))
                                                    .map(parentLocation -> structureRepository.getLocationsByParentId(parentLocation.getIdentifier())).flatMap(Collection::stream)
                                                    .map(childLocation -> childLocation.getProperties().getName()).filter(name -> !name.isEmpty()).collect(Collectors.toList());
        } else {
            final String currentFacility = PreferencesUtil.getInstance().getCurrentFacility();
            locationNames = LocationHelper.getInstance().locationNamesFromHierarchy(currentFacility).stream().filter(name -> !name.equals(currentFacility)).collect(Collectors.toList());
        }
        JSONObject property = new JSONObject();
        try {
            property.put("presumed-id","err");
            property.put("confirmed-id","err");

        } catch (JSONException e) {
            Timber.e(e);
        }
        locationNames.stream().forEach(name -> {
            MultiSelectItem item = new MultiSelectItem();
            item.setKey(name);
            item.setText(name);
            item.setOpenmrsEntityId("");
            item.setOpenmrsEntity("");
            item.setOpenmrsEntityParent("");
            item.setValue(property.toString());
            multiSelectItems.add(item);
        });
        return multiSelectItems;
    }
}

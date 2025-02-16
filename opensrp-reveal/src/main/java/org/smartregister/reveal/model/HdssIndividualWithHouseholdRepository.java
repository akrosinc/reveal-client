//package org.smartregister.reveal.model;
//
//import com.vijay.jsonwizard.domain.MultiSelectItem;
//import com.vijay.jsonwizard.interfaces.MultiSelectListRepository;
//
//import org.json.JSONException;
//import org.json.JSONObject;
//import org.smartregister.domain.HdssIndividual;
//import org.smartregister.repository.HdssRepository;
//import org.smartregister.reveal.application.RevealApplication;
//import org.smartregister.reveal.multiselect.HdssIndividualMultiSelectItem;
//import org.smartregister.reveal.util.PreferencesUtil;
//
//import java.util.ArrayList;
//import java.util.List;
//import java.util.stream.Collectors;
//
//import timber.log.Timber;
//
//public class HdssIndividualWithHouseholdRepository implements MultiSelectListRepository {
//    @Override
//    public List<MultiSelectItem> fetchData() {
//
//        HdssRepository hdssRepository =
//                RevealApplication.getInstance().getContext().getHdssRepository();
//
//        String selectedHouseholdID = PreferencesUtil.getInstance().getSelectedHouseholdID();
//
//        List<HdssIndividual> householdsByCompoundId;
//        if (selectedHouseholdID != null){
//            householdsByCompoundId = hdssRepository.getIndividualsByHouseholdId(selectedHouseholdID);
//        } else {
//            householdsByCompoundId = hdssRepository.getIndividuals();
//        }
//
//        List<MultiSelectItem> multiSelectItems = new ArrayList<>();
//        if (selectedHouseholdID != null){
//            if (householdsByCompoundId != null){
//                multiSelectItems = householdsByCompoundId.stream().map(individual->{
//                    HdssIndividualMultiSelectItem item = new HdssIndividualMultiSelectItem();
//                    item.setKey(individual.getIndividualId());
//                    item.setText(individual.getIndividualId());
//                    item.setOpenmrsEntityId("");
//                    item.setOpenmrsEntity("");
//                    item.setOpenmrsEntityParent("");
//
//                    JSONObject property = new JSONObject();
//                    try {
//                        property.put("dob",individual.getDob());
//                        property.put("gender",individual.getGender());
//                        item.setValue(property.toString());
//                    } catch (JSONException e) {
//                        Timber.tag("Reveal Exception").w(e);
//                        item.setValue(null);
//                    }
//
//                    return item;
//                }).collect(Collectors.toList());
//            }
//
//        }
//        return multiSelectItems;
//    }
//}

package org.smartregister.reveal.util;

import static org.smartregister.reveal.util.Constants.BusinessStatus.COMPLETE;
import static org.smartregister.reveal.util.Constants.BusinessStatus.INCOMPLETE;
import static org.smartregister.reveal.util.Constants.BusinessStatus.IN_PROGRESS;
import static org.smartregister.reveal.util.Constants.BusinessStatus.NOT_DISPENSED;
import static org.smartregister.reveal.util.Constants.BusinessStatus.NOT_ELIGIBLE;
import static org.smartregister.reveal.util.Constants.BusinessStatus.NOT_SPRAYABLE;
import static org.smartregister.reveal.util.Constants.BusinessStatus.NOT_SPRAYED;
import static org.smartregister.reveal.util.Constants.BusinessStatus.NOT_VISITED;
import static org.smartregister.reveal.util.Constants.BusinessStatus.PARTIALLY_SPRAYED;
import static org.smartregister.reveal.util.Constants.BusinessStatus.SPRAYED;
import static org.smartregister.reveal.util.Constants.BusinessStatus.TASKS_INCOMPLETE;
import static org.smartregister.reveal.util.Constants.Intervention.LARVAL_DIPPING;
import static org.smartregister.reveal.util.Constants.Intervention.MOSQUITO_COLLECTION;
import static org.smartregister.reveal.util.Constants.Intervention.PAOT;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import org.smartregister.AllConstants;
import org.smartregister.reveal.R;
import org.smartregister.reveal.application.RevealApplication;
import org.smartregister.reveal.model.CardDetails;
import org.smartregister.reveal.model.FamilyCardDetails;
import org.smartregister.reveal.model.IRSVerificationCardDetails;
import org.smartregister.reveal.model.MosquitoHarvestCardDetails;
import org.smartregister.reveal.model.SprayCardDetails;
import org.smartregister.reveal.model.SurveyCardDetails;
import org.smartregister.reveal.util.Constants.Action;
import org.smartregister.reveal.util.Constants.BusinessStatus;
import timber.log.Timber;

/**
 * Created by samuelgithengi on 3/22/19.
 */
public class CardDetailsUtil {

    public static void formatCardDetails(CardDetails cardDetails) {
        if (cardDetails == null || cardDetails.getStatus() == null)
            return;
        // extract status color
        String status = getBuildCountry() == Country.NIGERIA ? cardDetails.getStatus() : getBaseBusinessStatus(cardDetails.getStatus());
        switch (status) {
            case BusinessStatus.INELIGIBLE:
            case BusinessStatus.FAMILY_NO_TASK_REGISTERED:
                cardDetails.setStatusColor(R.color.family_no_task_registered);
                cardDetails.setStatusMessage(R.string.family_member_registered);
                break;
            case BusinessStatus.NOT_SPRAYED:
            case NOT_DISPENSED:
            case BusinessStatus.IN_PROGRESS:
            case BusinessStatus.NONE_RECEIVED:
                cardDetails.setStatusColor(R.color.unsprayed);
                cardDetails.setStatusMessage(R.string.details_not_sprayed);
                break;
            case BusinessStatus.SPRAYED:
            case BusinessStatus.SMC_COMPLETE:
            case BusinessStatus.SPAQ_COMPLETE:
            case BusinessStatus.ALL_TASKS_COMPLETE:
            case BusinessStatus.COMPLETE:
            case BusinessStatus.FULLY_RECEIVED:
                formatCardDetailsForCompletedTasks(cardDetails);
                break;
            case BusinessStatus.NOT_SPRAYABLE:
            case BusinessStatus.NOT_ELIGIBLE:
                cardDetails.setStatusColor(R.color.unsprayable);
                cardDetails.setStatusMessage(R.string.details_not_sprayable);
                cardDetails.setReason(null);
                break;
            case BusinessStatus.INCOMPLETE:
            case TASKS_INCOMPLETE:
            case BusinessStatus.PARTIALLY_RECEIVED:
                cardDetails.setStatusColor(R.color.partially_sprayed);
                break;
            case PARTIALLY_SPRAYED:
                if (getBuildCountry() == Country.ZAMBIA || getBuildCountry() == Country.SENEGAL || getBuildCountry()
                        == Country.SENEGAL_EN) {
                    formatCardDetailsForCompletedTasks(cardDetails);
                } else {
                    cardDetails.setStatusColor(R.color.partially_sprayed);
                    cardDetails.setStatusMessage(R.string.partially_sprayed);
                }
                break;
            default:
                Timber.w("business status not defined :" + cardDetails.getStatus());
                break;
        }
    }

    @NonNull
    private static Country getBuildCountry() {
        return PreferencesUtil.getInstance().getBuildCountry();
    }

    private static void formatCardDetailsForCompletedTasks(CardDetails cardDetails) {
        cardDetails.setStatusColor(R.color.sprayed);
        cardDetails.setStatusMessage(R.string.details_sprayed);
        cardDetails.setReason(null);
    }

    public void populateSprayCardTextViews(SprayCardDetails sprayCardDetails, Activity activity) {
        try {
            TextView tvSprayStatus = activity.findViewById(R.id.spray_status);
            TextView tvPropertyType = activity.findViewById(R.id.property_type);
            TextView tvSprayDate = activity.findViewById(R.id.spray_date);
            TextView tvSprayOperator = activity.findViewById(R.id.user_id);
            TextView tvFamilyHead = activity.findViewById(R.id.family_head);
            TextView tvReason = activity.findViewById(R.id.reason);

            Integer color = sprayCardDetails.getStatusColor();
            tvSprayStatus.setTextColor(color == null ? activity.getResources().getColor(R.color.black) : activity.getResources().getColor(color));

            Integer status = sprayCardDetails.getStatusMessage();
            tvSprayStatus.setText(status == null ? "" : activity.getString(status));

            tvPropertyType.setText(sprayCardDetails.getPropertyType());
            tvSprayDate.setText(sprayCardDetails.getSprayDate());
            tvSprayOperator.setText(sprayCardDetails.getSprayOperator());
            tvFamilyHead.setText(sprayCardDetails.getFamilyHead());
            if (!TextUtils.isEmpty(sprayCardDetails.getReason())) {
                tvReason.setVisibility(View.VISIBLE);
                tvReason.setText(sprayCardDetails.getReason());
            } else {
                tvReason.setVisibility(View.GONE);
            }
        } catch (Resources.NotFoundException e) {
            Timber.e(e);
        }
    }

    public void populateAndOpenMosquitoHarvestCard(MosquitoHarvestCardDetails mosquitoHarvestCardDetails, Activity activity) {
        String interventionType = mosquitoHarvestCardDetails.getInterventionType();
        String startDate = mosquitoHarvestCardDetails.getStartDate() != null ? mosquitoHarvestCardDetails.getStartDate() : "";
        String endDate = mosquitoHarvestCardDetails.getEndDate() != null ? mosquitoHarvestCardDetails.getEndDate() : "";
        if (MOSQUITO_COLLECTION.equals(interventionType)) {
            TextView tvMosquitoCollectionStatus = activity.findViewById(R.id.trap_collection_status);
            TextView tvMosquitoTrapSetDate = activity.findViewById(R.id.trap_set_date);
            TextView tvMosquitoTrapFollowUpDate = activity.findViewById(R.id.trap_follow_up_date);

            tvMosquitoCollectionStatus.setText(mosquitoHarvestCardDetails.getStatus());
            tvMosquitoTrapSetDate.setText(activity.getResources().getString(R.string.mosquito_collection_trap_set_date) + startDate);
            tvMosquitoTrapFollowUpDate.setText(activity.getResources().getString(R.string.mosquito_collection_trap_follow_up_date) + endDate);
            activity.findViewById(R.id.mosquito_collection_card_view).setVisibility(View.VISIBLE);
        } else if (LARVAL_DIPPING.equals(interventionType)) {
            TextView tvIdentifiedDate = activity.findViewById(R.id.larval_identified_date);
            TextView tvLarvicideDate = activity.findViewById(R.id.larvacide_date);

            tvIdentifiedDate.setText(activity.getResources().getString(R.string.larval_breeding_identified_date_test_text) + startDate);
            tvLarvicideDate.setText(activity.getResources().getString(R.string.larval_breeding_larvacide_date_test_text) + endDate);
            activity.findViewById(R.id.larval_breeding_card_view).setVisibility(View.VISIBLE);
        } else if (PAOT.equals(interventionType)) {
            String lastUpdatedDate =  mosquitoHarvestCardDetails.getStartDate();

            TextView lastUpdatedDateView = activity.findViewById(R.id.paot_last_updated_date);
            lastUpdatedDateView.setText(activity.getResources().getString(R.string.paot_last_updated_date_test_text) + lastUpdatedDate);
            activity.findViewById(R.id.potential_area_of_transmission_card_view).setVisibility(View.VISIBLE);
        }
    }

    public void populateAndOpenIRSVerificationCard(IRSVerificationCardDetails cardDetails, Activity activity) {
        try {

            TextView tvIneligibleStructuresLabel = activity.findViewById(R.id.ineligible_structures_label);
            ScrollView svEligibleStructuresScrollView = activity.findViewById(R.id.eligible_structures_scrollview);

            if(cardDetails.getTrueStructure().equalsIgnoreCase(AllConstants.BOOLEAN_FALSE)) {

                tvIneligibleStructuresLabel.setText(activity.getResources().getString(R.string.not_true_structure));
                tvIneligibleStructuresLabel.setVisibility(View.VISIBLE);
                svEligibleStructuresScrollView.setVisibility(View.GONE);
            } else if(cardDetails.getEligStruc().equalsIgnoreCase(AllConstants.BOOLEAN_FALSE)) {

                tvIneligibleStructuresLabel.setText(activity.getResources().getString(R.string.structure_ineligible));
                tvIneligibleStructuresLabel.setVisibility(View.VISIBLE);
                svEligibleStructuresScrollView.setVisibility(View.GONE);
            } else {

                tvIneligibleStructuresLabel.setVisibility(View.GONE);
                svEligibleStructuresScrollView.setVisibility(View.VISIBLE);
                TextView tvReportedSprayLabel = activity.findViewById(R.id.reported_spray_label);
                TextView tvReportedSprayStatus = activity.findViewById(R.id.reported_spray_status);
                TextView tvChalkSprayLabel = activity.findViewById(R.id.chalk_spray_label);
                TextView tvChalkSprayStatus = activity.findViewById(R.id.chalk_spray_status);
                TextView tvStickerSprayLabel = activity.findViewById(R.id.sticker_spray_label);
                TextView tvStickerSprayStatus = activity.findViewById(R.id.sticker_spray_status);
                TextView tvCardSprayLabel = activity.findViewById(R.id.card_spray_label);
                TextView tvCardSprayStatus = activity.findViewById(R.id.card_spray_status);

                tvReportedSprayLabel.setText(activity.getResources().getString(R.string.reported_spray_status) + ":");
                tvReportedSprayStatus.setText(getTranslatedIRSVerificationStatus(cardDetails.getReportedSprayStatus()));

                tvChalkSprayLabel.setText(activity.getResources().getString(R.string.chalk_spray_status) + ":");
                tvChalkSprayStatus.setText(getTranslatedIRSVerificationStatus(cardDetails.getChalkSprayStatus()));

                tvStickerSprayLabel.setText(activity.getResources().getString(R.string.sticker_spray_status) + ":");
                tvStickerSprayStatus.setText(getTranslatedIRSVerificationStatus(cardDetails.getStickerSprayStatus()));

                tvCardSprayLabel.setText(activity.getResources().getString(R.string.card_spray_status) + ":");
                tvCardSprayStatus.setText(getTranslatedIRSVerificationStatus(cardDetails.getCardSprayStatus()));
            }

            activity.findViewById(R.id.irs_verification_card_view).setVisibility(View.VISIBLE);
        } catch (Resources.NotFoundException e) {
            Timber.e(e);
        }
    }

    public void populateFamilyCard(FamilyCardDetails familyCardDetails, Activity activity) {
        try {
            TextView tvSprayStatus = activity.findViewById(R.id.spray_status);
            TextView tvPropertyType = activity.findViewById(R.id.property_type);
            TextView tvSprayDate = activity.findViewById(R.id.spray_date);
            TextView tvSprayOperator = activity.findViewById(R.id.user_id);
            TextView tvFamilyHead = activity.findViewById(R.id.family_head);
            TextView tvReason = activity.findViewById(R.id.reason);
            Button changeSprayStatus = activity.findViewById(R.id.change_spray_status);
            Button registerFamily  =  activity.findViewById(R.id.register_family);

            Integer color = familyCardDetails.getStatusColor();
            tvSprayStatus.setTextColor(color == null ? activity.getResources().getColor(R.color.black) : activity.getResources().getColor(color));

            tvSprayStatus.setText(familyCardDetails.getStatus());

            tvSprayDate.setText(familyCardDetails.getDateCreated());
            tvSprayOperator.setText(familyCardDetails.getOwner());

            registerFamily.setVisibility(View.VISIBLE);
            changeSprayStatus.setVisibility(View.GONE);
            tvPropertyType.setVisibility(View.GONE);
            tvFamilyHead.setVisibility(View.GONE);
            tvReason.setVisibility(View.GONE);

        } catch (Resources.NotFoundException e) {
            Timber.e(e);
        }
    }

    public void populateSurveyCard(SurveyCardDetails surveyCardDetails, Activity activity) {
        try {
            TextView tvSprayStatus = activity.findViewById(R.id.spray_status);
            TextView tvPropertyType = activity.findViewById(R.id.property_type);
            TextView tvSprayDate = activity.findViewById(R.id.spray_date);
            TextView tvSprayOperator = activity.findViewById(R.id.user_id);
            TextView tvFamilyHead = activity.findViewById(R.id.family_head);
            TextView tvReason = activity.findViewById(R.id.reason);
            TextView tvStructureNum = activity.findViewById(R.id.structure_number);
            Button changeSprayStatus = activity.findViewById(R.id.change_spray_status);
            Button registerFamily = activity.findViewById(R.id.register_family);

            Button changeHouseholdStatus  =  activity.findViewById(R.id.change_household_status);
            Button changeHabitatStatus  =  activity.findViewById(R.id.change_habitat_status);
            Button changeLsmHouseholdStatus  =  activity.findViewById(R.id.change_lsm_household_status);
            Button changeOnchoStatus = activity.findViewById(R.id.change_oncho_status);


            Integer color = surveyCardDetails.getStatusColor();
            tvSprayStatus.setTextColor(color == null ? activity.getResources().getColor(R.color.black) : activity.getResources().getColor(color));

            tvSprayStatus.setText(getTranslatedBusinessStatus(surveyCardDetails.getStatus()));

            tvSprayDate.setText(surveyCardDetails.getDateCreated());
            tvSprayOperator.setText(surveyCardDetails.getOwner());

            if (Action.MDA_SURVEY.equals(surveyCardDetails.getInterventionType())){
                tvStructureNum.setVisibility(View.VISIBLE);
                tvStructureNum.setText(String.format(activity.getResources().getString(R.string.structure_number), surveyCardDetails.getStructureNumber()));
            }
            changeHouseholdStatus.setVisibility(Action.MDA_SURVEY.equals(surveyCardDetails.getInterventionType()) ? View.VISIBLE : View.GONE);
            changeHabitatStatus.setVisibility(Action.HABITAT_SURVEY.equals(surveyCardDetails.getInterventionType()) ? View.VISIBLE : View.GONE);
            changeLsmHouseholdStatus.setVisibility(Action.LSM_HOUSEHOLD_SURVEY.equals(surveyCardDetails.getInterventionType()) ? View.VISIBLE : View.GONE);
            changeOnchoStatus.setVisibility(Action.MDA_ONCHOCERCIASIS_SURVEY.equals(surveyCardDetails.getInterventionType()) ? View.VISIBLE : View.GONE);
            changeSprayStatus.setVisibility(View.GONE);
            registerFamily.setVisibility(View.GONE);
            tvPropertyType.setVisibility(View.GONE);
            tvFamilyHead.setVisibility(View.GONE);
            tvReason.setVisibility(View.GONE);

        } catch (Resources.NotFoundException e) {
            Timber.e(e);
        }
    }

    /**
     * Takes in a business status and returns the translated value according to locale set.
     *
     * @param businessStatus Business status of the task attached to a structure
     * @return status Translated status according to locale set
     */
    public static String getTranslatedBusinessStatus(String businessStatus) {
        Context context =  RevealApplication.getInstance().getContext().applicationContext();

        if (businessStatus == null)
            return context.getString(R.string.not_eligible);
        switch (businessStatus) {
            case NOT_VISITED:
                return context.getString(R.string.not_visited);
            case NOT_SPRAYED:
                return context.getString(R.string.not_sprayed);
            case SPRAYED:
                return context.getString(R.string.sprayed);
            case NOT_SPRAYABLE:
                return context.getString(R.string.not_sprayable);
            case COMPLETE:
                return context.getString(R.string.complete);
            case INCOMPLETE:
                return context.getString(R.string.incomplete);
            case TASKS_INCOMPLETE:
                return context.getString(R.string.tasks_incomplete);
            case NOT_ELIGIBLE:
                return context.getString(R.string.not_eligible);
            case IN_PROGRESS:
                return context.getString(R.string.in_progress);
            case NOT_DISPENSED:
                return context.getString(R.string.not_dispensed);
            case PARTIALLY_SPRAYED:
                if (getBuildCountry() == Country.ZAMBIA || getBuildCountry() == Country.SENEGAL || getBuildCountry()
                        == Country.SENEGAL_EN) {
                    return context.getString(R.string.sprayed);
                } else {
                    return context.getString(R.string.partially_sprayed);
                }
            case "Refused or Permanently Absent":
                return context.getString(R.string.refused_or_permanently_absent);
            case "Partially complete or Temporarily Absent":
                return context.getString(R.string.partially_complete_or_temp_absent);
            case "MDA complete":
                return context.getString(R.string.mda_complete);
            default:
                return businessStatus;
        }

    }

    /**
     * Takes in a IRS intervention status and returns the translated value .
     *
     * @param status Status of the IRS Verification type
     * @return status Translated status
     */
    public static String getTranslatedIRSVerificationStatus(String status) {
        Context context = RevealApplication.getInstance().getApplicationContext();

        if (status == null)
            return context.getString(R.string.not_sprayed);
        switch (status) {
            case Constants.IRSVerificationStatus.SPRAYED:
                return context.getString(R.string.sprayed);
            case Constants.IRSVerificationStatus.NOT_SPRAYED:
                return context.getString(R.string.not_sprayed);
            case Constants.IRSVerificationStatus.NOT_FOUND_OR_VISITED:
                return context.getString(R.string.structure_not_found_or_visited_during_campaign);
            case Constants.IRSVerificationStatus.OTHER:
                return context.getString(R.string.other);
            default:
                return status;
        }


    }

    public static String getBaseBusinessStatus(String businessStatus){
        Context context =  RevealApplication.getInstance().getContext().applicationContext();
        if(context.getString(R.string.not_visited).equals(businessStatus)){
            return NOT_VISITED;
        } else if (context.getString(R.string.not_sprayed).equals(businessStatus)){
            return NOT_SPRAYED;
        } else if (context.getString(R.string.sprayed).equals(businessStatus)){
            return SPRAYED;
        } else if (context.getString(R.string.not_sprayable).equals(businessStatus)){
            return NOT_SPRAYABLE;
        } else if (context.getString(R.string.complete).equals(businessStatus)){
            return COMPLETE;
        } else if (context.getString(R.string.incomplete).equals(businessStatus)){
            return INCOMPLETE;
        } else if (context.getString(R.string.not_eligible).equals(businessStatus)){
            return NOT_ELIGIBLE;
        } else if (context.getString(R.string.in_progress).equals(businessStatus)){
            return IN_PROGRESS;
        } else if (context.getString(R.string.sprayed).equals(businessStatus) && (getBuildCountry() == Country.ZAMBIA || getBuildCountry()
                == Country.SENEGAL || getBuildCountry() == Country.SENEGAL_EN)){
            return PARTIALLY_SPRAYED;
        } else if( context.getString(R.string.partially_sprayed).equals(businessStatus)){
            return PARTIALLY_SPRAYED;
        }
        return businessStatus;
    }


}

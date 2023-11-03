package org.smartregister.reveal.util;

import static org.smartregister.reveal.util.Constants.Action.HABITAT_SURVEY;
import static org.smartregister.reveal.util.Constants.Action.LSM_HOUSEHOLD_SURVEY;
import static org.smartregister.reveal.util.Constants.Action.MDA_ONCHOCERCIASIS_SURVEY;
import static org.smartregister.reveal.util.Constants.Action.MDA_SURVEY;

import java.util.Arrays;
import java.util.List;

public interface Constants {

    public static String VIEW_CONFIGURATION_PREFIX = "ViewConfiguration_";

    public static String FILTER_TEAM_ID = "teamId";

    public static String JSON_FORM_PARAM_JSON = "json";

    public static String METADATA = "metadata";

    public static String DETAILS = "details";

    public static String ENTITY_ID = "entity_id";

    public static String SPRAY_EVENT = "Spray";

    public static String REGISTER_STRUCTURE_EVENT = "Register_Structure";

    public static String MOSQUITO_COLLECTION_EVENT = "mosquito_collection";

    public static String LARVAL_DIPPING_EVENT = "larval_dipping";

    public static String BEDNET_DISTRIBUTION_EVENT = "bednet_distribution";

    public static String BLOOD_SCREENING_EVENT = "blood_screening";

    public static String BEHAVIOUR_CHANGE_COMMUNICATION = "behaviour_change_communication";

    public static String TASK_RESET_EVENT = "reset_task";

    public static String STRUCTURE = "Structure";

    public static String FOCUS = "FOCUS";

     public static double MY_LOCATION_ZOOM_LEVEL = 17.5; // modifying this will also necessitate modifying the VERTICAL_OFFSET

     public static double VERTICAL_OFFSET = 0.0;

     public static double REFRESH_MAP_MINIMUM_DISTANCE = 5;

     public static int ANIMATE_TO_LOCATION_DURATION = 1000;

    public static String DIGITAL_GLOBE_CONNECT_ID = "DG_CONNECT_ID";

    public static String HYPHEN = "-";

    public static String COMMA = ",";

    public static String TILDE = "~";

    public static String UNDERSCRORE = "_";

     public static int STORAGE_PERMISSIONS = 1;

    public static String NULL_KEY = "NULL";

     public static int IRS_VERIFICATION_PERIOD = 30;

    public static String DBNAME = "drishti.db";
    public static String COPYDBNAME = "reveal";

    public static String DG_ID_PLACEHOLDER = "DIGITAL_GLOBE_ID";

    public static List<String> MACEPA_PROVINCES = Arrays.asList("Western", "Southern", "Lusaka");

    public static String ACTIONS = "actions";
    public static String THAILAND_SYNC_INTERVAL = "720";

     public static int SYNC_BACK_OFF_DELAY = 8000;

    public static String USER_NAME = "user_name";

    public static String BUILD_COUNTRY = "build_country";

    public static String ADMIN_PASSWORD_REQUIRED = "admin_password_required";

    public static String ADMIN_PASSWORD_ENTERED = "admin_password_entered";

    public static String GPS_ACCURACY = "gps_accuracy";
     public static int MDA_MIN_AGE = 5;

     public static int ADMINISTERED_SPAQ = 1;
     public static int NOT_ADMINISTERED_SPAQ = 0;

    public static String READ_ONLY = "READ_ONLY";

    public static String MDA_DISPENSE_TASK_COUNT = "mda_dispense_task_count";
    public static String MDA_TASK_COUNT = "mda_task_count";
    public static String MDA_ADHERENCE_COMPLETE_COUNT = "mda_adherence_complete_count";
    public static String MDA_DRUG_RECON_COMPLETE_COUNT = "mda_drug_recon_complete_count";

     public static int SMC_DISPENSE_MIN_MONTHS = 3;

     public static int SYNC_ENTITY_COUNT = 4;


    public static class CONFIGURATION {
        public static String LOGIN = "login";
        public static String GLOBAL_CONFIGS = "global_configs";
        public static String TEAM_CONFIGS = "team_configs";
        public static String JURISDICTION_METADATA = "jurisdiction_metadata-target";
        public static String KEY = "key";
        public static String VALUE = "value";
        public static String VALUES = "values";
        public static String SETTINGS = "settings";
        public static String LOCATION_BUFFER_RADIUS_IN_METRES = "location_buffer_radius_in_metres";
        public static String LOCATION_BUFFER_RADIUS_IN_METRES_FOR_LITE = "location_buffer_radius_in_metres_for_lite";
        public static String UPDATE_LOCATION_BUFFER_RADIUS = "update_location_buffer_radius";
        public static String INDEX_CASE_CIRCLE_RADIUS_IN_METRES = "index_case_circle_radius_in_metres";
        public static Float DEFAULT_INDEX_CASE_CIRCLE_RADIUS_IN_METRES = 1000f;
        public static String DRAW_OPERATIONAL_AREA_BOUNDARY_AND_LABEL = "draw_operational_area_boundary_and_label";
        public static Boolean DEFAULT_DRAW_OPERATIONAL_AREA_BOUNDARY_AND_LABEL = true;
        public static String LOCAL_SYNC_DONE = "local_sync_done";
        public static Float DEFAULT_GEO_JSON_CIRCLE_SIDES = 120F;
        public static Float METERS_PER_KILOMETER = 1000f;
        public static Float KILOMETERS_PER_DEGREE_OF_LONGITUDE_AT_EQUITOR = 111.320f;
        public static Float KILOMETERS_PER_DEGREE_OF_LATITUDE_AT_EQUITOR = 110.574f;
        public static String VALIDATE_FAR_STRUCTURES = "validate_far_structures";
        public static String RESOLVE_LOCATION_TIMEOUT_IN_SECONDS = "resolve_location_timeout_in_seconds";
        public static String ADMIN_PASSWORD_NOT_NEAR_STRUCTURES = "admin_password_not_near_structures";
        public static String DISPLAY_ADD_STRUCTURE_OUT_OF_BOUNDARY_WARNING_DIALOG = "display_add_structure_out_of_boundary_warning_dialog";
        public static Boolean DEFAULT_DISPLAY_ADD_STRUCTURE_OUT_OF_BOUNDARY_WARNING_DIALOG = false;
        public static Float OUTSIDE_OPERATIONAL_AREA_MASK_OPACITY = 0.65f;
        public static String SPRAY_OPERATORS = "spray_operators";
        public static String DATA_COLLECTORS = "data_collectors";
        public static String SUPERVISORS = "supervisors";
        public static String TEAM_LEADERS = "team_leaders";
        public static String FIELD_OFFICERS = "field_officers";
        public static String DISTRICT_MANAGERS = "district_managers";
        public static String HEALTH_FACILITIES = "health_facilities";
        public static String COMMUNITY_HEALTH_WORKERS = "community_health_workers";
        public static String CODE = "code";
        public static String NAME = "name";
        public static String SYNC_INTERVAL_IN_MINUTES = "sync_ public static interval_in_minutes";


        public static String MDA_CORDINATORS = "mda_coordinators";
        public static String MDA_ENUMERATORS = "mda_enumerators";
        public static String MDA_COMMUNITY_HEALTH_WORKERS = "mda_community_health_workers";
        public static String MDA_ADHERENCE_OFFICERS = "mda_adherence_officers";
        public static String MDA_CATCHMENT_AREAS = "mda_catchment_areas";
        public static String DISPLAY_DISTANCE_SCALE = "display_distance_scale";
        public static String DISTRICTS = "districts";
        public static String WARDS = "wards";
        public static String COMMUNITY_DRUG_DISTRIBUTORS = "community_drug_distributors";
        public static String HEALTH_WORKER_SUPERVISORS = "health_worker_supervisors";
        public static String VILLAGES = "villages";
        public static String SELECT_JURISDICTION_MAX_SELECT_ZOOM_LEVEL = "select_jurisdiction_max_select_zoom_level";
        public static String MAX_SELECT_ZOOM_LEVEL = "max_select_zoom_level";
        public static String SPRAY_AREAS = "spray_areas";
        public static String ZONES = "zones";
        public static String COUNTY_LIST = "county_list";
        public static String SUB_COUNTY_LIST = "sub_county_list";
    }

    public static class Preferences {
        public static String CURRENT_FACILITY = "CURRENT_FACILITY";
        public static String CURRENT_DISTRICT = "CURRENT_DISTRICT";
        public static String CURRENT_PROVINCE = "CURRENT_PROVINCE";
        public static String CURRENT_PLAN = "CURRENT_PLAN";
        public static String CURRENT_PLAN_ID = "CURRENT_PLAN_ID";
        public static String FACILITY_LEVEL = "FACILITY_LEVEL";
        public static String CURRENT_OPERATIONAL_AREA = "CURRENT_OPERATIONAL_AREA";
        public static String CURRENT_OPERATIONAL_AREA_ID = "CURRENT_OPERATIONAL_AREA_ID";
        public static String EVENT_LATITUDE = "EVENT_LATITUDE";
        public static String EVENT_LONGITUDE = "EVENT_LONGITUDE";
        public static String GPS_ACCURACY = "GPS_ACCURACY";
        public static String ADMIN_PASSWORD_ENTERED = "ADMIN_PASSWORD_ENTERED";
        public static String TOTAL_SYNC_PROGRESS = "TOTAL_SYNC_PROGRESS";
        public static String CURRENT_PLAN_TARGET_LEVEL = "CURRENT_PLAN_TARGET_LEVEL";
    }

    public static class Tags {
        public static String COUNTRY = "Country";
        public static String PROVINCE = "Province";
        public static String STATE = "State";
        public static String LGA = "Lga";
        public static String REGION = "Region";
        public static String DISTRICT = "District";
        public static String SUB_DISTRICT = "Sub-district";
        public static String HEALTH_CENTER = "Rural Health Centre";
        public static String CANTON = "Canton";
        public static String VILLAGE = "Village";
        public static String OPERATIONAL_AREA = "Operational Area";
        public static String ZONE = "Zones";
        public static String SECTOR = "Sector";
        public static String CELL = "Cell";
        public static String OPERATIONAL = "Operational";
        public static String CATCHMENT = "Catchment";
    }

    public static class Properties {
        public static String TASK_IDENTIFIER = "taskIdentifier";
        public static String TASK_BUSINESS_STATUS = "taskBusinessStatus";
        public static String TASK_STATUS = "taskStatus";
        public static String TASK_CODE = "taskCode";
        public static String LOCATION_UUID = "locationUUID";
        public static String LOCATION_VERSION = "locationVersion";
        public static String LOCATION_TYPE = "locationType";
        public static String LOCATION_PARENT = "locationParent";
        public static String LOCATION_ID = "location_id";
        public static String FEATURE_SELECT_TASK_BUSINESS_STATUS = "featureSelectTaskBusinessStatus";
        public static String BASE_ENTITY_ID = "baseEntityId";
        public static String STRUCTURE_NAME = "structure_name";
        public static String APP_VERSION_NAME = "appVersionName";
        public static String FORM_VERSION = "form_version";
        public static String TASK_CODE_LIST = "task_code_list";
        public static String FAMILY_MEMBER_NAMES = "family_member_names";
        public static String PLAN_IDENTIFIER = "planIdentifier";
        public static String LOCATION_STATUS = "status";
        public static String LOCATION_NAME = "name";
    }


    public static class GeoJSON {
        public static String TYPE = "type";
        public static String FEATURE_COLLECTION = "FeatureCollection";
        public static String FEATURES = "features";
        public static String IS_INDEX_CASE = "is_index_case";
    }

    public interface Intervention {
        public static String IRS = "IRS";

        public static String DYNAMIC_IRS = "Dynamic-IRS";

        public static String MOSQUITO_COLLECTION = "Mosquito Collection";

        public static String LARVAL_DIPPING = "Larval Dipping";

        public static String BCC = "BCC";

        public static String BEDNET_DISTRIBUTION = "Bednet Distribution";

        public static String BLOOD_SCREENING = "Blood Screening";

        public static String CASE_CONFIRMATION = "Case Confirmation";

        public static String REGISTER_FAMILY = "RACD Register Family";

        public static String FI = "FI";

        public static String DYNAMIC_FI = "Dynamic-FI";

        public static String PAOT = "PAOT";

        public static String MDA_DISPENSE = "MDA Dispense";

        public static String MDA_ADHERENCE = "MDA Adherence";

        public static String MDA = "MDA";

        public static String MDA_LITE = "MDA-Lite";

        public static String DYNAMIC_MDA = "Dynamic-MDA";

        public static String IRS_VERIFICATION = "IRS Verification";

        // New Drug Recon Form
        public static String MDA_DRUG_RECON = "Drug Reconciliation";

        public static String SMC = "SMC";

        public static List<String> PERSON_INTERVENTIONS = Arrays.asList(BLOOD_SCREENING, CASE_CONFIRMATION, MDA_DISPENSE, MDA_ADHERENCE, MDA_DRUG_RECON);

        public static List<String> IRS_INTERVENTIONS = Arrays.asList(IRS, IRS_VERIFICATION);

        public static List<String> FI_INTERVENTIONS = Arrays.asList(MOSQUITO_COLLECTION,
                LARVAL_DIPPING, BCC, BEDNET_DISTRIBUTION, BLOOD_SCREENING, CASE_CONFIRMATION,
                REGISTER_FAMILY, PAOT);

        public static List<String> MDA_INTERVENTIONS = Arrays.asList(REGISTER_FAMILY, MDA_ADHERENCE, MDA_DISPENSE, MDA_DRUG_RECON);

        public static List<String> TASK_RESET_INTERVENTIONS = Arrays.asList(MOSQUITO_COLLECTION,
                LARVAL_DIPPING, BCC, CASE_CONFIRMATION,
                PAOT, IRS, IRS_VERIFICATION);

        public static String CDD_SUPERVISION = "CDD Supervision";

        public static String CELL_COORDINATION = "Cell Coordination";

        public static List<String> RWANDA_INTERVENTIONS = Arrays.asList(CELL_COORDINATION);

        public static List<String> KENYA_INTERVENTIONS = Arrays.asList(CELL_COORDINATION);

        public static List<String> LOCATION_VALIDATION_TASK_CODES = Arrays.asList(IRS,MOSQUITO_COLLECTION,LARVAL_DIPPING,PAOT,IRS_VERIFICATION,REGISTER_FAMILY,MDA_SURVEY,LSM_HOUSEHOLD_SURVEY,HABITAT_SURVEY,MDA_ONCHOCERCIASIS_SURVEY);

        public static String LSM = "LSM";
    }


    public interface EventType {

        public static String CASE_CONFIRMATION_EVENT = "case_confirmation";

        public static String CASE_DETAILS_EVENT = "Case_Details";

        public static String PAOT_EVENT = "PAOT";

        public static String MDA_DISPENSE = "mda_dispense";

        public static String MDA_ADHERENCE = "mda_adherence";

        public static String IRS_VERIFICATION = "irs_verification";

        public static String IRS_LITE_VERIFICATION = "irs_lite_verification";

        public static String DAILY_SUMMARY_EVENT = "daily_summary";

        public static String IRS_FIELD_OFFICER_EVENT = "irs_field_officer";

        public static String IRS_SA_DECISION_EVENT = "irs_sa_decision";

        public static String MOBILIZATION_EVENT = "mobilization";

        public static String TEAM_LEADER_DOS_EVENT = "team_leader_dos";

        public static String VERIFICATION_EVENT = "verification";

        public static String CB_SPRAY_AREA_EVENT = "cb_spray_area";

        public static String CDD_SUPERVISOR_DAILY_SUMMARY =  "cdd_supervisor_daily_summary";

        public static String TABLET_ACCOUNTABILITY_EVENT =  "tablet_accountability";

        public static String FPP_EVENT = "FPP";

        public static String CDD_DRUG_ALLOCATION_EVENT = "cdd_drug_allocation";

        public static String CDD_DRUG_WITHDRAWAL_EVENT = "cdd_drug_withdrawal";

        public static String CDD_DRUG_RECEIVED_EVENT = "cdd_drug_received";
        public static String GENERAL_SUPERVISION = "general_supervision";
        public static String COUNTY_CDD_SUPERVISORY_EVENT = "county_cdd_supervisory";

        public static String MDA_SURVEY_EVENT = "mda_survey";
        public static String MDA_ONCHO_EVENT = "mda_onchocerciasis_survey";
        public static String HABITAT_SURVEY_EVENT =  "habitat_survey";
        public static String LSM_HOUSEHOLD_SURVEY_EVENT =  "lsm_household_survey";


        public static List<String> SUMMARY_EVENT_TYPES = Arrays.asList(DAILY_SUMMARY_EVENT, IRS_FIELD_OFFICER_EVENT,
                IRS_SA_DECISION_EVENT, MOBILIZATION_EVENT, TEAM_LEADER_DOS_EVENT, VERIFICATION_EVENT,TABLET_ACCOUNTABILITY_EVENT,FPP_EVENT,
                CDD_DRUG_ALLOCATION_EVENT,GENERAL_SUPERVISION,COUNTY_CDD_SUPERVISORY_EVENT,CDD_DRUG_RECEIVED_EVENT,CDD_DRUG_WITHDRAWAL_EVENT);

        public static String CELL_COORDINATOR_DAILY_SUMMARY = "cell_coordinator_daily_summary";
        public static String MDA_DRUG_RECON = "mda_drug_reconciliation";
        public static String TREATMENT_OUTSIDE_HOUSEHOLD_EVENT = "treatment_outside_household";
        public static String ADVERSE_EVENTS_RECORD_EVENT = "adverse_events_record";



        public static List<String> EVENTS_FOR_CARD_DISPLAY = Arrays.asList(MOSQUITO_COLLECTION_EVENT, LARVAL_DIPPING_EVENT,
        BEDNET_DISTRIBUTION_EVENT, BEDNET_DISTRIBUTION_EVENT, BEHAVIOUR_CHANGE_COMMUNICATION,
        IRS_VERIFICATION, MDA_SURVEY_EVENT, LSM_HOUSEHOLD_SURVEY_EVENT, HABITAT_SURVEY_EVENT,
        MDA_ONCHO_EVENT,TREATMENT_OUTSIDE_HOUSEHOLD_EVENT,ADVERSE_EVENTS_RECORD_EVENT);
    }

    public interface Tables {
        public static String MOSQUITO_COLLECTIONS_TABLE = "mosquito_collections";
        public static String LARVAL_DIPPINGS_TABLE = "larval_dippings";
        public static String PAOT_TABLE = "potential_area_of_transmission";
        public static String IRS_VERIFICATION_TABLE = "irs_verification";
        public static String CLIENT_TABLE = "client";
        public static String EVENT_TABLE = "event";
        public static String TASK_TABLE = "task";
        public static String STRUCTURE_TABLE = "structure";
        public static String EC_EVENTS_TABLE = "ec_events";
        public static String EC_EVENTS_SEARCH_TABLE = "ec_events_search";
        public static String SPRAYED_STRUCTURES = "sprayed_structures";
    }

    public interface BusinessStatus {
        public static String NOT_VISITED = "Not Visited";
        public static String NOT_SPRAYED = "Not Sprayed";
        public static String SPRAYED = "Sprayed";
        public static String NOT_SPRAYABLE = "Not Sprayable";
        public static String COMPLETE = "Complete";
        public static String ALL_TASKS_COMPLETE = "All Tasks Complete";
        public static String INCOMPLETE = "Incomplete";
        public static String NOT_ELIGIBLE = "Not Eligible";
        public static String IN_PROGRESS = "In Progress";


        //MDA status
        public static String FULLY_RECEIVED = "Fully Received";
        public static String NONE_RECEIVED = "None Received";
        public static String ADHERENCE_VISIT_DONE = "Adherence Visit Done";
        public static String PARTIALLY_RECEIVED = "Partially Received";

        // Nigeria SMC workflow
        public static String SMC_COMPLETE = "SMC Complete";
        public static String SPAQ_COMPLETE = "SPAQ Complete";
        public static String INELIGIBLE = "Ineligible";
        public static String TASKS_INCOMPLETE = "Tasks Incomplete";
        public static String NOT_DISPENSED = "Not Dispensed";
        public static String FAMILY_NO_TASK_REGISTERED = "Family No Task Registered";

        // Following are for grouped structure tasks. Not synced to server
        public static String FAMILY_REGISTERED = "Family Registered";
        public static String BEDNET_DISTRIBUTED = "Bednet Distributed";
        public static String BLOOD_SCREENING_COMPLETE = "Blood Screening Complete";
        public static String PARTIALLY_SPRAYED = "Partially Sprayed";


        public static List<String> IRS_BUSINESS_STATUS = Arrays.asList(NOT_VISITED, NOT_SPRAYED,
                SPRAYED, NOT_SPRAYABLE, COMPLETE, INCOMPLETE, NOT_ELIGIBLE, IN_PROGRESS);

        public static List<String> FI_BUSINESS_STATUS = Arrays.asList(NOT_VISITED, FAMILY_REGISTERED, BEDNET_DISTRIBUTED,
                BLOOD_SCREENING_COMPLETE, COMPLETE, NOT_ELIGIBLE);

        public static List<String> MDA_LITE_BUSINESS_STATUS = Arrays.asList(NOT_VISITED,IN_PROGRESS,COMPLETE);
        public static List<String> MDA_BUSINESS_STATUS = Arrays.asList(NOT_VISITED, FULLY_RECEIVED, NONE_RECEIVED,
                ADHERENCE_VISIT_DONE, PARTIALLY_RECEIVED, COMPLETE, NOT_ELIGIBLE,NOT_VISITED, SMC_COMPLETE, INELIGIBLE,
                TASKS_INCOMPLETE, COMPLETE, NOT_ELIGIBLE, FAMILY_NO_TASK_REGISTERED, ALL_TASKS_COMPLETE, SPAQ_COMPLETE);
    }

    public interface BusinessStatusWrapper {

        public static List<String> SPRAYED = Arrays.asList(BusinessStatus.SPRAYED, BusinessStatus.COMPLETE, BusinessStatus.PARTIALLY_SPRAYED);
        public static List<String> NOT_SPRAYED = Arrays.asList(BusinessStatus.NOT_SPRAYED, BusinessStatus.IN_PROGRESS, BusinessStatus.INCOMPLETE);
        public static List<String> NOT_ELIGIBLE = Arrays.asList(BusinessStatus.NOT_SPRAYABLE, BusinessStatus.NOT_ELIGIBLE);
        public static List<String> NOT_VISITED = Arrays.asList(BusinessStatus.NOT_VISITED);
        public static List<String> MDA_DISPENSE_ELIGIBLE_STATUS = Arrays.asList(BusinessStatus.NOT_VISITED, BusinessStatus.SMC_COMPLETE, BusinessStatus.NOT_DISPENSED);
    }

    public interface Map {
         public static int MAX_SELECT_ZOOM_LEVEL = 16;
         public static int SELECT_JURISDICTION_MAX_SELECT_ZOOM_LEVEL = 14;
         public static int CLICK_SELECT_RADIUS = 24;
        public static String NAME_PROPERTY = "name";
         public static double DOWNLOAD_MAX_ZOOM = 21.0;
         public static double DOWNLOAD_MIN_ZOOM = 13.5;
    }

    public interface JsonForm {

        public static String ENCOUNTER_TYPE = "encounter_type";

        public static String SPRAY_STATUS = "sprayStatus";

        public static String TRAP_SET_DATE = "trap_start";

        public static String TRAP_FOLLOW_UP_DATE = "trap_end";

        public static String BUSINESS_STATUS = "business_status";

        public static String STRUCTURE_TYPE = "structureType";

        public static String STRUCTURE = "structure";

        public static String HEAD_OF_HOUSEHOLD = "familyHeadName";

        public static String STRUCTURE_PROPERTIES_TYPE = "[structure_type]";

        public static String WAS_ADMINISTERED_SPAQ = "[administeredSpaq]";
        public static String WAS_NOT_ADMINISTERED_SPAQ = "[wasNotAdministeredSpaq]";

        public static String NUMBER_OF_FAMILY_MEMBERS = "[num_fam_members]";

        public static String NUMBER_OF_ADHERED_FAMILY_MEMBERS = "[num_adhered_family_members]";

        public static String NUMBER_OF_FAMILY_MEMBERS_SLEEPING_OUTDOORS = "[num_sleeps_outdoors]";

        public static String SPRAY_FORM = "json.form/spray_form.json";

        public static String MOSQUITO_COLLECTION_FORM = "json.form/mosquito_collection_form.json";

        public static String SPRAY_FORM_NAMIBIA = "json.form/namibia_spray_form.json";

        public static String SPRAY_FORM_BOTSWANA = "json.form/botswana_spray_form.json";

        public static String SPRAY_FORM_REFAPP = "json.form/refapp_spray_form.json";

        public static String SPRAY_FORM_ZAMBIA = "json.form/zambia_spray_form_deceased_parents.json";
//        public static String SPRAY_FORM_ZAMBIA = "json.form/zambia_spray_form.json";

        public static String SPRAY_FORM_SENEGAL = "json.form/senegal_spray_form.json";

        public static String SPRAY_FORM_SENEGAL_EN = "json.form/senegal_en_spray_form.json";

        public static String LARVAL_DIPPING_FORM = "json.form/larval_dipping_form.json";

        public static String ADD_STRUCTURE_FORM = "json.form/add_structure.json";

        public static String IRS_ADD_STRUCTURE_FORM = "json.form/irs_add_structure.json";


        public static String MDA_SURVEY_ADD_STRUCTURE_FORM = "json.form/mda_survey_add_structure.json";

        public static String BEDNET_DISTRIBUTION_FORM = "json.form/bednet_distribution.json";

        public static String BLOOD_SCREENING_FORM = "json.form/blood_screening.json";

        public static String CASE_CONFIRMATION_FORM = "json.form/case_confirmation.json";

        public static String BEHAVIOUR_CHANGE_COMMUNICATION_FORM = "json.form/behaviour_change_communication.json";

        public static String PAOT_FORM = "json.form/paot.json";

        public static String THAILAND_LARVAL_DIPPING_FORM = "json.form/thailand_larval_dipping_form.json";

        public static String THAILAND_EN_LARVAL_DIPPING_FORM = "json.form/thailand_en_larval_dipping_form.json";

        public static String THAILAND_MOSQUITO_COLLECTION_FORM = "json.form/thailand_mosquito_collection_form.json";

        public static String THAILAND_EN_MOSQUITO_COLLECTION_FORM = "json.form/thailand_en_mosquito_collection_form.json";

        public static String THAILAND_SPRAY_FORM = "json.form/thailand_spray_form.json";

        public static String THAILAND_ADD_STRUCTURE_FORM = "json.form/thailand_add_structure.json";

        public static String THAILAND_BEDNET_DISTRIBUTION_FORM = "json.form/thailand_bednet_distribution.json";

        public static String THAILAND_EN_BEDNET_DISTRIBUTION_FORM = "json.form/thailand_en_bednet_distribution.json";

        public static String THAILAND_BLOOD_SCREENING_FORM = "json.form/thailand_blood_screening.json";

        public static String THAILAND_EN_BLOOD_SCREENING_FORM = "json.form/thailand_en_blood_screening.json";

        public static String THAILAND_CASE_CONFIRMATION_FORM = "json.form/thailand_case_confirmation.json";

        public static String THAILAND_BEHAVIOUR_CHANGE_COMMUNICATION_FORM = "json.form/thailand_behaviour_change_communication.json";

        public static String THAILAND_PAOT_FORM = "json.form/thailand_paot.json";

        public static String ZAMBIA_MDA_DISPENSE_FORM = "json.form/zambia_mda_dispense.json";

        public static String ZAMBIA_MDA_ADHERENCE_FORM = "json.form/zambia_mda_adherence.json";

        public static String ZAMBIA_IRS_VERIFICATION_FORM = "json.form/zambia_irs_verification.json";

        public static String REFAPP_MDA_DISPENSE_FORM = "json.form/refapp_mda_dispense.json";

        public static String REFAPP_MDA_ADHERENCE_FORM = "json.form/refapp_mda_adherence.json";

        public static String REFAPP_BEDNET_DISTRIBUTION_FORM = "json.form/refapp_bednet_distribution.json";

        public static String REFAPP_PAOT_FORM = "json.form/refapp_paot.json";

        public static String REFAPP_LARVAL_DIPPING_FORM = "json.form/refapp_larval_dipping_form.json";

        public static String REFAPP_MOSQUITO_COLLECTION_FORM = "json.form/refapp_mosquito_collection_form.json";

        public static String REFAPP_BLOOD_SCREENING_FORM = "json.form/refapp_blood_screening.json";

        public static String REFAPP_CASE_CONFIRMATION_FORM = "json.form/refapp_case_confirmation.json";

        public static String CDD_SUPERVISOR_DAILY_SUMMARY_FORM = "json.form/community_drug_distributor_supervisor_daily_summary_form.json";

        public static String RWANDA_CELL_COORDINATOR_DAILY_SUMMARY_FORM = "json.form/rwanda_cell_coordinator_daily_summary_form.json";

        public static String RWANDA_CELL_COORDINATOR_DAILY_SUMMARY_FORM_EN = "json.form/rwanda_en_cell_coordinator_daily_summary_form.json";

        public static String JSON_FORM_FOLDER = "json.form/";

        public static String LOCATION_COMPONENT_ACTIVE = "my_location_active";

        public static String VALID_OPERATIONAL_AREA = "valid_operational_area";

        // Nigeria
        public static String NIGERIA_MDA_ADHERENCE_FORM = "json.form/nigeria_second_dose_of_spaq.json";
        public static String NIGERIA_MDA_DISPENSE_FORM = "json.form/nigeria_child_smc_form.json";
        public static String NIGERIA_MDA_DRUG_RECON_FORM = "json.form/nigeria_structure_level_drug.json";

        // Summary Forms
        public static String DAILY_SUMMARY_MORNING = "json.form/nigeria_daily_summary_morning.json";

        public static String DAILY_SUMMARY_EVENING = "json.form/nigeria_daily_summary_evening.json";

        public static String HFW_LEVEL_REFERRAL = "json.form/nigeria_hfw_level_referral.json";

        public static String CDD_SUPERVISOR_CHECKLIST = "json.form/nigeria_cdd_supervisor_checklist.json";

        public static String HFW_SUPERVISOR_CHECKLIST = "json.form/nigeria_hfw_supervisor_checklist.json";


        public static String ZAMBIA_GENERAL_SUPERVISION_FORM = "json.form/zambia_general_supervision_form.json";

        public static String MDA_HOUSEHOLD_STATUS_MOZ_FORM = "json.form/mda_household_status_moz.json";

        public static String LSM_HABITAT_SURVEY_FORM_ZAMBIA = "json.form/lsm_habitat_survey_form_zambia.json";

        public static String LSM_HOUSEHOLD_SURVEY_ZAMBIA = "json.form/lsm_household_survey_form_zambia.json";

        public static String MDA_ONCHO_SURVEY_FORM = "json.form/mda_oncho_survey_form.json";

        public static String ADVERSE_EVENTS_RECORD_FORM = "json.form/adverse_events_record.json";

        public static String OPERATIONAL_AREA_TAG = "operational_area";

        public static String STRUCTURES_TAG = "structures";


        public static String NO_PADDING = "no_padding";

        public static String SHORTENED_HINT = "shortened_h public static int";

        public static String HINT = "h public static int";

        public static String TITLE = "title";

        public static String FAMILY_MEMBER = "familyMember";

        public static String STRUCTURE_NAME = "structureName";

        public static String PHYSICAL_TYPE = "physicalType";

        public static String PAOT_STATUS = "paotStatus";

        public static String PAOT_COMMENTS = "paotComments";

        public static String LAST_UPDATED_DATE = "lastUpdatedDate";

        public static String SELECTED_OPERATIONAL_AREA_NAME = "selectedOpAreaName";

        public static String NAMIBIA_ADD_STRUCTURE_FORM = "json.form/namibia_add_structure.json";

        public static String HOUSEHOLD_ACCESSIBLE = "householdAccessible";
        public static String ABLE_TO_SPRAY_FIRST = "ableToSprayFirst";
        public static String MOP_UP_VISIT = "mopUpVisit";
        public static String DISTRICT_NAME = "districtName";
        public static String PROVINCE_NAME = "provinceName";

        public static String HH_ID   ="hh_id";

        /**
         * Non-Task Related Forms
         */
        public static String DAILY_SUMMARY_ZAMBIA = "json.form/zambia_daily_summary_deceased_parents.json";

        public static String DAILY_SUMMARY_ZAMBIA_LITE = "json.form/zambia_daily_summary_lite.json";

        public static String TEAM_LEADER_DOS_ZAMBIA = "json.form/zambia_team_leader_dos.json";

        public static String CB_SPRAY_AREA_ZAMBIA = "json.form/zambia_cb_spray_area.json";

        public static String IRS_LITE_VERIFICATION = "json.form/zambia_irs_lite_verification.json";

        public static String IRS_SA_DECISION_ZAMBIA = "json.form/zambia_irs_sa_decision_deceased_parents.json";

        public static String MOBILIZATION_FORM_ZAMBIA = "json.form/zambia_mobilization_form.json";

        public static String IRS_FIELD_OFFICER_ZAMBIA = "json.form/zambia_irs_field_officer.json";

        public static String VERIFICATION_FORM_ZAMBIA = "json.form/zambia_verification_form.json";

        public static String DAILY_SUMMARY_SENEGAL= "json.form/senegal_daily_summary.json";

        public static String DAILY_SUMMARY_SENEGAL_EN= "json.form/senegal_en_daily_summary.json";

        public static String TEAM_LEADER_DOS_SENEGAL = "json.form/senegal_team_leader_dos.json";

        public static String CB_SPRAY_AREA_SENEGAL = "json.form/senegal_cb_spray_area.json";

        public static String IRS_SA_DECISION_SENEGAL = "json.form/senegal_irs_sa_decision.json";

        public static String IRS_SA_DECISION_SENEGAL_EN = "json.form/senegal_en_irs_sa_decision.json";

        public static String MOBILIZATION_FORM_SENEGAL = "json.form/senegal_mobilization_form.json";

        public static String IRS_FIELD_OFFICER_SENEGAL = "json.form/senegal_irs_field_officer.json";

        public static String IRS_FIELD_OFFICER_SENEGAL_EN = "json.form/senegal_en_irs_field_officer.json";

        public static String VERIFICATION_FORM_SENEGAL = "json.form/senegal_verification_form.json";

        public static String TABLET_ACCOUNTABILITY_FORM = "json.form/tablet_accountability_form.json";

        public static String TABLET_ACCOUNTABILITY_FORM_RWANDA = "json.form/rwanda_tablet_accountability_form.json";

        public static String TABLET_ACCOUNTABILITY_FORM_RWANDA_EN = "json.form/rwanda_en_tablet_accountability_form.json";

        public static String CDD_DRUG_ALLOCATION_FORM = "json.form/cdd_drug_allocation_form.json";

        public static String CDD_DRUG_RECEIVED_FORM = "json.form/cdd_drug_received_form.json";

        public static String MALI_DRUG_RECEIVED_FORM = "json.form/mali_drug_received_form.json";

        public static String TREATMENT_OUTSIDE_HOUSEHOLD_FORM = "json.form/treatment_outside_household.json";

        public static String CDD_DRUG_WITHDRAWAL_FORM = "json.form/cdd_drug_withdrawal_form.json";

        public static String COUNTY_CDD_SUPERVISORY_FORM = "json.form/county_cdd_supervisory_form.json" ;

        public static String FPP_FORM_ZAMBIA = "json.form/zambia_fpp_form.json";

        public static String SPRAY_OPERATOR_CODE = "sprayop_code";

        public static String SPRAY_OPERATOR_CODE_CONFIRMATION ="sprayop_code_confirm";

        public static String DATA_COLLECTOR = "datacollector";

        public static String DISTRICT_MANAGER = "district_manager";

        public static String SUPERVISOR = "supervisor";

        public static String SUPERVISOR_CONFIRMATION = "supervisor_confirmation";

        public static String TEAM_LEADER = "teamLeader";

        public static String FIELD_OFFICER = "fieldOfficer";

        public static String HFC_SEEK = "hfc_seek";

        public static String HFC_BELONG = "hfc_belong";

        public static String CHW_NAME = "chw_name";

        public static String COORDINATOR_NAME = "coordinator_name";

        public static String ADHERENCE_NAME = "adherence_name";

        public static String CATCHMENT_AREA = "catchment_area";

        public static String HEALTH_FACILITY = "health_facility";

        public static String DISTRICT = "district";


        public static String ZONE = "zone";

        public static String MOP_UP = "mopup";

        public static String GENERATED_GRP = "generated_group";

        public static String REPEATING_GROUP_UNIQUE_ID = "unique_id";

        public static String YES =  "Yes";

        public static String LOCATION = "location";

        public static String DRUG_WITHDRAWN  = "drug_withdrawn";

        public static String DRUG_ISSUED = "drug_issued";

        public static String DRUG_DISTRIBUTED =  "drug_distributed";

        public static String COMMUNITY_DRUG_DISTRIBUTOR_NAME = "cdd_name";

        public static String HEALTH_WORKER_SUPERVISOR = "health_worker_supervisor";

        public static String CDD_SUPERVISION_TASK_COMPLETE = "task_complete";

        public static String CELL_COORDINATOR = "cell_coordinator";

        public static String VILLAGE = "village";

        public static String HEALTH_EDUCATION_5_TO_15 = "health_education_5_to_15";

        public static String HEALTH_EDUCATION_ABOVE_16 = "health_education_above_16";

        public static String SUM_TREATED_6_TO_11_MOS = "sum_treated_6_to_11_mos";

        public static String SUM_TREATED_1_TO_4 = "sum_treated_1_to_4";

        public static String SUM_TREATED_5_TO_15 = "sum_treated_5_to_15";

        public static String SUM_TREATED_ABOVE_16 = "sum_treated_above_16";

        public static String COMPOUND_STRUCTURE = "compoundStructure";

        public static String LOCATION_ZONE = "location_zone";

        public static String NTD_TREATED = "ntd_treated";

        public static String ROOMS_SPRAYED = "rooms_sprayed";

        public static String ROOMS_ELIGIBLE = "rooms_eligible";

        public static String COLLECTION_DATE = "collection_date";


        public static String LOCATION_OTHER = "location_other";

        public static String SPRAY_AREAS = "spray_areas";

        public static String DATE = "date";

        public static String DATE_COMM = "dateComm";

        public static String SPRAY_DATE = "sprayDate";

        public static String EVENT_POSITION = "event_position";


        public static String ADMINISTERED_SPAQ = "administeredSpaq";

        public static String ADDITIONAL_DOSES_ADMINISTERED = "additionalDosesAdministered";

        public static String CHILDREN_TREATED = "childrenTreated";

        public static String CALCULATED_CHILDREN_TREATED = "calculatedChildrenTreated";

        public static String NUMBER_OF_ADDITIONAL_DOSES = "number_of_additional_doses";

        public static String TOTAL_ADMINISTERED_SPAQ = "totalAdministeredSpaq";

        public static String TOTAL_NUMBER_OF_ADDITIONAL_DOSES = "totalNumberOfAdditionalDoses";

        public static String DRUG_REALLOCATEE = "drug_reallocatee";


        public static String SPRAYOP_NAME = "sprayop_name";
        public static String COUNTY = "county";
        public static String SUB_COUNTY = "sub_county";
        public static String CDD_BORROWED_FORM = "cdd_borrowed_from" ;
    }

    public interface DateFormat {

        public static String EVENT_DATE_FORMAT_Z = "yyyy-MM-dd'T'HH:mm:ss.SSSZ";

        public static String EVENT_DATE_FORMAT_XXX = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX";

        public static String CARD_VIEW_DATE_FORMAT = "dd MMM yyyy";
    }

    public interface Action {
        public static String STRUCTURE_TASK_SYNCED = "reveal.STRUCTURE_TASK_SYNCED";
        public static String MDA_SURVEY = "MDA Survey";
        public static String HABITAT_SURVEY = "Habitat Survey";
        public static String LSM_HOUSEHOLD_SURVEY =  "LSM Household Survey";
        public static String MDA_ONCHOCERCIASIS_SURVEY = "MDA Onchocerciasis Survey";
    }

    public interface ECClientConfig {
        public static String ZAMBIA_EC_CLIENT_FIELDS = "ec_client_fields_zambia.json";
        public static String SENEGAL_EC_CLIENT_FIELDS = "ec_client_fields_senegal.json";
        public static String KENYA_EC_CLIENT_FIELDS = "ec_client_fields_kenya.json";
        public static String RWANDA_EC_CLIENT_FIELDS = "ec_client_fields_rwanda.json";
        public static String NIGERIA_EC_CLIENT_FIELDS = "ec_client_fields_nigeria.json";
    }






    public interface StructureType {
        public static String RESIDENTIAL = "Residential Structure";

        public static String NON_RESIDENTIAL = "Non-Residential Structure";

        public static String MOSQUITO_COLLECTION_POINT = "Mosquito Collection Po public static int";

        public static String LARVAL_BREEDING_SITE = "Larval Breeding Site";

        public static String POTENTIAL_AREA_OF_TRANSMISSION = "Potential Area of Transmission";

        public static String BODY_OF_WATER = "Body of Water";
    }

    public interface TaskRegister {
        public static String VIEW_IDENTIFIER = "task_register";

        public static String INTERVENTION_TYPE = " public static intervention_type";

        public static String LAST_USER_LOCATION = "last_location";

    }

    public interface EventsRegister {
        public static String VIEW_IDENTIFIER = "event_register";

        public static String TABLE_NAME = "ec_events";
    }

    public interface DatabaseKeys {

        public static String TASK_TABLE = "task";

        public static String SPRAYED_STRUCTURES = "sprayed_structures";

        public static String STRUCTURES_TABLE = "structure";

        public static String STRUCTURE_NAME = "structure_name";

        public static String STRUCTURE_ID = "structure_id";

        public static String ID_ = "_id";

        public static String ID = "id";

        public static String CODE = "code";

        public static String FOR = "for";

        public static String BUSINESS_STATUS = "business_status";

        public static String STATUS = "status";

        public static String REFERENCE_REASON = "reason_reference";

        public static String FAMILY_NAME = "family_head_name";

        public static String SPRAY_STATUS = "spray_status";

        public static String LATITUDE = "latitude";

        public static String LONGITUDE = "longitude";

        public static String NAME = "name";

        public static String GROUPID = "group_id";

        public static String PLAN_ID = "plan_id";

        public static String NOT_SRAYED_REASON = "not_sprayed_reason";

        public static String NOT_SRAYED_OTHER_REASON = "not_sprayed_other_reason";

        public static String OTHER = "other";

        public static String COMPLETED_TASK_COUNT = "completed_task_count";

        public static String TASK_COUNT = "task_count";

        public static String BASE_ENTITY_ID = "base_entity_id";

        public static String FIRST_NAME = "first_name";

        public static String LAST_NAME = "last_name";

        public static String GROUPED_STRUCTURE_TASK_CODE_AND_STATUS = "grouped_structure_task_code_and_status";

        public static String GROUPED_TASKS = "grouped_tasks";

        public static String LAST_UPDATED_DATE = "last_updated_date";

        public static String PAOT_STATUS = "paot_status";

        public static String PAOT_COMMENTS = "paot_comments";

        public static String EVENT_TASK_TABLE = "event_task";

        public static String EVENT_ID = "event_id";

        public static String TASK_ID = "task_id";

        public static String EVENT_DATE = "event_date";

        public static String EVENTS_PER_TASK = "events_per_task";

        public static String TRUE_STRUCTURE = "true_structure";

        public static String ELIGIBLE_STRUCTURE = "eligible_structure";

        public static String REPORT_SPRAY = "report_spray";

        public static String CHALK_SPRAY = "chalk_spray";

        public static String STICKER_SPRAY = "sticker_spray";

        public static String CARD_SPRAY = "card_spray";

        public static String SYNC_STATUS = "syncStatus";

        public static String TASK_SYNC_STATUS = "sync_Status";

        public static String STRUCTURE_SYNC_STATUS = "sync_Status";

        public static String VALIDATION_STATUS = "validationStatus";

        public static String AUTHORED_ON = "authored_on";

        public static String OWNER = "owner";

        public static String PROPERTY_TYPE = "property_type";

        public static String PARENT_ID = "parent_id";

        public static String FORM_SUBMISSION_ID = "formSubmissionId";

        public static String EVENT_TYPE_FIELD = "eventType";

        public static String CASE_CONFIRMATION_FIELD = "case_confirmation";

        public static String EVENT_TABLE = "event";

        public static String PERSON_TESTED = "person_tested";

        public static String LOCATION_TABLE = "location";

        public static String LOCATION_SYNC_STATUS = "sync_status";

        public static String SOP = "sop";

        public static String ENTITY = "entity";

        public static String EVENT_TYPE = "event_type";

        public static String SPRAYED = "sprayed";

        public static String FOUND = "found";

        public static String PROVIDER_ID = "provider_id";

        public static String VERSION = "version";

        Object SERVER_VERSION = "server_version";

        Object GEOJSON = "geojson";

        public static String DATA_COLLECTION_DATE = "data_collection_date";


        public static String STRUCTURE_HEAD_NAME = "structure_head_name";

        public static String COMPOUND_HEAD_NAME = "compound_head_name";

        public static String COMPOUND_STRUCTURE = "compound_structure";

        public static String SPRAY_DATE = "spray_date";
        public static String ADMINISTERED_SPAQ = "administered_spaq";

        public static String NUMBER_OF_ADDITIONAL_DOSES = "number_of_additional_doses";

        public static String JOB_AID = "job_aid";

        public static String LAST_INTERACTED_WITH = "last_ public static interacted_with";

        public static String ENTITY_STATUS = "entity_status";

    }

    public interface UseContextCode {
        public static String INTERVENTION_TYPE = " public static interventionType";
    }

    public interface IRSVerificationStatus {
        public static String SPRAYED = "sprayed";
        public static String NOT_SPRAYED = "notSprayed";
        public static String NOT_FOUND_OR_VISITED = "notFoundOrVisited";
        public static String OTHER = "other";
    }

    public interface SyncInfo {
        public static String SYNCED_EVENTS = "syncedEvents";
        public static String SYNCED_CLIENTS = "syncedClients";
        public static String UNSYNCED_EVENTS = "unsyncedEvents";
        public static String UNSYNCED_CLIENTS = "unsyncedClients";
        public static String VALID_EVENTS = "validEvents";
        public static String INVALID_EVENTS = "invalidEvents";
        public static String VALID_CLIENTS = "validClients";
        public static String INVALID_CLIENTS = "INValidClients";
        public static String TASK_UNPROCESSED_EVENTS = "taskUnprocessedEvents";
        public static String NULL_EVENT_SYNC_STATUS = "nullEventSyncStatus";
    }

    public interface RequestCode {
         public static int REQUEST_CODE_GET_JSON = 3432;

         public static int REQUEST_CODE_GET_JSON_FRAGMENT = 3439;

         public static int REQUEST_CODE_FAMILY_PROFILE = 3576;

         public static int REQUEST_CODE_FILTER_TASKS = 3596;

         public static int REQUEST_CODE_TASK_LISTS = 3617;
    }


    public interface InterventionType {
        public static String OPERATIONAL_AREA = "operational_area";
        public static String STRUCTURE = "structure";
        public static String FAMILY = "family";
        public static String PERSON = "person";

        public static List<String> FILTERABLE_INTERVENTION_TYPES = Arrays.asList(OPERATIONAL_AREA, STRUCTURE, FAMILY, PERSON);

    }

    public interface Filter {
        public static String CODE = "task_code";
        public static String STATUS = "task_status";
        public static String INTERVENTION_UNIT = " public static intervention_unit";
        public static String FORM_NAME = "form_name";
        public static String FILTER_SORT_PARAMS = "filter_sort_params";
        public static String FILTER_CONFIGURATION = "filter_configuration";
    }

    public static String SPRAY_OPERATOR = "spray_operator";
}

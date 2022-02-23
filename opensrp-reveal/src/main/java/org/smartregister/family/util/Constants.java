package org.smartregister.family.util;

/**
 * Created by keyman on 12/11/2018.
 */

public abstract class Constants {

    public static class JSON_FORM_KEY {
        public static final String ENTITY_ID = "entity_id";
        public static final String OPTIONS = "options";
        public static final String ENCOUNTER_LOCATION = "encounter_location";
        public static final String ATTRIBUTES = "attributes";
        public static final String DEATH_DATE = "deathdate";
        public static final String DEATH_DATE_APPROX = "deathdateApprox";
        public static final String UNIQUE_ID = "unique_id";
        public static final String FAMILY_NAME = "fam_name";
        public static final String LAST_INTERACTED_WITH = "last_interacted_with";
        public static final String DOB = "dob";
        public static final String DOB_UNKNOWN = "dob_unknown";
        public static final String AGE = "age";

    }

    public static class JSON_FORM_EXTRA {
        public static final String JSON = "json";
        public static final String NEXT = "next";

    }

    public static class OPENMRS {
        public static final String ENTITY = "openmrs_entity";
        public static final String ENTITY_ID = "openmrs_entity_id";
    }

    public static final class KEY {
        public static final String KEY = "key";
        public static final String VALUE = "value";
        public static final String TREE = "tree";
        public static final String DEFAULT = "default";
        public static final String PHOTO = "photo";
        public static final String TYPE = "type";
        public static final String FAMILY_HEAD_NAME = "family_head_name";
    }

    public static final class INTENT_KEY {
        public static final String BASE_ENTITY_ID = "base_entity_id";
        public static final String FAMILY_BASE_ENTITY_ID = "family_base_entity_id";
        public static final String FAMILY_HEAD = "family_head";
        public static final String PRIMARY_CAREGIVER = "primary_caregiver";
        public static final String VILLAGE_TOWN = "village_town";
        public static final String FAMILY_NAME = "family_name";
        public static final String JSON = "json";
        public static final String TO_RESCHEDULE = "to_reschedule";
        public static final String IS_REMOTE_LOGIN = "is_remote_login";
        public static final String GO_TO_DUE_PAGE = "go_to_due_page";
    }

    public interface CustomConfig {
        String FAMILY_FORM_IMAGE_STEP = "family_form_image_step";
        String FAMILY_MEMBER_FORM_IMAGE_STEP = "family_member_form_image_step";
    }

    public static class ENTITY {
        public static final String PERSON = "person";
    }

    public static class BOOLEAN_INT {
        public static final int TRUE = 1;
    }

    public static class IDENTIFIER {
        public static final String FAMILY_SUFFIX = "_family";
    }

    public static class WizardFormActivity {
        public static final String EnableOnCloseDialog = "EnableOnCloseDialog";
    }

    public static class Properties {
        public static final String FAMILY_HEAD_FIRSTNAME_ENABLED = "family.head.first.name.enabled";
    }

    public interface EntityType{
        String INDEPENDENT_CLIENT = "ec_independent_client";
    }
}

package org.smartregister.reveal.api;




public interface RevealService {
    //maybe call this class service constants
    String SYNC_PLANS_URL = "/rest/plans/sync";


    String SYNC_TASK_URL = "/rest/v2/task/sync";
    String ADD_TASK_URL = "/rest/v2/task/add";
    String UPDATE_STATUS_URL = "/rest/v2/task/update_status";


    String MANIFEST_SYNC_URL = "/rest/manifest/";
    String MANIFEST_SEARCH_URL = "/rest/manifest/search";
    String CLIENT_FORM_SYNC_URL = "/rest/clientForm";
    String FORM_DOWNLOAD_URL = "/form/form-files?formDirName=";
    String ALL_FORM_VERSION_URL = "/form/latest-form-versions";
    String FORM_SUBMISSIONS_PATH = "form-submissions";

    String LOCATION_STRUCTURE_URL = "/rest/location/sync";
    String CREATE_STRUCTURE_URL = "/rest/location/add";

   String ACCOUNT_CONFIGURATION_ENDPOINT = "/rest/config/keycloak";
   String TOKEN_ENDPOINT = "/auth/realms/reveal/protocol/openid-connect/token";
    String USER_ASSIGNMENT_URL = "/rest/organization/user-assignment";
    String OPENSRP_AUTH_USER_URL_PATH = "/security/authenticate";

    String SETTINGS_URL = "/rest/settings/sync";

   String VALIDATE_SYNC_PATH = "rest/validate/sync";

    String SYNC_URL = "/rest/event/sync";
    String ADD_URL = "rest/event/add";


   String ID_URL = "/uniqueids/get";

   String ACTIONS_URL = "/actions";
}

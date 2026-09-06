package org.smartregister.family.util;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.smartregister.Context;
import org.smartregister.reveal.R;
import org.smartregister.util.DateUtil;

import java.util.ArrayList;
import java.util.Calendar;

import timber.log.Timber;

public class Utils extends org.smartregister.util.Utils {

    public static final ArrayList<String> ALLOWED_LEVELS;
    public static final String DEFAULT_LOCATION_LEVEL = "Health Facility";
    public static final String FACILITY = "Dispensary";

    static {
        ALLOWED_LEVELS = new ArrayList<>();
        ALLOWED_LEVELS.add(DEFAULT_LOCATION_LEVEL);
        ALLOWED_LEVELS.add(FACILITY);
    }

    public static int getProfileImageResourceIDentifier() {
        return R.mipmap.ic_family_white;
    }










}

package org.smartregister.family.util;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.smartregister.Context;
import org.smartregister.family.FamilyLibrary;
import org.smartregister.reveal.R;
import org.smartregister.family.domain.FamilyMetadata;
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

    public static int getProfileImageTwoResourceIDentifier() {
        return R.mipmap.ic_family;
    }

    public static int getMemberProfileImageResourceIDentifier(String entityType) {
        if (StringUtils.isBlank(entityType) || Constants.EntityType.INDEPENDENT_CLIENT.equalsIgnoreCase(entityType)) {
            return R.mipmap.ic_member;
        }

        if (Utils.metadata().familyMemberRegister.tableName.equals(entityType)) {
            return R.mipmap.ic_member;
        } else {
            return R.mipmap.ic_child;
        }
    }

    public static int getActivityProfileImageResourceVistedIDentifier() {
        return R.mipmap.ic_activity_visited;
    }

    public static int getActivityProfileImageResourceNotVistedIDentifier() {
        return R.mipmap.ic_activity_notvisited;
    }

    public static int getDueProfileImageResourceIDentifier() {
        return R.color.due_profile_blue;
    }

    public static int getChildProfileImageResourceIDentifier() {
        return R.mipmap.ic_child;
    }

    public static String getDuration(String date1, String date2) {
        try {
            DateTime dateTime1 = toDateTime(date1);
            DateTime dateTime2 = toDateTime(date2);

            if (dateTime1 == null || dateTime2 == null) {
                return "";
            }

            Calendar calendar1 = Calendar.getInstance();
            calendar1.setTime(dateTime1.toDate());
            calendar1.set(Calendar.HOUR_OF_DAY, 0);
            calendar1.set(Calendar.MINUTE, 0);
            calendar1.set(Calendar.SECOND, 0);
            calendar1.set(Calendar.MILLISECOND, 0);

            Calendar calendar2 = Calendar.getInstance();
            calendar2.setTime(dateTime2.toDate());
            calendar2.set(Calendar.HOUR_OF_DAY, 0);
            calendar2.set(Calendar.MINUTE, 0);
            calendar2.set(Calendar.SECOND, 0);
            calendar2.set(Calendar.MILLISECOND, 0);


            long timeDiff = Math.abs(calendar1.getTimeInMillis() - calendar2.getTimeInMillis());
            return DateUtil.getDuration(timeDiff);

        } catch (Exception e) {
            Timber.tag("Reveal Exception").w(e);
        }
        return "";
    }

    public static DateTime toDateTime(String date) {
        if (StringUtils.isNotBlank(date)) {
            try {
                return new DateTime(date);
            } catch (Exception e) {
                Timber.tag("Reveal Exception").w(e);
            }
        }
        return null;
    }

    public static String getName(String firstName, String middleName, String lastName) {
        firstName = firstName.trim();
        middleName = middleName.trim();
        lastName = lastName.trim();
        if (StringUtils.isNotBlank(firstName) && StringUtils.isNotBlank(lastName)) {
            if (StringUtils.isNotBlank(middleName)) {
                return firstName + " " + middleName + " " + lastName;
            }
            return firstName + " " + lastName;

        } else {
            if (StringUtils.isNotBlank(firstName)) {
                if (StringUtils.isNotBlank(middleName)) {
                    return firstName + " " + middleName;
                }
                return firstName;

            } else if (StringUtils.isNotBlank(lastName)) {
                if (StringUtils.isNotBlank(middleName)) {
                    return middleName + " " + lastName;
                }
                return lastName;
            }
        }

        return "";
    }

    public static String getTranslatedDate(String str_date, android.content.Context context) {
        return str_date
                .replace("d", context.getString(R.string.abbrv_days))
                .replace("w", context.getString(R.string.abbrv_weeks))
                .replace("m", context.getString(R.string.abbrv_months))
                .replace("y", context.getString(R.string.abbrv_years));
    }

    public static Context context() {
        return FamilyLibrary.getInstance().context();
    }

    public static FamilyMetadata metadata() {
        return FamilyLibrary.getInstance().metadata();
    }

    public static boolean getBooleanProperty(String propertyKey) {
        return FamilyLibrary.getInstance().getProperties().hasProperty(propertyKey) && FamilyLibrary.getInstance().getProperties().getPropertyBoolean(propertyKey);
    }

    public static String getCustomConfigs(String key) {
        if (metadata().getCustomConfigs() != null)
            return metadata().getCustomConfigs().get(key);

        return null;
    }
}

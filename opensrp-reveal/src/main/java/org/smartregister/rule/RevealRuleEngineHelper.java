package org.smartregister.rule;
import org.joda.time.LocalDate;
import org.joda.time.Period;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import com.vijay.jsonwizard.utils.Utils;

import timber.log.Timber;

public class RevealRuleEngineHelper {

    public long getTodaysDateInMillis(){
        return new LocalDate().toDate().getTime();
    }
    public long getDateInMillis(String date){
        return Utils.getDateFromString(date).getTime();
    }

    public String nonNull(Object value) {
        Timber.tag("loadlistitems").i(" loadlistitems %s",value);

        return value == null ?  "false" :  value instanceof String && ((String)value).isEmpty() ? "false" : "true" ;
    }

    public String getAgeInMonths(String dateString) {
        // Define the date format
        DateTimeFormatter formatter = DateTimeFormat.forPattern("dd-MM-yyyy");

        // Parse the input date string to a LocalDate
        LocalDate birthDate = formatter.parseLocalDate(dateString);

        // Get the current date
        LocalDate currentDate = LocalDate.now();

        // Calculate the period between the two dates
        Period period = new Period(birthDate, currentDate);

        // Return the total number of months
        return String.valueOf(period.getYears() * 12 + period.getMonths());

        // Return the total number of months
    }
    public String getAgeInMonthsyyyyMMdd(String dateString) {
        // Define the date format
        DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyy-MM-dd");

        // Parse the input date string to a LocalDate
        LocalDate birthDate = formatter.parseLocalDate(dateString);

        // Get the current date
        LocalDate currentDate = LocalDate.now();

        // Calculate the period between the two dates
        Period period = new Period(birthDate, currentDate);

        // Return the total number of months
        return String.valueOf(period.getYears() * 12 + period.getMonths());

        // Return the total number of months
    }
}

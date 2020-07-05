package com.patrolsystemapp.Utils;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class CustomDateUtils {

    // format 24hre ex. 12:12 , 17:15
    private static String HOUR_FORMAT = "HH:mm";

    public static String getCurrentHour() {
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat sdfHour = new SimpleDateFormat(HOUR_FORMAT);
        String hour = sdfHour.format(cal.getTime());
        return hour;
    }

    /**
     * @param target hour to check
     * @param start  interval start
     * @param end    interval end
     * @return true    true if the given hour is between
     */
    public boolean isHourInInterval(String target, String start, String end) {
        if (start.compareTo(end) < 0)
            return ((target.compareTo(start) >= 0)
                    && (target.compareTo(end) <= 0));
        else return ((target.compareTo(start) >= 0)
                || (target.compareTo(end) <= 0));
    }

    /**
     * @param start interval start
     * @param end   interval end
     * @return true    true if the current hour is between
     */
    public boolean isNowInInterval(String start, String end) {
        return isHourInInterval
                (CustomDateUtils.getCurrentHour(), start, end);
    }
}

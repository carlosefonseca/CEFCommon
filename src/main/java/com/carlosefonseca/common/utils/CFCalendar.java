package com.carlosefonseca.common.utils;

import android.support.annotation.NonNull;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

public class CFCalendar extends GregorianCalendar {

    public CFCalendar() {
        super();
    }

    public CFCalendar(Date date) {
        super();
        setTime(date);
    }

    public int year() {
        return get(Calendar.YEAR);
    }

    public int month() {
        return get(Calendar.MONTH);
    }

    public int dayOfMonth() {
        return get(Calendar.DAY_OF_MONTH);
    }


    public CFCalendar addMillis(int millis) {
        add(Calendar.MILLISECOND, millis);
        return this;
    }
    public CFCalendar addSeconds(int secs) {
        add(Calendar.SECOND, secs);
        return this;
    }
    public CFCalendar addMinutes(int mins) {
        add(Calendar.MINUTE, mins);
        return this;
    }
    public CFCalendar addHours(int hours) {
        add(Calendar.HOUR, hours);
        return this;
    }
    public CFCalendar addDays(int days) {
        add(Calendar.DAY_OF_MONTH, days);
        return this;
    }
    public CFCalendar addWeeks(int weeks) {
        add(Calendar.WEEK_OF_YEAR, weeks);
        return this;
    }
    public CFCalendar addMonths(int months) {
        add(Calendar.MONTH, months);
        return this;
    }
    public CFCalendar addYears(int years) {
        add(Calendar.YEAR, years);
        return this;
    }

    @NonNull
    public static Calendar getTodayAtMidnight() {
        Calendar todayAtMidnight = getInstance();
        todayAtMidnight.set(HOUR_OF_DAY, 0);
        todayAtMidnight.set(MINUTE, 0);
        return todayAtMidnight;
    }
}

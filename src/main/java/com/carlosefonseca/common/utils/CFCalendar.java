package com.carlosefonseca.common.utils;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

public class CFCalendar extends GregorianCalendar {

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

}



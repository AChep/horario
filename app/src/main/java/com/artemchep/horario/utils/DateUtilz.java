package com.artemchep.horario.utils;

import android.support.annotation.NonNull;

public class DateUtilz {

    public static final int MASK_DAY = 0b11111;
    public static final int MASK_MONTH = 0b1111;

    @NonNull
    public static String format(int h, int m) {
        return (h > 9 ? "" + h : "0" + h) + ":" + (m > 9 ? "" + m : "0" + m);
    }

    @NonNull
    public static String formatLessonTime(int time) {
        time = Math.max(time - 1, 0);
        int m = time % 60;
        int h = time / 60;
        return format(h, m);
    }

    public static int mergeDate(int month, int day) {
        return day & MASK_DAY | month << 5;
    }

    public static int mergeDate(int year, int month, int day) {
        day &= MASK_DAY;
        month &= MASK_MONTH;
        return day | month << 5 | year << 9;
    }

    public static int getDay(int date) {
        return date & MASK_DAY;
    }

    public static int getMonth(int date) {
        return (date >>> 5) & MASK_MONTH;
    }

    public static int getYear(int date) {
        return date >>> 9;
    }

}

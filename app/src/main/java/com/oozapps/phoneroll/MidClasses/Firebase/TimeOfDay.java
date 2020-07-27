package com.oozapps.phoneroll.MidClasses.Firebase;

import android.util.Log;

import static android.content.ContentValues.TAG;

/**
 * Stores time in 24h format
 */
public class TimeOfDay {
    private int hour;
    private int minute;

    public TimeOfDay() {
        hour = 0;
        minute = 0;

    }

    public TimeOfDay(int hour, int minute) {
        super();
        if (hour < 24 && hour >= 0 && minute >= 0 && minute < 60) {
            this.hour = hour;
            this.minute = minute;
        } else {
            Log.e(TAG, "TimeOfDay: invalid time is given to constructor");
        }
    }

    public int getHour() {
        return hour;
    }

    public void setHour(int hour) {
        this.hour = hour;
    }

    public int getMinute() {
        return minute;
    }

    public void setMinute(int minute) {
        this.minute = minute;
    }
}

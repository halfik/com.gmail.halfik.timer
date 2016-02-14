package com.gmail.halfik.timer.activity;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;


import com.gmail.halfik.timer.fragment.TimerFragment;

public class TimerActivity extends SingleFragmentActivity
{
    public static final String PREF_NAME = "TimerPreferences";
    public static final String SETTINGS_ALERT_TIME = "alert_time";
    public static final String SETTINGS_ALERT_SOUND = "alert_sound";
    public static final String ALARMS_FOLDER = "alarm";
    public static final String DEFAULT_BEEP = "beep-06.mp3";


    private static final String TAG = "TimerActivity";

    public static Intent newIntent(Context packageContext) {
        Intent intent = new Intent(packageContext, TimerActivity.class);

        return intent;
    }

    @Override
    protected Fragment createFragment(){
        return TimerFragment.newInstance();
    }

}

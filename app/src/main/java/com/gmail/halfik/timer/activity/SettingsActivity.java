package com.gmail.halfik.timer.activity;


import android.content.Context;
import android.content.Intent;
import android.support.v4.app.Fragment;

import com.gmail.halfik.timer.fragment.SettingsFragment;


public class SettingsActivity extends SingleFragmentActivity
{
    private static final String TAG = "SettingsActivity";

    public static Intent newIntent(Context packageContext) {
        Intent intent = new Intent(packageContext, SettingsActivity.class);

        return intent;
    }

    @Override
    protected Fragment createFragment() {
        return SettingsFragment.newInstance();
    }
}

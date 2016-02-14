package com.gmail.halfik.timer.fragment;


import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import com.gmail.halfik.timer.R;
import com.gmail.halfik.timer.activity.TimerActivity;

import java.io.IOException;


public class SettingsFragment extends Fragment
{
    private static final String TAG = "SettingsFragment";

    private int mAlarmEvery;
    private String mAlarmSound;
    private EditText mEditAlarmEvery;
    private Spinner mBipperSelector;
    private Button mSaveButton;

    public static SettingsFragment newInstance(){
        Bundle args = new Bundle();

        SettingsFragment fragment = new SettingsFragment();
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences settings = getActivity().getSharedPreferences(TimerActivity.PREF_NAME, 0);
        mAlarmEvery = settings.getInt(TimerActivity.SETTINGS_ALERT_TIME, 0);
        mAlarmSound = settings.getString(TimerActivity.SETTINGS_ALERT_SOUND, TimerActivity.DEFAULT_BEEP);


        Log.i(TAG, "Extra alarm every :" + String.valueOf(mAlarmEvery));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_settings, container, false);

        mEditAlarmEvery = (EditText) v.findViewById(R.id.editAlarmEvery);
        mSaveButton = (Button) v.findViewById(R.id.save_button);

        mEditAlarmEvery.setText(Integer.toString(mAlarmEvery));

        final Spinner mBipperSelector = (Spinner) v.findViewById(R.id.biper_spinner);

        String[] items = getSpinnerItems();
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_dropdown_item, items);
        mBipperSelector.setAdapter(adapter);

        mBipperSelector.setSelection(getCurrentBipPosition(items));

        mSaveButton.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                SharedPreferences settings = getActivity().getSharedPreferences(TimerActivity.PREF_NAME, 0);
                SharedPreferences.Editor editor = settings.edit();

                editor.putInt(TimerActivity.SETTINGS_ALERT_TIME, Integer.valueOf(mEditAlarmEvery.getText().toString()));
                editor.putString(TimerActivity.SETTINGS_ALERT_SOUND, mBipperSelector.getSelectedItem().toString());

                editor.commit();

                getActivity().finish();
            }
        });


        return v;
    }

    private int getCurrentBipPosition(String[] soundNames){
       for (int i=0; i<soundNames.length; i++){
            if (soundNames[i].contentEquals(mAlarmSound) ){
                return i;
            }
        }


        return 0;
    }

    private String[] getSpinnerItems(){
         String[] soundNames = new String[]{};

        AssetManager assets = getActivity().getApplicationContext().getAssets();
        try{
            soundNames = assets.list(TimerActivity.ALARMS_FOLDER);
            Log.i(TAG, "Found " + soundNames.length + " sounds");
        }catch (IOException e){
            Log.e(TAG, "Could not list sets", e);
            return soundNames;
        }


        return soundNames;
    }
}

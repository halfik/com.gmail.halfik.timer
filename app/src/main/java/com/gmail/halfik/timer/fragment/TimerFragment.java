package com.gmail.halfik.timer.fragment;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.AssetFileDescriptor;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.gmail.halfik.timer.R;
import com.gmail.halfik.timer.activity.SettingsActivity;
import com.gmail.halfik.timer.activity.TimerActivity;
import com.gmail.halfik.timer.service.BackgroundAudioService;


public class TimerFragment extends Fragment
{
    private static final String TAG = "TimerFragment";

    private static final String STATE_TIME_SWAP_BUFFER = "time_sawp_buffer";
    private static final String STATE_IS_TIMER_RUNNING = "is_running";
    private static final String STATE_LAST_ALARM_TIME = "last_alarm_time";
    private static final String STATE_START_TIME = "start_time";
    private static final String STATE_INIT_TIMER = "init_timer";


    private Button mStartButton;
    private Button mResetButton;
    private TextView mTimeView;
    private boolean isRunning = false;;

    private long mInitTimer = 0L;
    private long mStartTime = 0L;
    private Handler mCustomHandler = new Handler();
    long mTimeInMilliseconds = 0L;
    long mTimeSwapBuff = 0L;
    long mUpdatedTime = 0L;
    long mLastAlarmTime = 0L;
    int mAlarmEvery;
    String mAlarmSound;

    public static TimerFragment newInstance(){
        Bundle args = new Bundle();

        TimerFragment fragment = new TimerFragment();
        fragment.setArguments(args);

        return fragment;
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences settings = getActivity().getSharedPreferences(TimerActivity.PREF_NAME, 0);
        mAlarmEvery = settings.getInt(TimerActivity.SETTINGS_ALERT_TIME, 0);
        mAlarmSound = settings.getString(TimerActivity.SETTINGS_ALERT_SOUND, "beep-06.mp3");

        if (savedInstanceState != null){
            if (savedInstanceState.containsKey(STATE_TIME_SWAP_BUFFER)){
                mTimeSwapBuff = savedInstanceState.getLong(STATE_TIME_SWAP_BUFFER);
            }

            if (savedInstanceState.containsKey(STATE_IS_TIMER_RUNNING)){
                isRunning = savedInstanceState.getBoolean(STATE_IS_TIMER_RUNNING);
            }

            if (savedInstanceState.containsKey(STATE_LAST_ALARM_TIME)){
                mLastAlarmTime = savedInstanceState.getLong(STATE_LAST_ALARM_TIME);
            }

            if (savedInstanceState.containsKey(STATE_START_TIME)){
                mStartTime = savedInstanceState.getLong(STATE_START_TIME);
            }

            if (savedInstanceState.containsKey(STATE_INIT_TIMER)){
                mInitTimer = savedInstanceState.getLong(STATE_INIT_TIMER);
            }
        }

        setHasOptionsMenu(true);
    }


    @Override
    public void onResume(){
        super.onResume();

        SharedPreferences settings = getActivity().getSharedPreferences(TimerActivity.PREF_NAME, 0);
        mAlarmEvery = settings.getInt(TimerActivity.SETTINGS_ALERT_TIME, 0);
        mAlarmSound = settings.getString(TimerActivity.SETTINGS_ALERT_SOUND, TimerActivity.DEFAULT_BEEP);
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        mCustomHandler.removeCallbacks(updateTimerThread);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putLong(STATE_TIME_SWAP_BUFFER, mTimeSwapBuff);
        outState.putBoolean(STATE_IS_TIMER_RUNNING, isRunning);
        outState.putLong(STATE_LAST_ALARM_TIME, mLastAlarmTime);
        outState.putLong(STATE_START_TIME, mStartTime);

        //we need this variable to restore time display on screen orientation change
        if (mUpdatedTime == 0){
            outState.putLong(STATE_INIT_TIMER, mInitTimer);
        }else{
            outState.putLong(STATE_INIT_TIMER, mUpdatedTime);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        View v = inflater.inflate(R.layout.fragment_timer, container, false);

        mStartButton = (Button) v.findViewById(R.id.start_button);
        mResetButton = (Button) v.findViewById(R.id.reset_button);
        mTimeView = (TextView)  v.findViewById(R.id.time);

        mResetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mTimeSwapBuff = 0L;
                mLastAlarmTime = 0L;
                mTimeView.setText(getResources().getString(R.string.init_time));
                updateUI();
            }
        });


        mStartButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isRunning = !isRunning;
                isRunning(false);
            }
        });

        if (mInitTimer > 0){
            int secs = (int) (mInitTimer / 1000);
            int mins = secs / 60;
            int currentSecs = secs % 60;
            int milliseconds = (int) (mInitTimer % 1000);

            mTimeView.setText("" + String.format("%02d", mins) + ":"
                    + String.format("%02d", currentSecs) + ":"
                    + String.format("%03d", milliseconds));
            ;
        }

        isRunning(true);

        return v;
    }


    private void isRunning(boolean useOldTimer){
        if (isRunning) {
            mStartButton.setText(getResources().getString(R.string.stop));

            //after screen rotartion we want to use old timer but only for init runn
            if (mInitTimer > 0 && useOldTimer == true){
               mInitTimer = 0L;
            }else{
                mStartTime = SystemClock.uptimeMillis();
            }

            mCustomHandler.postDelayed(updateTimerThread, 50);
        } else {
            mStartButton.setText(getResources().getString(R.string.start));
            mTimeSwapBuff += mTimeInMilliseconds;
            mCustomHandler.removeCallbacks(updateTimerThread);
        }

        updateUI();
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater){
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_timer, menu);

        MenuItem settingItem = menu.findItem(R.id.action_settings);
        settingItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                Intent intent = SettingsActivity.newIntent(getActivity());
                startActivity(intent);

                return false;
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        switch (item.getItemId()){
            case R.id.action_settings:

                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void updateUI(){
        if (isRunning){
            mResetButton.setEnabled(false);
        }else{
            mResetButton.setEnabled(true);
        }
    }


    public void playAlarm() {
        Intent i = new Intent(getActivity(), BackgroundAudioService.class);
        i.putExtra(BackgroundAudioService.EXTRA_FILE_PATH, mAlarmSound);

        getActivity().startService(i);
    }


    private Runnable updateTimerThread = new Runnable() {

        public void run() {
            mTimeInMilliseconds = SystemClock.uptimeMillis()  - mStartTime;

            mUpdatedTime = mTimeSwapBuff + mTimeInMilliseconds;
            //Log.i(TAG, "Timers: " + SystemClock.uptimeMillis() + ", " + String.valueOf(mStartTime) + ", " + String.valueOf(mTimeInMilliseconds) + ", " + String.valueOf(mTimeSwapBuff));
            int secs = (int) (mUpdatedTime / 1000);
            int mins = secs / 60;
            int currentSecs = secs % 60;
            int milliseconds = (int) (mUpdatedTime % 1000);

            if (secs > 0 && mAlarmEvery > 0 && secs % mAlarmEvery == 0 && secs != mLastAlarmTime){
                playAlarm();
                mLastAlarmTime = secs;
            }

            mTimeView.setText("" + String.format("%02d", mins) + ":"
                    + String.format("%02d", currentSecs) + ":"
                    + String.format("%03d", milliseconds));

            mCustomHandler.postDelayed(this, 50);
        }

    };
}

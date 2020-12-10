package com.example.petdiary.activity;

import android.app.NotificationManager;
import android.content.Context;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;

import com.example.petdiary.PreferenceManager;
import com.example.petdiary.R;
import com.google.firebase.messaging.FirebaseMessaging;

public class SettingNotificationActivity extends AppCompatActivity {

    SwitchCompat pushSwitch;
    SwitchCompat soundSwitch;
    SwitchCompat vibrateSwitch;

    private Context mContext;

    String pushCheck;
    String soundCheck;
    String vibrateCheck;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting_notification);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        pushSwitch = (SwitchCompat)findViewById(R.id.pushSwitch);
        soundSwitch = (SwitchCompat)findViewById(R.id.soundSwitch);
        vibrateSwitch = (SwitchCompat)findViewById(R.id.vibrateSwitch);

        mContext = this;

        //PreferenceManager.clear(mContext);

        pushCheck = PreferenceManager.getString(mContext, "pushCkecked");
        if(pushCheck.equals("")){
             pushSwitch.setChecked(true);
        } else {
            if(pushCheck.equals("true")){
                pushSwitch.setChecked(true);
            } else if(pushCheck.equals("false")) {
                pushSwitch.setChecked(false);
            }
        }
        soundCheck = PreferenceManager.getString(mContext, "soundChecked");
        if(soundCheck.equals("")){
            soundSwitch.setChecked(true);
        } else {
            if(soundCheck.equals("true")){
                soundSwitch.setChecked(true);
            } else if(soundCheck.equals("false")) {
                soundSwitch.setChecked(false);
            }
        }
        vibrateCheck = PreferenceManager.getString(mContext, "vibrateChecked");
        if(vibrateCheck.equals("")){
            vibrateSwitch.setChecked(true);
        } else {
            if(vibrateCheck.equals("true")){
                vibrateSwitch.setChecked(true);
            } else if(vibrateCheck.equals("false")) {
                vibrateSwitch.setChecked(false);
            }
        }

        pushSwitch.setOnCheckedChangeListener(new pushSwitchListener());
        soundSwitch.setOnCheckedChangeListener(new soundSwitchListener());
        vibrateSwitch.setOnCheckedChangeListener(new vibrateSwitchListener());

    }

    private void startToast(String msg){
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    class pushSwitchListener implements CompoundButton.OnCheckedChangeListener{
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            if(isChecked){
                PreferenceManager.setString(mContext, "pushCkecked", "true");
                FirebaseMessaging.getInstance().subscribeToTopic("1");
                startToast("푸쉬알림 ON");
            }
            else {
                PreferenceManager.setString(mContext, "pushCkecked", "false");
                FirebaseMessaging.getInstance().unsubscribeFromTopic("1");
                startToast("푸쉬알림 OFF");
            }

        }
    }

    class soundSwitchListener implements CompoundButton.OnCheckedChangeListener{
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            if(isChecked){
                PreferenceManager.setString(mContext, "soundChecked", "true");
                startToast("소리 ON");
            }
            else {
                PreferenceManager.setString(mContext, "soundChecked", "false");
                startToast("소리 OFF");
            }
        }
    }

    class vibrateSwitchListener implements CompoundButton.OnCheckedChangeListener{
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            if(isChecked){
                PreferenceManager.setString(mContext, "vibrateChecked", "true");
                startToast("진동 ON");
            }
            else {
                PreferenceManager.setString(mContext, "vibrateChecked", "false");
                startToast("진동 OFF");
            }
        }
    }



    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home ){
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

}

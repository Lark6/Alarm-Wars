package com.example.alarm__wars;


import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DatabaseReference;

public class EndAlarmActivity extends AppCompatActivity {

    private DatabaseReference mDatabase;
    private String hostCode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_end_alarm);
        if (RingActivity.mediaPlayer != null && RingActivity.mediaPlayer.isPlaying()) {
            RingActivity.mediaPlayer.stop();
            resetAlarm();
        }
    }

    /* 알람 초기화 */
    private void resetAlarm() {
        SharedPreferences sharedPreferences = getSharedPreferences("AlarmPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("isAlarmSet", false);
        editor.remove("alarmTimeInMillis"); // Remove the alarm time
        editor.apply();
    }
}
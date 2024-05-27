package com.example.alarm__wars;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.widget.Toast;

import com.example.alarm__wars.databinding.ActivityWaitBinding;

public class WaitActivity extends AppCompatActivity {
    private ActivityWaitBinding binding;
    private CountDownTimer countDownTimer;
    private long alarmTimeInMillis;
    private long timeLeftInMillis;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityWaitBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Intent intent = getIntent();
        alarmTimeInMillis = intent.getLongExtra("alarmTimeInMillis", 0);
        updateRemainingTime();

        startCountDown();

        binding.checkButton.setOnClickListener(view -> {
            cancelAlarm();
            Toast.makeText(this, "알람이 취소되었습니다.", Toast.LENGTH_SHORT).show();
            SharedPreferences sharedPreferences = getSharedPreferences("AlarmPrefs", MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean("isAlarmSet", false);
            editor.apply();
            Intent mainIntent = new Intent(WaitActivity.this, MakeRoomActivity.class);
            startActivity(mainIntent);
            finish();
        });
    }

    private void updateRemainingTime() {
        long currentTimeInMillis = System.currentTimeMillis();
        timeLeftInMillis = alarmTimeInMillis - currentTimeInMillis;
    }

    private void startCountDown() {
        countDownTimer = new CountDownTimer(timeLeftInMillis, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                timeLeftInMillis = millisUntilFinished;
                updateCountDownText();
            }

            @Override
            public void onFinish() {
                // 알람이 울릴 때 AlarmActivity로 전환
                Intent alarmIntent = new Intent(WaitActivity.this, RingActivity.class);
                startActivity(alarmIntent);
                finish();
            }
        }.start();
    }

    private void updateCountDownText() {
        int hours = (int) (timeLeftInMillis / 1000) / 3600;
        int minutes = (int) ((timeLeftInMillis / 1000) % 3600) / 60;
        int seconds = (int) (timeLeftInMillis / 1000) % 60;
        String timeLeftFormatted = String.format("%02d:%02d:%02d", hours, minutes, seconds);
        binding.timerText.setText(timeLeftFormatted);
    }

    private void cancelAlarm() {
        Intent intent = new Intent(this, AlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_IMMUTABLE);
        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        if (alarmManager != null) {
            alarmManager.cancel(pendingIntent);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
    }
}

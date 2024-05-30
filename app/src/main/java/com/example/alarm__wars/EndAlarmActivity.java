package com.example.alarm__wars;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class EndAlarmActivity extends AppCompatActivity {

    private DatabaseReference mDatabase;
    private String hostCode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_end_alarm);

        mDatabase = FirebaseDatabase.getInstance().getReference().child("rooms");
        hostCode = getIntent().getStringExtra("hostCode");

        if (RingActivity.mediaPlayer != null && RingActivity.mediaPlayer.isPlaying()) {
            RingActivity.mediaPlayer.stop();
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    resetAlarm(); // resetAlarm() 함수 실행
                }
            }, 5000); // 5초의 딜레이를 줌

        }
    }

    /* 알람 초기화 */
    private void resetAlarm() {
        SharedPreferences sharedPreferences = getSharedPreferences("AlarmPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("isAlarmSet", false);
        editor.remove("alarmTimeInMillis"); // Remove the alarm time
        editor.apply();

        cancelAlarm(this);
        fetchAlarmDataAndSetAlarms();

    }
    public static void cancelAlarm(Context context) {
        Intent intent = new Intent(context, AlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_IMMUTABLE);

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        // 현재 설정된 알람 취소
        alarmManager.cancel(pendingIntent);

        Toast.makeText(context, "알람 취소 완료", Toast.LENGTH_SHORT).show();
    }


    private void fetchAlarmDataAndSetAlarms() {

        List<Boolean> ServerDays = new ArrayList<>();
        mDatabase.child(hostCode).child("dates").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    ServerDays.clear();
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        ServerDays.add(snapshot.getValue(Boolean.class));
                    }

                    mDatabase.child(hostCode).child("alarmTime").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if (dataSnapshot.exists()) {
                                int serverAlarmTime = dataSnapshot.getValue(int.class);
                                System.out.println(serverAlarmTime);
//                                alarmTime = Calendar.getInstance();
//                                alarmTime.setTimeInMillis(serverAlarmTime);
//
                                boolean[] selectedDays = new boolean[ServerDays.size()];
                                for (int i = 0; i < ServerDays.size(); i++) {
                                    selectedDays[i] = ServerDays.get(i) != null && ServerDays.get(i); // null 체크와 함께 변환
                                }
                                System.out.println(selectedDays);

                                int hours = serverAlarmTime / 100;
                                int minutes = serverAlarmTime % 100;

                                long alarmTimeInMillis = MakeRoomActivity.calculateAlarmTimeInMillis(hours, minutes, selectedDays);
                                setAlarm(alarmTimeInMillis);


                                SharedPreferences sharedPreferences = getSharedPreferences("AlarmPrefs", MODE_PRIVATE);
                                Boolean isHost = sharedPreferences.getBoolean("isHost", false);
                                Intent waitIntent;
                                if(isHost) {
                                    waitIntent = new Intent(EndAlarmActivity.this, hostWaitActivity.class);
                                }
                                else{
                                    waitIntent = new Intent(EndAlarmActivity.this, WaitActivity.class);
                                }

//                                waitIntent.putExtra("alarmTimeInMillis", alarmTimeInMillis);
                                waitIntent.putExtra("hostCode", hostCode);

                                SharedPreferences.Editor editor = sharedPreferences.edit();
                                editor.putBoolean("isAlarmSet", true);
                                editor.putLong("alarmTimeInMillis", alarmTimeInMillis);
                                editor.apply();
                                startActivity(waitIntent);
                                finish();


                            } else {
                                // 문제 정보를 찾을 수 없는 경우
                                Toast.makeText(EndAlarmActivity.this, "알람 정보를 찾을 수 없습니다", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                            // 데이터베이스 오류 발생 시
                            Toast.makeText(EndAlarmActivity.this, "데이터베이스 오류가 발생했습니다", Toast.LENGTH_SHORT).show();
                        }
                    });

                } else {
                    // 날짜 정보를 찾을 수 없는 경우
                    Toast.makeText(EndAlarmActivity.this, "날짜 정보를 찾을 수 없습니다", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // 데이터베이스 오류 발생 시
                Toast.makeText(EndAlarmActivity.this, "데이터베이스 오류가 발생했습니다", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @SuppressLint("ScheduleExactAlarm")
    private void setAlarm(long alarmTimeInMillis) {
        Intent intent = new Intent(this, AlarmReceiver.class);
        intent.putExtra("hostCode", hostCode);

        // 현재 시간을 액션에 포함하여 고유한 값을 만듭니다.
//        long currentTime1 = System.currentTimeMillis();
//        String action = "com.example.alarm__wars.ACTION_ALARM_" + currentTime1;
//        intent.setAction(action);

//        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_IMMUTABLE);

        // PendingIntent를 생성합니다.
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                this,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);

        alarmManager.setExact(AlarmManager.RTC, alarmTimeInMillis, pendingIntent);

        Toast.makeText(this, "알람 설정 완료", Toast.LENGTH_SHORT).show();
    }

}
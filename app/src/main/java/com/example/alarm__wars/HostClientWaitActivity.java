package com.example.alarm__wars;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class HostClientWaitActivity extends AppCompatActivity {

    private DatabaseReference mDatabase;
    private String hostCode;

    private SharedPreferences sharedPreferences;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sharedPreferences = getSharedPreferences("AlarmPrefs", MODE_PRIVATE);
        mDatabase = FirebaseDatabase.getInstance().getReference().child("rooms");
        hostCode = sharedPreferences.getString("hostCode", "없어요.");

        setContentView(R.layout.activity_host_client_wait);

        TextView hostCodeView = findViewById(R.id.host_code_view);
        Button outButton = findViewById(R.id.out);

        hostCodeView.setText(hostCode);
        outButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putBoolean("isHost", false);
                editor.apply();
                Intent intent = new Intent(HostClientWaitActivity.this, MainActivity.class);
                startActivity(intent);
                // 여기에 방 room class 제거
                finish();
            }
        });


        // 데이터 변경 감지 리스너 등록
        mDatabase.child(hostCode).child("hostSelected").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                // 데이터가 변경될 때마다 호출됨
                boolean hostSelected = dataSnapshot.getValue(boolean.class);
                if(hostSelected){
                    long alarmTimeInMillis = sharedPreferences.getLong("alarmTimeInMillis", 0);

                    boolean isAlarmSet = sharedPreferences.getBoolean("isAlarmSet", false);
                    if(!isAlarmSet){
                        setAlarm(alarmTimeInMillis);
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putBoolean("isAlarmSet", true);
                        editor.apply();
                    }

                    finish();

                    Intent intent = new Intent(HostClientWaitActivity.this, hostWaitActivity.class);
                    intent.putExtra("hostCode", hostCode);
                    startActivity(intent);

                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // 데이터베이스 오류 발생 시 호출됨
                Log.e("Firebase", "Failed to read value.", databaseError.toException());
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

        // PendingIntent를 생성합니다.
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                this,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
//        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_IMMUTABLE);

        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);

        alarmManager.setExact(AlarmManager.RTC, alarmTimeInMillis, pendingIntent);

        Toast.makeText(this, "알람 설정 완료", Toast.LENGTH_SHORT).show();
    }


}
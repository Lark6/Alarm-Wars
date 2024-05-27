package com.example.alarm__wars;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class RoomActivity extends AppCompatActivity {

    private int alarmNo = 0; // 초기 알람 번호를 0으로 설정

    private DatabaseReference mDatabase;
    private String hostCode;

    private Calendar alarmTime;

    private List<Boolean> ServerDays = new ArrayList<>();

    private boolean[] selectedDays;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_room);

        mDatabase = FirebaseDatabase.getInstance().getReference().child("rooms");
        hostCode = getIntent().getStringExtra("hostCode");
        System.out.println("RoomActivity hostcode: "+hostCode);

        Button questionButton = findViewById(R.id.question_button);
        Button answerButton = findViewById(R.id.answer_button);
        Button ringTestButton = findViewById(R.id.ring_test);
        Button setAlarmButton = findViewById(R.id.set_alarm);

        // 출제 버튼 클릭 시
        questionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkHostSelectionAndProceed();
            }
        });

        // 풀이 버튼 클릭 시
        answerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkHostSelectionAndProceed();
            }
        });

        ringTestButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(RoomActivity.this, RingActivity.class);
                intent.putExtra("hostCode", hostCode);
                startActivity(intent);
            }
        });

        // 알람설정 버튼 클릭 시
        setAlarmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                fetchAlarmDataAndSetAlarms();
//                Intent intent = new Intent(RoomActivity.this, WaitActivity.class);
//                startActivity(intent);
            }
        });

    }

    private void checkHostSelectionAndProceed() {
        // 데이터베이스에서 호스트 선택 여부 확인
        mDatabase.child(hostCode).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    boolean isHostSelected = dataSnapshot.getValue(Boolean.class);
                    if (!isHostSelected) {
                        // 호스트가 선택되지 않았을 경우 출제 액티비티로 이동
                        // 출제 액티비티로 이동하기 전에 hostSelected 값을 참으로 변경
                        mDatabase.child(hostCode).child("hostSelected").setValue(true);

                        Intent intent = new Intent(RoomActivity.this, QuestionActivity.class);
                        intent.putExtra("hostCode", hostCode);
                        startActivity(intent);
                    } else {
                        // 호스트가 이미 선택된 경우 토스트 메시지 표시
                        Toast.makeText(RoomActivity.this, "이미 선택된 호스트입니다", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    // 호스트 정보를 찾을 수 없는 경우
                    Toast.makeText(RoomActivity.this, "호스트 정보를 찾을 수 없습니다", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // 데이터베이스 오류 발생 시
                Toast.makeText(RoomActivity.this, "데이터베이스 오류가 발생했습니다", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchAlarmDataAndSetAlarms() {
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
                                selectedDays = new boolean[ServerDays.size()];
                                for (int i = 0; i < ServerDays.size(); i++) {
                                    selectedDays[i] = ServerDays.get(i) != null && ServerDays.get(i); // null 체크와 함께 변환
                                }
                                System.out.println(selectedDays);

                                int hours = serverAlarmTime / 100;
                                int minutes = serverAlarmTime % 100;

                                long alarmTimeInMillis = MakeRoomActivity.calculateAlarmTimeInMillis(hours, minutes, selectedDays);
                                setAlarm(alarmTimeInMillis);

                                Intent waitIntent = new Intent(RoomActivity.this, WaitActivity.class);
                                waitIntent.putExtra("alarmTimeInMillis", alarmTimeInMillis);
                                waitIntent.putExtra("hostCode", hostCode);

                                SharedPreferences sharedPreferences = getSharedPreferences("AlarmPrefs", MODE_PRIVATE);
                                SharedPreferences.Editor editor = sharedPreferences.edit();
                                editor.putBoolean("isAlarmSet", true);
                                editor.putLong("alarmTimeInMillis", alarmTimeInMillis);
                                editor.apply();
                                startActivity(waitIntent);
                                finish();


                            } else {
                                // 문제 정보를 찾을 수 없는 경우
                                Toast.makeText(RoomActivity.this, "알람 정보를 찾을 수 없습니다", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                            // 데이터베이스 오류 발생 시
                            Toast.makeText(RoomActivity.this, "데이터베이스 오류가 발생했습니다", Toast.LENGTH_SHORT).show();
                        }
                    });

                } else {
                    // 날짜 정보를 찾을 수 없는 경우
                    Toast.makeText(RoomActivity.this, "날짜 정보를 찾을 수 없습니다", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // 데이터베이스 오류 발생 시
                Toast.makeText(RoomActivity.this, "데이터베이스 오류가 발생했습니다", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @SuppressLint("ScheduleExactAlarm")
    private void setAlarm(long alarmTimeInMillis) {
        Intent intent = new Intent(this, AlarmReceiver.class);
        intent.putExtra("hostCode", hostCode);
        // 현재 시간을 액션에 포함하여 고유한 값을 만듭니다.
        long currentTime1 = System.currentTimeMillis();
        String action = "com.example.alarm__wars.ACTION_ALARM_" + currentTime1;
        intent.setAction(action);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_IMMUTABLE);

        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);

        alarmManager.setExact(AlarmManager.RTC, alarmTimeInMillis, pendingIntent);

        Toast.makeText(this, "알람 설정 완료", Toast.LENGTH_SHORT).show();
    }

}

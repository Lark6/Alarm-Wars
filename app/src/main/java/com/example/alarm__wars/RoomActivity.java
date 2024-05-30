package com.example.alarm__wars;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
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
                // 권한 요청
                onRequestPermissionsResult();
            }
        });
    }


    // 권한 요청 결과에 따라 다음 작업 수행
    public void onRequestPermissionsResult() {
        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        if (alarmManager.canScheduleExactAlarms()) {
                // 알람 설정 메서드 호출
            fetchAlarmDataAndSetAlarms();
        } else {
            requestExactAlarmPermission(this);
            Toast.makeText(this, "정확한 알람 권한이 필요합니다.", Toast.LENGTH_SHORT).show();
        }
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
                                editor.putBoolean("isHost", false);
                                editor.putString("hostCode", hostCode);
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
        // 브로드캐스트 수신기를 지정하는 인텐트를 생성합니다.
        Intent intent = new Intent(this, AlarmReceiver.class);
        intent.putExtra("hostCode", hostCode);

        // PendingIntent를 생성합니다.
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                this,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
        // AlarmManager를 통해 알람을 설정합니다.
        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);

        alarmManager.setExact(AlarmManager.RTC, alarmTimeInMillis, pendingIntent);

        Toast.makeText(this, "알람 설정 완료", Toast.LENGTH_SHORT).show();
    }

    public static void requestExactAlarmPermission(Context context) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (!alarmManager.canScheduleExactAlarms()) {
                Log.d("AlarmDebug", "Requesting exact alarm permission");
                Intent settingsIntent = new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
                context.startActivity(settingsIntent);
                Toast.makeText(context, "정확한 알람 권한이 필요합니다. 설정에서 권한을 부여하세요.", Toast.LENGTH_SHORT).show();
            } else {
                Log.d("AlarmDebug", "Exact alarm permission already granted");
            }
        } else {
            Log.d("AlarmDebug", "SDK version is lower than S, exact alarm permission not required");
        }
    }




}

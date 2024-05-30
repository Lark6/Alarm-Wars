package com.example.alarm__wars;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.alarm__wars.databinding.ActivityWaitBinding;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class WaitActivity extends AppCompatActivity {
    private ActivityWaitBinding binding;
    private CountDownTimer countDownTimer;
    private long alarmTimeInMillis;
    private long timeLeftInMillis;
    private  String hostCode;
    private DatabaseReference mDatabase;

    private SharedPreferences sharedPreferences;

    private Handler handler;
    private Runnable checkHostRunnable;

    private boolean isSelf;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityWaitBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        TextView hostCodeText = findViewById(R.id.host_code);

        sharedPreferences = getSharedPreferences("AlarmPrefs", MODE_PRIVATE);
        alarmTimeInMillis = sharedPreferences.getLong("alarmTimeInMillis", 0);
        hostCode = sharedPreferences.getString("hostCode", "호스트 코드가 없어요");
        hostCodeText.setText(hostCode);

        isSelf = false;

        updateRemainingTime();

        startCountDown();
        mDatabase = FirebaseDatabase.getInstance().getReference().child("rooms");

        // 호스트 나가는 지 확인
        mDatabase.child(hostCode).child("hostOuted").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    boolean isHostOuted = dataSnapshot.getValue(Boolean.class);
                    if (isHostOuted == true) {
                        isSelf = true;
                        Toast.makeText(WaitActivity.this, "집이 불타서 없어져 버렸습니다.", Toast.LENGTH_SHORT).show();
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        mDatabase.child(hostCode).child("hostOuted").setValue(false);
                        mDatabase.child(hostCode).child("hostSelected").setValue(false);
                        editor.putBoolean("isAlarmSet", false);
                        editor.apply();
                        EndAlarmActivity.cancelAlarm(WaitActivity.this);
                        Intent mainIntent = new Intent(WaitActivity.this, MainActivity.class);
                        startActivity(mainIntent);
                        finish();
//                        Intent intent = new Intent(RoomActivity.this, QuestionActivity.class);
//                        intent.putExtra("hostCode", hostCode);
//                        startActivity(intent);
                    }
                }
//                } else {
//                    // 호스트 정보를 찾을 수 없는 경우
//                    Toast.makeText(WaitActivity.this, "호스트 정보를 찾을 수 없습니다", Toast.LENGTH_SHORT).show();
//                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // 데이터베이스 오류 발생 시
                Toast.makeText(WaitActivity.this, "데이터베이스 오류가 발생했습니다", Toast.LENGTH_SHORT).show();
            }
        });


        // 데이터 변경 감지 리스너 등록
        mDatabase.child(hostCode).child("timeChanged").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                // 데이터가 변경될 때마다 호출됨
                boolean timeChanged = dataSnapshot.getValue(boolean.class);
                if(timeChanged){
//                    cancelAlarm();
                    SharedPreferences sharedPreferences = getSharedPreferences("AlarmPrefs", MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putBoolean("isAlarmSet", false);
                    editor.apply();

                    EndAlarmActivity.cancelAlarm(WaitActivity.this);
                    mDatabase.child(hostCode).child("timeChanged").setValue(false);

                    fetchAlarmDataAndSetAlarms();
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // 데이터베이스 오류 발생 시 호출됨
                Log.e("Firebase", "Failed to read value.", databaseError.toException());
            }
        });


        Button button = findViewById(R.id.checkButton);
        button.setVisibility(View.INVISIBLE);

        binding.cancelButton.setOnClickListener(view -> {
            // 팝업창 띄우기
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("정말 나갈꺼임?")
                    .setPositiveButton("네", (dialog, which) -> {
                        // 기존 코드 실행
                        mDatabase.child(hostCode).child("clientOuted").setValue(true);
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putBoolean("isAlarmSet", false);
                        editor.apply();
                        EndAlarmActivity.cancelAlarm(this);
                        Intent mainIntent = new Intent(WaitActivity.this, MainActivity.class);
                        startActivity(mainIntent);
                        finish();
                    })
                    .setNegativeButton("아니오", (dialog, which) -> {
                        // "아니오"를 선택한 경우 실행될 코드 (아무 작업 없음)
                    });
            AlertDialog alertDialog;
            alertDialog = builder.create();
            alertDialog.show();
        });


//        handler = new Handler();
//        checkHostRunnable = new Runnable() {
//            @Override
//            public void run() {
//                checkHost();
//                handler.postDelayed(this, 500); // 5초마다 실행
////                if(!isSelf){
////
////                }
////                else {
////                    handler.removeCallbacks(this);
////                }
//            }
//        };
//        handler.post(checkHostRunnable);




//        binding.checkButton.setOnClickListener(view -> {
//            cancelAlarm();
////            Toast.makeText(this, "알람이 취소되었습니다.", Toast.LENGTH_SHORT).show();
////            sharedPreferences = getSharedPreferences("AlarmPrefs", MODE_PRIVATE);
//            SharedPreferences.Editor editor = sharedPreferences.edit();
//            editor.putBoolean("isAlarmSet", false);
//            editor.apply();
//
//            EndAlarmActivity.cancelAlarm(this);
//
//            Intent mainIntent = new Intent(WaitActivity.this, updateTimeActivity.class);
//            mainIntent.putExtra("hostCode", hostCode);
//            startActivity(mainIntent);
//            finish();
//        });
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
//                Intent alarmIntent = new Intent(WaitActivity.this, RingActivity.class);
//                startActivity(alarmIntent);
//                finish();
            }
        }.start();
    }

    private void updateCountDownText() {
        int days = (int) (timeLeftInMillis / (1000 * 60 * 60 * 24));
        int hours = (int) ((timeLeftInMillis / (1000 * 60 * 60)) % 24);
        int minutes = (int) ((timeLeftInMillis / (1000 * 60)) % 60);
        int seconds = (int) (timeLeftInMillis / 1000) % 60;

        String timeLeftFormatted;
        if (days > 0) {
            timeLeftFormatted = String.format("%d일 %02d:%02d:%02d", days, hours, minutes, seconds);
        } else {
            timeLeftFormatted = String.format("%02d:%02d:%02d", hours, minutes, seconds);
        }

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

                                Intent waitIntent = new Intent(WaitActivity.this, WaitActivity.class);
//                                waitIntent.putExtra("alarmTimeInMillis", alarmTimeInMillis);
//                                waitIntent.putExtra("hostCode", hostCode);

                                SharedPreferences sharedPreferences = getSharedPreferences("AlarmPrefs", MODE_PRIVATE);
                                SharedPreferences.Editor editor = sharedPreferences.edit();
                                editor.putBoolean("isAlarmSet", true);
                                editor.putLong("alarmTimeInMillis", alarmTimeInMillis);
                                editor.apply();
                                startActivity(waitIntent);
                                finish();


                            } else {
                                // 문제 정보를 찾을 수 없는 경우
                                Toast.makeText(WaitActivity.this, "알람 정보를 찾을 수 없습니다", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                            // 데이터베이스 오류 발생 시
                            Toast.makeText(WaitActivity.this, "데이터베이스 오류가 발생했습니다", Toast.LENGTH_SHORT).show();
                        }
                    });

                } else {
                    // 날짜 정보를 찾을 수 없는 경우
                    Toast.makeText(WaitActivity.this, "날짜 정보를 찾을 수 없습니다", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // 데이터베이스 오류 발생 시
                Toast.makeText(WaitActivity.this, "데이터베이스 오류가 발생했습니다", Toast.LENGTH_SHORT).show();
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

        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);

        alarmManager.setExact(AlarmManager.RTC, alarmTimeInMillis, pendingIntent);

        Toast.makeText(this, "알람 설정 완료", Toast.LENGTH_SHORT).show();
    }


    // 호스트 체크
    private void checkHost() {
        // 데이터베이스에서 호스트 선택 여부 확인
        mDatabase.child(hostCode).child("hostOuted").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    boolean isHostOuted = dataSnapshot.getValue(Boolean.class);
                    if (isHostOuted == true) {
                        isSelf = true;
                        Toast.makeText(WaitActivity.this, "집이 불타서 없어져 버렸습니다.", Toast.LENGTH_SHORT).show();
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        mDatabase.child(hostCode).child("hostOuted").setValue(false);
                        mDatabase.child(hostCode).child("hostSelected").setValue(false);
                        editor.putBoolean("isAlarmSet", false);
                        editor.apply();
                        EndAlarmActivity.cancelAlarm(WaitActivity.this);
                        Intent mainIntent = new Intent(WaitActivity.this, MainActivity.class);
                        startActivity(mainIntent);
                        finish();
//                        Intent intent = new Intent(RoomActivity.this, QuestionActivity.class);
//                        intent.putExtra("hostCode", hostCode);
//                        startActivity(intent);
                    }
                }
//                } else {
//                    // 호스트 정보를 찾을 수 없는 경우
//                    Toast.makeText(WaitActivity.this, "호스트 정보를 찾을 수 없습니다", Toast.LENGTH_SHORT).show();
//                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // 데이터베이스 오류 발생 시
                Toast.makeText(WaitActivity.this, "데이터베이스 오류가 발생했습니다", Toast.LENGTH_SHORT).show();
            }
        });
    }

}

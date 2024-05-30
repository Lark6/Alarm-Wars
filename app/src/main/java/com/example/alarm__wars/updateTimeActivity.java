package com.example.alarm__wars;
// MainActivity.java

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AlertDialog;


import com.example.alarm__wars.databinding.ActivityMakeRoomBinding;
import com.example.alarm__wars.databinding.ActivityUpdateTimeBinding;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Random;

public class updateTimeActivity extends AppCompatActivity {
    private DatabaseReference mDatabase;
    private  String hostCode;



    String[] amPm = {"AM", "PM"};
    String[] hour = {"1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12"};
    String[] minute = {"00", "01", "02", "03", "04", "05", "06", "07", "08", "09",
            "10", "11", "12", "13", "14", "15", "16", "17", "18", "19",
            "20", "21", "22", "23", "24", "25", "26", "27", "28", "29",
            "30", "31", "32", "33", "34", "35", "36", "37", "38", "39",
            "40", "41", "42", "43", "44", "45", "46", "47", "48", "49",
            "50", "51", "52", "53", "54", "55", "56", "57", "58", "59"};

    private String dayOfWeekToString(int dayOfWeek) {
        switch (dayOfWeek) {
            case Calendar.SUNDAY:
                return "일요일";
            case Calendar.MONDAY:
                return "월요일";
            case Calendar.TUESDAY:
                return "화요일";
            case Calendar.WEDNESDAY:
                return "수요일";
            case Calendar.THURSDAY:
                return "목요일";
            case Calendar.FRIDAY:
                return "금요일";
            case Calendar.SATURDAY:
                return "토요일";
            default:
                return "";
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityUpdateTimeBinding binding = ActivityUpdateTimeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mDatabase = FirebaseDatabase.getInstance().getReference().child("rooms");
        hostCode = getIntent().getStringExtra("hostCode");

        // 오전 오후 조정
        Spinner spinner1 = findViewById(R.id.spinner1);
        ArrayAdapter<String> adapter1 = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, amPm);
        adapter1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner1.setAdapter(adapter1);

        // 시간 조정
        Spinner spinner2 = findViewById(R.id.spinner2);
        ArrayAdapter<String> adapter2 = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, hour);
        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner2.setAdapter(adapter2);

        // 분 조정
        Spinner spinner3 = findViewById(R.id.spinner3);
        ArrayAdapter<String> adapter3 = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, minute);
        adapter3.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner3.setAdapter(adapter3);

        // Check if there is a pending alarm and navigate to WaitActivity if so
        SharedPreferences sharedPreferences = getSharedPreferences("AlarmPrefs", MODE_PRIVATE);
//        if (sharedPreferences.getBoolean("isAlarmSet", false)) {
//            Intent waitIntent = new Intent(MakeRoomActivity.this, WaitActivity.class);
//            waitIntent.putExtra("alarmTimeInMillis", sharedPreferences.getLong("alarmTimeInMillis", 0));
//            startActivity(waitIntent);
//            finish();
//        }

        binding.btnCancel.setOnClickListener(view -> finish());

        binding.btnSet.setOnClickListener(view -> {
            // 알람 설정 버튼 클릭 시 WaitActivity 시작
            Intent waitIntent = new Intent(updateTimeActivity.this, hostWaitActivity.class);
            // 현재 시간을 액션에 포함하여 고유한 값을 만듭니다.
//            long currentTime3 = System.currentTimeMillis();
//            String action = "com.example.alarm__wars.ACTION_ALARM_" + currentTime3;
//            waitIntent.setAction(action);

            int selectedHour = Integer.parseInt(hour[spinner2.getSelectedItemPosition()]);
            int selectedMinute = Integer.parseInt(minute[spinner3.getSelectedItemPosition()]);
            String selectedAmPm = amPm[spinner1.getSelectedItemPosition()];

            if (selectedAmPm.equals("PM") && selectedHour != 12) {
                selectedHour += 12;
            } else if (selectedAmPm.equals("AM") && selectedHour == 12) {
                selectedHour = 0;
            }

            // 선택된 요일 확인
            boolean[] selectedDays = {
                    binding.sunday.isChecked(),
                    binding.monday.isChecked(),
                    binding.tuesday.isChecked(),
                    binding.wednesday.isChecked(),
                    binding.thursday.isChecked(),
                    binding.friday.isChecked(),
                    binding.saturday.isChecked()
            };


            long alarmTimeInMillis = MakeRoomActivity.calculateAlarmTimeInMillis(selectedHour, selectedMinute, selectedDays);
            // 알람 설정
            setAlarm(alarmTimeInMillis);

//            waitIntent.putExtra("alarmTimeInMillis", alarmTimeInMillis);
            // Save alarm details to SharedPreferences
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean("isAlarmSet", true);
            editor.putLong("alarmTimeInMillis", alarmTimeInMillis);
            editor.apply();
            startActivity(waitIntent);
//            // boolean[]을 List<Boolean>으로 전환
            List<Boolean> selectedDaysList = new ArrayList<>();
            for (boolean day : selectedDays) {
                selectedDaysList.add(day);
            }

            int alarmTime = selectedHour * 100 + selectedMinute;

            mDatabase.child(hostCode).child("alarmTime").setValue(alarmTime);
            mDatabase.child(hostCode).child("dates").setValue(selectedDaysList);
            mDatabase.child(hostCode).child("timeChanged").setValue(true);



            finish();


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
}
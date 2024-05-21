package com.example.myapplication;

// MainActivity.java

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.example.myapplication.databinding.ActivityMainBinding;

import java.util.Calendar;

public class MainActivity extends AppCompatActivity {
    String[] amPm = {"AM", "PM"};
    String[] hour = {"1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12"};
    String[] minute = {"00", "01", "02", "03", "04", "05", "06", "07", "08", "09",
            "10", "11", "12", "13", "14", "15", "16", "17", "18", "19",
            "20", "21", "22", "23", "24", "25", "26", "27", "28", "29",
            "30", "31", "32", "33", "34", "35", "36", "37", "38", "39",
            "40", "41", "42", "43", "44", "45", "46", "47", "48", "49",
            "50", "51", "52", "53", "54", "55", "56", "57", "58", "59"};

    private int alarmNo = 0; // 초기 알람 번호를 0으로 설정

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
        ActivityMainBinding binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Spinner spinner1 = findViewById(R.id.spinner1);
        ArrayAdapter<String> adapter1 = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, amPm);
        adapter1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner1.setAdapter(adapter1);

        Spinner spinner2 = findViewById(R.id.spinner2);
        ArrayAdapter<String> adapter2 = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, hour);
        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner2.setAdapter(adapter2);

        Spinner spinner3 = findViewById(R.id.spinner3);
        ArrayAdapter<String> adapter3 = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, minute);
        adapter3.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner3.setAdapter(adapter3);

        binding.btnCancel.setOnClickListener(view -> finish());

        binding.btnMake.setOnClickListener(view -> {
            int selectedHour = Integer.parseInt(hour[spinner2.getSelectedItemPosition()]);
            int selectedMinute = Integer.parseInt(minute[spinner3.getSelectedItemPosition()]);

            if (amPm[spinner1.getSelectedItemPosition()].equals("PM")) {
                selectedHour += 12;
            }

            Calendar alarmTime = Calendar.getInstance();
            alarmTime.set(Calendar.HOUR_OF_DAY, selectedHour);
            alarmTime.set(Calendar.MINUTE, selectedMinute);
            alarmTime.set(Calendar.SECOND, 0);

            Calendar currentTime = Calendar.getInstance();
            long differenceInMillis = alarmTime.getTimeInMillis() - currentTime.getTimeInMillis();

            int selectedDay = 0;
            if (binding.monday.isChecked()) {
                selectedDay = Calendar.MONDAY;
            } else if (binding.tuesday.isChecked()) {
                selectedDay = Calendar.TUESDAY;
            } else if (binding.wednesday.isChecked()) {
                selectedDay = Calendar.WEDNESDAY;
            } else if (binding.thursday.isChecked()) {
                selectedDay = Calendar.THURSDAY;
            } else if (binding.friday.isChecked()) {
                selectedDay = Calendar.FRIDAY;
            } else if (binding.saturday.isChecked()) {
                selectedDay = Calendar.SATURDAY;
            } else if (binding.sunday.isChecked()) {
                selectedDay = Calendar.SUNDAY;
            }

            if (selectedDay != 0) {
                int currentDay = currentTime.get(Calendar.DAY_OF_WEEK);
                int daysUntilAlarm = selectedDay - currentDay;
                if (daysUntilAlarm < 0) {
                    daysUntilAlarm += 7;
                }
                differenceInMillis += daysUntilAlarm * 24 * 60 * 60 * 1000;
            }

            int hourDifference = (int) (differenceInMillis / (1000 * 60 * 60));
            int minuteDifference = (int) ((differenceInMillis / (1000 * 60)) % 60);
            String toastMessage = "알람이 " + hourDifference + "시간 " + minuteDifference + "분 후에 울립니다.";
            if (selectedDay != 0) {
                toastMessage += selectedDay == currentTime.get(Calendar.DAY_OF_WEEK) ? " (오늘)" : " (다음 주 " + dayOfWeekToString(selectedDay) + ")";
            }
            Toast.makeText(this, toastMessage, Toast.LENGTH_SHORT).show();

            setAlarm(alarmTime, selectedDay);
        });
    }

    @SuppressLint("ScheduleExactAlarm")
    private void setAlarm(Calendar alarmTime, int selectedDay) {
        this.alarmNo++;

        Intent intent = new Intent(this, AlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, this.alarmNo, intent, PendingIntent.FLAG_IMMUTABLE);

        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);

        // 요일을 고려하여 알람을 설정
        if (selectedDay != 0) {
            Calendar currentTime = Calendar.getInstance();
            int currentDay = currentTime.get(Calendar.DAY_OF_WEEK);

            // 선택된 요일이 현재 요일보다 미래의 요일이면 그 차이만큼 일 단위로 더하여 알람을 설정
            int daysUntilAlarm = selectedDay - currentDay;
            if (daysUntilAlarm < 0) {
                daysUntilAlarm += 7; // 이미 지난 요일이라면 다음 주로 설정
            }
            alarmTime.add(Calendar.DAY_OF_MONTH, daysUntilAlarm);
        }

        alarmManager.setExact(AlarmManager.RTC, alarmTime.getTimeInMillis(), pendingIntent);

        Toast.makeText(this, "알람 설정 완료", Toast.LENGTH_SHORT).show();
    }

}

package com.example.alarm__wars;

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
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.alarm__wars.databinding.ActivityMakeRoomBinding;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Random;

public class MakeRoomActivity extends AppCompatActivity {
    private DatabaseReference mDatabase;
    private String hostCode;

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
        ActivityMakeRoomBinding binding = ActivityMakeRoomBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Random random = new Random();
        int randomNum = 100000 + random.nextInt(900000); // 100000부터 999999 사이의 난수 생성
        hostCode = String.valueOf(randomNum); // 생성된 숫자를 문자열로 변환

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

        SharedPreferences sharedPreferences = getSharedPreferences("AlarmPrefs", MODE_PRIVATE);

        binding.btnCancel.setOnClickListener(view -> finish());

        binding.btnMake.setOnClickListener(view -> {
            if (!isAnyDaySelected(binding)) {
                Toast.makeText(MakeRoomActivity.this, "요일이 선택되지 않았습니다", Toast.LENGTH_SHORT).show();
                return;
            }

            AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
            if (!alarmManager.canScheduleExactAlarms()) {
                // 알람 설정 메서드 호출
                RoomActivity.requestExactAlarmPermission(this);
                return;
            }

            // 알람 설정 버튼 클릭 시 WaitActivity 시작
            Intent waitIntent = new Intent(MakeRoomActivity.this, HostClientWaitActivity.class);
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

            long alarmTimeInMillis = calculateAlarmTimeInMillis(selectedHour, selectedMinute, selectedDays);
            waitIntent.putExtra("alarmTimeInMillis", alarmTimeInMillis);

            // Save alarm details to SharedPreferences
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putLong("alarmTimeInMillis", alarmTimeInMillis);
            editor.putBoolean("isHost", true);
            editor.putString("hostCode", hostCode);
            editor.apply();

            startActivity(waitIntent);
            finish();

            List<Boolean> selectedDaysList = new ArrayList<>();
            for (boolean day : selectedDays) {
                selectedDaysList.add(day);
            }

            int alarmTime = selectedHour * 100 + selectedMinute;
            createRoom(alarmTime, selectedDaysList);
        });
    }

    private boolean isAnyDaySelected(ActivityMakeRoomBinding binding) {
        return binding.sunday.isChecked() || binding.monday.isChecked() || binding.tuesday.isChecked() ||
                binding.wednesday.isChecked() || binding.thursday.isChecked() || binding.friday.isChecked() ||
                binding.saturday.isChecked();
    }

    public static int calculateDaysUntilNextAlarm(boolean[] selectedDays, int currentDay, boolean isTodayAlarmValid) {
        int daysUntilNextAlarm = Integer.MAX_VALUE;

        for (int i = 0; i < selectedDays.length; i++) {
            if (selectedDays[i]) {
                int dayIndex = (i + 1) % 7; // 배열 인덱스를 Calendar 요일 값으로 변환 (1: 일요일, 2: 월요일, ... , 7: 토요일)
                int daysDifference = dayIndex - currentDay;
                if (daysDifference < 0) {
                    daysDifference += 7; // 과거 요일일 경우 다음 주 같은 요일로 설정
                }
                if (daysDifference == 0 && !isTodayAlarmValid) {
                    // 당일이지만 오늘의 알람 시간이 이미 지난 경우, 다음 주 같은 요일로 설정
                    daysDifference += 7;
                }
                if (daysDifference < daysUntilNextAlarm) {
                    daysUntilNextAlarm = daysDifference;
                }
            }
        }
        return daysUntilNextAlarm;
    }

    public static long calculateAlarmTimeInMillis(int selectedHour, int selectedMinute, boolean[] selectedDays) {
        Calendar alarmTime = Calendar.getInstance();
        alarmTime.set(Calendar.HOUR_OF_DAY, selectedHour);
        alarmTime.set(Calendar.MINUTE, selectedMinute);
        alarmTime.set(Calendar.SECOND, 0);

        Calendar currentTime = Calendar.getInstance();
        boolean isTodayAlarmValid = !alarmTime.before(currentTime);

        int currentDay = currentTime.get(Calendar.DAY_OF_WEEK);
        int daysUntilNextAlarm = calculateDaysUntilNextAlarm(selectedDays, currentDay, isTodayAlarmValid);

        alarmTime.add(Calendar.DAY_OF_MONTH, daysUntilNextAlarm);

        return alarmTime.getTimeInMillis();
    }

    @SuppressLint("ScheduleExactAlarm")
    private void setAlarm(long alarmTimeInMillis) {
        Intent intent = new Intent(this, AlarmReceiver.class);
        intent.putExtra("hostCode", hostCode);

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

    private void createRoom(int alarmTime, List<Boolean> selectedDaysList) {
        mDatabase = FirebaseDatabase.getInstance().getReference().child("rooms");

        // 호스트 코드를 데이터베이스에 저장
        Room room = new Room("Q", "A", false, alarmTime, false, false, selectedDaysList, false, false, false);
        mDatabase.child(String.valueOf(hostCode)).setValue(room, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError error, @NonNull DatabaseReference ref) {
                if (error == null) {
                    // 팝업 창으로 호스트 코드 보여주기
                    AlertDialog.Builder builder = new AlertDialog.Builder(MakeRoomActivity.this);
                    builder.setTitle("방이 생성되었습니다")
                            .setMessage("호스트 코드: " + hostCode)
                            .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog1, int which) {
                                    dialog1.dismiss();
                                }
                            });
                    AlertDialog dialog = builder.create();
                    dialog.show();
                } else {
                    Toast.makeText(MakeRoomActivity.this, "방 생성에 실패했습니다", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}

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
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Random;

public class MakeRoomActivity extends AppCompatActivity {
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

        // Check if there is a pending alarm and navigate to WaitActivity if so
        SharedPreferences sharedPreferences = getSharedPreferences("AlarmPrefs", MODE_PRIVATE);
//        if (sharedPreferences.getBoolean("isAlarmSet", false)) {
//            Intent waitIntent = new Intent(MakeRoomActivity.this, WaitActivity.class);
//            waitIntent.putExtra("alarmTimeInMillis", sharedPreferences.getLong("alarmTimeInMillis", 0));
//            startActivity(waitIntent);
//            finish();
//        }

        binding.btnCancel.setOnClickListener(view -> finish());

        binding.btnMake.setOnClickListener(view -> {
            AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
            if (!alarmManager.canScheduleExactAlarms()) {
                // 알람 설정 메서드 호출
                RoomActivity.requestExactAlarmPermission(this);
                return;
            }

            // 알람 설정 버튼 클릭 시 WaitActivity 시작
            Intent waitIntent = new Intent(MakeRoomActivity.this, hostWaitActivity.class);
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

//            int selectedDay = getSelectedDay(binding);



            long alarmTimeInMillis = calculateAlarmTimeInMillis(selectedHour, selectedMinute, selectedDays);
            // 알람 설정
            setAlarm(alarmTimeInMillis);

            waitIntent.putExtra("alarmTimeInMillis", alarmTimeInMillis);
//            waitIntent.putExtra("hostCode", hostCode);
            // Save alarm details to SharedPreferences
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean("isAlarmSet", true);
            editor.putLong("alarmTimeInMillis", alarmTimeInMillis);
            editor.putBoolean("isHost", true);
            editor.putString("hostCode", hostCode);
            editor.apply();


            startActivity(waitIntent);
            finish();



//            for (int i = 0; i < selectedDays.length; i++) {
//                if (selectedDays[i]) {
//                    setAlarm(alarmTime, i + 1); // Calendar.DAY_OF_WEEK는 1부터 시작
//                }
//            }

//            // boolean[]을 List<Boolean>으로 전환
            List<Boolean> selectedDaysList = new ArrayList<>();
            for (boolean day : selectedDays) {
                selectedDaysList.add(day);
            }

            int alarmTime = selectedHour * 100 + selectedMinute;
            createRoom(alarmTime, selectedDaysList);

//            Calendar alarmTime = Calendar.getInstance();
//            alarmTime.set(Calendar.HOUR_OF_DAY, selectedHour);
//            alarmTime.set(Calendar.MINUTE, selectedMinute);
//            alarmTime.set(Calendar.SECOND, 0);
//
//            Calendar currentTime = Calendar.getInstance();
//            long differenceInMillis = alarmTime.getTimeInMillis() - currentTime.getTimeInMillis();

//            int selectedDay = 0;
//            if (binding.monday.isChecked()) {
//                selectedDay = Calendar.MONDAY;
//            } else if (binding.tuesday.isChecked()) {
//                selectedDay = Calendar.TUESDAY;
//            } else if (binding.wednesday.isChecked()) {
//                selectedDay = Calendar.WEDNESDAY;
//            } else if (binding.thursday.isChecked()) {
//                selectedDay = Calendar.THURSDAY;
//            } else if (binding.friday.isChecked()) {
//                selectedDay = Calendar.FRIDAY;
//            } else if (binding.saturday.isChecked()) {
//                selectedDay = Calendar.SATURDAY;
//            } else if (binding.sunday.isChecked()) {
//                selectedDay = Calendar.SUNDAY;
//            }


//            if (selectedDay != 0) {
//                int currentDay = currentTime.get(Calendar.DAY_OF_WEEK);
//                int daysUntilAlarm = selectedDay - currentDay;
//                if (daysUntilAlarm < 0) {
//                    daysUntilAlarm += 7;
//                }
//                differenceInMillis += daysUntilAlarm * 24 * 60 * 60 * 1000;
//            }

//            int hourDifference = (int) (differenceInMillis / (1000 * 60 * 60));
//            int minuteDifference = (int) ((differenceInMillis / (1000 * 60)) % 60);
//            String toastMessage = "알람이 " + hourDifference + "시간 " + minuteDifference + "분 후에 울립니다.";
//            if (selectedDay != 0) {
//                toastMessage += selectedDay == currentTime.get(Calendar.DAY_OF_WEEK) ? " (오늘)" : " (다음 주 " + dayOfWeekToString(selectedDay) + ")";
//            }
//            Toast.makeText(this, toastMessage, Toast.LENGTH_SHORT).show();
//
//            setAlarm(alarmTime, selectedDay);



        });
    }

//    @SuppressLint("ScheduleExactAlarm")
//    private void setAlarm(Calendar alarmTime, int selectedDay) {
//        this.alarmNo++;
//
//        Intent intent = new Intent(this, AlarmReceiver.class);
//        System.out.println("MakeRoom hostCode :" + hostCode);
//        intent.putExtra("hostCode", hostCode);
//
//        // 현재 시간을 액션에 포함하여 고유한 값을 만듭니다.
//        long currentTime1 = System.currentTimeMillis();
//        String action = "com.example.alarm__wars.ACTION_ALARM_" + currentTime1;
//        intent.setAction(action);
//
//        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, this.alarmNo, intent, PendingIntent.FLAG_IMMUTABLE);
//
//        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
//
//        // 요일을 고려하여 알람을 설정
//        if (selectedDay != 0) {
//            Calendar currentTime = Calendar.getInstance();
//            int currentDay = currentTime.get(Calendar.DAY_OF_WEEK);
//
//            // 선택된 요일이 현재 요일보다 미래의 요일이면 그 차이만큼 일 단위로 더하여 알람을 설정
//            int daysUntilAlarm = selectedDay - currentDay;
//            if (daysUntilAlarm < 0) {
//                daysUntilAlarm += 7; // 이미 지난 요일이라면 다음 주로 설정
//            }
//            alarmTime.add(Calendar.DAY_OF_MONTH, daysUntilAlarm);
//        }
//
//        alarmManager.setExact(AlarmManager.RTC, alarmTime.getTimeInMillis(), pendingIntent);
//
//        Toast.makeText(this, "알람 설정 완료", Toast.LENGTH_SHORT).show();
//    }

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

//    private int getSelectedDay(ActivityMakeRoomBinding binding) {
//        if (binding.monday.isChecked()) {
//            return Calendar.MONDAY;
//        } else if (binding.tuesday.isChecked()) {
//            return Calendar.TUESDAY;
//        } else if (binding.wednesday.isChecked()) {
//            return Calendar.WEDNESDAY;
//        } else if (binding.thursday.isChecked()) {
//            return Calendar.THURSDAY;
//        } else if (binding.friday.isChecked()) {
//            return Calendar.FRIDAY;
//        } else if (binding.saturday.isChecked()) {
//            return Calendar.SATURDAY;
//        } else if (binding.sunday.isChecked()) {
//            return Calendar.SUNDAY;
//        }
//        return 0;
//    }

    private void createRoom(int alarmTime, List<Boolean> selectedDaysList ) {

        mDatabase = FirebaseDatabase.getInstance().getReference().child("rooms");

        // 호스트 코드를 데이터베이스에 저장
        Room room = new Room("Q", "A", false, alarmTime, false, false, selectedDaysList, false);
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
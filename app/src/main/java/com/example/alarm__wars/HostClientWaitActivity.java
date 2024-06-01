package com.example.alarm__wars;

import android.annotation.SuppressLint;
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

                // 방 삭제
                deleteRoom(hostCode);
                // 현재 액티비티 종료 (이전 액티비티로 돌아가기)
                finish();
            }
        });

        mDatabase.child(hostCode).child("hostSelected").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Boolean hostSelected = dataSnapshot.getValue(Boolean.class);
                if (hostSelected != null && hostSelected) {
                    long alarmTimeInMillis = sharedPreferences.getLong("alarmTimeInMillis", 0);
                    boolean isAlarmSet = sharedPreferences.getBoolean("isAlarmSet", false);
                    if (!isAlarmSet) {
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
                Log.e("Firebase", "Failed to read value.", databaseError.toException());
            }
        });
    }

    private void deleteRoom(String hostCode) {
        DatabaseReference roomRef = mDatabase.child(hostCode);
        roomRef.removeValue().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(this, "방이 성공적으로 삭제되었습니다.", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "방 삭제에 실패하였습니다.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @SuppressLint("ScheduleExactAlarm")
    private void setAlarm(long alarmTimeInMillis) {
        Intent intent = new Intent(this, AlarmReceiver.class);
        intent.putExtra("hostCode", hostCode);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        alarmManager.setExact(AlarmManager.RTC, alarmTimeInMillis, pendingIntent);
        Toast.makeText(this, "알람 설정 완료", Toast.LENGTH_SHORT).show();
    }
}

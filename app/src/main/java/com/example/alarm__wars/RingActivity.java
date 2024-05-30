package com.example.alarm__wars;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class RingActivity extends AppCompatActivity {
    public static MediaPlayer mediaPlayer;
    private DatabaseReference mDatabase;
    private String hostCode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ring);

        // 알람음 재생
        this.mediaPlayer = MediaPlayer.create(this, R.raw.alarm);
        this.mediaPlayer.setLooping(true); // 반복 재생
        this.mediaPlayer.start();



        mDatabase = FirebaseDatabase.getInstance().getReference().child("rooms");
        hostCode = getIntent().getStringExtra("hostCode");
        System.out.println(hostCode);

        ImageButton stopAlarmButton = findViewById(R.id.stop_alarm_button);

        stopAlarmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                mediaPlayer.stop();
                checkButtonAndProceed();
            }
        });
    }
    private void checkButtonAndProceed() {
        // 데이터베이스에서 호스트 선택 여부 확인
        mDatabase.child(hostCode).child("buttonPressed").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    boolean isButtonPressed = dataSnapshot.getValue(Boolean.class);
                    if (!isButtonPressed) {
                        // 버튼을 먼저 누른 경우
                        // 출제 액티비티로 이동하기 전에 button state 값을 참으로 변경
                        mDatabase.child(hostCode).child("buttonPressed").setValue(true);
                        Intent intent = new Intent(RingActivity.this, MakeQuestionActivity.class);
                        intent.putExtra("hostCode", hostCode);
                        startActivity(intent);
                        finish();
                    } else {
                        // 버튼을 늦게 누른 경우
                        Intent intent = new Intent(RingActivity.this, WaitingQuestionActivity.class);
                        intent.putExtra("hostCode", hostCode);
                        startActivity(intent);
                        finish();
                    }
                } else {
                    // 호스트 정보를 찾을 수 없는 경우
                    Toast.makeText(RingActivity.this, "버튼 상태 정보를 찾을 수 없습니다", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // 데이터베이스 오류 발생 시
                Toast.makeText(RingActivity.this, "데이터베이스 오류가 발생했습니다", Toast.LENGTH_SHORT).show();
            }
        });
    }

}
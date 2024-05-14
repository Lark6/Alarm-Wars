package com.example.alarm__wars;

import android.content.Intent;
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

public class RoomActivity extends AppCompatActivity {

    private DatabaseReference mDatabase;
    private String hostCode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_room);

        mDatabase = FirebaseDatabase.getInstance().getReference().child("rooms");
        hostCode = getIntent().getStringExtra("hostCode");

        Button questionButton = findViewById(R.id.question_button);
        Button answerButton = findViewById(R.id.answer_button);

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
    }
    private void checkHostSelectionAndProceed() {
        // 데이터베이스에서 호스트 선택 여부 확인
        mDatabase.child(hostCode).child("hostSelected").addListenerForSingleValueEvent(new ValueEventListener() {
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


}

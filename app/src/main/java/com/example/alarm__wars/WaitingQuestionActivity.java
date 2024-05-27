package com.example.alarm__wars;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class WaitingQuestionActivity extends AppCompatActivity {

    private DatabaseReference mDatabase;
    private String hostCode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mDatabase = FirebaseDatabase.getInstance().getReference().child("rooms");
        hostCode = getIntent().getStringExtra("hostCode");
        setContentView(R.layout.activity_waiting_question);

        // 데이터 변경 감지 리스너 등록
        mDatabase.child(hostCode).child("questionSubmitted").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                // 데이터가 변경될 때마다 호출됨
                boolean QuestionReady = dataSnapshot.getValue(boolean.class);
                if(QuestionReady){
                    Intent intent = new Intent(WaitingQuestionActivity.this, SolveQuestionActivity.class);
                    intent.putExtra("hostCode", hostCode);
                    startActivity(intent);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // 데이터베이스 오류 발생 시 호출됨
                Log.e("Firebase", "Failed to read value.", databaseError.toException());
            }
        });
    }
}
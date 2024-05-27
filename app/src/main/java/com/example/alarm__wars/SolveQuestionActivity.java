package com.example.alarm__wars;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class SolveQuestionActivity extends AppCompatActivity {

    private DatabaseReference mDatabase;
    private String hostCode;

    private EditText userAnswer;
    private EditText questionView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mDatabase = FirebaseDatabase.getInstance().getReference().child("rooms");
        hostCode = getIntent().getStringExtra("hostCode");
        setContentView(R.layout.activity_solve_question);
        showQuestion();

        Button submitAnswerButton = findViewById(R.id.submit_answer);
        userAnswer = findViewById(R.id.user_answer);
        questionView = findViewById(R.id.question_view);

        submitAnswerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkAnswerAndProceed();
            }
        });
    }
    private void checkAnswerAndProceed() {
        // 데이터베이스에서 호스트 선택 여부 확인
        mDatabase.child(hostCode).child("answer").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String answer = dataSnapshot.getValue(String.class);
                    String finalAnswer = userAnswer.getText().toString().trim();
                    if (answer.equals(finalAnswer)) {

                        // 변수 초기화
                        mDatabase.child(hostCode).child("answer").setValue("A");
                        mDatabase.child(hostCode).child("buttonPressed").setValue(false);
                        mDatabase.child(hostCode).child("questionSubmited").setValue(false);
                        mDatabase.child(hostCode).child("question").setValue("Q");

                        Intent intent = new Intent(SolveQuestionActivity.this, EndAlarmActivity.class);
                        intent.putExtra("hostCode", hostCode);
                        startActivity(intent);
                        finish();
                    } else {
                        // 호스트가 이미 선택된 경우 토스트 메시지 표시
                        Toast.makeText(SolveQuestionActivity.this, "답이 틀렸어요", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    // 호스트 정보를 찾을 수 없는 경우
                    Toast.makeText(SolveQuestionActivity.this, "호스트 정보를 찾을 수 없습니다", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // 데이터베이스 오류 발생 시
                Toast.makeText(SolveQuestionActivity.this, "데이터베이스 오류가 발생했습니다", Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void showQuestion() {
        // 데이터베이스에서 호스트 선택 여부 확인
        mDatabase.child(hostCode).child("question").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String question = dataSnapshot.getValue(String.class);
                    questionView.setText(question);
                } else {
                    // 문제 정보를 찾을 수 없는 경우
                    Toast.makeText(SolveQuestionActivity.this, "문제 정보를 찾을 수 없습니다", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // 데이터베이스 오류 발생 시
                Toast.makeText(SolveQuestionActivity.this, "데이터베이스 오류가 발생했습니다", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
package com.example.alarm__wars;

// QuestionActivity.java
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class QuestionActivity extends AppCompatActivity {

    private DatabaseReference mDatabase;
    private String hostCode;

    private EditText editAnswer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_question);

        // Firebase 데이터베이스 초기화
        mDatabase = FirebaseDatabase.getInstance().getReference().child("rooms");

        // 호스트 코드 가져오기
        hostCode = getIntent().getStringExtra("hostCode");

        // UI 요소 초기화
        editAnswer = findViewById(R.id.edit_answer);
        Button submitButton = findViewById(R.id.button_submit);

        // 제출 버튼 클릭 리스너 설정
        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                submitAnswer();
            }
        });
    }

    private void submitAnswer() {
        // 사용자가 입력한 답 가져오기
        String answer = editAnswer.getText().toString().trim();

        // 답이 비어있는지 확인
        if (answer.isEmpty()) {
            Toast.makeText(this, "답을 입력하세요", Toast.LENGTH_SHORT).show();
            return;
        }

        // Firebase 데이터베이스에 정답 저장
        mDatabase.child(hostCode).child("answer").setValue(answer, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(@NonNull DatabaseError error, @NonNull DatabaseReference ref) {
                if (error == null) {
                    Toast.makeText(QuestionActivity.this, "정답이 제출되었습니다", Toast.LENGTH_SHORT).show();
                    finish(); // 액티비티 종료
                } else {
                    Toast.makeText(QuestionActivity.this, "정답 제출에 실패했습니다", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}

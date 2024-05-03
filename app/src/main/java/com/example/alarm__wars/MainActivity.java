package com.example.alarm__wars;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

public class MainActivity extends AppCompatActivity {

    private DatabaseReference mDatabase;
    private TextView textView;
    private Button button;
    private Button resetButton;
    private boolean isAttacker = false; // 공격자 여부를 나타내는 변수
    private EditText operand1EditText, operatorEditText, operand2EditText;
    private Button submitButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mDatabase = FirebaseDatabase.getInstance().getReference().child("game");
        textView = findViewById(R.id.textView);
        button = findViewById(R.id.button);
        resetButton = findViewById(R.id.resetButton);
        operand1EditText = findViewById(R.id.operand1EditText);
        operatorEditText = findViewById(R.id.operatorEditText);
        operand2EditText = findViewById(R.id.operand2EditText);
        submitButton = findViewById(R.id.submitButton);

        // 버튼 클릭 시 데이터베이스에 현재 시간을 저장하고, 공격자 여부를 결정함
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDatabase.child("timestamp").setValue(ServerValue.TIMESTAMP);
                mDatabase.child("isAttacker").setValue(isAttacker);
                // 공격자인 경우 문제 출제
                if (isAttacker) {
                    submitQuestion();
                }
            }
        });

        // 초기화 버튼 클릭 시 데이터베이스 초기화 및 텍스트뷰 초기화
        resetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDatabase.removeValue();
                textView.setText("");
            }
        });

        // 제출 버튼 클릭 시 문제 제출
        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                submitQuestion();
            }
        });

        // 데이터베이스 변경 사항을 감지하여 화면 업데이트
        mDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Long timestamp = dataSnapshot.child("timestamp").getValue(Long.class);
                Boolean attacker = dataSnapshot.child("isAttacker").getValue(Boolean.class);

                if (timestamp != null && attacker != null) {
                    // 현재 시간과 공격자 여부를 가져옴
                    if (timestamp.equals(dataSnapshot.child("timestamp").getValue())) {
                        // 가장 먼저 버튼을 누른 경우 공격자로 설정
                        isAttacker = attacker;
                    } else {
                        // 가장 먼저 버튼을 누른 사람이 공격자로 설정됨
                        isAttacker = !attacker;
                    }

                    // 공격자인지 여부에 따라 텍스트뷰 업데이트
                    if (isAttacker) {
                        textView.setText("공격자입니다");
                    } else {
                        textView.setText("수비자입니다");
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // 에러 처리
            }
        });
    }

    private void submitQuestion() {
        if (!isAttacker) return; // 수비자는 문제를 출제할 필요 없음
        int operand1 = Integer.parseInt(operand1EditText.getText().toString());
        String operator = operatorEditText.getText().toString();
        int operand2 = Integer.parseInt(operand2EditText.getText().toString());

        String question = operand1 + " " + operator + " " + operand2;
        mDatabase.child("question").setValue(question);
    }
}

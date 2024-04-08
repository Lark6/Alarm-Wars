package com.example.alarm__wars;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mDatabase = FirebaseDatabase.getInstance().getReference().child("game");
        textView = findViewById(R.id.textView);
        button = findViewById(R.id.button);
        resetButton = findViewById(R.id.resetButton);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDatabase.setValue(ServerValue.TIMESTAMP);
            }
        });

        resetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 초기화 버튼을 누르면 "game" 경로와 "status" 경로를 0으로 설정
                mDatabase.getParent().child("status").setValue(0);
                mDatabase.setValue(null);
                textView.setText("");
            }
        });

        mDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Long timestamp = dataSnapshot.getValue(Long.class);
                if (timestamp != null) {
                    mDatabase.getParent().child("status").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot statusSnapshot) {
                            Integer status = statusSnapshot.getValue(Integer.class);
                            if (status == null || status == 0) {
                                // 현재 상태가 0이면 공격자로 설정하고 상태를 1로 변경
                                textView.setText("공격자입니다");
                                mDatabase.getParent().child("status").setValue(1);
                            } else if (status == 1) {
                                // 현재 상태가 1이면 수비자로 설정
                                textView.setText("수비자입니다");
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                            Log.e("Firebase", "Failed to read value.", databaseError.toException());
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("Firebase", "Failed to read value.", databaseError.toException());
            }
        });
    }
}

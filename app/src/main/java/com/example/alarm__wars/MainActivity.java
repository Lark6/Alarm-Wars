package com.example.alarm__wars;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        EditText passwordEditText = findViewById(R.id.et_password);
        ImageButton togglePasswordVisibilityButton = findViewById(R.id.btn_toggle_password);

        // 비밀번호 가시성 토글 버튼 설정
        togglePasswordVisibilityButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        // 사용자가 버튼을 누르고 있을 때 비밀번호 표시
                        passwordEditText.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                        passwordEditText.setSelection(passwordEditText.getText().length());
                        togglePasswordVisibilityButton.setImageResource(R.drawable.ic_eye);
                        return true;
                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL:
                        // 사용자가 버튼에서 손을 뗐을 때 비밀번호 숨기기
                        passwordEditText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                        passwordEditText.setSelection(passwordEditText.getText().length());
                        togglePasswordVisibilityButton.setImageResource(R.drawable.ic_eye_off);
                        return true;
                }
                return false;
            }
        });

        // 로그인, ID/PW 찾기, 회원가입 버튼 리스너 설정
        Button btnLogin = findViewById(R.id.btnLogin);
        btnLogin.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, LoginCompleteActivity.class)));

        TextView btnFindIdPw = findViewById(R.id.btnFindIdPw);
        btnFindIdPw.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, FindIdPwActivity.class)));

        TextView btnSignUp = findViewById(R.id.btnSignUp);
        btnSignUp.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, SignUpActivity.class)));
    }
}

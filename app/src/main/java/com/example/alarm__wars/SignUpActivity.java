package com.example.alarm__wars;

import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.Toast;
import android.text.InputType;

import androidx.appcompat.app.AppCompatActivity;

public class SignUpActivity extends AppCompatActivity {

    private EditText nameEditText, idEditText, passwordEditText, phoneNumberEditText, verificationCodeEditText;
    private Spinner phoneSpinner;
    private Button signUpButton, sendVerificationButton, verifyButton, resendVerificationButton;
    private ImageButton togglePasswordVisibilityButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        // 뷰 바인딩
        nameEditText = findViewById(R.id.et_name);
        idEditText = findViewById(R.id.et_id);
        passwordEditText = findViewById(R.id.et_password);
        phoneNumberEditText = findViewById(R.id.phone_number);
        verificationCodeEditText = findViewById(R.id.et_verification_code);
        phoneSpinner = findViewById(R.id.phone_spinner);
        signUpButton = findViewById(R.id.btn_sign_up);
        sendVerificationButton = findViewById(R.id.btn_send_verification);
        verifyButton = findViewById(R.id.btn_verify);
        resendVerificationButton = findViewById(R.id.btn_resend_verification);


        // 스피너 초기화
        initializeSpinner();

        passwordEditText = findViewById(R.id.et_password);
        togglePasswordVisibilityButton = findViewById(R.id.btn_toggle_password);

        // 비밀번호 가시성 토글
        togglePasswordVisibilityButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        // 사용자가 버튼을 누르고 있을 때
                        passwordEditText.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                        passwordEditText.setSelection(passwordEditText.getText().length());
                        togglePasswordVisibilityButton.setImageResource(R.drawable.ic_eye);
                        return true;
                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL:
                        // 사용자가 버튼에서 손을 뗐을 때
                        passwordEditText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                        passwordEditText.setSelection(passwordEditText.getText().length());
                        togglePasswordVisibilityButton.setImageResource(R.drawable.ic_eye_off);
                        return true;
                }
                return false;
            }
        });

        // 인증번호 전송
        sendVerificationButton.setOnClickListener(v -> sendVerificationCode(phoneNumberEditText.getText().toString()));

        // 인증번호 재전송
        resendVerificationButton.setOnClickListener(v -> sendVerificationCode(phoneNumberEditText.getText().toString()));

        // 인증번호 검증
        verifyButton.setOnClickListener(v -> verifyCode(verificationCodeEditText.getText().toString()));

        // 회원가입 버튼 리스너 설정
        signUpButton.setOnClickListener(v -> registerUser());
    }

    private void initializeSpinner() {
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.phone_prefix, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        phoneSpinner.setAdapter(adapter);
        phoneSpinner.setSelection(0);  // 기본 선택 항목 설정
    }

    private void sendVerificationCode(String phoneNumber) {
        // 인증번호 전송 로직 구현 (여기서는 예시로 로컬에서 처리)
        Toast.makeText(this, "인증번호가 전송되었습니다: " + phoneNumber, Toast.LENGTH_SHORT).show();
    }

    private void verifyCode(String code) {
        // 인증번호 검증 로직 구현 (여기서는 예시로 로컬에서 처리)
        if (code.equals("123456")) {  // 예시 코드
            Toast.makeText(this, "인증 성공!", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "인증 실패, 다시 시도하세요.", Toast.LENGTH_SHORT).show();
        }
    }

    private void registerUser() {
        String name = nameEditText.getText().toString().trim();
        String id = idEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();

        if (name.isEmpty() || id.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "모든 필드를 채워주세요!", Toast.LENGTH_SHORT).show();
            return;
        }

        // 여기에 회원가입 처리 로직 구현
        Toast.makeText(this, "회원가입 성공!", Toast.LENGTH_SHORT).show();
    }
}

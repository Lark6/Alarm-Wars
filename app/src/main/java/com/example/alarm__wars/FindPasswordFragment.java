package com.example.alarm__wars;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.FirebaseException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.concurrent.TimeUnit;

public class FindPasswordFragment extends Fragment {

    private EditText editTextEmail, editTextPhoneNumber, editTextVerificationCode;
    private Button buttonSendVerification, buttonVerify, buttonResetPassword;
    private String verificationId;
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_pw, container, false);

        editTextEmail = view.findViewById(R.id.registered_id);
        editTextPhoneNumber = view.findViewById(R.id.phone_number);
        editTextVerificationCode = view.findViewById(R.id.et_verification_code);
        buttonSendVerification = view.findViewById(R.id.btn_send_verification);
        buttonVerify = view.findViewById(R.id.btn_verify);

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        buttonSendVerification.setOnClickListener(v -> sendVerificationCode());
        buttonVerify.setOnClickListener(v -> verifyCode());

        return view;
    }

    private void sendVerificationCode() {
        String phoneNumber = editTextPhoneNumber.getText().toString().trim();
        if (phoneNumber.startsWith("010")) {
            phoneNumber = "+82" + phoneNumber.substring(1);
        }
        if (TextUtils.isEmpty(phoneNumber) || !phoneNumber.matches("^\\+8210[0-9]{8}$")) {
            editTextPhoneNumber.setError("올바른 전화번호 형식이 아닙니다 (+8210XXXXXXXX)");
            editTextPhoneNumber.requestFocus();
            return;
        }

        PhoneAuthOptions options = PhoneAuthOptions.newBuilder(mAuth)
                .setPhoneNumber(phoneNumber)
                .setTimeout(60L, TimeUnit.SECONDS)
                .setActivity(getActivity())
                .setCallbacks(mCallbacks)
                .build();
        PhoneAuthProvider.verifyPhoneNumber(options);
    }

    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
        @Override
        public void onVerificationCompleted(@NonNull PhoneAuthCredential credential) {
            editTextVerificationCode.setText(credential.getSmsCode());
            verifyCode();
        }

        @Override
        public void onVerificationFailed(@NonNull FirebaseException e) {
            Toast.makeText(getActivity(), "인증 실패: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }

        @Override
        public void onCodeSent(@NonNull String tempVerificationId, @NonNull PhoneAuthProvider.ForceResendingToken token) {
            verificationId = tempVerificationId;
            Toast.makeText(getActivity(), "인증 코드 전송됨", Toast.LENGTH_SHORT).show();
        }
    };

    private void verifyCode() {
        String code = editTextVerificationCode.getText().toString().trim();
        if (TextUtils.isEmpty(code)) {
            editTextVerificationCode.setError("인증번호를 입력하세요");
            return;
        }

        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, code);
        signInWithPhoneAuthCredential(credential);
    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(getActivity(), task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(getActivity(), "전화번호 인증 성공", Toast.LENGTH_LONG).show();
                        resetPassword();
                    } else {
                        Toast.makeText(getActivity(), "인증 실패: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void resetPassword() {
        String email = editTextEmail.getText().toString().trim();
        if (TextUtils.isEmpty(email)) {
            editTextEmail.setError("이메일을 입력하세요");
            return;
        }

        sendPasswordResetEmail(email);
    }

    private void sendPasswordResetEmail(String email) {
        mAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(getActivity(), "비밀번호 재설정 링크가 이메일로 발송되었습니다.", Toast.LENGTH_LONG).show();
                        navigateToLoginActivity();
                    } else {
                        Toast.makeText(getActivity(), "비밀번호 재설정 링크 발송 실패: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }
    private void navigateToLoginActivity() {
        Intent intent = new Intent(getActivity(), LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }
}

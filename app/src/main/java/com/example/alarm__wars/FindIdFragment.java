package com.example.alarm__wars;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.google.firebase.FirebaseException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.functions.FirebaseFunctions;
import com.google.firebase.functions.HttpsCallableResult;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class FindIdFragment extends Fragment {

    private static final String TAG = "FindIdFragment";

    private EditText editTextName, editTextPhoneNumber, editTextVerificationCode;
    private Button buttonSendVerification, buttonVerify, buttonResendVerification, buttonNext;
    private String verificationId;
    private FirebaseAuth mAuth;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_id, container, false);

        editTextName = view.findViewById(R.id.name);
        editTextPhoneNumber = view.findViewById(R.id.phone_number);
        editTextVerificationCode = view.findViewById(R.id.et_verification_code);
        buttonSendVerification = view.findViewById(R.id.btn_send_verification);
        buttonVerify = view.findViewById(R.id.btn_verify);
        buttonResendVerification = view.findViewById(R.id.btn_resend_verification);
        buttonNext = view.findViewById(R.id.btn_next);

        mAuth = FirebaseAuth.getInstance();

        buttonSendVerification.setOnClickListener(v -> sendVerificationCode());
        buttonVerify.setOnClickListener(v -> verifyCode());
        buttonResendVerification.setOnClickListener(v -> resendVerificationCode());
        buttonNext.setOnClickListener(v -> findEmailByPhoneNumber());

        return view;
    }

    private void sendVerificationCode() {
        String phoneNumber = editTextPhoneNumber.getText().toString().trim();
        if (TextUtils.isEmpty(phoneNumber) || !phoneNumber.matches("^[0-9]{11}$")) {
            editTextPhoneNumber.setError("올바른 전화번호 형식이 아닙니다 (11자리 숫자 입력)");
            editTextPhoneNumber.requestFocus();
            return;
        }

        String formattedPhoneNumber = "+82" + phoneNumber.substring(1);
        PhoneAuthOptions options = PhoneAuthOptions.newBuilder(mAuth)
                .setPhoneNumber(formattedPhoneNumber)
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
                        buttonNext.setEnabled(true);
                    } else {
                        Toast.makeText(getActivity(), "인증 실패: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void resendVerificationCode() {
        sendVerificationCode();
    }

    private void findEmailByPhoneNumber() {
        String phoneNumber = editTextPhoneNumber.getText().toString().trim();
        String enteredName = editTextName.getText().toString().trim();

        if (TextUtils.isEmpty(phoneNumber) || !phoneNumber.matches("^[0-9]{11}$")) {
            editTextPhoneNumber.setError("올바른 전화번호 형식이 아닙니다 (11자리 숫자 입력)");
            editTextPhoneNumber.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(enteredName)) {
            editTextName.setError("이름을 입력하세요");
            editTextName.requestFocus();
            return;
        }

        String formattedPhoneNumber = "+82" + phoneNumber.substring(1);
        callFirebaseFunction(formattedPhoneNumber, enteredName);
    }

    private void callFirebaseFunction(String phoneNumber, String name) {
        FirebaseFunctions functions = FirebaseFunctions.getInstance();

        Map<String, Object> data = new HashMap<>();
        data.put("phoneNumber", phoneNumber);
        data.put("username", name);

        functions
                .getHttpsCallable("findUserByEmail")
                .call(data)
                .addOnSuccessListener(result -> {
                    String email = (String) result.getData();
                    if (email != null) {
                        showEmailDialog(email);
                    } else {
                        Toast.makeText(getActivity(), "해당 전화번호와 이름으로 등록된 이메일이 없습니다.", Toast.LENGTH_LONG).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getActivity(), "에러: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    private void showEmailDialog(String email) {
        new AlertDialog.Builder(getActivity())
                .setTitle("등록된 이메일")
                .setMessage("이메일: " + email)
                .setPositiveButton("확인", (dialog, which) -> {
                    startActivity(new Intent(getActivity(), LoginActivity.class));
                })
                .show();
    }
}

package com.example.alarm__wars;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

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

        mAuth = FirebaseAuth.getInstance();

        buttonSendVerification.setOnClickListener(v -> sendVerificationCode());
        buttonVerify.setOnClickListener(v -> verifyCode());
        buttonResendVerification.setOnClickListener(v -> resendVerificationCode());
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
            Toast.makeText(getActivity(), "인증 실패: " + e.getMessage(), Toast.LENGTH_SHORT).show();
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
                        findEmailByPhoneNumber();
                    } else {
                        Toast.makeText(getActivity(), "인증 실패: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void resendVerificationCode() {
        sendVerificationCode();
    }

    private void findEmailByPhoneNumber() {
        String phoneNumber = editTextPhoneNumber.getText().toString().trim();
        String enteredName = editTextName.getText().toString().trim();

        // 입력된 전화번호를 국제 포맷으로 변경
        if (!phoneNumber.startsWith("+82")) {
            phoneNumber = "+82" + phoneNumber.substring(1);
        }

        if (TextUtils.isEmpty(phoneNumber) || !phoneNumber.matches("^\\+8210[0-9]{8}$")) {
            editTextPhoneNumber.setError("올바른 전화번호 형식이 아닙니다 (+8210XXXXXXXX)");
            editTextPhoneNumber.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(enteredName)) {
            editTextName.setError("이름을 입력하세요");
            editTextName.requestFocus();
            return;
        }

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("users");
        databaseReference.orderByChild("phone").equalTo(phoneNumber).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                boolean found = false;
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    User user = snapshot.getValue(User.class);
                    if (user != null && user.getUsername().equals(enteredName)) {
                        showEmailDialog(user.getEmail());
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    Toast.makeText(getActivity(), "해당 전화번호와 이름으로 등록된 이메일이 없습니다.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(getActivity(), "데이터베이스 에러: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    public static class User {
        private String username;
        private String email;
        private String phone;

        public User() {
            // Default constructor required for calls to DataSnapshot.getValue(User.class)
        }

        public String getUsername() {
            return username;
        }

        public String getEmail() {
            return email;
        }

        public String getPhone() {
            return phone;
        }
    }

    private void showEmailDialog(String email) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("등록된 이메일");
        builder.setMessage("이메일: " + email);

        // '확인' 버튼 설정
        builder.setPositiveButton("확인", (dialog, which) -> {
            startActivity(new Intent(getActivity(), LoginActivity.class));
        });

        // '복사' 버튼 설정
        builder.setNegativeButton("복사", (dialog, which) -> {
            copyToClipboard(email);
            startActivity(new Intent(getActivity(), LoginActivity.class));
        });

        builder.show();
    }

    private void copyToClipboard(String email) {
        ClipboardManager clipboard = (ClipboardManager) getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("email", email);
        clipboard.setPrimaryClip(clip);
        Toast.makeText(getActivity(), "이메일이 클립보드에 복사되었습니다.", Toast.LENGTH_SHORT).show();
    }
}

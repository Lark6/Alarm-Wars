package com.example.alarm__wars;

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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.concurrent.TimeUnit;

public class FindPasswordFragment extends Fragment {

    private EditText editTextEmail, editTextName, editTextPhoneNumber, editTextVerificationCode;
    private Button buttonSendVerification, buttonVerify, buttonResendVerification, buttonNext;
    private String verificationId;
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_pw, container, false);

        editTextEmail = view.findViewById(R.id.registered_id);
        editTextName = view.findViewById(R.id.name);
        editTextPhoneNumber = view.findViewById(R.id.phone_number);
        editTextVerificationCode = view.findViewById(R.id.et_verification_code);
        buttonSendVerification = view.findViewById(R.id.btn_send_verification);
        buttonVerify = view.findViewById(R.id.btn_verify);
        buttonResendVerification = view.findViewById(R.id.btn_resend_verification);
        buttonNext = view.findViewById(R.id.btn_next);

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        buttonSendVerification.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendVerificationCode();
            }
        });

        buttonVerify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                verifyCode();
            }
        });

        buttonResendVerification.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resendVerificationCode();
            }
        });

        buttonNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resetPassword();
            }
        });

        return view;
    }

    private void sendVerificationCode() {
        String phoneNumber = editTextPhoneNumber.getText().toString().trim();

        if (TextUtils.isEmpty(phoneNumber)) {
            editTextPhoneNumber.setError("전화번호를 입력하세요");
            editTextPhoneNumber.requestFocus();
            return;
        }

        // 한국 국가 코드 +82 추가
        if (!phoneNumber.startsWith("+82")) {
            phoneNumber = "+82" + phoneNumber.substring(1);
        }

        PhoneAuthOptions options =
                PhoneAuthOptions.newBuilder(mAuth)
                        .setPhoneNumber(phoneNumber)       // 전화번호 입력
                        .setTimeout(60L, TimeUnit.SECONDS) // 타임아웃 시간 설정
                        .setActivity(getActivity())        // 현재 액티비티
                        .setCallbacks(mCallbacks)          // 인증 콜백
                        .build();
        PhoneAuthProvider.verifyPhoneNumber(options);
    }

    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks =
            new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

                @Override
                public void onVerificationCompleted(@NonNull PhoneAuthCredential credential) {
                    String code = credential.getSmsCode();
                    if (code != null) {
                        editTextVerificationCode.setText(code);
                        verifyCode();
                    }
                }

                @Override
                public void onVerificationFailed(@NonNull FirebaseException e) {
                    Toast.makeText(getActivity(), "인증 실패: " + e.getMessage(), Toast.LENGTH_LONG).show();
                }

                @Override
                public void onCodeSent(@NonNull String s,
                                       @NonNull PhoneAuthProvider.ForceResendingToken token) {
                    super.onCodeSent(s, token);
                    verificationId = s;
                    Toast.makeText(getActivity(), "인증 코드가 전송되었습니다.", Toast.LENGTH_LONG).show();
                }
            };

    private void verifyCode() {
        String code = editTextVerificationCode.getText().toString().trim();
        if (TextUtils.isEmpty(code)) {
            editTextVerificationCode.setError("인증번호를 입력하세요");
            editTextVerificationCode.requestFocus();
            return;
        }
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, code);
        signInWithPhoneAuthCredential(credential);
    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(getActivity(), task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(getActivity(), "전화번호 인증에 성공했습니다.", Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(getActivity(), "전화번호 인증에 실패했습니다.", Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void resendVerificationCode() {
        sendVerificationCode();
    }

    private void resetPassword() {
        String email = editTextEmail.getText().toString().trim();
        String name = editTextName.getText().toString().trim();
        String phoneNumber = editTextPhoneNumber.getText().toString().trim();

        if (TextUtils.isEmpty(email)) {
            editTextEmail.setError("이메일을 입력하세요");
            editTextEmail.requestFocus();
            return;
        }

        mDatabase.child("users")
                .orderByChild("email")
                .equalTo(email)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                                String registeredName = userSnapshot.child("name").getValue(String.class);
                                String registeredPhoneNumber = userSnapshot.child("phoneNumber").getValue(String.class);

                                if (name.equals(registeredName) && phoneNumber.equals(registeredPhoneNumber)) {
                                    FirebaseAuth.getInstance().sendPasswordResetEmail(email)
                                            .addOnCompleteListener(task -> {
                                                if (task.isSuccessful()) {
                                                    Toast.makeText(getActivity(), "비밀번호 재설정 이메일이 발송되었습니다.", Toast.LENGTH_LONG).show();
                                                } else {
                                                    Toast.makeText(getActivity(), "비밀번호 재설정 이메일 발송에 실패했습니다.", Toast.LENGTH_LONG).show();
                                                }
                                            });
                                } else {
                                    Toast.makeText(getActivity(), "입력된 정보가 일치하지 않습니다.", Toast.LENGTH_LONG).show();
                                }
                            }
                        } else {
                            Toast.makeText(getActivity(), "해당 이메일로 등록된 계정이 없습니다.", Toast.LENGTH_LONG).show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(getActivity(), "데이터베이스 오류: " + error.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }
}

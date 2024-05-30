package com.example.alarm__wars;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.text.InputType;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.android.gms.tasks.OnCompleteListener;

public class SignupActivity extends AppCompatActivity {

    private EditText editTextUsername;
    private EditText editTextEmail;
    private EditText editTextPassword;
    private ImageButton togglePasswordVisibilityButton;
    private EditText editTextPhone;
    private Button buttonSignUp;

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        // Initialize Views
        editTextUsername = findViewById(R.id.editTextUsername);
        editTextEmail = findViewById(R.id.editTextEmail);
        editTextPassword = findViewById(R.id.editTextPassword);
        togglePasswordVisibilityButton = findViewById(R.id.btn_toggle_password);
        editTextPhone = findViewById(R.id.editTextPhone);
        buttonSignUp = findViewById(R.id.buttonSignUp);

        togglePasswordVisibilityButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        // 사용자가 버튼을 누르고 있을 때 비밀번호 표시
                        editTextPassword.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                        editTextPassword.setSelection(editTextPassword.getText().length());
                        togglePasswordVisibilityButton.setImageResource(R.drawable.ic_eye);
                        return true;
                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL:
                        // 사용자가 버튼에서 손을 뗐을 때 비밀번호 숨기기
                        editTextPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                        editTextPassword.setSelection(editTextPassword.getText().length());
                        togglePasswordVisibilityButton.setImageResource(R.drawable.ic_eye_off);
                        return true;
                }
                return false;
            }
        });

        // Set onClickListener for SignUp Button
        buttonSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signUp();
            }
        });
    }

    private void signUp() {
        final String username = editTextUsername.getText().toString().trim();
        final String email = editTextEmail.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();
        final String phone = convertToInternationalFormat(editTextPhone.getText().toString().trim());

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            FirebaseUser user = mAuth.getCurrentUser();
                            // Add additional user information to Realtime Database
                            if (user != null) {
                                // Use UID as the primary key
                                writeNewUser(user.getUid(), username, email, phone);
                                Toast.makeText(SignupActivity.this, "회원가입 성공", Toast.LENGTH_SHORT).show();
                                finish(); // Close SignUpActivity and go back to LoginActivity
                            }
                        } else {
                            // If sign in fails, display a message to the user.
                            Toast.makeText(SignupActivity.this, "회원가입 실패", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private String convertToInternationalFormat(String phoneNumber) {
        // Check if the phone number starts with 010
        if (phoneNumber.startsWith("010")) {
            // Convert to +82 format
            return "+82" + phoneNumber.substring(1);
        }
        // Return the original phone number if it doesn't start with 010
        return phoneNumber;
    }

    private void writeNewUser(String userId, String name, String email, String phone) {
        User user = new User(name, email, phone);
        // Use UID as the primary key
        mDatabase.child("users").child(userId).setValue(user);
    }

    public static class User {
        public String username;
        public String email;
        public String phone;

        public User() {
            // Default constructor required for calls to DataSnapshot.getValue(User.class)
        }

        public User(String username, String email, String phone) {
            this.username = username;
            this.email = email;
            this.phone = phone;
        }
    }
}
package com.example.alarm__wars;

import static com.google.android.material.internal.ViewUtils.dpToPx;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "LoginActivity";

    private Button loginButton;
    private EditText editTextEmail, editTextPassword;
    private ImageButton togglePasswordVisibilityButton;
    private TextView signupButton;
    private GoogleSignInClient mGoogleSignInClient;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // 시스템 다크 모드 설정을 따라가도록 설정
        AppCompatDelegate.setDefaultNightMode(
                AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
        );

        mAuth = FirebaseAuth.getInstance();

        // Configure Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        editTextEmail = findViewById(R.id.etUserId);
        editTextPassword = findViewById(R.id.et_password);
        loginButton = findViewById(R.id.btnLogin);
        togglePasswordVisibilityButton = findViewById(R.id.btn_toggle_password);
        signupButton = findViewById(R.id.btnSignUp);

        loginButton.setOnClickListener(v -> loginUser());
        findViewById(R.id.Glogin).setOnClickListener(v -> googleSignIn());

        TextView findIdPw = findViewById(R.id.btnFindIdPw);
        findIdPw.setOnClickListener(v -> onFindIdClicked());


        signupButton = findViewById(R.id.btnSignUp);
        signupButton.setOnClickListener(v -> onSignUpClicked());


        SignInButton signInButton = findViewById(R.id.Glogin);
        setGooglePlusButtonText(signInButton, "Google로 로그인");

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
    }

    public void setGooglePlusButtonText(SignInButton signInButton, String buttonText) {
        for (int i = 0; i < signInButton.getChildCount(); i++) {
            View v = signInButton.getChildAt(i);

            if (v instanceof TextView) {
                TextView tv = (TextView) v;
                tv.setText(buttonText);
                tv.setGravity(Gravity.CENTER_VERTICAL | Gravity.CENTER);

                // FrameLayout.LayoutParams를 사용하여 왼쪽 마진 추가
                FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(
                        FrameLayout.LayoutParams.MATCH_PARENT, // 너비를 WRAP_CONTENT로 설정
                        FrameLayout.LayoutParams.WRAP_CONTENT); // 높이를 WRAP_CONTENT로 설정

                int leftPadding = dpToPx(10); // 10dp를 픽셀로 변환
                tv.setPadding(leftPadding, tv.getPaddingTop(), tv.getPaddingRight(), tv.getPaddingBottom());

                tv.setLayoutParams(layoutParams);

                return;
            }
        }
    }

    public int dpToPx(int dp) {
        return (int) (dp * getResources().getDisplayMetrics().density);
    }

    private void loginUser() {
        String email = editTextEmail.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();

        // 이메일과 비밀번호가 비어있는지 검사
        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(LoginActivity.this, "아이디와 비밀번호를 입력하세요", Toast.LENGTH_SHORT).show();
            return; // 정보가 부족하면 함수 실행을 중단
        }

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        goToMainActivity(mAuth.getCurrentUser());
                    } else {
                        Toast.makeText(LoginActivity.this, "인증에 실패했습니다", Toast.LENGTH_SHORT).show();
                    }
                });
    }
    private void googleSignIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, 9001);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 9001) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account.getIdToken());
            } catch (ApiException e) {
                Log.w(TAG, "Google 로그인 실패", e);
            }
        }
    }
    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        goToMainActivity(mAuth.getCurrentUser());
                    } else {
                        Toast.makeText(LoginActivity.this, "Firebase 인증 실패", Toast.LENGTH_SHORT).show();
                    }
                });
    }
    private void goToMainActivity(FirebaseUser user) {
        if (user != null) {
            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            intent.putExtra("USER_EMAIL", user.getEmail());
            startActivity(intent);
            finish();
        }
    }
    public void onFindIdClicked() {
        // Start the FindIdPwActivity when the text view is clicked
        Intent intent = new Intent(this, FindIdPwActivity.class);
        startActivity(intent);
    }
    public void onSignUpClicked() {
        // Start the SignupActivity
        Intent intent = new Intent(this, SignupActivity.class);
        startActivity(intent);
    }
}

package com.example.alarm__wars;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.nhn.android.naverlogin.OAuthLogin;
import com.nhn.android.naverlogin.OAuthLoginHandler;
import com.kakao.sdk.user.UserApiClient;

public class MainActivity extends AppCompatActivity {

    private static final int RC_SIGN_IN = 9001;
    private GoogleSignInClient mGoogleSignInClient;
    private OAuthLogin mOAuthLoginModule;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        configureGoogleLogin();
        configureNaverLogin();

        Button btnLogin = findViewById(R.id.btnLogin);
        btnLogin.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, LoginCompleteActivity.class);
            startActivity(intent);
        });

        findViewById(R.id.btnFindId).setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, FindIdActivity.class);
            startActivityForResult(intent, 1);
        });

        findViewById(R.id.btnFindPassword).setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, FindPasswordActivity.class);
            startActivityForResult(intent, 2);
        });

        findViewById(R.id.btnSignUp).setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, SignUpActivity.class);
            startActivityForResult(intent, 3);
        });

        setupSocialMediaLogins();
    }

    private void configureGoogleLogin() {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
    }

    private void configureNaverLogin() {
        mOAuthLoginModule = OAuthLogin.getInstance();
        mOAuthLoginModule.init(
                MainActivity.this,
                "YOUR_CLIENT_ID",
                "YOUR_CLIENT_SECRET",
                "YOUR_APP_NAME"
        );
    }

    private void setupSocialMediaLogins() {
        ImageView imgGoogle = findViewById(R.id.imgGoogle);
        imgGoogle.setOnClickListener(v -> performGoogleLogin());

        ImageView imgNaver = findViewById(R.id.imgNaver);
        imgNaver.setOnClickListener(v -> performNaverLogin());

        ImageView imgKakaotalk = findViewById(R.id.imgKakaotalk);
        imgKakaotalk.setOnClickListener(v -> performKakaoTalkLogin());
    }

    private void performGoogleLogin() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    private void performNaverLogin() {
        mOAuthLoginModule.startOauthLoginActivity(MainActivity.this, new OAuthLoginHandler() {
            @Override
            public void run(boolean success) {
                if (success) {
                    // 로그인 성공 처리
                } else {
                    String errorCode = mOAuthLoginModule.getLastErrorCode(MainActivity.this).getCode();
                    String errorDesc = mOAuthLoginModule.getLastErrorDesc(MainActivity.this);
                    Toast.makeText(MainActivity.this, "Naver 로그인 실패: " + errorCode + " - " + errorDesc, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void performKakaoTalkLogin() {
        UserApiClient.getInstance().loginWithKakaoTalk(MainActivity.this, (oAuthToken, error) -> {
            if (error != null) {
                Toast.makeText(MainActivity.this, "KakaoTalk 로그인 실패: " + error.toString(), Toast.LENGTH_SHORT).show();
            } else if (oAuthToken != null) {
                // 로그인 성공 처리
            }
            return null;
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Google 로그인 결과 처리
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                // 로그인 성공 처리, 예: 메인 액티비티로 사용자 정보 전달
                Toast.makeText(this, "Google 로그인 성공: " + account.getDisplayName(), Toast.LENGTH_LONG).show();
            } catch (ApiException e) {
                // 로그인 실패 처리
                Toast.makeText(this, "Google 로그인 실패: " + e.getStatusCode(), Toast.LENGTH_SHORT).show();
            }
        }

        // 아이디 찾기, 비밀번호 찾기, 회원가입 활동 결과 처리
        switch (requestCode) {
            case 1: // 아이디 찾기
                if (resultCode == RESULT_OK) {
                    String userId = data.getStringExtra("userId");
                    Toast.makeText(this, "아이디 찾기 성공: " + userId, Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(this, "아이디 찾기 실패", Toast.LENGTH_SHORT).show();
                }
                break;
            case 2: // 비밀번호 찾기
                if (resultCode == RESULT_OK) {
                    String userPasswordHint = data.getStringExtra("passwordHint");
                    Toast.makeText(this, "비밀번호 찾기 성공: " + userPasswordHint, Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(this, "비밀번호 찾기 실패", Toast.LENGTH_SHORT).show();
                }
                break;
            case 3: // 회원가입
                if (resultCode == RESULT_OK) {
                    String userName = data.getStringExtra("userName");
                    Toast.makeText(this, "회원가입 성공: " + userName, Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(this, "회원가입 실패", Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

}

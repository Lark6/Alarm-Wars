package com.example.alarm__wars;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.auth.SignInMethodQueryResult;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class LoginActivity extends AppCompatActivity {

    private SignInButton signInButton;
    private Button loginButton;
    private TextView signupButton;
    private EditText editTextEmail, editTextPassword;
    private GoogleSignInClient mGoogleSignInClient;
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private ActivityResultLauncher<Intent> activityResultLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        editTextEmail = findViewById(R.id.etUserId);
        editTextPassword = findViewById(R.id.etPassword);
        loginButton = findViewById(R.id.btnLogin);
        signInButton = findViewById(R.id.Glogin);
        signupButton = findViewById(R.id.btnSignUp);

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                login();
            }
        });

        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signIn();
            }
        });

        signupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // SignupActivity로 이동
                Intent intent = new Intent(LoginActivity.this, SignupActivity.class);
                startActivity(intent);
            }
        });

        init();
    }

    private void init() {
        activityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
            @Override
            public void onActivityResult(ActivityResult result) {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    Intent intent = result.getData();
                    Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(intent);
                    try {
                        GoogleSignInAccount account = task.getResult(ApiException.class);
                        firebaseAuthWithGoogle(account);
                    } catch (ApiException e) {
                        Toast.makeText(getApplicationContext(), "Google sign in failed", Toast.LENGTH_LONG).show();
                    }
                }
            }
        });
        configSignIn();
    }

    private void configSignIn() {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
    }

    // 로그인 성공 시 호출되는 메소드
    private void login() {
        final String email = editTextEmail.getText().toString().trim();
        final String password = editTextPassword.getText().toString().trim();

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            FirebaseUser user = mAuth.getCurrentUser();
                            if (user != null) {
                                // 로그인 성공 시 UID를 다음 액티비티로 전달
                                String uid = user.getUid();
                                goToMainActivity(uid);
                            }
                        } else {
                            // 로그인 실패 처리
                        }
                    }
                });
    }

    // 다음 액티비티로 이동하는 메소드
    private void goToMainActivity(String uid) {
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        // UID를 인텐트에 첨부하여 전달
        intent.putExtra("USER_ID", uid);
        startActivity(intent);
        finish(); // 현재 액티비티 종료
    }

    private void signIn() {
        Log.d("LoginActivity", "Starting Google Sign-In intent");
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        activityResultLauncher.launch(signInIntent);
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        Log.d("LoginActivity", "Authenticating with Google for account: " + acct.getEmail());
        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Log.d("LoginActivity", "Google sign in successful");
                            FirebaseUser user = mAuth.getCurrentUser();
                            if (user != null) {
                                checkUserData(user.getUid());
                            }
                        } else {
                            Log.e("LoginActivity", "Google sign in failed", task.getException());
                            Toast.makeText(getApplicationContext(), "로그인 실패", Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    private void checkUserData(String userId) {
        DatabaseReference usersRef = mDatabase.child("users").child(userId);
        usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    // 사용자 정보가 데이터베이스에 있으면 사용자의 이름을 가져와서 환영 메시지를 표시합니다.
                    String username = snapshot.child("username").getValue(String.class);
                    Toast.makeText(getApplicationContext(), "반갑습니다 " + username + " 님", Toast.LENGTH_LONG).show();
                    // 메인 액티비티로 이동합니다.
                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish(); // 현재 LoginActivity 종료
                } else {
                    // 사용자 정보가 데이터베이스에 없으면 새로운 사용자이므로 회원가입 화면으로 이동합니다.
                    Intent intent = new Intent(LoginActivity.this, SignupActivity.class);
                    startActivity(intent);
                    finish(); // 현재 LoginActivity 종료
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getApplicationContext(), "데이터베이스 오류: " + error.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    public void onSignUpClicked(View view) {
        // SignupActivity로 이동
        Intent intent = new Intent(this, SignupActivity.class);
        startActivity(intent);
    }

}

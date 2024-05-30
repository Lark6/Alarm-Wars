package com.example.alarm__wars;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
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
import com.google.android.gms.auth.api.signin.GoogleSignInStatusCodes;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "LoginActivity";

    private SignInButton signInButton;
    private Button loginButton;
    private TextView signupButton, findIdPwButton;
    private EditText editTextEmail, editTextPassword;
    private ImageButton togglePasswordVisibilityButton;
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
        editTextPassword = findViewById(R.id.et_password);
        togglePasswordVisibilityButton = findViewById(R.id.btn_toggle_password);
        loginButton = findViewById(R.id.btnLogin);
        signInButton = findViewById(R.id.Glogin);
        signupButton = findViewById(R.id.btnSignUp);
        findIdPwButton = findViewById(R.id.btnFindIdPw);

        signInButton.setOnClickListener(v -> signIn());
        loginButton.setOnClickListener(v -> manualLogin());
        signupButton.setOnClickListener(v -> startActivity(new Intent(this, SignupActivity.class)));
        findIdPwButton.setOnClickListener(v -> onFindIdClicked());

        setupGoogleSignIn();
    }

    private void setupGoogleSignIn() {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        activityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        handleSignInResult(GoogleSignIn.getSignedInAccountFromIntent(result.getData()));
                    }
                });
    }

    private void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        activityResultLauncher.launch(signInIntent);
    }

    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);
            firebaseAuthWithGoogle(account.getIdToken());
        } catch (ApiException e) {
            Log.w(TAG, "signInResult:failed code=" + e.getStatusCode());
            Toast.makeText(this, "Google 로그인 실패", Toast.LENGTH_SHORT).show();
        }
    }

    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential).addOnCompleteListener(this, task -> {
            if (task.isSuccessful()) {
                FirebaseUser user = mAuth.getCurrentUser();
                if (user != null) {
                    verifyUserInDatabase(user);
                }
            } else {
                Toast.makeText(LoginActivity.this, "Firebase 인증 실패", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void verifyUserInDatabase(FirebaseUser user) {
        DatabaseReference usersRef = mDatabase.child("users").child(user.getUid());
        usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists()) {
                    createUserInDatabase(user);
                } else {
                    goToMainActivity(user.getUid(), user.getEmail());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.w(TAG, "데이터베이스 오류", databaseError.toException());
            }
        });
    }

    private void createUserInDatabase(FirebaseUser user) {
        Map<String, Object> userData = new HashMap<>();
        userData.put("email", user.getEmail());
        userData.put("name", user.getDisplayName());
        DatabaseReference usersRef = mDatabase.child("users").child(user.getUid());
        usersRef.setValue(userData).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(LoginActivity.this, "새 사용자 등록 성공", Toast.LENGTH_LONG).show();
                goToMainActivity(user.getUid(), user.getEmail());
            } else {
                Toast.makeText(LoginActivity.this, "사용자 등록 실패", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void goToMainActivity(String uid, String email) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("USER_ID", uid);
        intent.putExtra("EMAIL", email);
        startActivity(intent);
        finish();
    }

    private void manualLogin() {
        String email = editTextEmail.getText().toString();
        String password = editTextPassword.getText().toString();
        // Implement manual login logic here
    }

    public void onFindIdClicked() {
        // Implement ID/PW find logic here
    }

    public void onSignUpClicked(View view) {
        startActivity(new Intent(this, SignupActivity.class));
    }
}

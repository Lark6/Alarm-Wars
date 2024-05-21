package com.example.alarm__wars;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Login button
        Button btnLogin = findViewById(R.id.btnLogin);
        btnLogin.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, LoginCompleteActivity.class);
            startActivity(intent);
        });

        // Find ID, Password button
        TextView btnFindIdPw = findViewById(R.id.btnFindIdPw);
        btnFindIdPw.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, FindIdPwActivity.class);
            startActivity(intent);
        });

        // Sign up button
        TextView btnSignUp = findViewById(R.id.btnSignUp);
        btnSignUp.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, SignUpActivity.class);
            startActivity(intent);
        });

        // Additional setup if needed
    }
}

package com.example.alarm__wars;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Random;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import java.util.Random;

    public class MainActivity extends AppCompatActivity {

        private DatabaseReference mDatabase;
        private FirebaseAuth mAuth;
        private GoogleSignInClient mGoogleSignInClient;
        private static final String TAG = "MainActivity";

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_main);
            Button createRoomButton = findViewById(R.id.create_room_button);
            Button joinRoomButton = findViewById(R.id.join_room_button);

            mAuth = FirebaseAuth.getInstance();
            mDatabase = FirebaseDatabase.getInstance().getReference().child("rooms");

            GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestIdToken(getString(R.string.default_web_client_id))
                    .requestEmail()
                    .build();

            mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

            FirebaseUser currentUser = mAuth.getCurrentUser();
            if (currentUser != null) {
                Log.d(TAG, "Current user UID: " + currentUser.getUid());
            }

            findViewById(R.id.logout_button).setOnClickListener(v -> logout());
            findViewById(R.id.exit_room_button).setOnClickListener(v -> promptForExitingRoom());
            createRoomButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(MainActivity.this, MakeRoomActivity.class);
                    startActivity(intent);
                }
            });
        }

        public void joinRoom(View view) {
            // 호스트 코드 입력을 위한 다이얼로그 표시
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("방 참여")
                    .setMessage("호스트 코드를 입력하세요");

            // EditText를 다이얼로그에 추가
            final EditText input = new EditText(this);
            input.setInputType(InputType.TYPE_CLASS_NUMBER);
            builder.setView(input);

            // 확인 버튼 클릭 시 호스트 코드 확인
            builder.setPositiveButton("확인", new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {

                    final String hostCode = input.getText().toString();

                    // 데이터베이스에서 호스트 코드 확인
                    mDatabase.child(hostCode).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if (dataSnapshot.exists()) {
//                            Room room = dataSnapshot.getValue(Room.class); 캘린더 추가로 못 가져옴.
                                // hostSelected를 통하여 연결된 방인지 확인(업데이트 해야함).
                                System.out.println(11);
                                if (true) { //!room.isHostSelected() 였던것
                                    // 호스트 코드를 룸 액티비티에 전달하여 실행
                                    Intent intent = new Intent(MainActivity.this, RoomActivity.class);
                                    intent.putExtra("hostCode", hostCode);
                                    startActivity(intent);
                                } else {
                                    Toast.makeText(MainActivity.this, "이미 선택된 호스트입니다", Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                Toast.makeText(MainActivity.this, "입력한 호스트 코드가 존재하지 않습니다!!", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                            Toast.makeText(MainActivity.this, "데이터베이스 오류가 발생했습니다", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            });

            // 취소 버튼
            builder.setNegativeButton("취소", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });

            builder.show();
        }
        private void promptForExitingRoom() {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("방 탈출");

            final EditText input = new EditText(this);
            input.setInputType(InputType.TYPE_CLASS_TEXT);
            builder.setView(input);

            builder.setPositiveButton("확인", (dialog, which) -> {
                String hostCode = input.getText().toString();
                deleteRoom(hostCode);
            });
            builder.setNegativeButton("취소", (dialog, which) -> dialog.cancel());

            builder.show();
        }

        private void deleteRoom(String hostCode) {
            DatabaseReference roomRef = mDatabase.child(hostCode);
            roomRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        roomRef.removeValue();
                        Toast.makeText(MainActivity.this, "방이 성공적으로 삭제되었습니다.", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(MainActivity.this, "해당 코드를 가진 방이 없습니다.", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Toast.makeText(MainActivity.this, "데이터베이스 오류: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }

        public void logout() {
            FirebaseAuth.getInstance().signOut();
            mGoogleSignInClient.signOut().addOnCompleteListener(this, task -> {
                Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                finish();
            });
        }
    }

package com.example.alarm__wars;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Random;

public class MainActivity extends AppCompatActivity {

    private DatabaseReference mDatabase;
    private FirebaseAuth mAuth;
    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference().child("rooms");

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            String uid = currentUser.getUid();
            Log.d(TAG, "Current user UID: " + uid);
        }
    }

    public void createRoom(View view) {
        // 랜덤한 6자리 호스트 코드 생성
        Random random = new Random();
        int hostCode = 100000 + random.nextInt(900000);

        // 알람 시간 입력을 위한 다이얼로그 표시
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("알람 시간 입력")
                .setMessage("알람 시간을 분 단위로 입력하세요");

        // EditText를 다이얼로그에 추가
        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_NUMBER);
        builder.setView(input);

        // 확인 버튼 클릭 시 알람 시간 확인
        builder.setPositiveButton("확인", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog1, int which) {
                final String alarmTimeStr = input.getText().toString();
                if (!alarmTimeStr.isEmpty()) {
                    final long alarmTime = Long.parseLong(alarmTimeStr);

                    // 호스트 코드를 데이터베이스에 저장
                    Room room = new Room("Q", "A", false, alarmTime);
                    mDatabase.child(String.valueOf(hostCode)).setValue(room, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(DatabaseError error, @NonNull DatabaseReference ref) {
                            if (error == null) {
                                // 두 개의 슬롯 중 하나에 UID 저장
                                FirebaseUser currentUser = mAuth.getCurrentUser();
                                if (currentUser != null) {
                                    String uid = currentUser.getUid();
                                    int slot = random.nextInt(2) + 1; // 1 또는 2
                                    mDatabase.child(String.valueOf(hostCode)).child("slot" + slot).setValue(uid, new DatabaseReference.CompletionListener() {
                                        @Override
                                        public void onComplete(@Nullable DatabaseError error, @NonNull DatabaseReference ref) {
                                            if (error != null) {
                                                Toast.makeText(MainActivity.this, "슬롯에 UID 저장에 실패했습니다", Toast.LENGTH_SHORT).show();
                                            } else {
                                                // 호스트 코드를 팝업으로 표시
                                                showAlertDialog("방 생성 완료", "호스트 코드: " + hostCode);
                                            }
                                        }
                                    });
                                } else {
                                    Toast.makeText(MainActivity.this, "현재 사용자 정보를 가져올 수 없습니다", Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                Toast.makeText(MainActivity.this, "방 생성에 실패했습니다", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                } else {
                    Toast.makeText(MainActivity.this, "알람 시간을 입력하세요", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // 취소 버튼
        builder.setNegativeButton("취소", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
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
                            Room room = dataSnapshot.getValue(Room.class);
                            if (!room.isHostSelected()) {
                                // UID를 룸 액티비티에 전달하여 실행
                                Intent intent = new Intent(MainActivity.this, RoomActivity.class);
                                intent.putExtra("hostCode", hostCode);

                                // 두 개의 슬롯 중 하나에 UID 저장
                                FirebaseUser currentUser = mAuth.getCurrentUser();
                                if (currentUser != null) {
                                    String uid = currentUser.getUid();
                                    int slot = dataSnapshot.child("slot1").exists() ? 2 : 1;
                                    mDatabase.child(hostCode).child("slot" + slot).setValue(uid, new DatabaseReference.CompletionListener() {
                                        @Override
                                        public void onComplete(@Nullable DatabaseError error, @NonNull DatabaseReference ref) {
                                            if (error == null) {
                                                // 알람 시간을 팝업으로 표시
                                                showAlertDialog("방 참여 완료", "저장된 알람 시간: " + room.getAlarmTime() + " 분");
                                                startActivity(intent);
                                            } else {
                                                Toast.makeText(MainActivity.this, "슬롯에 UID 저장에 실패했습니다", Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    });
                                } else {
                                    Toast.makeText(MainActivity.this, "현재 사용자 정보를 가져올 수 없습니다", Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                Toast.makeText(MainActivity.this, "이미 선택된 호스트입니다", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(MainActivity.this, "입력한 호스트 코드가 존재하지 않습니다", Toast.LENGTH_SHORT).show();
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

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    // 팝업 창을 표시하는 메소드
    private void showAlertDialog(String title, String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(title)
                .setMessage(message)
                .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        AlertDialog dialog = builder.create();
        dialog.show();
    }
}

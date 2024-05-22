package com.example.alarm__wars;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
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

public class MainActivity extends AppCompatActivity {

    private DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button createRoomButton = findViewById(R.id.create_room_button);
        Button joinRoomButton = findViewById(R.id.join_room_button);



        // 방 만들기 버튼 설정
        createRoomButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, MakeRoomActivity.class);
                startActivity(intent);
            }
        });
        // 방 접속하기 버튼 설정
//        joinRoomButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Intent intent = new Intent(MainActivity.this, MakeRoomActivity.class);
//                startActivity(intent);
//            }
//        });

        mDatabase = FirebaseDatabase.getInstance().getReference().child("rooms");
    }
//
//    public void createRoom(View view) {
//        // 랜덤한 6자리 호스트 코드 생성
//        Random random = new Random();
//        int hostCode = 100000 + random.nextInt(900000);
//
//        // 알람 시간 입력을 위한 다이얼로그 표시
//        AlertDialog.Builder builder = new AlertDialog.Builder(this);
//        builder.setTitle("알람 시간 입력")
//                .setMessage("알람 시간을 분 단위로 입력하세요");
//
//        // EditText를 다이얼로그에 추가
//        final EditText input = new EditText(this);
//        input.setInputType(InputType.TYPE_CLASS_NUMBER);
//        builder.setView(input);
//
//        // 확인 버튼 클릭 시 알람 시간 확인
//        builder.setPositiveButton("확인", new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialog1, int which) {
//                final String alarmTimeStr = input.getText().toString();
//                if (!alarmTimeStr.isEmpty()) {
//                    final long alarmTime = Long.parseLong(alarmTimeStr);
//
//                    // 호스트 코드를 데이터베이스에 저장
//                    Room room = new Room("Q", "A", false, alarmTime, false, false);
//                    mDatabase.child(String.valueOf(hostCode)).setValue(room, new DatabaseReference.CompletionListener() {
//                        @Override
//                        public void onComplete(DatabaseError error, @NonNull DatabaseReference ref) {
//                            if (error == null) {
//                                // 팝업 창으로 호스트 코드 보여주기
//                                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
//                                builder.setTitle("방이 생성되었습니다")
//                                        .setMessage("호스트 코드: " + hostCode)
//                                        .setPositiveButton("확인", new DialogInterface.OnClickListener() {
//                                            @Override
//                                            public void onClick(DialogInterface dialog1, int which) {
//                                                dialog1.dismiss();
//                                            }
//                                        });
//                                AlertDialog dialog = builder.create();
//                                dialog.show();
//                            } else {
//                                Toast.makeText(MainActivity.this, "방 생성에 실패했습니다", Toast.LENGTH_SHORT).show();
//                            }
//                        }
//                    });
//                } else {
//                    Toast.makeText(MainActivity.this, "알람 시간을 입력하세요", Toast.LENGTH_SHORT).show();
//                }
//            }
//        });
//
//        // 취소 버튼
//        builder.setNegativeButton("취소", new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialog, int which) {
//                dialog.cancel();
//            }
//        });
//
//        builder.show();
//    }

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
}

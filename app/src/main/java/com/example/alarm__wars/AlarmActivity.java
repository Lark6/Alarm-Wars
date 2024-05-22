package com.example.alarm__wars;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;

import androidx.appcompat.app.AppCompatActivity;

import com.example.alarm__wars.databinding.ActivityAlarmBinding;

public class AlarmActivity extends AppCompatActivity {

    private MediaPlayer mediaPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 액티비티를 화면에 보이도록 함
        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON |
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON |
                WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
        ActivityAlarmBinding binding = ActivityAlarmBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // 알람음 재생
        this.mediaPlayer = MediaPlayer.create(this, R.raw.alarm);
        this.mediaPlayer.setLooping(true); // 반복 재생
        this.mediaPlayer.start();

        binding.btnClose.setOnClickListener(view -> {
            // 알람 종료
            close();
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // MediaPlayer release
        if (this.mediaPlayer != null) {
            this.mediaPlayer.release();
            this.mediaPlayer = null;
        }
    }

    @Override
    protected void onStop() {
        super.onStop();

        // 액티비티가 화면에서 사라질 때 알람이 종료되도록 함
        close();
    }

    /* 알람 종료  */
    private void close() {
        if (this.mediaPlayer != null && this.mediaPlayer.isPlaying()) {
            this.mediaPlayer.stop();
        }

        finish();
    }
}

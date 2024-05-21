package com.example.myapplication;

import android.app.AlarmManager;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Window;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.databinding.ActivityIntroBinding;

public class IntroActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 툴바 안보이게 하기 위함
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        ActivityIntroBinding binding = ActivityIntroBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // 알람 권한 설정 api 31 이상
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            // 알람 권한 설정 확인
            checkAlarmPermission(0);
        } else {
            // 다른 화면 위에 그리기 권한 설정 api 29 이상
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                // 다른 화면 위에 그리기 권한 설정 확인
                checkOverlayPermission(0);
            } else {
                // 메인으로 이동
                goMain();
            }
        }
    }

    /* 알람 권한 설정 api 31 이상 */
    @RequiresApi(api = Build.VERSION_CODES.S)
    private void checkAlarmPermission(int flag) {
        // 알람 권한 허용 여부
        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        if (alarmManager.canScheduleExactAlarms()) {
            // 허용됨

            // 다른 화면 위에 그리기 권한 설정 확인
            checkOverlayPermission(0);
        } else {
            // 권한이 거부됨
            if (flag == 0) {
                // (알람 권한 요청)
                Intent intent = new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
                this.activityLauncher1.launch(intent);
            } else {
                Toast.makeText(this, "앱을 이용하기 위해 권한이 필요합니다.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    /* 다른 화면 위에 그리기 권한 설정 api 29 이상 */
    @RequiresApi(api = Build.VERSION_CODES.Q)
    private void checkOverlayPermission(int flag) {
        // 다른 화면위에 그리기 권한을 허용해야 백그라운드에서 activity 실행이 정상적으로 됨 (android 10부터는 activity 실행에 제한이 있음)
        if (Settings.canDrawOverlays(this)) {
            // 허용됨
            goMain();
        } else {
            // 권한이 거부됨
            if (flag == 0) {
                // 다른 화면 위에 그리기 권한 요청
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:" + getPackageName()));
                this.activityLauncher2.launch(intent);
            } else {
                Toast.makeText(this, "앱을 이용하기 위해 권한이 필요합니다.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    /* 메인으로 이동 */
    private void goMain() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    /* ActivityForResult 알람 권한 요청 후 결과 */
    private final ActivityResultLauncher<Intent> activityLauncher1 = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                // api 31 이상
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    // 알람 권한 설정 확인
                    checkAlarmPermission(1);
                }
            });

    /* ActivityForResult 다른 화면 위에 그리기 권한 요청 후 결과 */
    private final ActivityResultLauncher<Intent> activityLauncher2 = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    // 다른 화면 위에 그리기 권한 설정 확인
                    checkOverlayPermission(1);
                }
            });
}

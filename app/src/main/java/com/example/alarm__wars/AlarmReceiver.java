package com.example.alarm__wars;


import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class AlarmReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent)
    {
        String hostCode = intent.getStringExtra("hostCode");
        // 알람 activity 호출
        Intent intent1 = new Intent(context, RingActivity.class);
        intent1.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_SINGLE_TOP);
        intent1.putExtra("hostCode", hostCode);

        System.out.println("Receiver Hostcode : " + hostCode);

        context.startActivity(intent1);
    }
}

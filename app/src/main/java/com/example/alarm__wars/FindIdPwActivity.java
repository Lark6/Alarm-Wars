package com.example.alarm__wars;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

public class FindIdPwActivity extends AppCompatActivity {

    private TextView tabId;
    private TextView tabPw;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_findidpw);

        tabId = findViewById(R.id.tab_id);
        tabPw = findViewById(R.id.tab_pw);

        tabId.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectTab(true);
            }
        });

        tabPw.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectTab(false);
            }
        });

        // Initialize with ID tab selected
        selectTab(true);
    }

    private void selectTab(boolean isIdTabSelected) {
        if (isIdTabSelected) {
            loadFragment(new FindIdFragment());
            tabId.setBackgroundResource(R.drawable.tab_background_selected);
            tabId.setTextColor(getResources().getColor(R.color.white));
            tabPw.setBackgroundResource(R.drawable.tab_background);
            tabPw.setTextColor(getResources().getColor(R.color.darkCharcoal));
        } else {
            loadFragment(new FindPasswordFragment());
            tabPw.setBackgroundResource(R.drawable.tab_background_selected);
            tabPw.setTextColor(getResources().getColor(R.color.white));
            tabId.setBackgroundResource(R.drawable.tab_background);
            tabId.setTextColor(getResources().getColor(R.color.darkCharcoal));
        }
    }

    private void loadFragment(Fragment fragment) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragmentContainer, fragment);
        transaction.commit();
    }
}

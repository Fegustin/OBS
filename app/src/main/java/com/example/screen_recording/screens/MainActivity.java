package com.example.screen_recording.screens;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.MenuItem;
import android.widget.Toast;

import com.example.screen_recording.R;
import com.example.screen_recording.screens.main_record.MainFragment;
import com.example.screen_recording.screens.setting_record.SettingFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    SharedPreferences p;
    private int currentPageId = -1;
    private boolean isNightTheme;
    Fragment selectedFragment;

    private ConstraintLayout constraintLayout;


    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        p = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
        isNightTheme = p.getBoolean("isNightTheme", false);

        if (isNightTheme) {
            setTheme(R.style.NightTheme);
        }
        setContentView(R.layout.activity_main);
        constraintLayout = findViewById(R.id.rootLayout);

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigation);
        if (isNightTheme) {
            bottomNavigationView.setBackgroundResource(R.color.colorBlue);
            constraintLayout.setBackgroundResource(R.color.colorBlack);
        }
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new MainFragment()).commit();
        }

        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                boolean isRecording = p.getBoolean("isRecord", false);
                if (isRecording) {
                    Toast.makeText(MainActivity.this, "Во время записи нельзя меня настройки", Toast.LENGTH_SHORT).show();
                    return false;
                }

                if (currentPageId == item.getItemId()){
                    return false;
                } else {
                    currentPageId = item.getItemId();
                    selectedFragment = null;
                    switch (item.getItemId()) {
                        case R.id.mainMenu:
                            selectedFragment = new MainFragment();
                            break;
                        case R.id.settingMenu:
                            selectedFragment = new SettingFragment();
                            break;
                    }

                    getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, selectedFragment).commit();
                    return true;
                }
            }
        });
    }
}

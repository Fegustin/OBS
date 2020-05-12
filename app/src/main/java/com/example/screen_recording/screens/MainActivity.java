package com.example.screen_recording.screens;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import com.example.screen_recording.R;
import com.example.screen_recording.screens.main.MainFragment;
import com.example.screen_recording.screens.setting.SettingFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    private int currentPageId = -1;

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        // Доделать потом
//        if(!Settings.canDrawOverlays(this)){
//            // ask for setting
//            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
//                    Uri.parse("package:" + getPackageName()));
//            startActivityForResult(intent, 228);
//        }

        // Bottom Navigation

//        WindowManager manager = (WindowManager) getSystemService(WINDOW_SERVICE);
//        WindowManager.LayoutParams params = new WindowManager.LayoutParams(
//                100,
//                100,
//                WindowManager.LayoutParams.TYPE_PHONE,
//                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
//                PixelFormat.TRANSPARENT);
//
//        params.gravity = Gravity.CENTER_HORIZONTAL;
//        params.x = 0;
//        params.y = 0;
//
//        ConstraintLayout constraintLayout = (ConstraintLayout) LayoutInflater.from(this).inflate(R.layout.over_main, null);
//        ImageView imageViewShow = constraintLayout.findViewById(R.id.imageViewShow);
//        imageViewShow.setImageResource(R.drawable.ic_home);
//
//        manager.addView(constraintLayout, params);

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigation);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new MainFragment()).commit();
        }


        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {

                SharedPreferences p = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
                boolean isRecording = p.getBoolean("isRecord", false);
                if (isRecording) {
                    Toast.makeText(MainActivity.this, "Во время записи нельзя меня настройки", Toast.LENGTH_SHORT).show();
                    return false;
                }

                if (currentPageId == item.getItemId()){
                    return false;
                } else {
                    Fragment selectedFragment = null;
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

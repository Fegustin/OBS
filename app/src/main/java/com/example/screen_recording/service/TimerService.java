package com.example.screen_recording.service;

import android.app.Service;
import android.content.Intent;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.IBinder;

import androidx.annotation.Nullable;

import java.util.Locale;

public class TimerService extends Service {

    private static long TIME_LIMIT = 900000000;
    private int seconds = 0;
    private CountDownTimer Count;


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Count = new CountDownTimer(TIME_LIMIT, 1000) {
            public void onTick(long millisUntilFinished) {
                int hours = seconds / 3600;
                int minutes = (seconds % 3600) / 60;
                int secs = seconds % 60;

                String time = String.format(Locale.getDefault(), "%02d:%02d:%02d", hours, minutes, secs);

                Intent i = new Intent("COUNTDOWN_UPDATED");
                i.putExtra("countdown",time);

                seconds++;

                sendBroadcast(i);
            }

            public void onFinish() {
                seconds = 0;
                Intent i = new Intent("COUNTDOWN_UPDATED");
                i.putExtra("countdown","Sent!");

                sendBroadcast(i);
                stopSelf();
            }
        };

        Count.start();
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        Count.cancel();
        super.onDestroy();
    }
}

package com.example.screen_recording.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.MediaRecorder;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.Build;
import android.os.Environment;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.SparseIntArray;
import android.view.Surface;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.ToggleButton;
import android.widget.VideoView;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.cardview.widget.CardView;
import androidx.core.app.NotificationCompat;

import com.example.screen_recording.R;
import com.example.screen_recording.screens.MainActivity;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class RecordService extends Service {
    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    SharedPreferences p;
    WindowManager window;

    private static final SparseIntArray ORIENTATIONS = new SparseIntArray();
    private boolean isRecord = false;
    private String videoUri = "";

    private MediaProjectionManager mediaProjectionManager;
    private MediaProjection mediaProjection;
    private VirtualDisplay virtualDisplay;
    private MediaRecorder mediaRecorder;

    private int mScreenDensity;
    private static int DISPLAY_WIDTH;
    private static int DISPLAY_HEIGHT;
    private int resultCode;
    private Intent resultData;

    static {
        ORIENTATIONS.append(Surface.ROTATION_0, 90);
        ORIENTATIONS.append(Surface.ROTATION_90, 0);
        ORIENTATIONS.append(Surface.ROTATION_180, 270);
        ORIENTATIONS.append(Surface.ROTATION_270, 180);
    }

    @Override
    public void onCreate() {
        super.onCreate();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            startMyOwnForeground();
        else
            startForeground(1, new Notification());

        p = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        window = (WindowManager) getSystemService(Context.WINDOW_SERVICE);

        DisplayMetrics metrics = new DisplayMetrics();
        window.getDefaultDisplay().getMetrics(metrics);
        mScreenDensity = metrics.densityDpi;
        DISPLAY_WIDTH = metrics.widthPixels;
        DISPLAY_HEIGHT = metrics.heightPixels;


        mediaRecorder = new MediaRecorder();
        mediaProjectionManager = (MediaProjectionManager) getSystemService(Context.MEDIA_PROJECTION_SERVICE);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void startMyOwnForeground(){
        String NOTIFICATION_CHANNEL_ID = "com.example.screen_recording";
        String channelName = "My Background Service";
        NotificationChannel chan = new NotificationChannel(NOTIFICATION_CHANNEL_ID, channelName, NotificationManager.IMPORTANCE_NONE);
        chan.setLightColor(Color.BLUE);
        chan.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        assert manager != null;
        manager.createNotificationChannel(chan);

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID);
        Notification notification = notificationBuilder.setOngoing(true)
                .setSmallIcon(R.drawable.ic_file)
                .setContentTitle("Идёт запись")
                .setPriority(NotificationManager.IMPORTANCE_MIN)
                .setCategory(Notification.CATEGORY_SERVICE)
                .build();
        startForeground(2, notification);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        if (intent.getAction() == null) {
            resultCode = intent.getIntExtra("code", 1337);
            resultData = intent.getParcelableExtra("data");
            onStartRecord(resultCode, resultData);
        }

        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        onStopRecord();
        super.onDestroy();
    }

    private void onStartRecord(int code, Intent data) {
        mediaProjection = mediaProjectionManager.getMediaProjection(code, data);

        int quality = p.getInt("quality", 1080);
        boolean micro = p.getBoolean("micro", false);
        int fps = p.getInt("FPS", 60);

        initRecorder(quality, micro, fps);
        virtualDisplay = createVirtualDisplay();

        isRecord = true;
        p.edit().putBoolean("isRecord", isRecord).apply();
        mediaRecorder.start();
    }

    private void onStopRecord() {
        mediaRecorder.stop();
        mediaRecorder.reset();

        stopRecordScreen();


        isRecord = false;
        p.edit().putBoolean("isRecord", isRecord).apply();
    }

    private VirtualDisplay createVirtualDisplay() {
        return mediaProjection.createVirtualDisplay("MainFragment", DISPLAY_WIDTH, DISPLAY_HEIGHT, mScreenDensity,
                DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
                mediaRecorder.getSurface(), null, null);
    }

    private void initRecorder(int QUALITY, boolean isMicro, int fps) {
        try {
            int bitrateVideo = 0;
            switch (QUALITY) {
                case 1080:
                    bitrateVideo = 7000000;
                    break;
                case 720:
                    bitrateVideo = 4000000;
                    break;
                default:
                    bitrateVideo = 2000000;
                    break;
            }

            if (isMicro) {
                mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            }

            mediaRecorder.setVideoSource(MediaRecorder.VideoSource.SURFACE);
            mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);

            if (isMicro) {
                mediaRecorder.setAudioSamplingRate(44100);
                mediaRecorder.setAudioEncodingBitRate(16 * 44100);
                mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
            }


            videoUri = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM)
                    + new StringBuilder("/FreeRecord_").append(new SimpleDateFormat("dd-MM-yyyy-hh-mm-ss")
                    .format(new Date())).append(".mp4").toString();

            mediaRecorder.setOutputFile(videoUri);
            mediaRecorder.setVideoSize(DISPLAY_WIDTH, DISPLAY_HEIGHT);
            mediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
            mediaRecorder.setVideoEncodingBitRate(bitrateVideo);
            mediaRecorder.setCaptureRate(fps);
            mediaRecorder.setVideoFrameRate(fps);

            int rotation = window.getDefaultDisplay().getRotation();
            int orientation = ORIENTATIONS.get(rotation + 90);
            mediaRecorder.setOrientationHint(orientation);
            mediaRecorder.prepare();
        } catch (IOException e) {
            e.printStackTrace();
            Log.i("prepare" , "" + e.getMessage());
        }
    }

    private void stopRecordScreen() {
        if (virtualDisplay == null) {
            return;
        }
        virtualDisplay.release();
        destroyMediaProject();
    }

    private void destroyMediaProject() {
        if (mediaProjection != null) {
            mediaProjection.stop();
            mediaProjection = null;
        }
    }
}

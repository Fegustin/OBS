package com.example.screen_recording.screens.main;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;

import android.media.AudioFormat;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.SparseIntArray;
import android.view.MenuItem;
import android.view.Surface;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;
import android.widget.ToggleButton;
import android.widget.VideoView;

import com.example.screen_recording.R;
import com.example.screen_recording.SettingActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.snackbar.Snackbar;

import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_CODE = 1000;
    private static final int REQUEST_PERMISSION = 1001;
    private static final SparseIntArray ORIENTATIONS = new SparseIntArray();
    private boolean isRecord = false;
    private boolean isPause = false;
    private String videoUri = "";

    private MediaProjectionManager mediaProjectionManager;
    private MediaProjection mediaProjection;
    private VirtualDisplay virtualDisplay;
    private MediaProjectionCallBack mediaProjectionCallBack;
    private MediaRecorder mediaRecorder;

    private int mScreenDensity;
    private static int DISPLAY_WIDTH = 720;
    private static int DISPLAY_HEIGHT = 1280;

    static {
        ORIENTATIONS.append(Surface.ROTATION_0, 90);
        ORIENTATIONS.append(Surface.ROTATION_90, 0);
        ORIENTATIONS.append(Surface.ROTATION_180, 270);
        ORIENTATIONS.append(Surface.ROTATION_270, 180);
    }

    //View
    private ConstraintLayout rootLayout;
    private VideoView videoView;
    private Button buttonStart;
    private Button buttonStop;
    private RadioGroup radioGroup;
    private RadioButton radioButton;
    private ImageButton imageButtonToggle;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        mScreenDensity = metrics.densityDpi;
        DISPLAY_WIDTH = metrics.widthPixels;
        DISPLAY_HEIGHT = metrics.heightPixels;


        mediaRecorder = new MediaRecorder();
        mediaProjectionManager = (MediaProjectionManager) getSystemService(Context.MEDIA_PROJECTION_SERVICE);

        // View
        videoView = findViewById(R.id.videoView);
        rootLayout = findViewById(R.id.rootLayout);
        buttonStart = findViewById(R.id.buttonStart);
        buttonStop = findViewById(R.id.buttonStop);
        radioGroup = findViewById(R.id.radioGroupFPS);
        imageButtonToggle = findViewById(R.id.imageButtonToggle);

        // Bottom Navigation
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigation);
        bottomNavigationView.setSelectedItemId(R.id.mainMenu);

        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.mainMenu:
                        return true;
                    case R.id.settingMenu:
                        startActivity(new Intent(getApplicationContext(), SettingActivity.class));
                        overridePendingTransition(0, 0);
                        return true;
                }
                return false;
            }
        });

        // Event
        buttonStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        + ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.RECORD_AUDIO)
                        != PackageManager.PERMISSION_GRANTED) {

                    Toast.makeText(MainActivity.this, "Для записи приложению нужны разрешения", Toast.LENGTH_SHORT).show();

                    if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                            || ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, Manifest.permission.RECORD_AUDIO)) {

                        Snackbar.make(rootLayout, "Права доступа", Snackbar.LENGTH_INDEFINITE)
                                .setAction("Включить", new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        ActivityCompat.requestPermissions(MainActivity.this,
                                                new String[]{

                                                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                                                        Manifest.permission.RECORD_AUDIO

                                                }, REQUEST_PERMISSION);
                                    }
                                }).show();
                    } else {
                        ActivityCompat.requestPermissions(MainActivity.this,
                                new String[]{

                                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                                        Manifest.permission.RECORD_AUDIO

                                }, REQUEST_PERMISSION);
                    }
                } else {
                    // get Quality
                    if (!isRecord) {
                        int selectFPSid = radioGroup.getCheckedRadioButtonId();
                        radioButton = findViewById(selectFPSid);
                        String btnQuality = String.valueOf(radioButton.getText());
                        final int QUALITY = Integer.parseInt(btnQuality.substring(0, btnQuality.length() - 1));

                        initRecorder(QUALITY);
                        recorderScreen();

                        Snackbar.make(rootLayout, "Начало записи", Snackbar.LENGTH_SHORT)
                                .show();
                    }
                }
            }
        });

        buttonStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isRecord) {
                    mediaRecorder.stop();
                    mediaRecorder.reset();
                    stopRecordScreen();
                    Snackbar.make(rootLayout, "Запись остановлена", Snackbar.LENGTH_SHORT)
                            .show();

                    isRecord = false;
                } else {
                    Snackbar.make(rootLayout, "Включите запись перед тем как её останавливать", Snackbar.LENGTH_SHORT)
                            .show();
                }

                videoView.setVisibility(View.VISIBLE);
                videoView.setVideoURI(Uri.parse(videoUri));
                videoView.start();
            }
        });


        // Pause and Resume record
        imageButtonToggle.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onClick(View v) {
                if (isRecord) {
                    if (!isPause) {
                        mediaRecorder.pause();
                        isPause = true;
                        imageButtonToggle.setImageResource(R.drawable.pause);
                    } else {
                        mediaRecorder.resume();
                        isPause = false;
                        imageButtonToggle.setImageResource(R.drawable.play);
                    }
                } else {
                    Snackbar.make(rootLayout, "Включите запись перед тем как её останавливать", Snackbar.LENGTH_SHORT)
                            .show();
                }
            }
        });
    }


    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void recorderScreen() {
        if (mediaProjection == null) {
            startActivityForResult(mediaProjectionManager.createScreenCaptureIntent(), REQUEST_CODE);
            return;
        }
        virtualDisplay = createVirtualDisplay();
        mediaRecorder.start();
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private VirtualDisplay createVirtualDisplay() {
        return mediaProjection.createVirtualDisplay("MainActivity", DISPLAY_WIDTH, DISPLAY_HEIGHT, mScreenDensity,
                DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
                mediaRecorder.getSurface(), null, null);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void initRecorder(int QUALITY) {
        try {
            isRecord = true;
            CamcorderProfile cpHigh;
            int bitRateVideo;
            switch (QUALITY){
                case 1080:
                    cpHigh = CamcorderProfile.get(CamcorderProfile.QUALITY_1080P);
                    bitRateVideo = 1000 * 10000;
                    break;
                case 720:
                    cpHigh = CamcorderProfile.get(CamcorderProfile.QUALITY_720P);
                    bitRateVideo = 1000 * 4000;
                    break;
                default:
                    cpHigh = CamcorderProfile.get(CamcorderProfile.QUALITY_480P);
                    bitRateVideo = 1000 * 2000;
                    break;
            }

            mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            mediaRecorder.setVideoSource(MediaRecorder.VideoSource.SURFACE);
            mediaRecorder.setAudioSamplingRate(44100);
            mediaRecorder.setAudioEncodingBitRate(16*44100);
            mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);


            // Переписать на более подхожящий код

            String recordPath = MainActivity.this.getExternalFilesDir("/").getAbsolutePath();
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy_MM_dd_hh_mm_ss", Locale.getDefault());
            Date now = new Date();
            videoUri = recordPath + "MagaRecord_" + dateFormat.format(now) + ".mp4";

//            videoUri = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM)
//                    + new StringBuilder("/EDMTRecord_").append(new SimpleDateFormat("dd-MM-yyyy-hh-mm-ss")
//                    .format(new Date())).append(".mp4").toString();

            Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
            File f = new File(videoUri);
            Uri contentUri = Uri.fromFile(f);
            mediaScanIntent.setData(contentUri);
            MainActivity.this.sendBroadcast(mediaScanIntent);

            mediaRecorder.setOutputFile(videoUri);
            mediaRecorder.setVideoSize(DISPLAY_WIDTH, DISPLAY_HEIGHT);
            mediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
            mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
            mediaRecorder.setVideoEncodingBitRate(cpHigh.videoBitRate);
            mediaRecorder.setCaptureRate(30);
            mediaRecorder.setVideoFrameRate(30);

            int rotation = getWindowManager().getDefaultDisplay().getRotation();
            int orientation = ORIENTATIONS.get(rotation + 90);
            mediaRecorder.setOrientationHint(orientation);
            mediaRecorder.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode != REQUEST_CODE) {
            Toast.makeText(this, "Unk error", Toast.LENGTH_SHORT).show();
            return;
        }

        if (resultCode != RESULT_OK) {
            Toast.makeText(this, "Доступ запрещен", Toast.LENGTH_SHORT).show();
            return;
        }

        mediaProjectionCallBack = new MediaProjectionCallBack();
        if (data != null) {
            mediaProjection = mediaProjectionManager.getMediaProjection(resultCode, data);
            mediaProjection.registerCallback(mediaProjectionCallBack, null);
            virtualDisplay = createVirtualDisplay();
            mediaRecorder.start();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private class MediaProjectionCallBack extends MediaProjection.Callback {
        @Override
        public void onStop() {
            mediaRecorder.stop();
            mediaRecorder.reset();
            mediaProjection = null;
            stopRecordScreen();
            super.onStop();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void stopRecordScreen() {
        if (virtualDisplay == null) {
            return;
        }
        virtualDisplay.release();
        destroyMediaProject();
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void destroyMediaProject() {
        if (mediaProjection != null) {
            mediaProjection.unregisterCallback(mediaProjectionCallBack);
            mediaProjection.stop();
            mediaProjection = null;
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQUEST_PERMISSION: {
                if ((grantResults.length > 0) && (grantResults[0] + grantResults[1] == PackageManager.PERMISSION_GRANTED)) {
                } else {
                    Snackbar.make(rootLayout, "Права доступа", Snackbar.LENGTH_INDEFINITE)
                            .setAction("Включить", new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    ActivityCompat.requestPermissions(MainActivity.this,
                                            new String[]{

                                                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                                                    Manifest.permission.RECORD_AUDIO

                                            }, REQUEST_PERMISSION);
                                }
                            }).show();
                }
                return;
            }
        }
    }
}

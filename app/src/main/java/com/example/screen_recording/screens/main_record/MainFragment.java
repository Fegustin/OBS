package com.example.screen_recording.screens.main_record;

import android.Manifest;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.SparseIntArray;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.screen_recording.R;
import com.example.screen_recording.service.TimerService;
import com.example.screen_recording.view_model.TimerViewModel;
import com.google.android.material.snackbar.Snackbar;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainFragment extends Fragment {

    SharedPreferences p;
    TimerViewModel model;

    private static final int REQUEST_CODE = 1000;
    private static final int REQUEST_PERMISSION = 1001;
    private static final SparseIntArray ORIENTATIONS = new SparseIntArray();
    private boolean isRecord = false;
    private boolean isPause = false;
    private boolean isNightTheme = false;
    private String videoUri = "";
    private String timerState = "";

    private MediaProjectionManager mediaProjectionManager;
    private MediaProjection mediaProjection;
    private VirtualDisplay virtualDisplay;
    private MediaProjectionCallBack mediaProjectionCallBack;
    private MediaRecorder mediaRecorder;

    private int mScreenDensity;
    private static int DISPLAY_WIDTH;
    private static int DISPLAY_HEIGHT;

    static {
        ORIENTATIONS.append(Surface.ROTATION_0, 90);
        ORIENTATIONS.append(Surface.ROTATION_90, 0);
        ORIENTATIONS.append(Surface.ROTATION_180, 270);
        ORIENTATIONS.append(Surface.ROTATION_270, 180);
    }

    //View
    private CardView rootLayout;
    private VideoView videoView;
    private ImageButton imageTogglePauseAndResume;
    private ToggleButton toggleButton;
    private TextView textViewTimer;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main, container, false);
        p = PreferenceManager.getDefaultSharedPreferences(getActivity());
        isNightTheme = p.getBoolean("isNightTheme", false);

        DisplayMetrics metrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(metrics);
        mScreenDensity = metrics.densityDpi;
        DISPLAY_WIDTH = metrics.widthPixels;
        DISPLAY_HEIGHT = metrics.heightPixels;


        mediaRecorder = new MediaRecorder();
        mediaProjectionManager = (MediaProjectionManager) getActivity().getSystemService(Context.MEDIA_PROJECTION_SERVICE);

        // View
        videoView = view.findViewById(R.id.videoView);
        rootLayout = view.findViewById(R.id.cardView);
        imageTogglePauseAndResume = view.findViewById(R.id.imageTogglePauseAndResume);
        toggleButton = view.findViewById(R.id.toggleButton);
        textViewTimer = view.findViewById(R.id.textViewTimer);

        // ViewModal
        model = new ViewModelProvider(getActivity()).get(TimerViewModel.class);
        textViewTimer.setText(model.timeState);

        if (isNightTheme) {
            rootLayout.setBackgroundResource(R.color.colorBlack);
            imageTogglePauseAndResume.setBackgroundResource(R.color.colorBlue);
            textViewTimer.setTextColor(Color.WHITE);
        }

        // Event

        //Record Toggle Start and Stop
        toggleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        + ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.RECORD_AUDIO)
                        != PackageManager.PERMISSION_GRANTED) {

                    errorRecordAction();

                    if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE)
                            || ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), Manifest.permission.RECORD_AUDIO)) {

                        errorRecordAction();
                        Snackbar.make(rootLayout, "Разрешения", Snackbar.LENGTH_INDEFINITE)
                                .setAction("Включить", new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        ActivityCompat.requestPermissions(getActivity(),
                                                new String[]{

                                                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                                                        Manifest.permission.RECORD_AUDIO

                                                }, REQUEST_PERMISSION);
                                    }
                                }).show();

                    } else {

                        ActivityCompat.requestPermissions(getActivity(),
                                new String[]{

                                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                                        Manifest.permission.RECORD_AUDIO

                                }, REQUEST_PERMISSION);

                    }
                } else {
                    toggleScreenShare(toggleButton);
                }
            }
        });

        // Pause and Resume record
        imageTogglePauseAndResume.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onClick(View v) {
                if (isRecord) {
                    if (!isPause) {
                        mediaRecorder.pause();
                        isPause = true;
                        imageTogglePauseAndResume.setImageResource(R.drawable.pause);
                    } else {
                        mediaRecorder.resume();
                        isPause = false;
                        imageTogglePauseAndResume.setImageResource(R.drawable.play);
                    }
                } else {
                    Snackbar.make(rootLayout, "Включите запись перед тем как её останавливать", Snackbar.LENGTH_SHORT)
                            .show();
                }
            }
        });
        return view;
    }

    private void toggleScreenShare(View v) {

        if (((ToggleButton) v).isChecked()) {
            int quality = p.getInt("quality", 480);
            boolean micro = p.getBoolean("micro", false);
            int fps = p.getInt("FPS", 15);

            initRecorder(quality, micro, fps);
            recorderScreen();

            isRecord = true;
            p.edit().putBoolean("isRecord", isRecord).apply();
        } else {
            mediaRecorder.stop();
            mediaRecorder.reset();

            stopRecordScreen();

            videoView.setVisibility(View.VISIBLE);
            videoView.setVideoURI(Uri.parse(videoUri));
            videoView.start();

            isRecord = false;
            p.edit().putBoolean("isRecord", isRecord).apply();

            getActivity().stopService(new Intent(getContext(), TimerService.class));
        }
    }

    private BroadcastReceiver uiUpdated = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            textViewTimer.setText(intent.getStringExtra("countdown"));
            model.timeState = intent.getStringExtra("countdown");
        }
    };

    private void recorderScreen() {
        if (mediaProjection == null) {
            startActivityForResult(mediaProjectionManager.createScreenCaptureIntent(), REQUEST_CODE);
            return;
        }
        virtualDisplay = createVirtualDisplay();
        mediaRecorder.start();
    }

    private VirtualDisplay createVirtualDisplay() {
        return mediaProjection.createVirtualDisplay("MainFragment", DISPLAY_WIDTH, DISPLAY_HEIGHT, mScreenDensity,
                DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
                mediaRecorder.getSurface(), null, null);
    }

    private void initRecorder(int QUALITY, boolean isMicro, int fps) {
        try {
            CamcorderProfile cpHigh;
            switch (QUALITY) {
                case 1080:
                    cpHigh = CamcorderProfile.get(CamcorderProfile.QUALITY_1080P);
                    break;
                case 720:
                    cpHigh = CamcorderProfile.get(CamcorderProfile.QUALITY_720P);
                    break;
                default:
                    cpHigh = CamcorderProfile.get(CamcorderProfile.QUALITY_480P);
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
            mediaRecorder.setVideoEncodingBitRate(cpHigh.videoBitRate);
            mediaRecorder.setCaptureRate(fps);
            mediaRecorder.setVideoFrameRate(fps);

            int rotation = getActivity().getWindowManager().getDefaultDisplay().getRotation();
            int orientation = ORIENTATIONS.get(rotation + 90);
            mediaRecorder.setOrientationHint(orientation);
            mediaRecorder.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode != REQUEST_CODE) {
            Toast.makeText(getActivity(), "Unk error", Toast.LENGTH_SHORT).show();
            errorRecordAction();
            return;
        }

        if (resultCode != Activity.RESULT_OK) {
            Log.i("Разрешения", "Я зашел");
            Toast.makeText(getActivity(), "Доступ запрещен", Toast.LENGTH_SHORT).show();
            errorRecordAction();
            return;
        }

        mediaProjectionCallBack = new MediaProjectionCallBack();
        if (data != null) {
            mediaProjection = mediaProjectionManager.getMediaProjection(resultCode, data);
            mediaProjection.registerCallback(mediaProjectionCallBack, null);
            virtualDisplay = createVirtualDisplay();
            mediaRecorder.start();
        } else {
            Toast.makeText(getActivity(), "Не удалось запустить запись", Toast.LENGTH_SHORT).show();
            Log.i("dataActivity", "data = null");
        }

        getActivity().startService(new Intent(getContext(), TimerService.class));
        getActivity().registerReceiver(uiUpdated, new IntentFilter("COUNTDOWN_UPDATED"));
    }

    private class MediaProjectionCallBack extends MediaProjection.Callback {
        @Override
        public void onStop() {
            if (toggleButton.isChecked()) {
                errorRecordAction();
                mediaRecorder.stop();
                mediaRecorder.reset();
            }
            mediaProjection = null;
            stopRecordScreen();
            super.onStop();
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
            mediaProjection.unregisterCallback(mediaProjectionCallBack);
            mediaProjection.stop();
            mediaProjection = null;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQUEST_PERMISSION: {
                if ((grantResults.length > 0) && (grantResults[0] + grantResults[1] == PackageManager.PERMISSION_GRANTED)) {
                    toggleScreenShare(toggleButton);
                } else {
                    errorRecordAction();
                    Snackbar.make(rootLayout, "Права доступа", Snackbar.LENGTH_INDEFINITE)
                            .setAction("Включить", new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    ActivityCompat.requestPermissions(getActivity(),
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

    private void errorRecordAction() {
        getActivity().stopService(new Intent(getContext(), TimerService.class));
        isRecord = false;
        toggleButton.setChecked(false);
        mediaRecorder.reset();
    }
}

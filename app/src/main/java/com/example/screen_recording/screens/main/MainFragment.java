package com.example.screen_recording.screens.main;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
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
import android.widget.Toast;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.screen_recording.R;
import com.google.android.material.snackbar.Snackbar;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainFragment extends Fragment {

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
    private CardView rootLayout;
    private VideoView videoView;
    private ImageButton imageTogglePauseAndResume;
    private ImageButton imageToggleRecord;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main, container, false);

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
        imageToggleRecord = view.findViewById(R.id.imageToggleRecord);

        // Event

        //Record Start and Stop
        imageToggleRecord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        + ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.RECORD_AUDIO)
                        != PackageManager.PERMISSION_GRANTED) {

                    if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE)
                            || ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), Manifest.permission.RECORD_AUDIO)) {

                        isRecord = false;

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
                    toggleRecord(isRecord);
                    // get Quality
////                        int selectFPSid = radioGroup.getCheckedRadioButtonId();
////                        radioButton = findViewById(selectFPSid);
////                        String btnQuality = String.valueOf(radioButton.getText());
////                        final int QUALITY = Integer.parseInt(btnQuality.substring(0, btnQuality.length() - 1));
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

    private void toggleRecord(boolean record) {
        if (!record) {
            isRecord = true;
            imageToggleRecord.setImageResource(R.drawable.ic_album_red);

            // get settings
            SharedPreferences p = PreferenceManager.getDefaultSharedPreferences(getActivity());
            int quality = p.getInt("quality", 480);
            boolean micro = p.getBoolean("micro", false);
            int fps = p.getInt("FPS", 15);

            initRecorder(quality, micro, fps);
            recorderScreen();

            Snackbar.make(rootLayout, "Начало записи", Snackbar.LENGTH_SHORT)
                    .show();
        } else {
            isRecord = false;
            imageToggleRecord.setImageResource(R.drawable.ic_album_gray);

            mediaRecorder.stop();
            mediaRecorder.reset();

            Snackbar.make(rootLayout, "Запись остановлена", Snackbar.LENGTH_SHORT)
                    .show();

            stopRecordScreen();

            videoView.setVisibility(View.VISIBLE);
            videoView.setVideoURI(Uri.parse(videoUri));
            videoView.start();
        }
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


            // Переписать на более подхожящий код

//            String recordPath = MainActivity.this.getExternalFilesDir("/").getAbsolutePath();
//            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy_MM_dd_hh_mm_ss", Locale.getDefault());
//            Date now = new Date();
//            videoUri = recordPath + "MagaRecord_" + dateFormat.format(now) + ".mp4";
//
//            ContentResolver resolver = getApplicationContext().getContentResolver();
//            Uri audioCollection = MediaStore.Video.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY);
//            ContentValues newSongDetails = new ContentValues();
//            newSongDetails.put(MediaStore.Video.Media.DISPLAY_NAME, videoUri);


            videoUri = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM)
                    + new StringBuilder("/EDMTRecord_").append(new SimpleDateFormat("dd-MM-yyyy-hh-mm-ss")
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

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode != REQUEST_CODE) {
            Toast.makeText(getActivity(), "Unk error", Toast.LENGTH_SHORT).show();
            return;
        }

        if (resultCode != Activity.RESULT_OK) {
            Toast.makeText(getActivity(), "Доступ запрещен", Toast.LENGTH_SHORT).show();
            isRecord = false;
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
            if (isRecord) {
                isRecord = false;
                mediaRecorder.stop();
                mediaRecorder.reset();
            }
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
                    toggleRecord(isRecord);
                } else {
                    isRecord = false;
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
}

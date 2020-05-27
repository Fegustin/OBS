package com.example.screen_recording.screens.main_record;

import android.Manifest;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
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
import com.example.screen_recording.service.RecordService;
import com.example.screen_recording.service.TimerService;
import com.example.screen_recording.view_model.TimerViewModel;
import com.google.android.material.snackbar.Snackbar;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainFragment extends Fragment {

    SharedPreferences p;
    TimerViewModel model;
    Intent intent;

    private static final int REQUEST_CODE = 1000;
    private static final int REQUEST_PERMISSION = 1001;
    private boolean isNightTheme = false;

    private MediaProjectionManager mediaProjectionManager;

    //View
    private CardView rootLayout;
    private ToggleButton toggleButton;
    private TextView textViewTimer;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main, container, false);
        p = PreferenceManager.getDefaultSharedPreferences(getActivity());
        isNightTheme = p.getBoolean("isNightTheme", false);
        intent = new Intent(getActivity(), RecordService.class);

        DisplayMetrics metrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(metrics);

        // View
        rootLayout = view.findViewById(R.id.cardView);
        toggleButton = view.findViewById(R.id.toggleButton);
        textViewTimer = view.findViewById(R.id.textViewTimer);

        // ViewModal
        model = new ViewModelProvider(getActivity()).get(TimerViewModel.class);
        textViewTimer.setText(model.timeState);

        if (isNightTheme) {
            rootLayout.setBackgroundResource(R.color.colorBlack);
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

                    toggleButton.setChecked(false);

                    if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE)
                            || ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), Manifest.permission.RECORD_AUDIO)) {

                        toggleButton.setChecked(false);
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
                    if (toggleButton.isChecked()) {
                        recordVideo();
                    } else {
                        getActivity().stopService(intent);
                    }
                }
            }
        });
        return view;
    }

    private void recordVideo() {
        mediaProjectionManager = (MediaProjectionManager) getActivity().getSystemService(Context.MEDIA_PROJECTION_SERVICE);
        startActivityForResult(mediaProjectionManager.createScreenCaptureIntent(), REQUEST_CODE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode != REQUEST_CODE) {
            toggleButton.setChecked(false);
            Toast.makeText(getActivity(), "Unk error", Toast.LENGTH_SHORT).show();
            return;
        }

        if (resultCode != Activity.RESULT_OK) {
            toggleButton.setChecked(false);
            Toast.makeText(getActivity(), "Доступ запрещен", Toast.LENGTH_SHORT).show();
            return;
        }

        intent = new Intent(getActivity(), RecordService.class)
                .putExtra("code", resultCode)
                .putExtra("data", data);


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            getActivity().startForegroundService(intent);
        } else {
            getActivity().startService(intent);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQUEST_PERMISSION: {
                if ((grantResults.length > 0) && (grantResults[0] + grantResults[1] == PackageManager.PERMISSION_GRANTED)) {

                } else {
                    toggleButton.setChecked(false);
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

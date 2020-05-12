package com.example.screen_recording.screens.setting;

import android.Manifest;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.screen_recording.R;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;

public class SettingFragment extends Fragment {

    private static final int REQUEST_PERMISSION = 228;

    private ConstraintLayout layoutQuality;
    private ConstraintLayout layoutFPS;
    private ConstraintLayout layoutSetting;
    private CheckBox checkBoxMicro;
    private TextView textViewQuality;
    private TextView textViewFPS;
    private TextView textViewFile;

    private CharSequence[] mAlertItem;
    SharedPreferences preferences;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_setting, container, false);

        layoutQuality = view.findViewById(R.id.layoutQuality);
        layoutFPS = view.findViewById(R.id.layoutFPS);
        layoutSetting = view.findViewById(R.id.layoutSetting);
        checkBoxMicro = view.findViewById(R.id.checkBoxMicro);
        textViewQuality = view.findViewById(R.id.textViewQuality);
        textViewFPS = view.findViewById(R.id.textViewFPS);
        textViewFile = view.findViewById(R.id.textViewFile);

        preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());

        String fileVideoPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).toString();
        textViewFile.setText(fileVideoPath);

        // Set Quality and get Quality
        showTextViewQuality(preferences.getInt("quality", 15));
        layoutQuality.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                mAlertItem = new CharSequence[]{
                        "Высокое",
                        "Среднее",
                        "Низкое"
                };

                MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(getActivity());
                builder.setTitle(R.string.Quality);
                builder.setIcon(R.drawable.ic_quality);
                builder.setSingleChoiceItems(mAlertItem, preferences.getInt("checkedItemQuality", 0), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case 0:
                                preferences.edit().putInt("quality", 1080).apply();
                                preferences.edit().putInt("checkedItemQuality", 0).apply();
                                dialog.dismiss();
                                break;
                            case 1:
                                preferences.edit().putInt("quality", 720).apply();
                                preferences.edit().putInt("checkedItemQuality", 1).apply();
                                dialog.dismiss();
                                break;
                            default:
                                preferences.edit().putInt("quality", 480).apply();
                                preferences.edit().putInt("checkedItemQuality", 2).apply();
                                dialog.dismiss();
                                break;
                        }
                        showTextViewQuality(preferences.getInt("quality", 15));
                    }
                });
                builder.setNegativeButton("Отмена", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
                builder.show();
            }
        });

        // Set state Microphone and get state Microphone
        checkBoxMicro.setChecked(preferences.getBoolean("micro", false));

        checkBoxMicro.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.RECORD_AUDIO)
                        != PackageManager.PERMISSION_GRANTED) {
                    if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), Manifest.permission.RECORD_AUDIO)) {

                        checkBoxMicro.setChecked(false);
                        preferences.edit().putBoolean("micro", checkBoxMicro.isChecked()).apply();

                        Snackbar.make(layoutSetting, "Разрешения", Snackbar.LENGTH_INDEFINITE)
                                .setAction("Включить", new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        ActivityCompat.requestPermissions(getActivity(),
                                                new String[]{
                                                        Manifest.permission.RECORD_AUDIO

                                                }, REQUEST_PERMISSION);
                                    }
                                }).show();
                    } else {
                        checkBoxMicro.setChecked(false);
                        preferences.edit().putBoolean("micro", checkBoxMicro.isChecked()).apply();

                        ActivityCompat.requestPermissions(getActivity(),
                                new String[]{
                                        Manifest.permission.RECORD_AUDIO
                                },
                                REQUEST_PERMISSION);
                    }
                } else {
                    checkBoxMicro.setChecked(true);
                    preferences.edit().putBoolean("micro", checkBoxMicro.isChecked()).apply();
                }
            }
        });

        // Set Fps and Get Fps
        showTextViewFPS(preferences.getInt("FPS", 15));
        layoutFPS.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAlertItem = new CharSequence[]{
                        "60FPS",
                        "30FPS",
                        "15FPS"
                };

                MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(getActivity());
                builder.setTitle(R.string.FPS);
                builder.setIcon(R.drawable.ic_videogame);
                builder.setSingleChoiceItems(mAlertItem, preferences.getInt("checkedItemFPS", 0), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case 0:
                                preferences.edit().putInt("FPS", 60).apply();
                                preferences.edit().putInt("checkedItemFPS", 0).apply();
                                dialog.dismiss();
                                break;
                            case 1:
                                preferences.edit().putInt("FPS", 30).apply();
                                preferences.edit().putInt("checkedItemFPS", 1).apply();
                                dialog.dismiss();
                                break;
                            default:
                                preferences.edit().putInt("FPS", 15).apply();
                                preferences.edit().putInt("checkedItemFPS", 2).apply();
                                dialog.dismiss();
                                break;
                        }
                        showTextViewFPS(preferences.getInt("FPS", 15));
                    }
                });
                builder.setNegativeButton("Отмена", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
                builder.show();
            }
        });

        return view;
    }

    private void showTextViewQuality(int quality) {
        switch (quality){
            case 1080:
                textViewQuality.setText("Высокое");
                break;
            case 720:
                textViewQuality.setText("Среднее");
                break;
            default:
                textViewQuality.setText("Низкое");
                break;
        }
    }

    private void showTextViewFPS(int fps) {
        switch (fps){
            case 60:
                textViewFPS.setText("60FPS");
                break;
            case 30:
                textViewFPS.setText("30FPS");
                break;
            default:
                textViewFPS.setText("15FPS");
                break;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQUEST_PERMISSION: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    checkBoxMicro.setChecked(true);
                    preferences.edit().putBoolean("micro", checkBoxMicro.isChecked()).apply();

                } else {
                    checkBoxMicro.setChecked(false);
                    preferences.edit().putBoolean("micro", checkBoxMicro.isChecked()).apply();

                    Snackbar.make(layoutSetting, "Разрешения", Snackbar.LENGTH_INDEFINITE)
                            .setAction("Включить", new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    ActivityCompat.requestPermissions(getActivity(),
                                            new String[]{
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

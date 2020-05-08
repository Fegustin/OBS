package com.example.screen_recording.screens.setting;

import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;

import com.example.screen_recording.R;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

public class SettingFragment extends Fragment {

    private ConstraintLayout layoutQuality;
    private ConstraintLayout layoutFPS;
    private CheckBox checkBoxMicro;
    private TextView textViewQuality;
    private TextView textViewFPS;

    private CharSequence[] mAlertItem;
    SharedPreferences preferences;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_setting, container, false);

        layoutQuality = view.findViewById(R.id.layoutQuality);
        layoutFPS = view.findViewById(R.id.layoutFPS);
        checkBoxMicro = view.findViewById(R.id.checkBoxMicro);
        textViewQuality = view.findViewById(R.id.textViewQuality);
        textViewFPS = view.findViewById(R.id.textViewFPS);

        preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());

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
                builder.setSingleChoiceItems(mAlertItem, 0, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case 0:
                                preferences.edit().putInt("quality", 1080).apply();
                                break;
                            case 1:
                                preferences.edit().putInt("quality", 720).apply();
                                break;
                            default:
                                preferences.edit().putInt("quality", 480).apply();
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
                preferences.edit().putBoolean("micro", checkBoxMicro.isChecked()).apply();
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
                builder.setSingleChoiceItems(mAlertItem, 0, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case 0:
                                preferences.edit().putInt("FPS", 60).apply();
                                break;
                            case 1:
                                preferences.edit().putInt("FPS", 30).apply();
                                break;
                            default:
                                preferences.edit().putInt("FPS", 15).apply();
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
}

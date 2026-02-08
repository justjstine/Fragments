package com.example.fragments;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class WinterFragmentScene extends Fragment {

    public static final String SELECTED_BUTTON_ID = "selected_button_id";
    private View lastSelectedButton = null;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflates fragment_winter_scene.xml
        View view = inflater.inflate(R.layout.fragment_winter_scene, container, false);

        // Buttons for the different paths
        View quiet = view.findViewById(R.id.btnQuietPath);
        View gentle = view.findViewById(R.id.btnGentlePath);
        View peaceful = view.findViewById(R.id.btnPeacefulPath);
        View calm = view.findViewById(R.id.btnCalmPath);

        View.OnClickListener pathListener = v -> {
            if (lastSelectedButton != null) {
                lastSelectedButton.setActivated(false);
            }
            v.setActivated(true);
            lastSelectedButton = v;
            playSparkleSound();
            navigateToNextScene(v.getId());
        };

        quiet.setOnClickListener(pathListener);
        gentle.setOnClickListener(pathListener);
        peaceful.setOnClickListener(pathListener);
        calm.setOnClickListener(pathListener);

        return view;
    }

    private void playSparkleSound() {
        MediaPlayer sparklePlayer = MediaPlayer.create(getContext(), R.raw.sfx_sparkle);
        if (sparklePlayer != null) {
            sparklePlayer.setVolume(0.4f, 0.4f);
            sparklePlayer.setOnCompletionListener(MediaPlayer::release);
            sparklePlayer.start();
        }
    }

    private void navigateToNextScene(int selectedButtonId) {
        // Transition to the next logic class
        WinterFragmentScene1 nextScene = new WinterFragmentScene1();
        Bundle args = new Bundle();
        args.putInt(SELECTED_BUTTON_ID, selectedButtonId);
        nextScene.setArguments(args);

        if (getParentFragmentManager() != null) {
            getParentFragmentManager().beginTransaction()
                    .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
                    .replace(R.id.fragment_container, nextScene)
                    .commit();
        }
    }
}
package com.example.musicalears.Fragment;

import android.animation.ValueAnimator;
import android.os.Bundle;
import android.os.CountDownTimer;

import androidx.constraintlayout.widget.ConstraintLayout;

import com.example.musicalears.IntervalTrainingActivity;
import com.example.musicalears.R;

import java.util.Objects;

public class IntervalMainNoteFragment extends NoteFragment {
    private static final String PARAM_DURATION = "duration";
    private static final String PARAM_SHOULD_SHOW_NOTE_NAME = "showName";

    private long paramDuration;
    private boolean paramShouldShowNoteName;

    public IntervalMainNoteFragment() {
        // Required empty public constructor
    }

    public static IntervalMainNoteFragment newInstance(long duration, boolean showNote) {
        IntervalMainNoteFragment fragment = new IntervalMainNoteFragment();
        Bundle args = new Bundle();
        args.putLong(PARAM_DURATION, duration);
        args.putBoolean(PARAM_SHOULD_SHOW_NOTE_NAME, showNote);
        fragment.setArguments(args);
        return fragment;
    }

    public void processPitch(float pitchInHz) {
        float[] noteBubbleParams = super.getNoteBubbleParams(pitchInHz);
        super.moveBubble(noteBubbleParams);
        if (checkAccuracy(noteBubbleParams[0])) {
            noteBubble.setImageResource(R.drawable.note_bubble_on);
            updateTimer();
        } else {
            noteBubble.setImageResource(R.drawable.note_bubble_off);
            stopTimer();
        }
    }

    public void stopTimer() {
        if (accuracyTimer != null) {
            accuracyTimer.cancel();
            accuracyTimer = null;

            if (progressBarAnimator != null) progressBarAnimator.cancel();
            progressBarAnimator = ValueAnimator.ofFloat(progressBar.getMeasuredWidth(), 1);
            progressBarAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator valueAnimator) {
                    float value = (float) valueAnimator.getAnimatedValue();
                    ConstraintLayout.LayoutParams progressBarLayoutParams = (ConstraintLayout.LayoutParams) progressBar.getLayoutParams();
                    progressBarLayoutParams.width = (int) value;
                    progressBar.setLayoutParams(progressBarLayoutParams);
                }
            });
            progressBarAnimator.setDuration(1000);
            progressBarAnimator.start();
        }
    }

    public void resetSelf() {
        new android.os.Handler().postDelayed(
                new Runnable() {
                    public void run() {
                        stopTimer();
                        targetNote = null;
                        Objects.requireNonNull(getActivity()).findViewById(R.id.skipView).setAlpha(0.5f);
                        ConstraintLayout.LayoutParams noteBubbleLayoutParams = (ConstraintLayout.LayoutParams) noteBubble.getLayoutParams();
                        noteBubbleLayoutParams.verticalBias = (float) 0.5;
                        noteBubble.setLayoutParams(noteBubbleLayoutParams);
                        noteBubble.setImageResource(R.drawable.note_bubble_off);
                        noteText.setText("");
                        targetNoteText.setText("");
                    }
                },
                100);
    }

    @Override
    public void setTargetNote(int adjustedIndex) {
        super.setTargetNote(adjustedIndex);
        if (paramShouldShowNoteName && targetNote != null)
            targetNoteText.setText(targetNote.getNoteName());
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() == null) {
            return;
        }
        paramDuration = getArguments().getLong(PARAM_DURATION);
        paramShouldShowNoteName = getArguments().getBoolean(PARAM_SHOULD_SHOW_NOTE_NAME);
    }

    private void updateTimer() {
        if (accuracyTimer != null) {
            return;
        }
        accuracyTimer = new CountDownTimer(paramDuration, 1000) {
            @Override
            public void onTick(long l) {
            }

            @Override
            public void onFinish() {
                ((IntervalTrainingActivity) Objects.requireNonNull(getActivity())).switchFragments();
            }
        }.start();
        super.updateTimer(paramDuration);
    }

}
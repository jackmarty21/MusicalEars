package com.example.musicalears.Fragment;

import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.fragment.app.Fragment;
import androidx.transition.AutoTransition;
import androidx.transition.TransitionManager;

import com.example.musicalears.Note.Note;
import com.example.musicalears.Note.TargetNote;
import com.example.musicalears.R;

import java.util.List;
import java.util.Objects;

import static com.example.musicalears.Note.NoteList.noteList;

public class NoteFragment extends Fragment {
    ImageView noteBubble;
    TextView noteText;
    TextView targetNoteText;
    View progressBar;
    ValueAnimator progressBarAnimator;
    CountDownTimer accuracyTimer;

    TargetNote targetNote = null;

    private View targetView;
    private ConstraintLayout fragmentLayout;

    private boolean shouldListen = false;

    public NoteFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_pitch_match, container, false);
        noteBubble = view.findViewById(R.id.noteBubble);
        noteText = view.findViewById(R.id.noteText);
        targetNoteText = view.findViewById(R.id.targetNoteText);
        targetView = view.findViewById(R.id.targetLine);
        progressBar = view.findViewById(R.id.progressBar);
        fragmentLayout = view.findViewById(R.id.fragment);
        return view;
    }

    boolean checkAccuracy(float frequency) {
        float upperTwenty = targetNote.getNoteList().get(5).getNoteFrequency() + (targetNote.getNoteList().get(6).getNoteFrequency() - targetNote.getNoteList().get(5).getNoteFrequency()) / 2;
        float lowerTwenty = targetNote.getNoteList().get(5).getNoteFrequency() + (targetNote.getNoteList().get(4).getNoteFrequency() - targetNote.getNoteList().get(5).getNoteFrequency()) / 2;
        return frequency < upperTwenty && frequency > lowerTwenty;
    }

    float[] getNoteBubbleParams(float pitchInHz) {
        final List<Note> list = targetNote.getNoteList();
        float frequency = pitchInHz;

        float minDistance = (float) 10000.0;
        int index = 0;

        while (frequency > list.get(list.size() - 1).getNoteFrequency()) {
            frequency /= 2.0;
            if (frequency < list.get(0).getNoteFrequency()) {
                frequency = list.get(0).getNoteFrequency();
            }
        }

        for (int i = 0; i < list.size(); i++) {
            float distance = Math.abs(list.get(i).getNoteFrequency() - frequency);
            if (distance < minDistance) {
                index = i;
                minDistance = distance;
            }
        }

        return new float[]{frequency, index};
    }

    void moveBubble(float[] bubbleParams) {
        final float frequency = bubbleParams[0];
        final int index = (int) bubbleParams[1];
        final List<Note> list = targetNote.getNoteList();
        final float upperBoundsFrequency = list.get(7).getNoteFrequency();
        final float lowerBoundsFrequency = list.get(3).getNoteFrequency();

        if (frequency < list.get(0).getNoteFrequency())
            noteText.setText(list.get(0).getNoteName());
        else if (frequency > list.get(list.size() - 1).getNoteFrequency())
            noteText.setText(list.get(list.size() - 1).getNoteName());
        else
            noteText.setText(list.get(index).getNoteName());
        final float bias = getBias(frequency, lowerBoundsFrequency, upperBoundsFrequency);

        ConstraintSet noteBubbleConstraintSet = new ConstraintSet();
        noteBubbleConstraintSet.clone(fragmentLayout);
        noteBubbleConstraintSet.setVerticalBias(R.id.noteBubble, bias);
        AutoTransition noteBubbleTransition = new AutoTransition();
        noteBubbleTransition.setDuration(100);
        noteBubbleTransition.setInterpolator(new AccelerateDecelerateInterpolator());
        TransitionManager.beginDelayedTransition(fragmentLayout, noteBubbleTransition);
        noteBubbleConstraintSet.applyTo(fragmentLayout);
    }

    void updateTimer(long duration) {
        if (progressBarAnimator != null) {
            progressBarAnimator.cancel();
            progressBarAnimator = null;
        }
        progressBarAnimator = ValueAnimator.ofInt(progressBar.getMeasuredWidth(), targetView.getMeasuredWidth());
        progressBarAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                progressBar.getLayoutParams().width = (Integer) valueAnimator.getAnimatedValue();
                progressBar.requestLayout();
            }
        });
        progressBarAnimator.setDuration(duration);
        AnimatorSet animationSet = new AnimatorSet();
        animationSet.setInterpolator(new AccelerateDecelerateInterpolator());
        animationSet.play(progressBarAnimator);
        animationSet.start();
    }

    void setTargetNote(int adjustedIndex) {
        if (adjustedIndex < 0) {
            targetNote = null;
            return;
        }
        targetNote = new TargetNote(
                noteList.get(adjustedIndex).getNoteName(),
                noteList.get(adjustedIndex).getNoteFrequency(),
                adjustedIndex);
    }

    public void stopTimer() {
        if (accuracyTimer == null) {
            return;
        }
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

    public void resetSelf() {
        new android.os.Handler().postDelayed(
                new Runnable() {
                    public void run() {
                        stopTimer();
                        targetNote = null;
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

    public TargetNote getTargetNote() {
        return targetNote;
    }

    public void setShouldListen(boolean shouldListen) {
        this.shouldListen = shouldListen;
    }

    public boolean getShouldListen() {
        return this.shouldListen;
    }

    public void disableSelf() {
        Objects.requireNonNull(getView()).setBackgroundResource(R.drawable.layout_disabled);
        getView().findViewById(R.id.noteBubble).setAlpha(.5f);
        getView().findViewById(R.id.progressBar).setAlpha(.3f);
        getView().findViewById(R.id.targetLine).setAlpha(.05f);
    }

    public void enableSelf() {
        Objects.requireNonNull(getView()).setBackgroundResource(R.drawable.layout_border);
        getView().findViewById(R.id.noteBubble).setAlpha(1f);
        getView().findViewById(R.id.progressBar).setAlpha(1f);
        getView().findViewById(R.id.targetLine).setAlpha(1f);
    }

    private float getBias(float frequency, float lowerBoundsFrequency, float upperBoundsFrequency) {
        float bias = (float) (.97 - (frequency - lowerBoundsFrequency) / (upperBoundsFrequency - lowerBoundsFrequency));
        if (bias < 0)
            bias = 0;
        else if (bias > 1)
            bias = 1;
        return bias;
    }

}
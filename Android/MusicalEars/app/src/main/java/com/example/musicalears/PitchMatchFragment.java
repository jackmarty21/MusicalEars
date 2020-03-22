package com.example.musicalears;

import android.animation.ValueAnimator;
import android.os.Bundle;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;

import android.os.CountDownTimer;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;
import java.util.Objects;

import static com.example.musicalears.Note.adjustedIntervalNoteList;
import static com.example.musicalears.Note.adjustedNoteList;

public class PitchMatchFragment extends Fragment {
    private static final String PARAM_PARENT_ACTIVITY = "param1";
    private static final String PARAM_IS_DISABLED = "param2";

    private String parentActivity;
    private String isFragmentDisabled;

    private ImageView noteBubble;
    private TextView noteText;
    private TextView targetNoteText;

    private View targetView;
    private View progressBar;

    private TargetNote targetNote = null;

    private ValueAnimator progressBarAnimator;

    boolean didGetBaseNote;

    private CountDownTimer accuracyTimer;
    private boolean shouldListen = false;

    public PitchMatchFragment() {
        // Required empty public constructor
    }

    static PitchMatchFragment newInstance(String parentActivity, String isFragmentDisabled) {
        PitchMatchFragment fragment = new PitchMatchFragment();
        Bundle args = new Bundle();
        args.putString(PARAM_IS_DISABLED, isFragmentDisabled);
        args.putString(PARAM_PARENT_ACTIVITY, parentActivity);
        fragment.setArguments(args);
        return fragment;
    }

    private float getBias(float frequency, float lowerBoundsFrequency, float upperBoundsFrequency) {
        float bias = 1 - (frequency - lowerBoundsFrequency)/(upperBoundsFrequency - lowerBoundsFrequency);
        if (bias < 0) {
            bias = 0;
        } else if (bias > 1) {
            bias = 1;
        }
        return bias;
    }

    private boolean checkAccuracy(float frequency) {
        return frequency < targetNote.getUpperTwenty(didGetBaseNote) &&
                frequency > targetNote.getLowerTwenty(didGetBaseNote);
    }

    void stopTimer() {
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

    private void startTimer(long duration, final boolean scorePoint) {
        if (accuracyTimer == null) {
            accuracyTimer = new CountDownTimer(duration, 300) {
                @Override
                public void onTick(long l) {}

                @Override
                public void onFinish() {
                    if(scorePoint) {
                        scorePointAndReset();
                    } else {
                        resetAndSwitchFragments();
                    }
                }
            }.start();
            if (progressBarAnimator != null) progressBarAnimator.cancel();
            progressBarAnimator = ValueAnimator.ofFloat(progressBar.getMeasuredWidth(), targetView.getMeasuredWidth());
            progressBarAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator valueAnimator) {
                    float value = (float) valueAnimator.getAnimatedValue();
                    ConstraintLayout.LayoutParams progressBarLayoutParams = (ConstraintLayout.LayoutParams) progressBar.getLayoutParams();
                    progressBarLayoutParams.width = (int) value;
                    progressBar.setLayoutParams(progressBarLayoutParams);
                }
            });
            progressBarAnimator.setDuration(duration);
            progressBarAnimator.start();
        }
    }

    private void resetAndSwitchFragments() {
        didGetBaseNote = !didGetBaseNote;
        shouldListen = false;
        disableSelf();
        ((IntervalTrainingActivity) Objects.requireNonNull(getActivity())).switchFragments();
    }

    void resetSelf(boolean isDisabled) {
        if (isDisabled) disableSelf();
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

    private void scorePointAndReset() {
        shouldListen = false;
        switch (parentActivity) {
            case "interval":
                ((IntervalTrainingActivity) Objects.requireNonNull(getActivity())).switchFragments();
                ((IntervalTrainingActivity)getActivity()).scorePointAndReset();
                break;
            case "pitch":
                ((PitchMatchingActivity) Objects.requireNonNull(getActivity())).scorePointAndReset();
                break;
        }
    }

    void processPitch(float pitchInHz, boolean shouldScorePoint) {
        float frequency = pitchInHz;

        float minDistance = (float) 10000.0;
        int index = 0;

        List<Note> list;
        if (!didGetBaseNote) {
            list = adjustedNoteList;
        } else {
            list = adjustedIntervalNoteList;
        }
        while (frequency > list.get(list.size() - 1).getNoteFrequency()) {
            frequency /= 2.0;
            if (frequency < list.get(0).getNoteFrequency()) {
                frequency = list.get(0).getNoteFrequency();
            }
        }

        for (int i=0; i<list.size(); i++) {
            float distance = Math.abs(list.get(i).getNoteFrequency() - frequency);
            if (distance < minDistance) {
                index = i;
                minDistance = distance;
            }
        }
        moveBubble(frequency, index, list, shouldScorePoint);
    }

    private void moveBubble(float frequency, int index, List<Note> list, boolean shouldScorePoint) {
        final float upperBoundsFrequency;
        final float lowerBoundsFrequency;
        upperBoundsFrequency = list.get(7).getNoteFrequency();
        lowerBoundsFrequency = list.get(3).getNoteFrequency();
        if (frequency < list.get(3).getNoteFrequency()) {
            noteText.setText(list.get(3).getNoteName());
        } else if (frequency > list.get(7).getNoteFrequency()) {
            noteText.setText(list.get(7).getNoteName());
        } else {
            noteText.setText(list.get(index).getNoteName());
        }
        final float bias = getBias(frequency, lowerBoundsFrequency, upperBoundsFrequency);
        ConstraintLayout.LayoutParams noteBubbleLayoutParams = (ConstraintLayout.LayoutParams) noteBubble.getLayoutParams();

        noteBubbleLayoutParams.verticalBias = bias;
        noteBubble.setLayoutParams(noteBubbleLayoutParams);

        long duration;
        if (parentActivity.equals("interval")) {
            if (shouldScorePoint) {
                duration = 2000;
            } else {
                duration = 1000;
            }
        } else {
            duration = 3000;
        }

        if (checkAccuracy(frequency)) {
            noteBubble.setImageResource(R.drawable.note_bubble_on);
            startTimer(duration, shouldScorePoint);
        } else {
            noteBubble.setImageResource(R.drawable.note_bubble_off);
            stopTimer();
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        if (isFragmentDisabled.equals("true")) {
            noteBubble.setAlpha(.3f);
            targetView.setAlpha(.2f);
            disableSelf();
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            parentActivity = getArguments().getString(PARAM_PARENT_ACTIVITY);
            isFragmentDisabled = getArguments().getString(PARAM_IS_DISABLED);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_pitch_match, container, false);

        noteBubble = view.findViewById(R.id.noteBubble);
        noteText = view.findViewById(R.id.noteText);
        targetNoteText = view.findViewById(R.id.targetNoteText);
        targetView = view.findViewById(R.id.targetLine);
        progressBar = view.findViewById(R.id.progressBar);

        return view;
    }

    void setTargetNote(TargetNote target) {
        targetNote = target;
        targetNoteText.setText(targetNote.getNoteName());
    }

    void setShouldListen(boolean shouldListen) {
        this.shouldListen = shouldListen;
    }

    boolean getShouldListen() {
        return this.shouldListen;
    }

    void disableSelf() {
        Objects.requireNonNull(getView()).setBackgroundResource(R.drawable.layout_disabled);
        getView().findViewById(R.id.noteBubble).setAlpha(.3f);
        getView().findViewById(R.id.progressBar).setAlpha(.3f);
        getView().findViewById(R.id.targetLine).setAlpha(.2f);
    }

    void enableSelf() {
        Objects.requireNonNull(getView()).setBackgroundResource(R.drawable.layout_border);
        getView().findViewById(R.id.noteBubble).setAlpha(1f);
        getView().findViewById(R.id.progressBar).setAlpha(1f);
        getView().findViewById(R.id.targetLine).setAlpha(1f);
    }
}
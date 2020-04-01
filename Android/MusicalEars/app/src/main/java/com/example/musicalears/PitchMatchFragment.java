package com.example.musicalears;

import android.animation.ValueAnimator;
import android.os.Bundle;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.fragment.app.Fragment;
import androidx.transition.AutoTransition;
import androidx.transition.TransitionManager;

import android.os.CountDownTimer;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;
import java.util.Objects;

import static com.example.musicalears.Note.adjustedIntervalNoteList;
import static com.example.musicalears.Note.adjustedNoteList;

public class PitchMatchFragment extends Fragment {
    private static final String PARAM_DURATION = "duration";
    private static final String PARAM_SHOULD_SHOW_NOTE_NAME = "showName";
    private static final String PARAM_IS_DISABLED = "isDisabled";
    private static final String PARAM_PARENT_ACTIVITY = "parent";

    private long paramDuration;
    private boolean paramShouldShowNoteName;
    private boolean paramIsDisabled;
    private String paramParentActivity;

    private ImageView noteBubble;
    private TextView noteText;
    private TextView targetNoteText;

    private View targetView;
    private View progressBar;

    private ConstraintLayout fragmentLayout;

    private TargetNote targetNote = null;

    private ValueAnimator progressBarAnimator;
    private AutoTransition noteBubbleTransition;

    boolean didGetBaseNote;

    private CountDownTimer accuracyTimer;
    private boolean shouldListen = false;

    public PitchMatchFragment() {
        // Required empty public constructor
    }

    static PitchMatchFragment newInstance(long duration, boolean showNote, boolean isDisabled, String parentActivity) {
        PitchMatchFragment fragment = new PitchMatchFragment();
        Bundle args = new Bundle();
        args.putLong(PARAM_DURATION, duration);
        args.putBoolean(PARAM_SHOULD_SHOW_NOTE_NAME, showNote);
        args.putBoolean(PARAM_IS_DISABLED, isDisabled);
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

    private void startTimer(final boolean scorePoint) {
        if (accuracyTimer == null) {
            accuracyTimer = new CountDownTimer(paramDuration, 300) {
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
            progressBarAnimator.setDuration(paramDuration);
            progressBarAnimator.start();
        }
    }

    private void resetAndSwitchFragments() {
        didGetBaseNote = !didGetBaseNote;
        shouldListen = false;
        disableSelf();
        ((IntervalTrainingActivity) Objects.requireNonNull(getActivity())).switchFragments();
    }

    void resetSelf(final boolean isDisabled) {
        new android.os.Handler().postDelayed(
                new Runnable() {
                    public void run() {
                        if (isDisabled) disableSelf();
                        stopTimer();
                        targetNote = null;
                        Objects.requireNonNull(getActivity()).findViewById(R.id.skipView).setAlpha(0.5f);
                        ConstraintLayout.LayoutParams noteBubbleLayoutParams = (ConstraintLayout.LayoutParams) noteBubble.getLayoutParams();
                        noteBubbleLayoutParams.verticalBias = (float) 0.5;
                        noteBubble.setLayoutParams(noteBubbleLayoutParams);
                        noteBubble.setImageResource(R.drawable.note_bubble_off);
                        noteText.setText("");
                        targetNoteText.setText("");                    }
                },
                100);
    }

    private void scorePointAndReset() {
        shouldListen = false;
        if (paramParentActivity.equals("interval")) {
            ((IntervalTrainingActivity) Objects.requireNonNull(getActivity())).switchFragments();
            ((IntervalTrainingActivity)getActivity()).scorePointAndReset();
        } else {
            ((PitchMatchingActivity) Objects.requireNonNull(getActivity())).scorePointAndReset();
        }
    }

    void processPitch(float pitchInHz, final boolean shouldScorePoint) {
        float frequency = pitchInHz;

        float minDistance = (float) 10000.0;
        int index = 0;

        final List<Note> list;
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

        final float finalFrequency = frequency;
        final int finalIndex = index;
        if (noteBubbleTransition != null) {
            noteBubbleTransition = null;
        } else {
            moveBubble(finalFrequency, finalIndex, list);
        }

//        long duration;
//        if (paramParentActivity.equals("interval")) {
//            if (shouldScorePoint) {
//                duration = 2000;
//            } else {
//                duration = 1000;
//            }
//        } else {
//            duration = 3000;
//        }
        if (checkAccuracy(frequency)) {
            noteBubble.setImageResource(R.drawable.note_bubble_on);
            startTimer(shouldScorePoint);
        } else {
            noteBubble.setImageResource(R.drawable.note_bubble_off);
            stopTimer();
        }
    }

    private void moveBubble(float frequency, int index, List<Note> list) {
        final float upperBoundsFrequency = list.get(7).getNoteFrequency();
        final float lowerBoundsFrequency = list.get(3).getNoteFrequency();
        if (frequency < list.get(0).getNoteFrequency()) {
            noteText.setText(list.get(0).getNoteName());
        } else if (frequency > list.get(list.size() - 1).getNoteFrequency()) {
            noteText.setText(list.get(list.size() - 1).getNoteName());
        } else {
            noteText.setText(list.get(index).getNoteName());
        }
        final float bias = getBias(frequency, lowerBoundsFrequency, upperBoundsFrequency);

        ConstraintSet noteBubbleConstraintSet = new ConstraintSet();
        noteBubbleConstraintSet.clone(fragmentLayout);
        noteBubbleConstraintSet.setVerticalBias(R.id.noteBubble, bias);
        noteBubbleTransition = new AutoTransition();
        noteBubbleTransition.setDuration(100);
        noteBubbleTransition.setInterpolator(new AccelerateDecelerateInterpolator());
        TransitionManager.beginDelayedTransition(fragmentLayout, noteBubbleTransition);
        noteBubbleConstraintSet.applyTo(fragmentLayout);
    }

    @Override
    public void onStart() {
        super.onStart();
        if (paramIsDisabled) {
            noteBubble.setAlpha(.3f);
            targetView.setAlpha(.2f);
            disableSelf();
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            paramDuration = getArguments().getLong(PARAM_DURATION);
            paramShouldShowNoteName = getArguments().getBoolean(PARAM_SHOULD_SHOW_NOTE_NAME);
            paramParentActivity = getArguments().getString(PARAM_PARENT_ACTIVITY);
            paramIsDisabled = getArguments().getBoolean(PARAM_IS_DISABLED);
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
        fragmentLayout = view.findViewById(R.id.fragment);

        return view;
    }

    void setTargetNote(TargetNote target) {
        targetNote = target;
        if (paramShouldShowNoteName) targetNoteText.setText(targetNote.getNoteName());
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
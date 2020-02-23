package com.example.musicalears;

import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.os.Bundle;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;

import android.os.CountDownTimer;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import be.tarsos.dsp.pitch.PitchDetectionHandler;

import static com.example.musicalears.Note.adjustedIntervalNoteList;
import static com.example.musicalears.Note.noteList;
import static com.example.musicalears.Note.adjustedNoteList;



public class PitchMatchFragment extends Fragment {
    private static final int PERMISSIONS_REQUEST_RECORD_AUDIO = 1;
    private static final String PARAM_PARENT_ACTIVITY = "param1";
    private static final String PARAM_IS_DISABLED = "param2";

    private String parentActivity;
    private String isFragmentDisabled;

    private static PitchDetectionHandler pitchDetectionHandler;

    private ImageView noteBubble;
    private TextView noteText;

    private TextView upperMaxText;
    private TextView lowerMaxText;
    private View upperTwentyView;
    private View lowerTwentyView;

    TargetNote targetNote = null;
    private CountDownTimer accuracyTimer = null;

    private int score = 0;

    public PitchMatchFragment() {
        // Required empty public constructor
    }

    public static PitchMatchFragment newInstance(String parentActivity, String isFragmentDisabled) {
        Log.d("parent", parentActivity);
        Log.d("disable", isFragmentDisabled);
        PitchMatchFragment fragment = new PitchMatchFragment();
        Bundle args = new Bundle();
        args.putString(PARAM_IS_DISABLED, isFragmentDisabled);
        args.putString(PARAM_PARENT_ACTIVITY, parentActivity);
        fragment.setArguments(args);
        return fragment;
    }

    void onPlayRandomTone(TargetNote randomNote, boolean isInterval) {
        targetNote = randomNote;
        noteText.setText(targetNote.getNoteName());
        if (!isInterval) {
            upperMaxText.setText(adjustedNoteList.get(7).getNoteName());
            lowerMaxText.setText(adjustedNoteList.get(3).getNoteName());
        } else {
            upperMaxText.setText(adjustedIntervalNoteList.get(7).getNoteName());
            lowerMaxText.setText(adjustedIntervalNoteList.get(3).getNoteName());
        }

        addTargetBounds(isInterval);
    }

    private void addTargetBounds(boolean isInterval) {
        float targetFrequency;
        float nextFrequency;
        float prevFrequency;
        float targetUpperFrequency;
        float targetLowerFrequency;
        float targetUpperBias;
        float targetLowerBias;

        if (!isInterval) {
            targetFrequency = adjustedNoteList.get(5).getNoteFrequency();
            nextFrequency = adjustedNoteList.get(6).getNoteFrequency();
            prevFrequency = adjustedNoteList.get(4).getNoteFrequency();
//            find value 20 cents above
            targetUpperFrequency = targetFrequency + (nextFrequency - targetFrequency)/2;
            Log.d("upperTwenty", String.valueOf(targetUpperFrequency));
            targetNote.setTargetUpperTwenty(targetUpperFrequency);

//            find value 20 cents below
            targetLowerFrequency = targetFrequency + (prevFrequency - targetFrequency)/2;
            Log.d("lowerTwenty", String.valueOf(targetLowerFrequency));
            targetNote.setTargetLowerTwenty(targetLowerFrequency);
            targetUpperBias = getBias(targetUpperFrequency,
                    adjustedNoteList.get(3).getNoteFrequency(),
                    adjustedNoteList.get(7).getNoteFrequency());
            Log.d("upperTwentyBias", String.valueOf(targetUpperBias));

            targetLowerBias = getBias(targetLowerFrequency,
                    adjustedNoteList.get(3).getNoteFrequency(),
                    adjustedNoteList.get(7).getNoteFrequency());
            Log.d("lowerTwentyBias", String.valueOf(targetLowerBias));
        } else {
            targetFrequency = adjustedIntervalNoteList.get(5).getNoteFrequency();
            nextFrequency = adjustedIntervalNoteList.get(6).getNoteFrequency();
            prevFrequency = adjustedIntervalNoteList.get(4).getNoteFrequency();
//            find value 20 cents above
            targetUpperFrequency = targetFrequency + (nextFrequency - targetFrequency)/2;
            Log.d("upperTwenty", String.valueOf(targetUpperFrequency));
            targetNote.setTargetUpperTwenty(targetUpperFrequency);
//            find value 20 cents below
            targetLowerFrequency = targetFrequency + (prevFrequency - targetFrequency)/2;
            Log.d("lowerTwenty", String.valueOf(targetLowerFrequency));
            targetNote.setTargetLowerTwenty(targetLowerFrequency);
            targetUpperBias = getBias(targetUpperFrequency,
                    adjustedIntervalNoteList.get(3).getNoteFrequency(),
                    adjustedIntervalNoteList.get(7).getNoteFrequency());
            Log.d("upperTwentyBias", String.valueOf(targetUpperBias));

            targetLowerBias = getBias(targetLowerFrequency,
                    adjustedIntervalNoteList.get(3).getNoteFrequency(),
                    adjustedIntervalNoteList.get(7).getNoteFrequency());
            Log.d("lowerTwentyBias", String.valueOf(targetLowerBias));
        }

        ConstraintLayout.LayoutParams upperLimitLayoutParams = (ConstraintLayout.LayoutParams) upperTwentyView.getLayoutParams();
        ConstraintLayout.LayoutParams lowerLimitLayoutParams = (ConstraintLayout.LayoutParams) lowerTwentyView.getLayoutParams();
        upperLimitLayoutParams.verticalBias = targetUpperBias;
        lowerLimitLayoutParams.verticalBias = targetLowerBias;

        upperTwentyView.setLayoutParams(upperLimitLayoutParams);
        lowerTwentyView.setLayoutParams(lowerLimitLayoutParams);
    }

    private float getBias(float frequency, float lowerBoundsFrequency, float upperBoundsFrequency) {
//        Log.d("bias-freq", String.valueOf(frequency));
//        Log.d("bias-lower", String.valueOf(lowerBoundsFrequency));
//        Log.d("bias-higher", String.valueOf(upperBoundsFrequency));


        float bias = 1 - (frequency - lowerBoundsFrequency)/(upperBoundsFrequency - lowerBoundsFrequency);
        if (bias < 0) {
            bias = 0;
        } else if (bias > 1) {
            bias = 1;
        }
//        Log.d("bias", String.valueOf(bias));
        return bias;
    }

    boolean checkAccuracy(float frequency) {
        return frequency < targetNote.getTargetUpperTwenty() &&
                frequency > targetNote.getTargetLowerTwenty();
    }

    void stopTimer() {
        if (accuracyTimer != null) {
            //The following transition implementation is based on an online tutorial, found at:
            //https://riptutorial.com/android/example/21224/add-transition-or-cross-fade-between-two-images-
            TransitionDrawable transition = new TransitionDrawable(new Drawable[] {
                    getResources().getDrawable(R.drawable.circle_green),
                    getResources().getDrawable(R.drawable.circle_blue)

            });
            noteBubble.setImageDrawable(transition);
            transition.startTransition(100);
            Log.d("timer", "stop");

            accuracyTimer.cancel();
            accuracyTimer = null;
        }
    }

    void startTimer(long duration, final boolean scorePoint) {
        if (accuracyTimer == null) {
            accuracyTimer = new CountDownTimer(duration, 300) {

                @Override
                public void onTick(long l) {
                    Log.d("tick", String.valueOf(l));
                }

                @Override
                public void onFinish() {
                    Log.d("should score point", String.valueOf(scorePoint));
                    if(scorePoint) {
                        scorePointAndReset();
                    } else {
//                        interval
                        resetAndSwitchFragments();
                    }
                }
            }.start();
            TransitionDrawable transition = new TransitionDrawable(new Drawable[] {
                    getResources().getDrawable(R.drawable.circle_blue),
                    getResources().getDrawable(R.drawable.circle_green)
            });
            noteBubble.setImageDrawable(transition);
            transition.startTransition((int) duration);
            Log.d("timer", "start");
        }
    }

    private void resetAndSwitchFragments() {
        //nullify values and constraints, reset for new note
        targetNote = null;
        upperMaxText.setText(noteList.get(noteList.size() - 1).getNoteName());
        lowerMaxText.setText(noteList.get(0).getNoteName());
        ConstraintLayout.LayoutParams upperLimitLayoutParams = (ConstraintLayout.LayoutParams) upperTwentyView.getLayoutParams();
        ConstraintLayout.LayoutParams lowerLimitLayoutParams = (ConstraintLayout.LayoutParams) lowerTwentyView.getLayoutParams();
        upperLimitLayoutParams.verticalBias = 0;
        lowerLimitLayoutParams.verticalBias = 1;
        upperTwentyView.setLayoutParams(upperLimitLayoutParams);
        lowerTwentyView.setLayoutParams(lowerLimitLayoutParams);

        ((IntervalTrainingActivity)getActivity()).switchFragments();
    }

    private void scorePointAndReset() {
        score++;
        switch (parentActivity) {
            case "interval":
                ((IntervalTrainingActivity)getActivity()).scorePoint(score);
                ((IntervalTrainingActivity)getActivity()).reset();
                break;
            case "pitch":
                ((PitchMatchingActivity)getActivity()).scorePoint(score);
                ((PitchMatchingActivity)getActivity()).reset();
                break;
        }

        //nullify values and constraints, reset for new note
        targetNote = null;
        upperMaxText.setText(noteList.get(noteList.size() - 1).getNoteName());
        lowerMaxText.setText(noteList.get(0).getNoteName());
        ConstraintLayout.LayoutParams upperLimitLayoutParams = (ConstraintLayout.LayoutParams) upperTwentyView.getLayoutParams();
        ConstraintLayout.LayoutParams lowerLimitLayoutParams = (ConstraintLayout.LayoutParams) lowerTwentyView.getLayoutParams();
        upperLimitLayoutParams.verticalBias = 0;
        lowerLimitLayoutParams.verticalBias = 1;
        upperTwentyView.setLayoutParams(upperLimitLayoutParams);
        lowerTwentyView.setLayoutParams(lowerLimitLayoutParams);
    }

    public float processPitch(float pitchInHz, float probability, boolean isInterval) {
        float frequency = -1;

        //filter out background noise, smooth out UI
        if (probability > .85) {
            frequency = pitchInHz;
            int octave = 0;

            if (targetNote != null) {
                if (!isInterval) {
                    while (frequency > adjustedNoteList.get(adjustedNoteList.size() - 1).getNoteFrequency()) {
                        frequency /= 2.0;
                        octave++;
                        if (frequency < adjustedNoteList.get(0).getNoteFrequency()) {
                            frequency = adjustedNoteList.get(0).getNoteFrequency();
                        }
                    }
                } else {
                    while (frequency > adjustedIntervalNoteList.get(adjustedIntervalNoteList.size() - 1).getNoteFrequency()) {
                        frequency /= 2.0;
                        octave++;
                        if (frequency < adjustedIntervalNoteList.get(0).getNoteFrequency()) {
                            frequency = adjustedIntervalNoteList.get(0).getNoteFrequency();
                        }
                    }
                }
            } else {
                while (frequency > noteList.get(noteList.size() - 1).getNoteFrequency()) {
                    if (frequency < 32) {
                        frequency = noteList.get(noteList.size() - 1).getNoteFrequency();
                    } else {
                        frequency /= 2.0;
                        octave++;
                        if (frequency < noteList.get(0).getNoteFrequency()) {
                            frequency = noteList.get(0).getNoteFrequency();
                        }
                    }
                }
            }

            float minDistance = (float) 10000.0;
            int index = 0;

            for (int i=0; i<noteList.size(); i++) {
                float distance = Math.abs(noteList.get(i).getNoteFrequency() - frequency);
                if (distance < minDistance) {
                    index = i;
                    minDistance = distance;
                }
            }
            moveBubble(frequency, index, isInterval);
        }
        return frequency;
    }

    private void moveBubble(float frequency, int index, boolean isInterval) {
        final float upperBoundsFrequency;
        final float lowerBoundsFrequency;
        //if the app is expecting a certain tone, the scale should be +-200c
        //otherwise, reset the scale to span the entire octave.
        if (targetNote != null ) {
            if (!isInterval) {
                upperBoundsFrequency = adjustedNoteList.get(7).getNoteFrequency();
                lowerBoundsFrequency = adjustedNoteList.get(3).getNoteFrequency();
                noteText.setText(adjustedNoteList.get(5).getNoteName());
            } else {
                upperBoundsFrequency = adjustedIntervalNoteList.get(7).getNoteFrequency();
                lowerBoundsFrequency = adjustedIntervalNoteList.get(3).getNoteFrequency();
                noteText.setText(adjustedIntervalNoteList.get(5).getNoteName());
            }
        } else {
            upperBoundsFrequency = noteList.get(noteList.size() - 1).getNoteFrequency();
            lowerBoundsFrequency = noteList.get(0).getNoteFrequency();
            noteText.setText(noteList.get(index).getNoteName()) ;
        }

        final float bias = getBias(frequency, lowerBoundsFrequency, upperBoundsFrequency);
        ConstraintLayout.LayoutParams imageParams = (ConstraintLayout.LayoutParams) noteBubble.getLayoutParams();
        imageParams.verticalBias = bias;
        noteBubble.setLayoutParams(imageParams);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            Log.d("args", String.valueOf(getArguments()));
            parentActivity = getArguments().getString(PARAM_PARENT_ACTIVITY);
            Log.d("parent activity", parentActivity);
            isFragmentDisabled = getArguments().getString(PARAM_IS_DISABLED);
            Log.d("is disabled", isFragmentDisabled);

        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_pitch_match, container, false);
        View disableFilter = view.findViewById(R.id.disableFilter);
        if (isFragmentDisabled.equals("true")) {
            disableFilter.setVisibility(View.VISIBLE);
        } else {
            disableFilter.setVisibility(View.GONE);
        }
        TextView statusText = view.findViewById(R.id.statusText);

        noteBubble = view.findViewById(R.id.imageView);
        noteText = view.findViewById(R.id.noteText);

        upperMaxText = view.findViewById(R.id.octaveUpperText);
        lowerMaxText = view.findViewById(R.id.octaveLowerText);
        upperTwentyView = view.findViewById(R.id.targetUpperLimit);
        lowerTwentyView = view.findViewById(R.id.targetLowerLimit);

        return view;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        boolean shouldListen = false;
    }
}
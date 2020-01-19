package com.example.guitartunerprototype;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import be.tarsos.dsp.AudioDispatcher;
import be.tarsos.dsp.AudioEvent;
import be.tarsos.dsp.AudioProcessor;
import be.tarsos.dsp.io.android.AudioDispatcherFactory;
import be.tarsos.dsp.pitch.PitchDetectionHandler;
import be.tarsos.dsp.pitch.PitchDetectionResult;
import be.tarsos.dsp.pitch.PitchProcessor;

import android.Manifest;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.media.Image;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;


public class MainActivity extends AppCompatActivity {
    private int STORAGE_PERMISSION_CODE = 1;

    private static final int MY_PERMISSION = 0;

    private static float[] noteFrequencies = new float[] {(float) 16.35, (float) 17.32, (float) 18.35, (float) 19.45, (float) 20.6, (float) 21.83, (float) 23.12, (float) 24.5, (float) 25.96, (float) 27.5, (float) 29.14, (float) 30.87};
    private static String[] noteNames = new String[] {"C", "C♯", "D", "D♯", "E", "F", "F♯", "G", "G♯", "A", "A♯", "B"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        AudioDispatcher dispatcher = AudioDispatcherFactory.fromDefaultMicrophone(22050, 1024, 0);

        PitchDetectionHandler pdh = new PitchDetectionHandler() {
            @Override
            public void handlePitch(PitchDetectionResult pitchDetectionResult, AudioEvent audioEvent) {
                final float pitchInHz = pitchDetectionResult.getPitch();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        processPitch(pitchInHz);
                    }
                });
            }
        };

        AudioProcessor pitchProcessor = new PitchProcessor(PitchProcessor.PitchEstimationAlgorithm.FFT_YIN, 22050, 1024, pdh);
        dispatcher.addAudioProcessor(pitchProcessor);

        Thread audioThread = new Thread(dispatcher, "Audio Thread");
        audioThread.start();

        Button button = findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (ContextCompat.checkSelfPermission(MainActivity.this,
                        Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(MainActivity.this, "You have already granted this permission!", Toast.LENGTH_SHORT).show();

                } else {
                    requestPermission();
                }
            }
        });
    }

    private void processPitch(float pitchInHz) {
        TextView pitchText = findViewById(R.id.pitchText);
        TextView noteText = findViewById(R.id.noteText);
        pitchText.setText("" + pitchInHz);
        if (pitchInHz > 0) {
            float frequency = pitchInHz;
            while (frequency > noteFrequencies[noteFrequencies.length - 1]) {
                frequency /= (float) 2.0;
            }
            while (frequency < noteFrequencies[0]) {
                frequency *= (float) 2.0;
            }
            Log.d("freq", String.valueOf(frequency));


            float minDistance = (float) 10000.0;
            int index = 0;

            for (int i=0; i<noteFrequencies.length; i++) {
                float distance = Math.abs(noteFrequencies[i] - frequency);
                if (distance < minDistance) {
                    index = i;
                    minDistance = distance;
                }
            }

            int octave = (int) (Math.log(pitchInHz / frequency)/Math.log(2));

            noteText.setText("" + noteNames[index] + "" + octave);
            moveImage(pitchInHz);
        }
    }

    private void moveImage(final float pitch) {

        final float bias = (float) 1 - ((pitch - 50) / 300);

        Log.d("height", String.valueOf(bias));
        final ImageView image = findViewById(R.id.imageView);

        Animation animation = new Animation() {
            @Override
            protected void applyTransformation(float interpolatedTime, Transformation t) {
                super.applyTransformation(interpolatedTime, t);

                if (pitch > 0) {
                    ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams)image.getLayoutParams();
                    params.verticalBias = bias;

                    image.setLayoutParams(params);
                }
            }
        };

        animation.setDuration(1000);
        image.startAnimation(animation);

    }


    private void requestPermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.RECORD_AUDIO)) {
            new AlertDialog.Builder(this)
                    .setTitle("Permission needed")
                    .setMessage("This permission is needed for this and that")
                    .setPositiveButton("ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.RECORD_AUDIO}, MY_PERMISSION);
                        }
                    })
                    .setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();
                        }
                    })
                    .create().show();
        } else {
            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.RECORD_AUDIO},
                    STORAGE_PERMISSION_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == STORAGE_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permission GRANTED", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Permission DENIED", Toast.LENGTH_SHORT).show();
            }
        }
    }
}



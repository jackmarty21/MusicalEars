package com.example.musicalears;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Switch;
import android.widget.TextView;

public class IntervalSettingsActivity extends AppCompatActivity {
    private static final String PARAM_NUM_NOTES = "numNotes";
    private static final String PARAM_DURATION = "duration";
    private static final String PARAM_SHOULD_SHOW_BASE_NOTE = "showBaseNote";
    private static final String PARAM_SHOULD_SHOW_INTERVAL_NOTE = "showIntervalNote";

    private ImageButton shortNumButton;
    private ImageButton mediumNumButton;
    private ImageButton longNumButton;
    private ImageButton easyDiffButton;
    private ImageButton mediumDiffButton;
    private ImageButton hardDiffButton;
    private Switch baseNoteToggle;
    private Switch intervalNoteToggle;

    private int numberOfNotes = 20;
    private long duration = 3000;

    private TextView numNotesText;
    private TextView difficultyText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_interval_settings);

        shortNumButton = findViewById(R.id.button1);
        shortNumButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toggleButtons(1);
            }
        });

        mediumNumButton = findViewById(R.id.button2);
        mediumNumButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toggleButtons(2);
            }
        });

        longNumButton = findViewById(R.id.button3);
        longNumButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toggleButtons(3);
            }
        });

        easyDiffButton = findViewById(R.id.button4);
        easyDiffButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toggleButtons(4);
            }
        });

        mediumDiffButton = findViewById(R.id.button5);
        mediumDiffButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toggleButtons(5);
            }
        });

        hardDiffButton = findViewById(R.id.button6);
        hardDiffButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toggleButtons(6);
            }
        });

        numNotesText = findViewById(R.id.numNotesText);
        difficultyText = findViewById(R.id.difficultyText);

        baseNoteToggle = findViewById(R.id.baseNoteToggle);
        intervalNoteToggle = findViewById(R.id.intervalNoteToggle);

        ImageButton startButton = findViewById(R.id.startButton);
        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startModule();
            }
        });
        setTexts();
    }

    private void toggleButtons(int buttonId) {
        switch (buttonId) {
            case 1:
                numberOfNotes = 10;
                shortNumButton.setBackgroundResource(R.drawable.short_on);
                mediumNumButton.setBackgroundResource(R.drawable.medium_off);
                longNumButton.setBackgroundResource(R.drawable.long_off);
                break;
            case 2:
                numberOfNotes = 20;
                shortNumButton.setBackgroundResource(R.drawable.short_off);
                mediumNumButton.setBackgroundResource(R.drawable.medium_on);
                longNumButton.setBackgroundResource(R.drawable.long_off);
                break;
            case 3:
                numberOfNotes = 50;
                shortNumButton.setBackgroundResource(R.drawable.short_off);
                mediumNumButton.setBackgroundResource(R.drawable.medium_off);
                longNumButton.setBackgroundResource(R.drawable.long_on);
                break;
            case 4:
                duration = 1000;
                easyDiffButton.setBackgroundResource(R.drawable.easy_on);
                mediumDiffButton.setBackgroundResource(R.drawable.medium_off);
                hardDiffButton.setBackgroundResource(R.drawable.hard_off);
                break;
            case 5:
                duration = 3000;
                easyDiffButton.setBackgroundResource(R.drawable.easy_off);
                mediumDiffButton.setBackgroundResource(R.drawable.medium_on);
                hardDiffButton.setBackgroundResource(R.drawable.hard_off);
                break;
            case 6:
                duration = 5000;
                easyDiffButton.setBackgroundResource(R.drawable.easy_off);
                mediumDiffButton.setBackgroundResource(R.drawable.medium_off);
                hardDiffButton.setBackgroundResource(R.drawable.hard_on);
                break;
        }
        setTexts();
    }

    private void setTexts() {
        String numNotesString = numberOfNotes + " intervals will need to be matched.";
        String difficultyString;
        if (duration == 1000) {
            difficultyString = "Notes will need to be held for " + duration/1000 + " second.";
        } else {
            difficultyString = "Notes will need to be held for " + duration/1000 + " seconds.";
        }
        numNotesText.setText(numNotesString);
        difficultyText.setText(difficultyString);
    }

    private void startModule() {
        Intent intent = new Intent(this, IntervalTrainingActivity.class);
        intent.putExtra(PARAM_NUM_NOTES, numberOfNotes);
        intent.putExtra(PARAM_DURATION, duration);
        intent.putExtra(PARAM_SHOULD_SHOW_BASE_NOTE, baseNoteToggle.isChecked());
        intent.putExtra(PARAM_SHOULD_SHOW_INTERVAL_NOTE, intervalNoteToggle.isChecked());

        startActivity(intent);
    }
}

package com.example.musicalears.Note;

import androidx.annotation.NonNull;

public class Note {
    private final String noteName;
    private float noteFrequency;

    Note(String noteName, float noteFrequency) {
        this.noteName = noteName;
        this.noteFrequency = noteFrequency;
    }

    public String getNoteName() {
        return noteName;
    }

    public float getNoteFrequency() {
        return noteFrequency;
    }

    void setNoteFrequency(float frequency) {
        noteFrequency = frequency;
    }

    @NonNull
    @Override
    public String toString() {
        return noteName;
    }
}

package com.example.musicalears;

import android.util.Log;
import java.util.ArrayList;
import java.util.List;

public class Note {
    private String noteName;
    private float noteFrequency;
    private int noteResource;

    //constructor
    Note(String noteName, float noteFrequency, int noteResource) {
        this.noteName = noteName;
        this.noteFrequency = noteFrequency;
        this.noteResource = noteResource;
    }

    final static List<Note> noteList = new ArrayList<Note>() {{
        add(new Note("C", (float) 16.35, R.raw.c4));
        add(new Note("C♯", (float) 17.32, R.raw.csh4));
        add(new Note("D", (float) 18.35, R.raw.d4));
        add(new Note("D♯", (float) 19.45, R.raw.dsh4));
        add(new Note("E", (float) 20.6, R.raw.e4));
        add(new Note("F", (float) 21.83, R.raw.f4));
        add(new Note("F♯", (float) 23.12, R.raw.fsh4));
        add(new Note("G", (float) 24.5, R.raw.g3));
        add(new Note("G♯", (float) 25.96, R.raw.gsh3));
        add(new Note("A", (float) 27.5, R.raw.a3));
        add(new Note( "A♯", (float) 29.14, R.raw.bb3));
        add(new Note( "B", (float) 30.87, R.raw.b3));
    }};

    final static List<Note> adjustedNoteList = new ArrayList<Note>() {{
        add(new Note("C", (float) 16.35, R.raw.c4));
        add(new Note("C♯", (float) 17.32, R.raw.csh4));
        add(new Note("D", (float) 18.35, R.raw.d4));
        add(new Note("D♯", (float) 19.45, R.raw.dsh4));
        add(new Note("E", (float) 20.6, R.raw.e4));
        add(new Note("F", (float) 21.83, R.raw.f4));
        add(new Note("F♯", (float) 23.12, R.raw.fsh4));
        add(new Note("G", (float) 24.5, R.raw.g3));
        add(new Note("G♯", (float) 25.96, R.raw.gsh3));
        add(new Note("A", (float) 27.5, R.raw.a3));
        add(new Note( "A♯", (float) 29.14, R.raw.bb3));
        add(new Note( "B", (float) 30.87, R.raw.b3));
    }};

    final static List<Note> adjustedIntervalNoteList = new ArrayList<Note>() {{
        add(new Note("C", (float) 16.35, R.raw.c4));
        add(new Note("C♯", (float) 17.32, R.raw.csh4));
        add(new Note("D", (float) 18.35, R.raw.d4));
        add(new Note("D♯", (float) 19.45, R.raw.dsh4));
        add(new Note("E", (float) 20.6, R.raw.e4));
        add(new Note("F", (float) 21.83, R.raw.f4));
        add(new Note("F♯", (float) 23.12, R.raw.fsh4));
        add(new Note("G", (float) 24.5, R.raw.g3));
        add(new Note("G♯", (float) 25.96, R.raw.gsh3));
        add(new Note("A", (float) 27.5, R.raw.a3));
        add(new Note( "A♯", (float) 29.14, R.raw.bb3));
        add(new Note( "B", (float) 30.87, R.raw.b3));
    }};

    String getNoteName() {
        return noteName;
    }

    float getNoteFrequency() {
        return noteFrequency;
    }

    int getNoteResource() {
        return noteResource;
    }

    void adjustNotes(int randomNoteIndex, boolean isInterval) {
        int indexDiff = randomNoteIndex - 5;
        if (indexDiff > 0) {
            shiftArrayLeft(indexDiff, isInterval);
        } else if (indexDiff < 0) {
            shiftArrayRight(Math.abs(indexDiff), isInterval);
        }
        if (!isInterval) {
            Log.d("adjustedArray", adjustedNoteList.toString());
        } else {
            Log.d("adjustedIntervalArray", adjustedIntervalNoteList.toString());
        }
    }

    private void shiftArrayRight(int indexDiff, boolean isInterval) {

        if (!isInterval) {
            adjustedNoteList.clear();
            adjustedNoteList.add(new Note("C", (float) 16.35, R.raw.c4));
            adjustedNoteList.add(new Note("C♯", (float) 17.32, R.raw.csh4));
            adjustedNoteList.add(new Note("D", (float) 18.35, R.raw.d4));
            adjustedNoteList.add(new Note("D♯", (float) 19.45, R.raw.dsh4));
            adjustedNoteList.add(new Note("E", (float) 20.6, R.raw.e4));
            adjustedNoteList.add(new Note("F", (float) 21.83, R.raw.f4));
            adjustedNoteList.add(new Note("F♯", (float) 23.12, R.raw.fsh4));
            adjustedNoteList.add(new Note("G", (float) 24.5, R.raw.g3));
            adjustedNoteList.add(new Note("G♯", (float) 25.96, R.raw.gsh3));
            adjustedNoteList.add(new Note("A", (float) 27.5, R.raw.a3));
            adjustedNoteList.add(new Note( "A♯", (float) 29.14, R.raw.bb3));
            adjustedNoteList.add(new Note( "B", (float) 30.87, R.raw.b3));

            for (int i = 0; i < indexDiff; i++) {
                Note lastNote = adjustedNoteList.remove(adjustedNoteList.size() - 1);
                lastNote.noteFrequency = lastNote.noteFrequency / 2;
                adjustedNoteList.add(0, lastNote);
            }
        } else {
            adjustedIntervalNoteList.clear();
            adjustedIntervalNoteList.add(new Note("C", (float) 16.35, R.raw.c4));
            adjustedIntervalNoteList.add(new Note("C♯", (float) 17.32, R.raw.csh4));
            adjustedIntervalNoteList.add(new Note("D", (float) 18.35, R.raw.d4));
            adjustedIntervalNoteList.add(new Note("D♯", (float) 19.45, R.raw.dsh4));
            adjustedIntervalNoteList.add(new Note("E", (float) 20.6, R.raw.e4));
            adjustedIntervalNoteList.add(new Note("F", (float) 21.83, R.raw.f4));
            adjustedIntervalNoteList.add(new Note("F♯", (float) 23.12, R.raw.fsh4));
            adjustedIntervalNoteList.add(new Note("G", (float) 24.5, R.raw.g3));
            adjustedIntervalNoteList.add(new Note("G♯", (float) 25.96, R.raw.gsh3));
            adjustedIntervalNoteList.add(new Note("A", (float) 27.5, R.raw.a3));
            adjustedIntervalNoteList.add(new Note( "A♯", (float) 29.14, R.raw.bb3));
            adjustedIntervalNoteList.add(new Note( "B", (float) 30.87, R.raw.b3));

            for (int i = 0; i < indexDiff; i++) {
                Note lastNote = adjustedIntervalNoteList.remove(adjustedIntervalNoteList.size() - 1);
                lastNote.noteFrequency = lastNote.noteFrequency / 2;
                adjustedIntervalNoteList.add(0, lastNote);
            }
        }
    }

    private void shiftArrayLeft(int indexDiff, boolean isInterval) {
        if (!isInterval) {
            adjustedNoteList.clear();
            adjustedNoteList.add(new Note("C", (float) 16.35, R.raw.c4));
            adjustedNoteList.add(new Note("C♯", (float) 17.32, R.raw.csh4));
            adjustedNoteList.add(new Note("D", (float) 18.35, R.raw.d4));
            adjustedNoteList.add(new Note("D♯", (float) 19.45, R.raw.dsh4));
            adjustedNoteList.add(new Note("E", (float) 20.6, R.raw.e4));
            adjustedNoteList.add(new Note("F", (float) 21.83, R.raw.f4));
            adjustedNoteList.add(new Note("F♯", (float) 23.12, R.raw.fsh4));
            adjustedNoteList.add(new Note("G", (float) 24.5, R.raw.g3));
            adjustedNoteList.add(new Note("G♯", (float) 25.96, R.raw.gsh3));
            adjustedNoteList.add(new Note("A", (float) 27.5, R.raw.a3));
            adjustedNoteList.add(new Note( "A♯", (float) 29.14, R.raw.bb3));
            adjustedNoteList.add(new Note( "B", (float) 30.87, R.raw.b3));

            for (int i = 0; i < indexDiff; i++) {
                Note firstNote = adjustedNoteList.remove(0);
                firstNote.noteFrequency = firstNote.noteFrequency * 2;
                adjustedNoteList.add(adjustedNoteList.size(), firstNote);
            }

        } else {
            adjustedIntervalNoteList.clear();
            adjustedIntervalNoteList.add(new Note("C", (float) 16.35, R.raw.c4));
            adjustedIntervalNoteList.add(new Note("C♯", (float) 17.32, R.raw.csh4));
            adjustedIntervalNoteList.add(new Note("D", (float) 18.35, R.raw.d4));
            adjustedIntervalNoteList.add(new Note("D♯", (float) 19.45, R.raw.dsh4));
            adjustedIntervalNoteList.add(new Note("E", (float) 20.6, R.raw.e4));
            adjustedIntervalNoteList.add(new Note("F", (float) 21.83, R.raw.f4));
            adjustedIntervalNoteList.add(new Note("F♯", (float) 23.12, R.raw.fsh4));
            adjustedIntervalNoteList.add(new Note("G", (float) 24.5, R.raw.g3));
            adjustedIntervalNoteList.add(new Note("G♯", (float) 25.96, R.raw.gsh3));
            adjustedIntervalNoteList.add(new Note("A", (float) 27.5, R.raw.a3));
            adjustedIntervalNoteList.add(new Note( "A♯", (float) 29.14, R.raw.bb3));
            adjustedIntervalNoteList.add(new Note( "B", (float) 30.87, R.raw.b3));

            for (int i = 0; i < indexDiff; i++) {
                Note firstNote = adjustedIntervalNoteList.remove(0);
                firstNote.noteFrequency = firstNote.noteFrequency * 2;
                adjustedIntervalNoteList.add(adjustedIntervalNoteList.size(), firstNote);
            }
        }
    }

    public String toString() {
        return this.noteName + " : " + this.noteFrequency;
    }
}

package com.example.musicalears;

import android.util.Log;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

public class Note {
    private int noteId;
    private String noteName;
    private float noteFrequency;
    private int noteResource;

    //constructor
    public Note(int noteId, String noteName, float noteFrequency, int noteResource) {
        this.noteId = noteId;
        this.noteName = noteName;
        this.noteFrequency = noteFrequency;
        this.noteResource = noteResource;
    }

    public final static List<Note> noteList = new ArrayList<Note>() {{
        add(new Note(0, "C", (float) 16.35, R.raw.c4));
        add(new Note(1, "C♯", (float) 17.32, R.raw.csh4));
        add(new Note(2, "D", (float) 18.35, R.raw.d4));
        add(new Note(3, "D♯", (float) 19.45, R.raw.dsh4));
        add(new Note(4, "E", (float) 20.6, R.raw.e4));
        add(new Note(5, "F", (float) 21.83, R.raw.f4));
        add(new Note(6, "F♯", (float) 23.12, R.raw.fsh4));
        add(new Note(7, "G", (float) 24.5, R.raw.g3));
        add(new Note(8, "G♯", (float) 25.96, R.raw.gsh3));
        add(new Note(9, "A", (float) 27.5, R.raw.a3));
        add(new Note(10, "A♯", (float) 29.14, R.raw.bb3));
        add(new Note(11, "B", (float) 30.87, R.raw.b3));
    }};

    public static List<Note> adjustedNoteList = new ArrayList<Note>() {{
        add(new Note(0, "C", (float) 16.35, R.raw.c4));
        add(new Note(1, "C♯", (float) 17.32, R.raw.csh4));
        add(new Note(2, "D", (float) 18.35, R.raw.d4));
        add(new Note(3, "D♯", (float) 19.45, R.raw.dsh4));
        add(new Note(4, "E", (float) 20.6, R.raw.e4));
        add(new Note(5, "F", (float) 21.83, R.raw.f4));
        add(new Note(6, "F♯", (float) 23.12, R.raw.fsh4));
        add(new Note(7, "G", (float) 24.5, R.raw.g3));
        add(new Note(8, "G♯", (float) 25.96, R.raw.gsh3));
        add(new Note(9, "A", (float) 27.5, R.raw.a3));
        add(new Note(10, "A♯", (float) 29.14, R.raw.bb3));
        add(new Note(11, "B", (float) 30.87, R.raw.b3));
    }};

    public int getNoteId() {
        return noteId;
    }

    public String getNoteName() {
        return noteName;
    }

    public float getNoteFrequency() {
        return noteFrequency;
    }

    public int getNoteResource() {
        return noteResource;
    }

    public void adjustNotes(int randomNoteIndex) {
        Log.d("originalArray", noteList.toString());
        int indexDiff = randomNoteIndex - 5;
        if (indexDiff > 0) {
            shiftArrayLeft(indexDiff);
        } else if (indexDiff < 0) {
            shiftArrayRight(Math.abs(indexDiff));
        }
        Log.d("adjustedArray", adjustedNoteList.toString());
    }

    public String toString() {
        return this.noteName + " : " + this.noteFrequency;
    }

    private void shiftArrayRight(int indexDiff) {

        adjustedNoteList.clear();
        adjustedNoteList.add(new Note(0, "C", (float) 16.35, R.raw.c4));
        adjustedNoteList.add(new Note(1, "C♯", (float) 17.32, R.raw.csh4));
        adjustedNoteList.add(new Note(2, "D", (float) 18.35, R.raw.d4));
        adjustedNoteList.add(new Note(3, "D♯", (float) 19.45, R.raw.dsh4));
        adjustedNoteList.add(new Note(4, "E", (float) 20.6, R.raw.e4));
        adjustedNoteList.add(new Note(5, "F", (float) 21.83, R.raw.f4));
        adjustedNoteList.add(new Note(6, "F♯", (float) 23.12, R.raw.fsh4));
        adjustedNoteList.add(new Note(7, "G", (float) 24.5, R.raw.g3));
        adjustedNoteList.add(new Note(8, "G♯", (float) 25.96, R.raw.gsh3));
        adjustedNoteList.add(new Note(9, "A", (float) 27.5, R.raw.a3));
        adjustedNoteList.add(new Note(10, "A♯", (float) 29.14, R.raw.bb3));
        adjustedNoteList.add(new Note(11, "B", (float) 30.87, R.raw.b3));

        for (int i = 0; i < indexDiff; i++) {
            Note lastNote = adjustedNoteList.remove(adjustedNoteList.size() - 1);
            lastNote.noteFrequency = lastNote.noteFrequency / 2;
            adjustedNoteList.add(0, lastNote);
        }
    }

    private void shiftArrayLeft(int indexDiff) {

        adjustedNoteList.clear();
        adjustedNoteList.add(new Note(0, "C", (float) 16.35, R.raw.c4));
        adjustedNoteList.add(new Note(1, "C♯", (float) 17.32, R.raw.csh4));
        adjustedNoteList.add(new Note(2, "D", (float) 18.35, R.raw.d4));
        adjustedNoteList.add(new Note(3, "D♯", (float) 19.45, R.raw.dsh4));
        adjustedNoteList.add(new Note(4, "E", (float) 20.6, R.raw.e4));
        adjustedNoteList.add(new Note(5, "F", (float) 21.83, R.raw.f4));
        adjustedNoteList.add(new Note(6, "F♯", (float) 23.12, R.raw.fsh4));
        adjustedNoteList.add(new Note(7, "G", (float) 24.5, R.raw.g3));
        adjustedNoteList.add(new Note(8, "G♯", (float) 25.96, R.raw.gsh3));
        adjustedNoteList.add(new Note(9, "A", (float) 27.5, R.raw.a3));
        adjustedNoteList.add(new Note(10, "A♯", (float) 29.14, R.raw.bb3));
        adjustedNoteList.add(new Note(11, "B", (float) 30.87, R.raw.b3));

        for (int i = 0; i < indexDiff; i++) {
            Note firstNote = adjustedNoteList.remove(0);
            firstNote.noteFrequency = firstNote.noteFrequency * 2;
            adjustedNoteList.add(adjustedNoteList.size(), firstNote);
        }
    }
}

package com.example.musicalears;

import java.util.ArrayList;
import java.util.List;

class Note {
    private final String noteName;
    private float noteFrequency;

    Note(String noteName, float noteFrequency) {
        this.noteName = noteName;
        this.noteFrequency = noteFrequency;
    }

    final static List<Note> noteList = new ArrayList<Note>() {{
        add(new Note("C", (float) 16.35));
        add(new Note("C♯", (float) 17.32));
        add(new Note("D", (float) 18.35));
        add(new Note("D♯", (float) 19.45));
        add(new Note("E", (float) 20.6));
        add(new Note("F", (float) 21.83));
        add(new Note("F♯", (float) 23.12));
        add(new Note("G", (float) 24.5));
        add(new Note("G♯", (float) 25.96));
        add(new Note("A", (float) 27.5));
        add(new Note( "A♯", (float) 29.14));
        add(new Note( "B", (float) 30.87));
    }};

    final static List<Note> adjustedNoteList = new ArrayList<Note>() {{
        add(new Note("C", (float) 16.35));
        add(new Note("C♯", (float) 17.32));
        add(new Note("D", (float) 18.35));
        add(new Note("D♯", (float) 19.45));
        add(new Note("E", (float) 20.6));
        add(new Note("F", (float) 21.83));
        add(new Note("F♯", (float) 23.12));
        add(new Note("G", (float) 24.5));
        add(new Note("G♯", (float) 25.96));
        add(new Note("A", (float) 27.5));
        add(new Note( "A♯", (float) 29.14));
        add(new Note( "B", (float) 30.87));
    }};

    final static List<Note> adjustedIntervalNoteList = new ArrayList<Note>() {{
        add(new Note("C", (float) 16.35));
        add(new Note("C♯", (float) 17.32));
        add(new Note("D", (float) 18.35));
        add(new Note("D♯", (float) 19.45));
        add(new Note("E", (float) 20.6));
        add(new Note("F", (float) 21.83));
        add(new Note("F♯", (float) 23.12));
        add(new Note("G", (float) 24.5));
        add(new Note("G♯", (float) 25.96));
        add(new Note("A", (float) 27.5));
        add(new Note( "A♯", (float) 29.14));
        add(new Note( "B", (float) 30.87));
    }};

    String getNoteName() {
        return noteName;
    }

    float getNoteFrequency() {
        return noteFrequency;
    }

    void adjustNotes(int randomNoteIndex, boolean isInterval) {
        int indexDiff = randomNoteIndex - 5;
        if (indexDiff > 0) {
            shiftArrayLeft(indexDiff, isInterval);
        } else if (indexDiff < 0) {
            shiftArrayRight(Math.abs(indexDiff), isInterval);
        }
    }

    private void shiftArrayRight(int indexDiff, boolean isInterval) {
        if (!isInterval) {
            adjustedNoteList.clear();
            adjustedNoteList.add(new Note("C", (float) 16.35));
            adjustedNoteList.add(new Note("C♯", (float) 17.32));
            adjustedNoteList.add(new Note("D", (float) 18.35));
            adjustedNoteList.add(new Note("D♯", (float) 19.45));
            adjustedNoteList.add(new Note("E", (float) 20.6));
            adjustedNoteList.add(new Note("F", (float) 21.83));
            adjustedNoteList.add(new Note("F♯", (float) 23.12));
            adjustedNoteList.add(new Note("G", (float) 24.5));
            adjustedNoteList.add(new Note("G♯", (float) 25.96));
            adjustedNoteList.add(new Note("A", (float) 27.5));
            adjustedNoteList.add(new Note( "A♯", (float) 29.14));
            adjustedNoteList.add(new Note( "B", (float) 30.87));

            for (int i = 0; i < indexDiff; i++) {
                Note lastNote = adjustedNoteList.remove(adjustedNoteList.size() - 1);
                lastNote.noteFrequency = lastNote.noteFrequency / 2;
                adjustedNoteList.add(0, lastNote);
            }
        } else {
            adjustedIntervalNoteList.clear();
            adjustedIntervalNoteList.add(new Note("C", (float) 16.35));
            adjustedIntervalNoteList.add(new Note("C♯", (float) 17.32));
            adjustedIntervalNoteList.add(new Note("D", (float) 18.35));
            adjustedIntervalNoteList.add(new Note("D♯", (float) 19.45));
            adjustedIntervalNoteList.add(new Note("E", (float) 20.6));
            adjustedIntervalNoteList.add(new Note("F", (float) 21.83));
            adjustedIntervalNoteList.add(new Note("F♯", (float) 23.12));
            adjustedIntervalNoteList.add(new Note("G", (float) 24.5));
            adjustedIntervalNoteList.add(new Note("G♯", (float) 25.96));
            adjustedIntervalNoteList.add(new Note("A", (float) 27.5));
            adjustedIntervalNoteList.add(new Note( "A♯", (float) 29.14));
            adjustedIntervalNoteList.add(new Note( "B", (float) 30.87));

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
            adjustedNoteList.add(new Note("C", (float) 16.35));
            adjustedNoteList.add(new Note("C♯", (float) 17.32));
            adjustedNoteList.add(new Note("D", (float) 18.35));
            adjustedNoteList.add(new Note("D♯", (float) 19.45));
            adjustedNoteList.add(new Note("E", (float) 20.6));
            adjustedNoteList.add(new Note("F", (float) 21.83));
            adjustedNoteList.add(new Note("F♯", (float) 23.12));
            adjustedNoteList.add(new Note("G", (float) 24.5));
            adjustedNoteList.add(new Note("G♯", (float) 25.96));
            adjustedNoteList.add(new Note("A", (float) 27.5));
            adjustedNoteList.add(new Note( "A♯", (float) 29.14));
            adjustedNoteList.add(new Note( "B", (float) 30.87));

            for (int i = 0; i < indexDiff; i++) {
                Note firstNote = adjustedNoteList.remove(0);
                firstNote.noteFrequency = firstNote.noteFrequency * 2;
                adjustedNoteList.add(adjustedNoteList.size(), firstNote);
            }
        } else {
            adjustedIntervalNoteList.clear();
            adjustedIntervalNoteList.add(new Note("C", (float) 16.35));
            adjustedIntervalNoteList.add(new Note("C♯", (float) 17.32));
            adjustedIntervalNoteList.add(new Note("D", (float) 18.35));
            adjustedIntervalNoteList.add(new Note("D♯", (float) 19.45));
            adjustedIntervalNoteList.add(new Note("E", (float) 20.6));
            adjustedIntervalNoteList.add(new Note("F", (float) 21.83));
            adjustedIntervalNoteList.add(new Note("F♯", (float) 23.12));
            adjustedIntervalNoteList.add(new Note("G", (float) 24.5));
            adjustedIntervalNoteList.add(new Note("G♯", (float) 25.96));
            adjustedIntervalNoteList.add(new Note("A", (float) 27.5));
            adjustedIntervalNoteList.add(new Note( "A♯", (float) 29.14));
            adjustedIntervalNoteList.add(new Note( "B", (float) 30.87));

            for (int i = 0; i < indexDiff; i++) {
                Note firstNote = adjustedIntervalNoteList.remove(0);
                firstNote.noteFrequency = firstNote.noteFrequency * 2;
                adjustedIntervalNoteList.add(adjustedIntervalNoteList.size(), firstNote);
            }
        }
    }
}

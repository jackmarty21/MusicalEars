package com.example.musicalears.Note;

import java.util.ArrayList;
import java.util.List;

public class NoteList {
    private List<Note> adjustedNoteList = new ArrayList<Note>() {{
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
        add(new Note("A♯", (float) 29.14));
        add(new Note("B", (float) 30.87));
    }};

    public final static List<Note> noteList = new ArrayList<Note>() {{
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
        add(new Note("A♯", (float) 29.14));
        add(new Note("B", (float) 30.87));
    }};

    NoteList(int randomNoteIndex) {
        adjustNotes(randomNoteIndex);
    }

    List<Note> getAdjustedNoteList() {
        return adjustedNoteList;
    }

    private void adjustNotes(int randomNoteIndex) {
        int indexDiff = randomNoteIndex - 5;
        if (indexDiff == 0) {
            return;
        }
        if (indexDiff > 0) {
            shiftArrayLeft(indexDiff);
        } else {
            shiftArrayRight(Math.abs(indexDiff));
        }
    }

    private void shiftArrayRight(int indexDiff) {
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
        adjustedNoteList.add(new Note("A♯", (float) 29.14));
        adjustedNoteList.add(new Note("B", (float) 30.87));

        for (int i = 0; i < indexDiff; i++) {
            Note lastNote = adjustedNoteList.remove(adjustedNoteList.size() - 1);
            lastNote.setNoteFrequency(lastNote.getNoteFrequency() / 2);
            adjustedNoteList.add(0, lastNote);
        }
    }

    private void shiftArrayLeft(int indexDiff) {
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
        adjustedNoteList.add(new Note("A♯", (float) 29.14));
        adjustedNoteList.add(new Note("B", (float) 30.87));

        for (int i = 0; i < indexDiff; i++) {
            Note firstNote = adjustedNoteList.remove(0);
            firstNote.setNoteFrequency(firstNote.getNoteFrequency() * 2);
            adjustedNoteList.add(adjustedNoteList.size(), firstNote);
        }
    }

    public Note get(int i) {
        return noteList.get(i);
    }

//    public int size() {
//        return noteList.size();
//    }
}
package com.mwangblog.midiprocessing;

import android.util.Log;

import com.mwangblog.midiprocessing.event.NoteOff;
import com.mwangblog.midiprocessing.event.NoteOn;
import com.mwangblog.midiprocessing.event.meta.Tempo;

import java.util.ArrayList;

public class MyNotes {

    private ArrayList<MyNote> mMyNotes;
    private Tempo mTempo;

    private static final String TAG = "MyNotes";

    public MyNotes (Tempo tempo, ArrayList<NoteOn> noteOns, ArrayList<NoteOff> noteOffs) {
        mMyNotes = new ArrayList<MyNote>();
        mTempo = tempo;

        for (NoteOn noteOn : noteOns) {
            Boolean isZeroTickNote = false;
            for (NoteOff noteOff : noteOffs) {
                if (noteOff.getTick() < noteOn.getTick()) {
                    continue;
                }
                if (noteOff.getNoteValue() == noteOn.getNoteValue()) {
                    /*
                    if (noteOff.getTick() == noteOn.getTick()) {
                        noteOffs.remove(noteOff);
                        break;
                    }
                    */
                    MyNote myNote = new MyNote (noteOn, noteOff);
                    mMyNotes.add(myNote);
                    noteOffs.remove(noteOff);
                    break;
                }
            }
        }
    }

    public ArrayList<MyNote> getMyNotes() {
        return mMyNotes;
    }

    public Tempo getTempo() {
        return mTempo;
    }

    public MyNote getMyNote (int i) {
        if (i >= mMyNotes.size()) {
            Log.e (TAG, "getMyNote() : index > lenght");
            return null;
        }
        return mMyNotes.get(i);
    }

    public int getLength() {
        return mMyNotes.size();
    }
}

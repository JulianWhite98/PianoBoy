package com.mwangblog.midiprocessing;

import android.util.Log;

import com.mwangblog.midiprocessing.event.NoteOff;
import com.mwangblog.midiprocessing.event.NoteOn;
import com.mwangblog.midiprocessing.event.meta.Tempo;

import java.util.ArrayList;

public class MyNotes {

    private ArrayList<MyNote> mMyNotes;
    private int mResolution;
    private Tempo mTempo;

    private int mPqn;

    private long mStartMs;
    private long mEndMs;

    private static final String TAG = "MyNotes";

    public MyNotes (int resolution, Tempo tempo, ArrayList<NoteOn> noteOns, ArrayList<NoteOff> noteOffs) {
        mMyNotes = new ArrayList<MyNote>();
        mResolution = resolution;
        mTempo = tempo;

        mPqn = mTempo.getMpqn();

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
                    MyNote myNote = new MyNote (noteOn, noteOff, mPqn, mResolution);
                    mMyNotes.add(myNote);
                    noteOffs.remove(noteOff);
                    break;
                }
            }
        }
        mStartMs = mMyNotes.get(0).getOnMs();
        mEndMs = mMyNotes.get(mMyNotes.size()-1).getOffMs();
    }

    public ArrayList<MyNote> getMyNotes() {
        return mMyNotes;
    }

    public Tempo getTempo() {
        return mTempo;
    }

    public int getResolution() {
        return mResolution;
    }

    public int getPqn() {
        return mPqn;
    }

    public long getStartMs() {
        return mStartMs;
    }

    public long getEndMs() {
        return mEndMs;
    }
}

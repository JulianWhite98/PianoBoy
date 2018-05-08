package com.mwangblog.midiprocessing;

import com.mwangblog.midiprocessing.event.NoteOff;
import com.mwangblog.midiprocessing.event.NoteOn;
import com.mwangblog.midiprocessing.util.MidiUtil;

public class MyNote {

    private NoteOn mNoteOn;
    private NoteOff mNoteOff;

    private long mOnTick;
    private long mOffTick;
    private long mOnMs;
    private long mOffMs;
    private int mPitch;

    public MyNote (NoteOn noteOn, NoteOff noteOff, int pqn, int resolution) {
        mNoteOn = noteOn;
        mNoteOff = noteOff;
        mOnTick = noteOn.getTick();
        mOffTick = noteOff.getTick();
        mOnMs = MidiUtil.ticksToMs(noteOn.getTick(), pqn, resolution);
        mOffMs = MidiUtil.ticksToMs(noteOff.getTick(), pqn, resolution);
        mPitch = noteOn.getNoteValue();

    }

    public NoteOn getNoteOn() {
        return mNoteOn;
    }

    public NoteOff getNoteOff() {
        return mNoteOff;
    }

    public long getOnTick() {
        return mOnTick;
    }

    public long getOffTick() {
        return mOffTick;
    }

    public long getOnMs() {
        return mOnMs;
    }

    public long getOffMs() {
        return mOffMs;
    }

    public int getPitch() {
        return mPitch;
    }
}

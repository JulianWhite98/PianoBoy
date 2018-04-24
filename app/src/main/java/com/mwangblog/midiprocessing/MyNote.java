package com.mwangblog.midiprocessing;

import com.mwangblog.midiprocessing.event.NoteOff;
import com.mwangblog.midiprocessing.event.NoteOn;

public class MyNote {

    private NoteOn mNoteOn;
    private NoteOff mNoteOff;

    public MyNote (NoteOn noteOn, NoteOff noteOff) {
        mNoteOn = noteOn;
        mNoteOff = noteOff;
    }

    public NoteOn getNoteOn() {
        return mNoteOn;
    }

    public void setNoteOn(NoteOn noteOn) {
        mNoteOn = noteOn;
    }

    public NoteOff getNoteOff() {
        return mNoteOff;
    }

    public void setNoteOff(NoteOff noteOff) {
        mNoteOff = noteOff;
    }
}

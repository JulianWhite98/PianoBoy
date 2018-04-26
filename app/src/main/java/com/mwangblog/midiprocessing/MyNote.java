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

    public NoteOff getNoteOff() {
        return mNoteOff;
    }
}

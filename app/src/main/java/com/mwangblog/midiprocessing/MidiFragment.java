package com.mwangblog.midiprocessing;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Point;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.mwangblog.midiprocessing.util.MidiUtil;

import org.w3c.dom.Text;

import java.lang.ref.WeakReference;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

import be.tarsos.dsp.AudioDispatcher;
import be.tarsos.dsp.AudioEvent;
import be.tarsos.dsp.AudioProcessor;
import be.tarsos.dsp.io.android.AudioDispatcherFactory;
import be.tarsos.dsp.pitch.PitchDetectionHandler;
import be.tarsos.dsp.pitch.PitchDetectionResult;
import be.tarsos.dsp.pitch.PitchProcessor;
import lecho.lib.hellocharts.model.Axis;
import lecho.lib.hellocharts.model.Line;
import lecho.lib.hellocharts.model.LineChartData;
import lecho.lib.hellocharts.model.PointValue;
import lecho.lib.hellocharts.model.ValueShape;
import lecho.lib.hellocharts.model.Viewport;
import lecho.lib.hellocharts.util.ChartUtils;
import lecho.lib.hellocharts.view.LineChartView;

public class MidiFragment extends Fragment {

    private Midi mMidi;
    private PitchList mRealPitchList;
    private PitchList mMidiPitchList;

    private static final String ARG_MIDI_NAME = "midi_id";
    private static final String TAG = "MidiFragment";

    private TextView mTitleTextView;
    private Button mPlayButton;
    private Button mStopButton;
    private Button mGetInfoButton;
    private Button mPracticeButton;
    private TextView mPitchTextView;
    private TextView mMidiInfoTextView;
    private ArrayList<Line> mLines;
    private LineChartView mChart;
    private ProgressBar mInfoProgressBar;

    private Handler mPitchHandler;
    private Runnable mPitchFreshRunnable;

    private final int mDelayTime = 50;
    private final int mCompareLength = 60;
    private final int mMaxViewMs = 3000;

    private boolean mIsPracticing;
    private boolean mDetectingPitch;

    private int mErrorCount = 0;


    public static MidiFragment newInstance (String name) {
        Bundle args = new Bundle();
        args.putSerializable(ARG_MIDI_NAME, name);

        MidiFragment fragment = new MidiFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String name = (String) getArguments().getSerializable(ARG_MIDI_NAME);
        mMidi = MidiLab.get(getActivity()).getMidi(name);
        mRealPitchList = new PitchList();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_midi, container, false);

        Log.i (TAG, "onCreateView()");

        mTitleTextView = (TextView) v.findViewById(R.id.midi_title_text_view);
        mTitleTextView.setText(mMidi.getName());

        mPlayButton = (Button) v.findViewById(R.id.midi_play_button);
        mPlayButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mMidi.play();
            }
        });

        mStopButton = (Button) v.findViewById(R.id.midi_stop_button);
        mStopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mMidi.stop();
            }
        });

        mMidiInfoTextView = (TextView) v.findViewById(R.id.midi_info_text_view);
        mMidiInfoTextView.setText(getResources().getString(R.string.get_information));

        mGetInfoButton = (Button) v.findViewById(R.id.midi_info_button);
        mGetInfoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mLines.clear();
                mRealPitchList.getPitchList().clear();
                setMidiInfo();
            }
        });

        mPitchTextView = (TextView) v.findViewById(R.id.pitch_text_view);

        mPitchHandler = new Handler();
        mPitchFreshRunnable = new Runnable() {
            @Override
            public void run() {
                Integer pitch = ((Long) Math.round(PitchIntentService.getPitchInSemitone())).intValue();
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        Integer pitch = ((Long) Math.round(PitchIntentService.getPitchInSemitone())).intValue();
                        if (!mIsPracticing && pitch > 25 && pitch < 4280) {
                            mIsPracticing = true;
                        }
                        if (mIsPracticing) {
                            mRealPitchList.getPitchList().add(pitch);
                            int length = mRealPitchList.getPitchList().size();
                            int index = length - 1;
                            int pitchTest = index < mMidiPitchList.getPitchList().size() ? mMidiPitchList.getPitchList().get(index) : -1;
                            if (pitch > 25 && pitch < 4280) {
                                if (pitchTest == mRealPitchList.getPitchList().get(index)) {
                                    addIntegerLineToChart(pitch, length - 1, ChartUtils.COLOR_GREEN);
                                } else {
                                    mErrorCount++;
                                    addIntegerLineToChart(pitch, length - 1, ChartUtils.COLOR_RED);
                                }
                                updateChartMaxViewport(length);
                            }
                        }
                    }
                }).start();
                mPitchTextView.setText(pitch + getResources().getString(R.string.note));
                if (mDetectingPitch) {
                    mPitchHandler.postDelayed(mPitchFreshRunnable, mDelayTime);
                }
            }
        };

        mInfoProgressBar = (ProgressBar) v.findViewById(R.id.info_progress_bar);
        mInfoProgressBar.setVisibility(View.GONE);

        mIsPracticing = false;
        mDetectingPitch = false;

        mPracticeButton = (Button) v.findViewById(R.id.practice_button);
        mPracticeButton.setEnabled(false);
        mPracticeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mDetectingPitch) {
                    mPracticeButton.setText(R.string.start_practicing);
                    mPitchTextView.setText(0 + getResources().getString(R.string.note));
                    mMidiInfoTextView.setText("Error: " + mErrorCount + "/" + mMidiPitchList.getPitchList().size());
                    mGetInfoButton.setEnabled(true);
                    mDetectingPitch = false;
                    mIsPracticing = false;
                } else {
                    mPracticeButton.setText(R.string.stop_practicing);
                    mGetInfoButton.setEnabled(false);
                    mDetectingPitch = true;
                    mPitchHandler.postDelayed(mPitchFreshRunnable, mDelayTime);
                }
            }
        });

        mChart = (LineChartView) v.findViewById(R.id.chart);
        mLines = new ArrayList<Line>();

        return v;
    }

    private void setMidiInfo () {

        final Handler handler = new SetMidiInfoHandler(this);
        mInfoProgressBar.setVisibility(View.VISIBLE);
        mGetInfoButton.setEnabled(false);
        mMidiInfoTextView.setText(getResources().getString(R.string.please_wait));
        new Thread(new Runnable() {
            @Override
            public void run() {
                mMidi.setMidiInfo();
                mMidi.setMidiPitchList(mDelayTime);
                mMidiPitchList = mMidi.getMidiPitchList();
                handler.sendEmptyMessage(0);
            }
        }).start();
    }

    private static class SetMidiInfoHandler extends Handler {
        private final WeakReference<MidiFragment> mMidiFragmentWeakReference;

        public SetMidiInfoHandler (MidiFragment fragment) {
            mMidiFragmentWeakReference = new WeakReference<MidiFragment>(fragment);
        }

        @Override
        public void handleMessage(Message msg) {
            MidiFragment fragment = mMidiFragmentWeakReference.get();
            // fragment.showMidiInfo();
            // fragment.showMidiPitchList();
            fragment.drawMidiChart();
            fragment.mPracticeButton.setEnabled(true);
            fragment.mGetInfoButton.setEnabled(true);
            fragment.mErrorCount = 0;
            fragment.mInfoProgressBar.setVisibility(View.GONE);
        }
    }

    private void showMidiInfo () {
        MyNotes myNotes = mMidi.getMyNotes();
        String midiInfo = "pqn:" + myNotes.getTempo().getMpqn() + "\n";
        midiInfo += "\n";
        for (MyNote myNote : myNotes.getMyNotes() ) {
            midiInfo += "NoteValue:" + myNote.getNoteOn().getNoteValue() + "\n";
            midiInfo += "Tick:" + myNote.getNoteOn().getTick() + "-";
            midiInfo += myNote.getNoteOff().getTick() + "\n";
            midiInfo += "\n";
        }

        midiInfo += "\n";
        midiInfo += "Full Info: \n";
        for (String mi : mMidi.getMidiInfo()) {
            midiInfo += mi + "\n";
        }

        mMidiInfoTextView.setText(midiInfo);
    }

    private void showMidiPitchList () {
        ArrayList<Integer> pitchList = mMidiPitchList.getPitchList();
        String pitchListInfo = "PitchList: \n";
        int i = 0;
        for (Integer j : pitchList) {
            pitchListInfo += (i*mDelayTime + " : " +  j + "\n");
            i++;
        }
        mMidiInfoTextView.setText(pitchListInfo);
    }

    private void drawMidiChart () {

        for (int i = 0; i < mMidiPitchList.getPitchList().size(); i++) {
            int pitch = mMidiPitchList.getPitchList().get(i);
            if (pitch > 0) {
                addIntegerLineToChart(mMidiPitchList.getPitchList().get(i), i, ChartUtils.COLOR_ORANGE);
            }
        }
        mMidiInfoTextView.setText(getResources().getString(R.string.chart_show_below));

        /*
        MyNotes myNotes = mMidi.getMyNotes();
        int pqn = myNotes.getTempo().getMpqn();
        int resolution = myNotes.getResolution();
        for (MyNote myNote : myNotes.getMyNotes()) {
            ArrayList<PointValue> values = new ArrayList<PointValue>();
            values.add(new PointValue(myNote.getOnMs(), myNote.getPitch()));
            values.add(new PointValue(myNote.getOffMs(), myNote.getPitch()));
            Line line = new Line (values);
            line.setColor(ChartUtils.COLOR_BLUE);
            line.setShape(ValueShape.CIRCLE);
            line.setHasPoints(false);
            line.setHasLabels(false);
            mLines.add(line);
        }
        updateChart();
        mMidiInfoTextView.setText(getResources().getString(R.string.chart_show_below));
        */
    }

    private void addIntegerLineToChart (int num, int index, int color) {
        ArrayList<PointValue> values = new ArrayList<PointValue>();
        values.add(new PointValue(mDelayTime * index, num));
        values.add(new PointValue(mDelayTime * (index+1), num));
        Line line = new Line (values);
        line.setColor(color);
        line.setShape(ValueShape.SQUARE);
        line.setHasPoints(false);
        line.setHasLabels(false);
        mLines.add(line);
        updateChart();
    }

    private void updateChart () {
        LineChartData data = new LineChartData(mLines);
        Axis axisX = new Axis();
        Axis axisY = new Axis();
        axisX.setName(getResources().getString(R.string.chart_axis_x));
        axisY.setName(getResources().getString(R.string.chart_axis_y));
        data.setAxisXBottom(axisX);
        data.setAxisYLeft(axisY);
        mChart.setZoomEnabled(true);
        mChart.setLineChartData(data);
    }

    private void updateChartMaxViewport (int index) {
        int length = mMaxViewMs / mDelayTime;
        if (index > length) {
            Viewport v = new Viewport(mChart.getMaximumViewport());
            v.left = (index - length) * mDelayTime;
            mChart.setCurrentViewport(v);
        }
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mMidi.stop();
        mPitchHandler.removeCallbacks(mPitchFreshRunnable);
        Log.i (TAG, "onDestroyView()");
    }
}

package com.mwangblog.midiprocessing;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
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

import org.w3c.dom.Text;

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
import lecho.lib.hellocharts.util.ChartUtils;
import lecho.lib.hellocharts.view.LineChartView;

public class MidiFragment extends Fragment {

    private Midi mMidi;

    private static final String ARG_MIDI_NAME = "midi_id";
    private static final String TAG = "MidiFragment";

    private TextView mTitleTextView;
    private Button mPlayButton;
    private Button mStopButton;
    private Button mGetInfoButton;
    private TextView mPitchTextView;
    private TextView mMidiInfoTextView;
    private LineChartView mChart;
    private ProgressBar mInfoProgressBar;

    private Handler mPitchHandler;
    private Runnable mPitchFreshRunnable;


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
        mMidiInfoTextView.setText("Press Button to get infomation.");

        mGetInfoButton = (Button) v.findViewById(R.id.midi_info_button);
        mGetInfoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setMidiInfo();
            }
        });

        mPitchTextView = (TextView) v.findViewById(R.id.pitch_text_view);

        mPitchHandler = new Handler();
        mPitchFreshRunnable = new Runnable() {
            @Override
            public void run() {
                mPitchTextView.setText(String.format(getResources().getString(R.string.pitch_information), PitchIntentService.getPitchInHz()));
                mPitchHandler.postDelayed(mPitchFreshRunnable, 200);
                // Log.i(TAG, "mPitchTextView setText" + PitchIntentService.getPitchInHz());
            }
        };
        mPitchHandler.postDelayed(mPitchFreshRunnable, 200);

        mInfoProgressBar = (ProgressBar) v.findViewById(R.id.info_progress_bar);
        mInfoProgressBar.setVisibility(View.GONE);

        mChart = (LineChartView) v.findViewById(R.id.chart);

        return v;
    }

    private void setMidiInfo () {
        final Handler handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                mInfoProgressBar.setVisibility(View.GONE);
                mGetInfoButton.setEnabled(true);
                DrawMidiChart();
                // ShowMidiInfo();
            }
        };

        mInfoProgressBar.setVisibility(View.VISIBLE);
        mGetInfoButton.setEnabled(false);
        new Thread(new Runnable() {
            @Override
            public void run() {
                mMidi.setMidiInfo();
                handler.sendEmptyMessage(0);
            }
        }).start();
    }

    private void ShowMidiInfo () {
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

    private void DrawMidiChart () {
        MyNotes myNotes = mMidi.getMyNotes();
        ArrayList<Line> lines = new ArrayList<Line>();
        for (MyNote myNote : myNotes.getMyNotes()) {
            ArrayList<PointValue> values = new ArrayList<PointValue>();
            values.add(new PointValue(myNote.getNoteOn().getTick(), myNote.getNoteOn().getNoteValue()));
            values.add(new PointValue(myNote.getNoteOff().getTick(), myNote.getNoteOff().getNoteValue()));
            Line line = new Line (values);
            line.setColor(ChartUtils.COLOR_RED);
            line.setShape(ValueShape.CIRCLE);
            line.setHasPoints(false);
            line.setHasLabels(false);
            lines.add(line);
        }
        LineChartData data = new LineChartData(lines);
        Axis axisX = new Axis();
        Axis axisY = new Axis();
        axisX.setName(getResources().getString(R.string.chart_axis_x));
        axisY.setName(getResources().getString(R.string.chart_axis_y));
        data.setAxisXBottom(axisX);
        data.setAxisYLeft(axisY);
        mChart.setZoomEnabled(true);
        mChart.setLineChartData(data);
        mMidiInfoTextView.setText(getResources().getString(R.string.chart_show_below));
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mMidi.stop();
        mPitchHandler.removeCallbacks(mPitchFreshRunnable);
        Log.i (TAG, "onDestroyView()");
    }
}

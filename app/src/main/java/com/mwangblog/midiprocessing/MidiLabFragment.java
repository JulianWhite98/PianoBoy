package com.mwangblog.midiprocessing;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Layout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.util.List;

public class MidiLabFragment extends Fragment {

    private MidiAdapter mAdapter;
    private RecyclerView mMidiRecyclerView;

    public static MidiLabFragment newInstance() {
        return new MidiLabFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_midi_lab, container, false);
        mMidiRecyclerView = (RecyclerView) view.findViewById(R.id.midi_recycler_view);
        mMidiRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        updateUI();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        updateUI();
    }

    private void updateUI() {
        MidiLab midiLab = MidiLab.get(getActivity());
        List<Midi> midis = midiLab.getMidis();

        if (mAdapter == null) {
            mAdapter = new MidiAdapter(midis);
            mMidiRecyclerView.setAdapter(mAdapter);
        } else {
            mAdapter.notifyDataSetChanged();
        }
    }

    private class MidiHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        private Midi mMidi;

        private TextView mTitleTextView;

        public MidiHolder (LayoutInflater inflater, ViewGroup parent) {
            super(inflater.inflate(R.layout.list_item_midi,parent,false));
            itemView.setOnClickListener(this);

            mTitleTextView = (TextView) itemView.findViewById(R.id.list_title_text_view);

        }

        public void bind (Midi midi) {
            mMidi = midi;
            mTitleTextView.setText(mMidi.getName());
        }

        @Override
        public void onClick(View v) {
            // Toast.makeText(getActivity(), mMidi.getName() + " clicked!", Toast.LENGTH_SHORT).show();
            Intent intent = MidiActivity.newIntent (getActivity(), mMidi.getName());
            startActivity(intent);
        }
    }

    private class MidiAdapter extends RecyclerView.Adapter<MidiHolder> {
        private List<Midi> mMidis;

        public MidiAdapter (List<Midi> midis) {
            mMidis = midis;
        }

        @NonNull
        @Override
        public MidiHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            LayoutInflater layoutInflater = LayoutInflater.from(getActivity());
            return new MidiHolder(layoutInflater, parent);
        }

        @Override
        public void onBindViewHolder(@NonNull MidiHolder holder, int position) {
            Midi midi = mMidis.get(position);
            holder.bind(midi);
        }

        @Override
        public int getItemCount() {
            return mMidis.size();
        }
    }

}

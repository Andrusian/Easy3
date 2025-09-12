package com.andrus.easy3;
// FirstLayoutFragment.java

import static android.content.ContentValues.TAG;
import static com.andrus.easy3.C.*;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class ThirdLayoutFragment extends Fragment {

    private Button button;
    TextView textVol;

    private ImageView scroller;                 //added
    private int position;                       //addded
    private Handler handler;
    TextView curVolR;
    TextView freqOut;
    TextView freq2Out;
    TextView mixText;

    private static final String[] WAVE_TYPES = {"Sine", "Square", "Saw", "Tri","TENS"};

    // ADDED: Factory method to create a new instance with position
    public static ThirdLayoutFragment newInstance(int position) {
        ThirdLayoutFragment fragment = new ThirdLayoutFragment();
        Bundle args = new Bundle();
        args.putInt("position", position);
        fragment.setArguments(args);
        return fragment;
    }

    // ADDED...

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            position = getArguments().getInt("position", 2);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.frag_third_layout, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Access shared data from activity
        MainActivity activity = (MainActivity) requireActivity();

        // Get the scroller ImageView                   ADDED
        scroller = view.findViewById(R.id.scroller2);   // ADDED

        if (scroller == null) {
            Log.e(TAG, "scroller3 ImageView not found in layout!");
        } else {
            Log.d(TAG, "Found scroller3: " + scroller.getWidth() + "x" + scroller.getHeight());

            // Make sure the scroller is visible and clickable
            scroller.setVisibility(View.VISIBLE);
            scroller.setClickable(true);
            scroller.setBackgroundColor(Color.parseColor("#33FF0000")); // Semi-transparent red for debugging

            // Wait for layout to complete before registering
            scroller.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    // Remove listener to avoid multiple calls
                    scroller.getViewTreeObserver().removeOnGlobalLayoutListener(this);

                    Log.d(TAG, "Scroller layout complete: " + scroller.getWidth() + "x" + scroller.getHeight());

                    // Register the scroller with the ScrollHelper
                    if (getActivity() instanceof MainActivity) {
                        ((MainActivity) getActivity()).registerScrollableView(position, scroller);
                        Log.d(TAG, "Registered scroller for position " + position);
                    } else {
                        Log.e(TAG, "Activity is not MainActivity!");
                    }
                }
            });
        }

        freqOut=view.findViewById(R.id.freqOut);
        freq2Out=view.findViewById(R.id.freq2Out);
        rightMix = view.findViewById(R.id.Rmix);
        rightMix.setLabel("Mix");
        rightMix.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                double position = 1. - seekBar.getProgress() / 100.;
                synth.mixR.setRatio(position);
                Log.i("EASY3", "right mix position "+position);
            }
        });

        mixText=view.findViewById((R.id.mix));

        button=view.findViewById((R.id.e));
        button.setText(String.format("%3.0fHz",freqL1default));
        button.setOnClickListener ( v-> synth.oscR1.setFreq(freqL1default));
        button=view.findViewById((R.id.e2));
        button.setText(String.format("%3.0fHz",freqL2default));
        button.setOnClickListener ( v-> synth.oscR2.setFreq(freqL2default));

        button=view.findViewById(R.id.a);
        button.setOnClickListener ( v-> synth.oscR1.changeFreq(3./2.));
        button=view.findViewById(R.id.b);
        button.setOnClickListener ( v-> synth.oscR1.changeFreq(4./3.));
        button=view.findViewById(R.id.c);
        button.setOnClickListener ( v-> synth.oscR1.changeFreq(5./4.));
        button=view.findViewById(R.id.d);
        button.setOnClickListener ( v-> synth.oscR1.changeFreq(6./5.));
        button=view.findViewById(R.id.f);
        button.setOnClickListener ( v-> synth.oscR1.changeFreq(5./6.));
        button=view.findViewById(R.id.g);
        button.setOnClickListener ( v-> synth.oscR1.changeFreq(4./5.));
        button=view.findViewById(R.id.h);
        button.setOnClickListener ( v-> synth.oscR1.changeFreq(3./4.));
        button=view.findViewById(R.id.i);
        button.setOnClickListener ( v-> synth.oscR1.changeFreq(2./3.));

        button=view.findViewById(R.id.a2);
        button.setOnClickListener ( v-> synth.oscR2.changeFreq(3./2.));
        button=view.findViewById(R.id.b2);
        button.setOnClickListener ( v-> synth.oscR2.changeFreq(4./3.));
        button=view.findViewById(R.id.c2);
        button.setOnClickListener ( v-> synth.oscR2.changeFreq(5./4.));
        button=view.findViewById(R.id.d2);
        button.setOnClickListener ( v-> synth.oscR2.changeFreq(6./5.));
        button=view.findViewById(R.id.f2);
        button.setOnClickListener ( v-> synth.oscR2.changeFreq(5./6.));
        button=view.findViewById(R.id.g2);
        button.setOnClickListener ( v-> synth.oscR2.changeFreq(4./5.));
        button=view.findViewById(R.id.h2);
        button.setOnClickListener ( v-> synth.oscR2.changeFreq(3./4.));
        button=view.findViewById(R.id.i2);
        button.setOnClickListener ( v-> synth.oscR2.changeFreq(2./3.));

        // note: detune is only offered on primary carrier

        button=view.findViewById((R.id.detunePlusR));
        button.setOnClickListener ( v-> synth.oscR1.incFreq(.3F));
        button=view.findViewById((R.id.detuneMinusR));
        button.setOnClickListener ( v-> synth.oscR1.incFreq(-.3F));

        button=view.findViewById((R.id.boostR));
        button.setOnClickListener (
                v-> synth.boostR(.01)
        );
        button=view.findViewById((R.id.duckR));
        button.setOnClickListener (
                v-> synth.duckR(.01)
        );

        button=view.findViewById((R.id.voldefR));
        button.setOnClickListener (
                v-> {
                    synth.outMod.doRampin(1.);
                    C.synth.setVolR(.7);
                }
        );

        button=view.findViewById(R.id.muteR);
        button.setOnClickListener (
                v-> C.synth.setVolR(0)
        );

        // do the 4 waveformSpinners

        waveformSpinnerR1 = view.findViewById(R.id.formR);
        waveformSpinnerR2 = view.findViewById(R.id.formR2);

        // Create adapter using the waveform types
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_spinner_dropdown_item,
                WAVE_TYPES);

        waveformSpinnerR1.setAdapter(adapter);
        waveformSpinnerR2.setAdapter(adapter);

        waveformSpinnerR1.setSelection(0); // Default to "Disabled"
        waveformSpinnerR2.setSelection(0); // Default to "Disabled"

        // Set up the listener for selections
        waveformSpinnerR1.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedType = WAVE_TYPES[position];
                handleWaveformSelection(parent,selectedType, position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Optional handling if nothing is selected
            }
        });

        // Set up the listener for selections
        waveformSpinnerR2.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedType = WAVE_TYPES[position];
                handleWaveformSelection(parent,selectedType, position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Optional handling if nothing is selected
            }
        });

        curVolR=view.findViewById((R.id.curVolR));
        handler=new Handler(Looper.getMainLooper());
        startUpdatingTextView();
    }



    private void startUpdatingTextView() {
        // Create a Runnable that updates the TextView
        Runnable updateRunnable = new Runnable() {
            @Override
            public void run() {
                // Update the counter and set the text
                curVolR.setText(String.format("%3.0f %%",synth.getVolR()*100.));
                String text=String.format("%5.1fHz",synth.oscR1.freq);
                freqOut.setText(text);
                text=String.format("%5.1fHz",synth.oscR2.freq);
                freq2Out.setText(text);
                mixText.setText(String.format("%3.0f%%",synth.mixR.ratio*100));

                // Schedule the next update in 500 milliseconds (half a second)
                handler.postDelayed(this, 250);
            }
        };

        // Start the updates
        handler.post(updateRunnable);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // Remove callbacks to prevent memory leaks
        handler.removeCallbacksAndMessages(null);
    }


    private void handleWaveformSelection(AdapterView<?> parent, String waveType, int position) {
        // You can identify which spinner triggered this by checking parent.getId() if needed
        int parentId=parent.getId();
        int oscForm;

        // This selection is specific to Carrier Oscillators.

        switch (position) {
            case 0: // None
            default:
                oscForm=Oscillator.SINE;
                break;
            case 1: // Square
                oscForm=Oscillator.SQUARE;
                break;
            case 2: // Saw
                oscForm=Oscillator.SAW;
                break;
            case 3: // Tri
                oscForm=Oscillator.TRI;
                break;
            case 4: // Random
                oscForm=Oscillator.TENS;
                break;
        }

        // assign the form to the appropriate oscillator

        if (parentId==R.id.formR) {
            C.synth.oscR1.setForm(oscForm);
        }
        else if (parentId==R.id.formR2) {
            C.synth.oscR2.setForm(oscForm);
        }

        // do a brief fadein

        C.synth.outMod.doRampin(.75);
    }

    public void updateVol(double current) {
        textVol.setText("Vol:"+String.format("3.0f%",current*100));
        textVol.invalidate();
    }
}

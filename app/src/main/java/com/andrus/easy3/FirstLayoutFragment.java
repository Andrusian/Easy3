package com.andrus.easy3;
// FirstLayoutFragment.java

import static android.content.ContentValues.TAG;
import static com.andrus.easy3.C.*;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class FirstLayoutFragment extends Fragment {

    private Button button;
    TextView textVol;
    private Dialog popup;
    private Button carrier_phase_button;

    private ImageView scroller;                 //added
    private int position;                       //addded
    private Handler handler1;
    private Handler handler2;
    TextView curVolL;
    TextView freqOut;
    TextView freq2Out;
    View popupView;
    Button cancelButton;
    AudioDialer audioDial1;

    private static final String[] WAVE_TYPES = {"Sine", "Square", "Saw", "Tri", "TENS"};


    // ADDED: Factory method to create a new instance with position
    public static FirstLayoutFragment newInstance(int position) {
        FirstLayoutFragment fragment = new FirstLayoutFragment();
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
            position = getArguments().getInt("position", 0);
        }
        //Log.d("EASY3", "FirstLayoutFragment created with position: " + position);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View inflate = inflater.inflate(R.layout.frag_first_layout, container, false);
        return inflate;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Access shared data from activity
        MainActivity activity = (MainActivity) requireActivity();

        createAnimatedPopup();   // create our phase popup

        // Get the scroller ImageView
        scroller = view.findViewById(R.id.scroller2);
        if (scroller == null) {
            Log.e(TAG, "scroller1 ImageView not found in layout!");
        } else {
            Log.d(TAG, "Found scroller1 FIRST: " + scroller.getWidth() + "x" + scroller.getHeight());
            // Make sure the scroller is visible and clickable
            scroller.setVisibility(View.VISIBLE);
            scroller.setClickable(true);
            scroller.setBackgroundColor(Color.parseColor("#33FF0000")); // Semi-transparent red for debuggin

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

        carrier_phase_button=view.findViewById(R.id.carrier_phase_button);
        carrier_phase_button.setOnClickListener(
           dialog -> showAnimatedPopup()
        );

        leftMix = view.findViewById(R.id.Lmix);
        button=view.findViewById((R.id.e));
        button.setText(String.format("%3.0fHz",freqL1default));
        button.setOnClickListener ( v-> synth.oscL1.setFreq(freqL1default));
        button=view.findViewById((R.id.e2));
        button.setText(String.format("%3.0fHz",freqL2default));
        button.setOnClickListener ( v-> synth.oscL1.setFreq(freqL2default));

        button=view.findViewById(R.id.a);
        button.setOnClickListener ( v-> synth.oscL1.changeFreq(3./2.));
        button=view.findViewById(R.id.b);
        button.setOnClickListener ( v-> synth.oscL1.changeFreq(4./3.));
        button=view.findViewById(R.id.c);
        button.setOnClickListener ( v-> synth.oscL1.changeFreq(5./4.));
        button=view.findViewById(R.id.d);
        button.setOnClickListener ( v-> synth.oscL1.changeFreq(6./5.));
        button=view.findViewById(R.id.f);
        button.setOnClickListener ( v-> synth.oscL1.changeFreq(5./6.));
        button=view.findViewById(R.id.g);
        button.setOnClickListener ( v-> synth.oscL1.changeFreq(4./5.));
        button=view.findViewById(R.id.h);
        button.setOnClickListener ( v-> synth.oscL1.changeFreq(3./4.));
        button=view.findViewById(R.id.i);
        button.setOnClickListener ( v-> synth.oscL1.changeFreq(2./3.));

        button=view.findViewById(R.id.a2);
        button.setOnClickListener ( v-> synth.oscL2.changeFreq(3./2.));
        button=view.findViewById(R.id.b2);
        button.setOnClickListener ( v-> synth.oscL2.changeFreq(4./3.));
        button=view.findViewById(R.id.c2);
        button.setOnClickListener ( v-> synth.oscL2.changeFreq(5./4.));
        button=view.findViewById(R.id.d2);
        button.setOnClickListener ( v-> synth.oscL2.changeFreq(6./5.));
        button=view.findViewById(R.id.f2);
        button.setOnClickListener ( v-> synth.oscL2.changeFreq(5./6.));
        button=view.findViewById(R.id.g2);
        button.setOnClickListener ( v-> synth.oscL2.changeFreq(4./5.));
        button=view.findViewById(R.id.h2);
        button.setOnClickListener ( v-> synth.oscL2.changeFreq(3./4.));
        button=view.findViewById(R.id.i2);
        button.setOnClickListener ( v-> synth.oscL2.changeFreq(2./3.));


        // note: detune is only offered on primary carrier

        button=view.findViewById((R.id.detunePlusR));
        button.setOnClickListener ( v-> synth.oscL1.incFreq(.3F));
        button=view.findViewById((R.id.detuneMinusR));
        button.setOnClickListener ( v-> synth.oscL1.incFreq(-.3F));

        button=view.findViewById((R.id.boostR));
        button.setOnClickListener (
                v-> C.synth.boostL(.01)
        );
        button=view.findViewById((R.id.duckR));
        button.setOnClickListener (
                v-> C.synth.duckL(.01)
        );
        button=view.findViewById((R.id.voldefR));
        button.setOnClickListener (
                v-> {
                    synth.outMod.doRampin(1.);
                    C.synth.setVolL(.7);
                }
                );

        button=view.findViewById(R.id.muteR);
        button.setOnClickListener ( v-> C.synth.setVolL(0));

        freqOut=view.findViewById(R.id.freqOut);
        freq2Out=view.findViewById(R.id.freq2Out);


        // do the 4 waveformSpinners

        waveformSpinnerL1 = view.findViewById(R.id.formR);
        waveformSpinnerL2 = view.findViewById(R.id.formR2);

        // Create adapter using the waveform types
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_spinner_dropdown_item,
                WAVE_TYPES);

        waveformSpinnerL1.setAdapter(adapter);
        waveformSpinnerL2.setAdapter(adapter);

        waveformSpinnerL1.setSelection(0); // Default to "Disabled"
        waveformSpinnerL2.setSelection(0); // Default to "Disabled"

        curVolL=view.findViewById((R.id.curVolR));
        handler1=new Handler(Looper.getMainLooper());
        handler2=new Handler(Looper.getMainLooper());
        startUpdatingTextView();

        // Set up the listener for selections
        waveformSpinnerL1.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
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
        waveformSpinnerL2.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
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

    }

    //------------------------------------------------------------------------------
    // updates current volume

    private void startUpdatingTextView() {
        // Create a Runnable that updates the TextView
        Runnable updateRunnable = new Runnable() {
            @Override
            public void run() {
                // Update the counter and set the textviews
                String text=String.format("%3.0f %%",synth.getVolL()*100.);
                curVolL.setText(text);
                text=String.format("%5.1fHz",synth.oscL1.freq);
                freqOut.setText(text);
                text=String.format("%5.1fHz",synth.oscL2.freq);
                freq2Out.setText(text);

                // Schedule the next update in 500 milliseconds (half a second)
                handler1.postDelayed(this, 250);              }
        };

        // Start the updates
        handler1.post(updateRunnable);
    }

    //-------------------------------------------------------------------------------
    // does picklist for carrier waveform types

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
            case 4: // TENS
                oscForm=Oscillator.TENS;
                break;
        }

        // assign the form to the appropriate oscillator

        if (parentId==R.id.formR) {
            C.synth.oscL1.setForm(oscForm);
        }
        else if (parentId==R.id.formR2) {
            C.synth.oscL2.setForm(oscForm);
        }

        // do a brief fadein

        C.synth.outMod.doRampin(.75);
    }

    //==================================================================
    // phase is handled through a popup because screen space is limited
    //
    // this routine creates the popup dialog

    private void createAnimatedPopup() {
        // Create dialog
        popup = new Dialog(getContext());
        popup.setContentView(R.layout.phase1_popup);
        popup.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        popup.setCancelable(false);

        // Set dialog size
        Window window = popup.getWindow();
        WindowManager.LayoutParams params = window.getAttributes();
        params.width = (int) (300 * getResources().getDisplayMetrics().density);
        params.height = (int) (300 * getResources().getDisplayMetrics().density);
        params.gravity = Gravity.CENTER;
        window.setAttributes(params);

        // Get the popup view for animation
        popupView = popup.findViewById(R.id.popup_container);
        cancelButton = popup.findViewById(R.id.cancel_button);
        audioDial1 = popup.findViewById(R.id.phaseDial2);

        // Set initial state for animation
        popupView.setScaleX(0.7f);
        popupView.setScaleY(0.7f);
        popupView.setAlpha(0f);
        popupView.setTranslationY(-50f);
    }

    //------------------------------------------------------
    // pops up the dialog

    private void showAnimatedPopup() {

        // Show dialog
        popup.show();
        audioDial1.setTitle("Carrier Phase Difference");
        audioDial1.setValue(synth.oscL1.getPhase());  // initialize on popup to current value

        // Start animation
        popupView.animate()
                .scaleX(1f)
                .scaleY(1f)
                .alpha(1f)
                .translationY(0f)
                .setDuration(500)
                .setInterpolator(new OvershootInterpolator(1.2f))
                .start();
        startUpdatingPhase();

        // Cancel button click listener
        cancelButton.setOnClickListener(
                v->popup.hide()
        );

    }

    private void closePopupWithAnimation() {
        if (popup != null && popup.isShowing()) {
            View popupView = popup.findViewById(R.id.popup_container);

            popupView.animate()
                    .scaleX(0.7f)
                    .scaleY(0.7f)
                    .alpha(0f)
                    .translationY(-50f)
                    .setDuration(300)
                    .setInterpolator(new AccelerateInterpolator())
                    .withEndAction(() -> {
                        if (popup != null) {
                            popup.dismiss();
                        }
                    })
                    .start();
        }
    }

    //------------------------------------------------------------------------------
    // updates phase
    //
    // Phase could set by touching a value or phase could be set by an
    // extra oscillator. A flag determines which it is.

    private void startUpdatingPhase() {
        // Create a Runnable that updates the AudioDialer
        Runnable updateRunnable = new Runnable() {
            @Override
            public void run() {
                if (destinationFlags[DEST_LEFTPHASE]) {
                    // set value from external oscillator
                    audioDial1.setValue(destinations[DEST_LEFTPHASE]);
                }
                else {
                    // set common block value from view
                    synth.oscL1.setPhase(audioDial1.getCurrentValue());
                }

                // phase isn't that sensitive, this
                // doesn't need to be super fast

                handler2.postDelayed(this, 100);              }
        };

        // Start the updates
        handler2.post(updateRunnable);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (popup != null && popup.isShowing()) {
            popup.hide();
        }
    }
}

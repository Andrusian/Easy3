package com.andrus.easy3;

import static android.content.ContentValues.TAG;
import static android.graphics.Color.*;
import static com.andrus.easy3.C.*;
import static com.andrus.easy3.Oscillator.SQUARE;

import android.annotation.SuppressLint;
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
import android.widget.SeekBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class SecondLayoutFragment extends Fragment {
    private Button button;
    private ImageView scroller;                 //added
    private int position;                       //addded
    private Button muteButton;
    boolean muted;
    View popupView;
    Button cancelButton;
    Button amod_phase_button;
    AudioDialer audioDial2;
    private Dialog popup;
    private Handler handler3;
    private Handler handler4;
    private Button logButton;
    volatile boolean blockUpdate=false;

    private static final String[] WAVE_TYPES = {"Off", "Sine", "Square", "Saw", "Tri","Random","Ramps","Pulses"};

    // ADDED: Factory method to create a new instance with position
    public static SecondLayoutFragment newInstance(int position) {
        SecondLayoutFragment fragment = new SecondLayoutFragment();
        Bundle args = new Bundle();
        args.putInt("position", position);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            position = getArguments().getInt("position", 1);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fraq_second_layout, container, false);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        handler3=new Handler(Looper.getMainLooper());

        createAnimatedPopup();   // create our phase popup

        logButton=view.findViewById(R.id.Log);
        logButton.setOnClickListener(v->
                synth.mydebug.trigger()
        );

        // Get the scroller ImageView
        scroller = view.findViewById(R.id.scroller2);
        if (scroller == null) {
            Log.e(TAG, "scroller2 ImageView not found in layout!");
        } else {
            Log.d(TAG, "Found scroller2: " + scroller.getWidth() + "x" + scroller.getHeight());

            // Make sure the scroller is visible and clickable
            scroller.setVisibility(View.VISIBLE);
            scroller.setClickable(true);
            scroller.setBackgroundColor(parseColor("#33FF0000")); // Semi-transparent red for debugging

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

            // Using View.post with lambda
            view.post(() -> {
                // This code executes after the view has been measured and laid out
                synth.initialize();
                synth.start();
            });
        }

        amod_phase_button=view.findViewById(R.id.amod_phase_button);
        amod_phase_button.setOnClickListener(
                dialog -> showAnimatedPopup()
        );

        padL1 = view.findViewById(R.id.padL1);
        padL1.setXAxisLabel("Freq");
        padL1.setYAxisLabel("Depth");
        padL1.setTitle("L1");
        padL1.gotoPosition(1.0F, .1F,true);
        padL1.setOnPositionChangedListener(new AudioTouchPad.OnPositionChangedListener() {
            @Override
            public void onPositionChanged(float x, float y) {
                // lower frequency range: 10s to 0.5Hz
                double freq=0.1*Math.exp(2.878*x);
                synth.amodL1.setFreq(freq);
                synth.amodL1.setDepth(1.-padL1.actualY);
            }
        });

        padL2 = view.findViewById(R.id.padL2);
        padL2.setXAxisLabel("Freq");
        padL2.setYAxisLabel("Depth");
        padL2.setTitle("L2");
        padL2.gotoPosition(1.0F, 0.1F,true);
        padL2.setOnPositionChangedListener(new AudioTouchPad.OnPositionChangedListener() {
            @Override
            public void onPositionChanged(float x, float y) {
                // higher frequency range: 1Hz to 20Hz
                double freq = x*19+1;
                synth.amodL2.setFreq(freq);
                synth.amodL2.setDepth(1.-padL2.actualY);
            }
        });

        padR1 = view.findViewById(R.id.padR1);
        padR1.setXAxisLabel("Freq");
        padR1.setYAxisLabel("Depth");
        padR1.setTitle("R1");
        padR1.gotoPosition(1.0F, 0.1F,true);
        padR1.setOnPositionChangedListener(new AudioTouchPad.OnPositionChangedListener() {
            @Override
            public void onPositionChanged(float x, float y) {
                synth.amodR1.setFreq(0.1*Math.exp(2.878* padR1.actualX));
                synth.amodR1.setDepth(1.-padR1.actualY);
            }
        });

        padR2 = view.findViewById(R.id.padR2);
        padR2.setXAxisLabel("Freq");
        padR2.setYAxisLabel("Depth");
        padR2.setTitle("R2");
        padR1.gotoPosition(1.0F, 0.1F,true);
        padR2.setOnPositionChangedListener(new AudioTouchPad.OnPositionChangedListener() {
            @Override
            public void onPositionChanged(float x, float y) {
                // higher frequency range: 1Hz to 20Hz
                double freq= x*19+1;
                synth.amodR2.setFreq(freq);
                synth.amodR2.setDepth(1.-padL2.actualY);
              }
        });

        // DUTY CYCLES (SQUARE WAVE + ODDBALL WAVES LIKE RAMPS)

        dutyL1 = view.findViewById(R.id.dutyL1);  // should be scaled 20-80
        dutyL1.setLabel("Duty");
        dutyL1.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int pos, boolean b) {
                // int pos=seekBar.getProgress();
                synth.amodL1.setDuty(pos/100.);
                // Log.i("EASY3","amodL1 duty: "+pos/100.);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
        dutyL2 = view.findViewById(R.id.dutyL2);  // should be scaled 20-80
        dutyL2.setLabel("Duty");
        dutyL2.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int pos, boolean b) {
                synth.amodL2.setDuty(pos/100.);
                // Log.i("EASY3","amodL1 duty: "+pos/100.);

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
        dutyR1 = view.findViewById(R.id.dutyR1);  // should be scaled 20-80
        dutyR1.setLabel("Duty");
        dutyR1.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int pos, boolean b) {
                synth.amodR1.setDuty(pos/100.);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        dutyR2 = view.findViewById(R.id.dutyR2);  // should be scaled 20-80
        dutyR2.setLabel("Duty");
        dutyR2.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int pos, boolean b) {
                synth.amodR2.setDuty(pos/100.);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        muteButton=view.findViewById((R.id.muteButton));
        muteButton.setOnClickListener ( v->{
                    if (!muted) {
                        muted=true;
                        synth.silence();
                        muteButton.setText("Unmute");
                        muteButton.setBackgroundColor(Color.rgb(255, 0, 0));
                    }
                    else {
                        muted=false;
                        synth.outMod.setVolL(.7);
                        synth.outMod.setVolR(.7);
                        synth.outMod.doRampin(2.);
                        muteButton.setText("Mute");
                        muteButton.setBackgroundColor(Color.rgb(200, 200, 200));
                    }
                }
        );

        // do the 4 waveformSpinners

        waveformSpinner1 = view.findViewById(R.id.L1form);
        waveformSpinner1.setSelection(0);
        waveformSpinner2 = view.findViewById(R.id.L2form);
        waveformSpinner1.setSelection(0);
        waveformSpinner3 = view.findViewById(R.id.R1form);
        waveformSpinner1.setSelection(0);
        waveformSpinner4 = view.findViewById(R.id.R2form);
        waveformSpinner1.setSelection(0);


        // Create adapter using the waveform types
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_spinner_dropdown_item,
                WAVE_TYPES);

        waveformSpinner1.setAdapter(adapter);
        waveformSpinner2.setAdapter(adapter);
        waveformSpinner3.setAdapter(adapter);
        waveformSpinner4.setAdapter(adapter);

        // Set up the listener for selections
        waveformSpinner1.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
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
        waveformSpinner2.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
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
        waveformSpinner3.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
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
        waveformSpinner4.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
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

        // need an updater for the 5 duty seekbars

        // handler4=new Handler(Looper.getMainLooper());
        //startUpdatingDuty();



         // Access shared data from activity
        MainActivity activity = (MainActivity) requireActivity();
    }

    //========================================================================------------------------------

    private void handleWaveformSelection(AdapterView<?> parent,String waveType, int position) {
        // You can identify which spinner triggered this by checking parent.getId() if needed
        int parentId=parent.getId();
        int oscForm;

        // This selection is specific to Amplitude modulators

        boolean dutyEnabled=false;
        switch (position) {

            case 1: // Sine
                oscForm=Oscillator.SINE;
                break;
            case 2: // Square
                oscForm= SQUARE;
                dutyEnabled=true;
                break;
            case 3: // Saw
                oscForm=Oscillator.SAW;
                break;
            case 4: // Tri
                oscForm=Oscillator.TRI;
                break;
            case 5: // Random
                oscForm=Oscillator.RANDOM;
                break;
            case 6: // Ramps
                oscForm=Oscillator.RAMPS;
                dutyEnabled=true;
                break;
            case 7: // Pulses
                oscForm=Oscillator.PULSES;
                dutyEnabled=true;
                break;
            case 0: // None
            default:
                oscForm=Oscillator.OFF;
                break;
        }

        // assign the form to the appropriate oscillator
        // also enable/disable the touchpad

        if (parentId==R.id.L1form) {
            C.synth.amodL1.setForm(oscForm);
            dutyL1.setEnabled(dutyEnabled);
        }
        else if (parentId==R.id.L2form) {
            C.synth.amodL2.setForm(oscForm);
            dutyL2.setEnabled(dutyEnabled);
        }
        else if (parentId==R.id.R1form) {
            C.synth.amodR1.setForm(oscForm);
            dutyR1.setEnabled(dutyEnabled);
        }
        else if (parentId==R.id.R2form) {
            C.synth.amodR2.setForm(oscForm);
            dutyR2.setEnabled(dutyEnabled);
        }
    }

    //=====================================================
    // phase dialog: pops up the dialog

    private void createAnimatedPopup() {
        // Create dialog
        popup = new Dialog(getContext());
        popup.setContentView(R.layout.phase2_popup);
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
        audioDial2 = popup.findViewById(R.id.phaseDial2);

        // Set initial state for animation
        popupView.setScaleX(0.7f);
        popupView.setScaleY(0.7f);
        popupView.setAlpha(0f);
        popupView.setTranslationY(-50f);
    }

    private void showAnimatedPopup() {

        // Show dialog
        popup.show();
        audioDial2.setTitle("AMOD Phase Difference");
        audioDial2.setValue(synth.amodL1.getPhase());  // initialize on popup to current value

        // Start animation
        popupView.animate()
                .scaleX(1f)
                .scaleY(1f)
                .alpha(1f)
                .translationY(0f)
                .setDuration(200)
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

    //==============================================================================
    // HANDLERS
    //
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
                if (destinationFlags[DEST_L1PHASE]) {
                    // set value from external oscillator
                    audioDial2.setValue(destinations[DEST_L1PHASE]);
                }
                else {
                    // set common block value from view
                    synth.amodL1.setPhase(audioDial2.getCurrentValue());
                }

                // phase isn't that sensitive, this
                // doesn't need to be super fast

                handler3.postDelayed(this, 100);              }
        };

        // Start the updates
        handler3.post(updateRunnable);
    }

//------------------------------------------------------------------------
// DUTY SLIDER HANDLER
// This doesn't work. It's an concurrency/thread issue I'm pretty sure.

    private void startUpdatingDuty() {
        // Create a Runnable that updates the AudioDialer
        Runnable updateRunnable = new Runnable() {
            @Override
            public void run() {

                //duty seekbars are 20 to 80 but
                // I don't think overrange/underrange is a problem.

                dutyL1.setProgress((int) Math.floor(synth.oscL1.getDuty()*100.));
                //Log.i("EASY3","amodL1 duty: "+synth.oscL1.getDuty());
                dutyL2.setProgress((int) Math.floor(synth.oscL2.getDuty()*100.));
                dutyR1.setProgress((int) Math.floor(synth.oscR1.getDuty()*100.));
                dutyR2.setProgress((int) Math.floor(synth.oscR2.getDuty()*100.));
                handler4.postDelayed(this, 500);
            }
        };

        // Start the updates
        handler4.post(updateRunnable);
    }

    //===============================================
@Override
    public void onDestroy() {
        super.onDestroy();
        if (popup != null && popup.isShowing()) {
            popup.hide();
        }
    }
}

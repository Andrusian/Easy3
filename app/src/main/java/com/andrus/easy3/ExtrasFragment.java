package com.andrus.easy3;


import static android.content.ContentValues.TAG;

import static com.andrus.easy3.C.SILENCE_LEFT;
import static com.andrus.easy3.C.SILENCE_OFF;
import static com.andrus.easy3.C.SILENCE_ON;
import static com.andrus.easy3.C.SILENCE_RIGHT;
import static com.andrus.easy3.C.destinationFlags;
import static com.andrus.easy3.C.synth;
import static com.andrus.easy3.Oscillator.SAW;
import static com.andrus.easy3.Oscillator.SINE;
import static com.andrus.easy3.Oscillator.SQUARE;
import static com.andrus.easy3.Oscillator.TRI;

import android.content.res.ColorStateList;
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
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.Guideline;
import androidx.fragment.app.Fragment;

public class ExtrasFragment extends Fragment implements AdapterView.OnItemSelectedListener {
    private TextView textView;
    private Button button;
    private ImageView scroller;                 //added
    private int position;                       //addded
    private AudioDialer dial1;
    Button oscAbutton;
    Button oscBbutton;
    Button oscCbutton;
    Button silOff;
    Button booOff;
    TextView settingsA;
    TextView settingsB;
    TextView settingsC;
    Spinner spinnerA;
    Spinner spinnerB;
    Spinner spinnerC;
    CheckBox activeA;
    CheckBox activeB;
    CheckBox activeC;

    SeekBar silTime1;
    SeekBar silTime2;
    SeekBar silEvents;


    Runnable updater;
    int purpose;

    private double highLimit;
    private double lowLimit;
    private double highfreq;
    private double lowfreq;
    private String comment = new String("Unknown");
    OscSettingsDialog dialog;
    private Handler handler;

    // ADDED: Factory method to create a new instance with position
    public static com.andrus.easy3.ExtrasFragment newInstance(int position) {
        com.andrus.easy3.ExtrasFragment fragment = new com.andrus.easy3.ExtrasFragment();
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
            position = getArguments().getInt("position", 3);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_extras_layout, container, false);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ConstraintLayout constraintLayout = view.findViewById(R.id.layout4);

        Guideline guideline = view.findViewById(R.id.guideline14);
        View titleView = view.findViewById(R.id.title4);
        View previousView = titleView;
        settingsA = view.findViewById(R.id.settingsA);
        settingsB = view.findViewById(R.id.settingsB);
        settingsC = view.findViewById(R.id.settingsC);
        spinnerA = view.findViewById(R.id.spinnerA);
        spinnerB = view.findViewById(R.id.spinnerB);
        spinnerC = view.findViewById(R.id.spinnerC);
        activeA = view.findViewById(R.id.activeA);
        activeB = view.findViewById(R.id.activeB);
        activeC = view.findViewById(R.id.activeC);
        silOff = view.findViewById(R.id.rndOff);
        booOff = view.findViewById(R.id.booOff);
        silTime1 = view.findViewById(R.id.silTime1);
        silTime2 = view.findViewById(R.id.silTime2);
        silEvents = view.findViewById(R.id.silEvents);

        // Get the scroller ImageView
        scroller = view.findViewById(R.id.scroller5);

        if (scroller == null) {
            Log.e(TAG, "scroller5 ImageView not found in layout!");
        } else {
            Log.d(TAG, "Found scroller5: " + scroller.getWidth() + "x" + scroller.getHeight());

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

        String[] spinnerItems = getResources().getStringArray(R.array.destinations);

        ArrayAdapter<String> adapter;
        adapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_spinner_dropdown_item,
                spinnerItems
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerA.setAdapter(adapter);
        spinnerB.setAdapter(adapter);
        spinnerC.setAdapter(adapter);

        spinnerA.setOnItemSelectedListener(this);
        spinnerB.setOnItemSelectedListener(this);
        spinnerC.setOnItemSelectedListener(this);

        oscAbutton = view.findViewById(R.id.oscA);
        oscAbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showOscSettingsDialog(1);
            }
        });
        oscAbutton = view.findViewById(R.id.oscB);
        oscAbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showOscSettingsDialog(2);
            }
        });
        oscAbutton = view.findViewById(R.id.oscC);
        oscAbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showOscSettingsDialog(3);
            }
        });

        activeA.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton arg0, boolean isChecked) {
                handleCheckedChange(spinnerA, isChecked);
                synth.oscA.setActive(isChecked);

            }
        });
        activeB.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton arg0, boolean isChecked) {
                handleCheckedChange(spinnerB, isChecked);
                synth.oscB.setActive(isChecked);
            }
        });
        activeC.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton arg0, boolean isChecked) {
                handleCheckedChange(spinnerC, isChecked);
                synth.oscC.setActive(isChecked);
            }
        });

        silOff.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // implement the modes on the silence button

                if (synth.silenceMode==SILENCE_OFF) {
                    synth.silenceMode=SILENCE_ON;
                    booOff.setEnabled(false);
                 }
                else if (synth.silenceMode==SILENCE_ON) {
                    synth.silenceMode = SILENCE_LEFT;
                }
                else if (synth.silenceMode==SILENCE_LEFT) {
                    synth.silenceMode = SILENCE_RIGHT;
                }
                else /* if (synth.silenceMode==SILENCE_RIGHT) */ {
                    synth.silenceMode = SILENCE_OFF;
                    booOff.setEnabled(true);
                }
                silOff.invalidate();
                Log.i("extras","Silence button "+synth.silenceMode);
            }
        });

        booOff.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // implement the modes on the silence button

                if (synth.boostMode==SILENCE_OFF) {
                    synth.boostMode=SILENCE_ON;
                    silOff.setEnabled(false);
                }
                else if (synth.boostMode==SILENCE_ON) {
                    synth.boostMode = SILENCE_LEFT;
                }
                else if (synth.boostMode==SILENCE_LEFT) {
                    synth.boostMode = SILENCE_RIGHT;
                }
                else /* if (synth.silenceMode==SILENCE_RIGHT) */ {
                    synth.boostMode = SILENCE_OFF;
                    silOff.setEnabled(true);
                }
                booOff.invalidate();
                Log.i("extras","Boost button "+synth.boostMode);
            }
        });

        // need a handler to update the text and color because it could be controlled
        // from a preset
        handler=new Handler(Looper.getMainLooper());
        updater = new Runnable() {
            @Override
            public void run() {
                if (synth.silenceMode==SILENCE_OFF) {
                    silOff.setText("Silence OFF");
                    silOff.setBackgroundTintList (ColorStateList.valueOf(Color.parseColor("#999999")));
                }
                else if (synth.silenceMode==SILENCE_ON) {
                    silOff.setText("Sil Both");
                    silOff.setBackgroundTintList (ColorStateList.valueOf(Color.parseColor("#66cccc")));
                }
                else if (synth.silenceMode==SILENCE_LEFT) {
                    silOff.setText("Sil Left");
                    silOff.setBackgroundTintList (ColorStateList.valueOf(Color.parseColor("#66cc66")));
                }
                else /*  if (synth.silenceMode==SILENCE_RIGHT) */ {
                    silOff.setText("Sil Right");
                    silOff.setBackgroundTintList (ColorStateList.valueOf(Color.parseColor("#9999cc")));
                }
                silOff.invalidate();

                if (synth.boostMode==SILENCE_OFF) {
                    booOff.setText("Boost OFF");
                    booOff.setBackgroundTintList (ColorStateList.valueOf(Color.parseColor("#999999")));
                }
                else if (synth.boostMode==SILENCE_ON) {
                    booOff.setText("Bst Both");
                    booOff.setBackgroundTintList (ColorStateList.valueOf(Color.parseColor("#66cccc")));
                }
                else if (synth.boostMode==SILENCE_LEFT) {
                    booOff.setText("Bst Left");
                    booOff.setBackgroundTintList (ColorStateList.valueOf(Color.parseColor("#66cc66")));
                }
                else /*  if (synth.boostMode==SILENCE_RIGHT) */ {
                    booOff.setText("Bst Right");
                    booOff.setBackgroundTintList (ColorStateList.valueOf(Color.parseColor("#9999cc")));
                }
                silOff.invalidate();


                // Log.i("extras","silence: "+synth.silenceTrigger+" "+synth.silenceMode+" triggered:"
                //         +synth.silenceTriggered+" status:"+synth.silenceStatus+" cd:"+synth.countdown+" dosil:"+synth.dosilence+" ev:"+synth.silenceEventCount+"/"+synth.silenceEvents);
                // Schedule next update
                handler.postDelayed(this, 500);
            }
        };

        updater.run();

        // look after changes to delay seekbars: EVENTS

        silEvents.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                synth.silenceEvents=i;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        // handle initial state
        synth.silenceEvents=1;

        // look after changes to delay seekbars: delay time

        // TODO: allow presets to somehow update the visual controls.

        silTime1.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                // delay time is scaled in seconds in the widget
                // currently 5s to 60s
                // can use it directly

                synth.silenceDelay = i;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        // handle initial state
        synth.silenceDelay=20;

        silTime2.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                // delay time is scaled as a percent in seekbar
                // 5-25% is the range currently
                // once again, we'll use this directly but convert to decimal

                synth.silenceLength = i / 100.;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        //handle initial state
        synth.silenceLength=.2;

    }

    // to set interface from preset restore... might or might not work

    public void setSeekbars (double delay, double length, int events) {
        silTime1.setProgress((int) Math.floor(delay));
        silTime2.setProgress((int) Math.floor(length));
        silEvents.setProgress((int) Math.floor(events));
    }


    //------------------------------------------------------------------------
    // STEP 1 : called when a purpose (destination)  is selected from the spinner

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
        if (id==R.id.spinnerA) {
            if (!check_destination_unused(1,position)) {
                Toast toast = Toast.makeText(getActivity(), "Error - Destination already chosen.", Toast.LENGTH_SHORT);
                toast.show();
            }
            else {
                oscAbutton.setEnabled(true);
            }

        }
        else if (id==R.id.spinnerB) {
            if (!check_destination_unused(2,position)) {
                Toast toast = Toast.makeText(getActivity(), "Error - Destination already chosen.", Toast.LENGTH_SHORT);
                toast.show();
            }
            else {
                oscBbutton.setEnabled(true);
            }

        }
        else if (id==R.id.spinnerC) {
            if (!check_destination_unused(3,position)) {
                Toast toast = Toast.makeText(getActivity(), "Error - Destination already chosen.", Toast.LENGTH_SHORT);
                toast.show();
            }
            else {
                oscCbutton.setEnabled(true);
            }
        }

    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }

    //-----------------------------------------------------------------------
    boolean check_destination_unused (int i, int chosen) {
        if (i==1) {
            //noinspection RedundantIfStatement
            if ((chosen==synth.oscB.getDestination()) ||
            (chosen==synth.oscC.getDestination())) {
                return false;
            }
        }
        else if (i==2) {
            if ((chosen==synth.oscA.getDestination()) ||
                    (chosen==synth.oscC.getDestination())) {
                return false;
            }
        }
        else if (i==3) {
            if ((chosen==synth.oscA.getDestination()) ||
                    (chosen==synth.oscB.getDestination())) {
                return false;
            }

        }
        return true;

    }


    //=============================================================================
    // STEP 2a: popup dialog to configure oscillator
    //

    private void showOscSettingsDialog(int oscNumber) {
        int purpose=0;

        if (oscNumber==1) {
            dialog = new OscSettingsDialog(getActivity(), "Oscillator A");
            dialog.setNumber(oscNumber);
            purpose=spinnerA.getSelectedItemPosition();
            getLimits(purpose);
            //dialog.setupDialogValues(synth.oscA);
        }
        else if (oscNumber==2) {
            dialog = new OscSettingsDialog(getActivity(), "Oscillator B");
            dialog.setNumber(oscNumber);
            purpose=spinnerB.getSelectedItemPosition();
            getLimits(purpose);
            //dialog.setupDialogValues(synth.oscB);
        }
        else if (oscNumber==3) {
            dialog = new OscSettingsDialog(getActivity(), "Oscillator C");
            dialog.setNumber(oscNumber);
            purpose=spinnerC.getSelectedItemPosition();
            getLimits(purpose);
            //dialog.setupDialogValues(synth.oscC);
        }
        else {

            return;
        }

        if (purpose==0) {
            CharSequence text = "First choose choose something to drive.";
            int duration = Toast.LENGTH_SHORT;

            Toast toast = Toast.makeText(getActivity(), text, duration);
            toast.show();
            return;
        }

        //Toast toast = Toast.makeText(getActivity(), "Purpose: "+purpose, 2);
        //toast.show();

        // Set limits before showing the dialog
        dialog.setFrequencyLimit(highfreq,lowfreq);    // Max frequency 20kHz
        dialog.setValueLimit(highLimit,lowLimit);       // Max value limit
        dialog.setPurpose(comment);

        // Set current values (optional)
//        dialog.setCurrentValues("Sine", 2f, 1.f, -1.0f, 0.0f);

        // VVVVVVVVVVVVVVVVVV---!!!!---VVVVVVVVVVVVVVVVV

        // Set callback listener
        dialog.setOnSettingsConfirmedListener(new OscSettingsDialog.OnSettingsConfirmedListener() {
            @Override
            public void onSettingsConfirmed(int num, String waveform, float frequency, float maxValue,
                                            float minValue, float phaseShift) {
                // Handle the confirmed settings
                handleOscSettings(num, waveform, frequency, maxValue, minValue, phaseShift);
                refreshAllViews();
            }
        });

        dialog.show();
    }

    //--------------------------------------------------------
    // most of these space oscillators are used for slow moving purposes.
    // high frequencies will do undesiable things and make
    // garbage signals. Not all features are available for all oscillators.

    private void getLimits(int purpose) {
        this.purpose=purpose;

        if (purpose==1) {          // carrier phase...
            highLimit=.5;
            lowLimit=-.5;
            highfreq=2;
            lowfreq=.2;
            comment=new String("Varies carrier phase for triphase effects.");
        } else
        if (purpose==4) {
            highLimit=.5;
            lowLimit=-.5;
            highfreq=2;
            lowfreq=.2;
            comment=new String("Varies AMOD phase for alternating pulses.");
        }

        else if ((purpose==2)||(purpose==3)) {     // volumes...
            highLimit=1.;
            lowLimit=0.;
            highfreq=5;
            lowfreq=.1;
            comment=new String("Varies output volume indepedent of AMODs");
        }
        else if ((purpose==6)||(purpose==9)) {     // depths
            highLimit=1.;
            lowLimit=0.;
            highfreq=2;
            lowfreq=.1;
            comment=new String("Varies AMOD depth which will vary perceived strength (at high frequency) or harshness (at low frequency).");
        }
        else if ((purpose==7)||(purpose==10)||(purpose==13)) {     // duty
            highLimit=0.8;
            lowLimit=0.2;
            highfreq=2;
            lowfreq=.05;
            comment=new String("Varies AMOD duty directly effecting perceived signal strength and smoothness.");
        }
        else if ((purpose==5)||(purpose==8)||(purpose==11)||(purpose==12)) {     // freq
            highLimit=10;
            lowLimit=0.2;
            highfreq=1;
            lowfreq=.05;
            comment=new String("Varies AMOD frequncies affecting signal effects.");
        }
    }


    //----------------------------------------------------------------------
    // STEP 2b: sets up an oscillator with the results of the dialog

    private void handleOscSettings(int oscillator, String waveform, float frequency, float maxValue, float minValue, float phaseShift) {

        String settingsDescription=new String(waveform+" "+frequency+"Hz\n ["+maxValue+" to "+minValue+"]\n+phase: "+phaseShift);


        //    private String[] waveformOptions = {"Sine", "Square", "Saw", "Tri"};

        int form=SINE;
        if (waveform.equals("Square")) {
            form = SQUARE;
        }
        else if (waveform.equals("Saw")) {
            form = SAW;
        }
        else if (waveform.equals("Tri")) {
            form = TRI;
        }

        if(oscillator==1) {
            settingsA.setText(settingsDescription);
            synth.oscA.setForm(form);
            synth.oscA.setFreq(frequency);
            synth.oscA.setPhase(phaseShift);
            synth.oscA.setRange(maxValue,minValue);
            synth.oscA.setDestination(purpose);            // destination is location in array
            activeA.setEnabled(true);                      // enable checkbox
            synth.oscA.setActive(true);
        }
        else if (oscillator==2) {
            settingsB.setText(settingsDescription);
            synth.oscB.setForm(form);
            synth.oscB.setFreq(frequency);
            synth.oscB.setPhase(phaseShift);
            synth.oscB.setRange(maxValue,minValue);
            synth.oscB.setDestination(purpose);     // destination is location in array
            activeB.setEnabled(true);
            synth.oscB.setActive(true);
        }
        else if (oscillator==3) {
            settingsC.setText(settingsDescription);
            synth.oscC.setForm(form);
            synth.oscC.setFreq(frequency);
            synth.oscC.setPhase(phaseShift);
            synth.oscC.setRange(maxValue,minValue);
            synth.oscC.setDestination(purpose);     // destination is location in array
            activeC.setEnabled(true);
            synth.oscC.setActive(true);
        }
        else {
            Toast toast = Toast.makeText(getActivity(), "Error - oscillator unknown.", Toast.LENGTH_SHORT);
            toast.show();
        }
        synth.outMod.doRampin(.5);
    }


    //=======================================================================
    // STEP 3: activate the destination by checking the check box
    //

    private void handleCheckedChange(Spinner spinner, boolean isChecked) {
        int purpose = spinner.getSelectedItemPosition();

        // make the link to the destination active!
        if (isChecked) {
            C.destinationFlags[purpose] = true;

            // set input flag on destination
        }
        // or shut that down...
        else {
            C.destinationFlags[purpose] = false;
            // clear input flag on destination
        }
    }


    //======================================================================
    // after dialog close we really need to refresh everything

    private void refreshAllViews() {
        View rootView = getView();
        if (rootView != null && rootView instanceof ViewGroup) {
            updateViewHierarchy((ViewGroup) rootView);
        }
    }

    private void updateViewHierarchy(ViewGroup viewGroup) {
        for (int i = 0; i < viewGroup.getChildCount(); i++) {
            View child = viewGroup.getChildAt(i);
            child.invalidate();
            if (child instanceof ViewGroup) {
                updateViewHierarchy((ViewGroup) child);
            }
        }
    }

}


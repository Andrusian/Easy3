package com.andrus.easy3;
// FirstLayoutFragment.java

import static android.content.ContentValues.TAG;
import static com.andrus.easy3.C.presets;
import static com.andrus.easy3.C.sequencer;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.card.MaterialCardView;

import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.Guideline;
import androidx.fragment.app.Fragment;

public class SequencerLayoutFragment extends Fragment {

    private ImageView scroller;                 //added
    private int position;                       //addded
    // Arrays to store references to the created components
    Button[] buttons = new Button[24]; // Assuming 5 button-textview pairs
    TextView[] textViews = new TextView[24];
    private Context mContext;
    ConstraintLayout constraintLayout;
    Button stopButton;
    Button runButton;
    Button clearButton;
    View previousView;
    Guideline guideline14;
    private LinearLayout stepsContainer;
    private ScrollView seqScroll;

    // ADDED: Factory method to create a new instance with position
    public static SequencerLayoutFragment newInstance(int position) {
        SequencerLayoutFragment fragment = new SequencerLayoutFragment();
        C.sequencerFragment=fragment;
        Bundle args = new Bundle();
        args.putInt("position", position);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;
    }

    // ADDED...

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            position = getArguments().getInt("position", 4);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.sequencer_layout, container, false);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        constraintLayout = view.findViewById(R.id.layout4);
        sequencer.clearSteps();

        Guideline guideline14 = view.findViewById(R.id.guideline14);
        View titleView = view.findViewById(R.id.title4);


        runButton = view.findViewById(R.id.seqRun);
        runButton.setOnClickListener(v ->{
            runButton.setBackgroundColor(Color.parseColor("#009900"));
            stopButton.setBackgroundColor(Color.parseColor("#cccccc"));
            runButton.invalidate();
            stopButton.invalidate();
            if (sequencer.stepCount>0) {
                sequencer.active = true;
                refreshStepsLayout();
            }
        });
        stopButton = view.findViewById(R.id.seqStop);
        stopButton.setOnClickListener(v ->{
            sequencer.active = false;
            runButton.setBackgroundColor(Color.parseColor("#cccccc"));
            stopButton.setBackgroundColor(Color.parseColor("#990000"));
            refreshStepsLayout();
            runButton.invalidate();
            stopButton.invalidate();
        });
        clearButton = view.findViewById(R.id.seqClear);
        clearButton.setOnClickListener(v ->{
            sequencer.active = false;
            sequencer.stepCount=0;
            sequencer.clearSteps();
            stopButton.setBackgroundColor(Color.parseColor("#cccccc"));
            runButton.setBackgroundColor(Color.parseColor("#cccccc"));
            runButton.invalidate();
            stopButton.invalidate();
            refreshStepsLayout();
        });

        // Find the ScrollView
        seqScroll = view.findViewById(R.id.seqScroll);

        // Find the container for steps (should be inside the ScrollView in your layout XML)
        stepsContainer = view.findViewById(R.id.stepsContainer);


        scroller = view.findViewById(R.id.scroller4);   // ADDED

        if (scroller == null) {
            Log.e(TAG, "scroller4 ImageView not found in layout!");
        } else {
            Log.d(TAG, "Found scroller4: " + scroller.getWidth() + "x" + scroller.getHeight());

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
        refreshStepsLayout();
    }

    /**
     * Refreshes and redraws the entire steps layout
     * Call this method when steps are added, removed, or modified
     */
    public void refreshStepsLayout() {
        if (stepsContainer == null || sequencer == null) {
            return;
        }

        // Clear existing views
        stepsContainer.removeAllViews();

        // Add step views
        for (int i = 0; i < sequencer.steps.size(); i++) {
            Sequencer.Step step = sequencer.steps.get(i);
            View stepView = createStepView(i, step);
            stepsContainer.addView(stepView);
        }

        // Scroll to current step if active
        if (sequencer.active && sequencer.steps.size() > 0) {
            final int currentStepPosition = sequencer.currentStep;
            seqScroll.post(new Runnable() {
                @Override
                public void run() {
                    // If we have many steps, make sure the current one is visible
                    if (currentStepPosition < sequencer.steps.size() &&
                            stepsContainer.getChildAt(currentStepPosition) != null) {
                        seqScroll.smoothScrollTo(0, stepsContainer.getChildAt(currentStepPosition).getTop());
                    }
                }
            });
        }
    }

    /**
     * Creates a view for a single step
     */
    private View createStepView(final int position, final Sequencer.Step step) {
        // Create a horizontal layout for the step
        LinearLayout stepLayout = new LinearLayout(getContext());
        stepLayout.setOrientation(LinearLayout.HORIZONTAL);
        stepLayout.setPadding(16, 16, 16, 16);

        // Parameters for the step layout
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        layoutParams.setMargins(0, 8, 0, 8);
        stepLayout.setLayoutParams(layoutParams);

        // Wrap in a MaterialCardView for the green box highlight
        MaterialCardView cardView = new MaterialCardView(getContext());
        cardView.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));
        cardView.setRadius(8);
        cardView.setContentPadding(8, 8, 8, 8);

        // Highlight current step with green border
        if (position == sequencer.currentStep) {
            cardView.setCardBackgroundColor(Color.parseColor("#E8F577")); // Light green background
            cardView.setCardElevation(8); // Add elevation instead of stroke
            cardView.setContentPadding(12, 12, 12, 12); // Slightly larger padding for active item
        } else {
            cardView.setCardBackgroundColor(Color.parseColor("#999999"));
            cardView.setCardElevation(2); // Lower elevation for inactive items
        }

        PresetItem presetItem=presets.getInfo(step.preset);

        // Step number button
        Button stepNumberBtn = new Button(getContext());
        stepNumberBtn.setText(String.valueOf(position + 1)); // 1-based for display
        stepNumberBtn.setBackgroundColor(Color.parseColor(presetItem.color));
        stepNumberBtn.setTextColor(Color.parseColor("#333333"));

        stepNumberBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Empty listener to be implemented later
                sequencer.gotoStep(position+1);
                refreshStepsLayout();
                Log.d(TAG, "Step button clicked: " + (position + 1));
            }
        });

        // Preset name text
        TextView presetText = new TextView(getContext());
        presetText.setTextColor(Color.parseColor("#333333"));
          presetText.setText(presetItem.name);
        presetText.setPadding(16, 0, 16, 0);

        // Length spinner
        Spinner lengthSpinner = new Spinner(getContext());
        lengthSpinner.setBackgroundColor((Color.parseColor("#333377")));
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(
                getContext(),
                android.R.layout.simple_spinner_item,
                new String[]{"10s", "30s", "60s", "120s", "240s"}
        );
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        lengthSpinner.setAdapter(spinnerAdapter);

        // Set the spinner to the current value
        int spinnerPosition = getSpinnerPositionForLength(step.length);
        lengthSpinner.setSelection(spinnerPosition);

        // Set the spinner listener
        lengthSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int spinnerPosition, long id) {
                String selectedLength = parent.getItemAtPosition(spinnerPosition).toString();
                double newLength = parseLength(selectedLength);

                // Update the model if the value changed
                if (step.length != newLength) {
                    step.length = newLength;
                    Log.d(TAG, "Step " + (position + 1) + " length changed to: " + newLength + "s");
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Do nothing
            }
        });

        // Add views to the layout with proper weights
        LinearLayout.LayoutParams buttonParams = new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1);
        stepLayout.addView(stepNumberBtn, buttonParams);

        LinearLayout.LayoutParams textParams = new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 2);
        stepLayout.addView(presetText, textParams);

        LinearLayout.LayoutParams spinnerParams = new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 2);
        stepLayout.addView(lengthSpinner, spinnerParams);

        // Add the layout to the card
        cardView.addView(stepLayout);

        return cardView;
    }

    /**
     * Converts length in seconds to spinner position
     */
    private int getSpinnerPositionForLength(double length) {
        if (length <= 10) return 0;
        else if (length <= 30) return 1;
        else if (length <= 60) return 2;
        else if (length <= 120) return 3;
        else return 4;
    }

    /**
     * Parses length string like "10s" into seconds
     */
    private double parseLength(String lengthStr) {
        return Double.parseDouble(lengthStr.replace("s", ""));
    }




    private void buttonCallback(Context context, TextView textView, Button button, int i) {


        // ---------------------------------------> SAVE



        // ---------------------------------------> LOAD

    }





}

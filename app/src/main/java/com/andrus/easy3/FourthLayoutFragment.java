package com.andrus.easy3;
// FirstLayoutFragment.java

import static android.content.ContentValues.TAG;

import static com.andrus.easy3.C.presets;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.Guideline;
import androidx.fragment.app.Fragment;

public class FourthLayoutFragment extends Fragment implements CustomInputDialog.DialogListener, LoadDialog.OnLoadListener {

    private ImageView scroller;                 //added
    private int position;                       //addded
    // Arrays to store references to the created components
    Button[] buttons = new Button[24]; // Assuming 5 button-textview pairs
    TextView[] textViews = new TextView[24];
    private Context mContext;
    PresetItem presetList[] = new PresetItem[24];
    ConstraintLayout constraintLayout;

    // ADDED: Factory method to create a new instance with position
    public static FourthLayoutFragment newInstance(int position) {
        FourthLayoutFragment fragment = new FourthLayoutFragment();
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
        View view = inflater.inflate(R.layout.fragment_fourth_layout, container, false);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        constraintLayout = view.findViewById(R.id.layout4);

        Guideline guideline = view.findViewById(R.id.guideline14);
        View titleView = view.findViewById(R.id.title4);
        View previousView = titleView;

        scroller = view.findViewById(R.id.scroller4);   // ADDED

        loadPresets();

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

        // create first column <6. The premade presets.

        // Create a container LinearLayout to hold all 6 button-text pairs
        LinearLayout containerLayout = new LinearLayout(getContext());
        containerLayout.setId(View.generateViewId());
        containerLayout.setOrientation(LinearLayout.VERTICAL);

// Add border to the container
        GradientDrawable border = new GradientDrawable();
        border.setShape(GradientDrawable.RECTANGLE);
        border.setStroke(2 * (int) getResources().getDisplayMetrics().density, Color.WHITE); // 2dp border
        border.setCornerRadius(8 * (int) getResources().getDisplayMetrics().density); // 8dp corners
        containerLayout.setBackground(border);

// Add some padding to the container
        int padding = 12 * (int) getResources().getDisplayMetrics().density;
        containerLayout.setPadding(padding, padding, padding, padding);

        for (int i = 0; i < 6; i++) {
            // Create a horizontal LinearLayout for each button-text pair
            LinearLayout rowLayout = new LinearLayout(getContext());
            rowLayout.setOrientation(LinearLayout.HORIZONTAL);
            rowLayout.setGravity(Gravity.CENTER_VERTICAL);

            // Add margin between rows (except first row)
            LinearLayout.LayoutParams rowParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            if (i > 0) {
                rowParams.topMargin = 10 * (int) getResources().getDisplayMetrics().density;
            }
            rowLayout.setLayoutParams(rowParams);

            // Create Button
            Button button = new Button(getContext());
            button.setId(View.generateViewId());
            button.setText(String.valueOf(i + 1));
            button.setPadding(10, 0, 10, 0);
            button.setTag(Integer.valueOf(i));

            // Create LinearLayout params for button
            LinearLayout.LayoutParams buttonParams = new LinearLayout.LayoutParams(
                    70 * (int) getResources().getDisplayMetrics().density,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            buttonParams.rightMargin = 12 * (int) getResources().getDisplayMetrics().density; // Space between button and text
            button.setLayoutParams(buttonParams);

            // Create TextView
            TextView textView = new TextView(getContext());
            textView.setId(View.generateViewId());
            textView.setText(presetList[i].name);
            textView.setTextColor(Color.parseColor(presetList[i].color));
            textView.setMaxLines(2);

            // Create LinearLayout params for textview
            LinearLayout.LayoutParams textParams = new LinearLayout.LayoutParams(
                    0,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    1.0f // This makes it take remaining space
            );
            textView.setLayoutParams(textParams);

            // Add button and textview to the row
            rowLayout.addView(button);
            rowLayout.addView(textView);

            // Add the row to the container
            containerLayout.addView(rowLayout);

            // Store references
            buttons[i] = button;
            textViews[i] = textView;

            buttonCallback(getActivity(), textView, button, i);
        }

// Create ConstraintLayout params for the container
        ConstraintLayout.LayoutParams containerParams = new ConstraintLayout.LayoutParams(
                0, // width will be constrained
                ConstraintLayout.LayoutParams.WRAP_CONTENT
        );

// Set constraints for container
        containerParams.topToBottom = previousView.getId();
        containerParams.topMargin = 10 * (int) getResources().getDisplayMetrics().density;
        containerParams.startToStart = ConstraintLayout.LayoutParams.PARENT_ID;
        containerParams.endToStart = guideline.getId();

// Set margins using setMargins method
        containerParams.setMargins(
                15 * (int) getResources().getDisplayMetrics().density, // left
                10 * (int) getResources().getDisplayMetrics().density, // top
                0, // right
                0  // bottom
        );

        containerLayout.setLayoutParams(containerParams);

// Add only the container to the parent ConstraintLayout
        constraintLayout.addView(containerLayout);

        previousView=containerLayout;

        // create first column, part 2

        for (int i = 6; i < 12; i++) {

            // Create Button
            Button button = new Button(getContext());
            button.setId(View.generateViewId());
            button.setText(String.valueOf(i + 1));
            button.setWidth((int) (30));
            button.setPadding(10, 0, 10, 0);
            button.setTag(Integer.valueOf(i));

            // Create ConstraintLayout params for button
            ConstraintLayout.LayoutParams buttonParams = new ConstraintLayout.LayoutParams(
                    70 * (int) getResources().getDisplayMetrics().density,
                    ConstraintLayout.LayoutParams.WRAP_CONTENT
            );

            // Set constraints for button
            buttonParams.topToBottom = previousView.getId();
            buttonParams.topMargin = 10 * (int) getResources().getDisplayMetrics().density;
            buttonParams.leftToLeft = ConstraintLayout.LayoutParams.PARENT_ID;
            buttonParams.leftMargin = 19 * (int) getResources().getDisplayMetrics().density;

            button.setLayoutParams(buttonParams);

            // Create TextView
            TextView textView;
            textView = new TextView(getContext());
            textView.setId(View.generateViewId());
            textView.setText(presetList[i].name);
            textView.setTextColor(Color.parseColor(presetList[i].color));
            textView.setMaxLines(2);

            // Create ConstraintLayout params for textview
            ConstraintLayout.LayoutParams textParams = new ConstraintLayout.LayoutParams(
                    0,
                    ConstraintLayout.LayoutParams.WRAP_CONTENT
            );

            // Set constraints for textview
            textParams.topToTop = button.getId();
            textParams.bottomToBottom = button.getId();
            textParams.startToEnd = button.getId();
            textParams.endToStart = guideline.getId();

            textView.setLayoutParams(textParams);

            // Add views to parent
            constraintLayout.addView(button);
            constraintLayout.addView(textView);

            // Store references
            buttons[i] = button;
            textViews[i] = textView;

            buttonCallback(getActivity(), textView, button, i);

            // Update the previous view for next iteration
            previousView = button;
        }


        // create first column  -------------------------------------

        previousView = titleView;

        for (int i = 12; i < 24; i++) {
            // Create Button
            Button button = new Button(getContext());
            button.setId(View.generateViewId());
            button.setText(String.valueOf(i + 1));
            button.setWidth((int) (50 * getResources().getDisplayMetrics().density));
            button.setPadding(10, 0, 10, 0);
            button.setTag(Integer.valueOf(i));

            // Create ConstraintLayout params for button
            ConstraintLayout.LayoutParams buttonParams = new ConstraintLayout.LayoutParams(
                    70 * (int)getResources().getDisplayMetrics().density,
                    ConstraintLayout.LayoutParams.WRAP_CONTENT
            );

            // Set constraints for button
            buttonParams.topToBottom = previousView.getId();
            buttonParams.topMargin = 10 * (int)getResources().getDisplayMetrics().density;
            buttonParams.startToStart = guideline.getId();
            buttonParams.leftMargin = 19 * (int)getResources().getDisplayMetrics().density;

            button.setLayoutParams(buttonParams);


            // Create TextView
            TextView textView = new TextView(getContext());
            textView.setId(View.generateViewId());
            textView.setText(presetList[i].name);
            textView.setEnabled(false);
            textView.setMaxLines(2);
            textView.setTextColor(Color.parseColor(presetList[i].color));

            // Create ConstraintLayout params for textview
            ConstraintLayout.LayoutParams textParams = new ConstraintLayout.LayoutParams(
                    0,
                    ConstraintLayout.LayoutParams.WRAP_CONTENT
            );

            // Set constraints for textview
            textParams.topToTop = button.getId();
            textParams.bottomToBottom = button.getId();
            textParams.startToEnd = button.getId();
            textParams.rightToRight = ConstraintLayout.LayoutParams.PARENT_ID;

            textView.setLayoutParams(textParams);

            // Add views to parent
            constraintLayout.addView(button);
            constraintLayout.addView(textView);

            // Store references
            buttons[i] = button;
            textViews[i] = textView;

            buttonCallback(getActivity(),textView,button,i);

            // Update the previous view for next iteration
            previousView = button;
        }

    }

    private void loadPresets () {
        for (int i=0;i<24;i++) {
            presetList[i]=presets.getInfo(i+1);
        }
    }

    public void updatePresets() {
        Activity activity = getActivity(); // For Fragments
        // OR
        // Context context = textViews[0].getContext(); // Another option

        if (activity != null) {
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    for (int i=0; i<24; i++) {
                        textViews[i].setText(presetList[i].name);
                        textViews[i].setTextColor(Color.parseColor(presetList[i].color));
                        textViews[i].invalidate();
                        buttonCallback(getActivity(),textViews[i],buttons[i],i);
                    }
                    constraintLayout.forceLayout();
                 }
            });
        }
    }

    private void buttonCallback(Context context, TextView textView, Button button, int i) {


        // ---------------------------------------> SAVE

        if (textView.getText().equals("Empty")) {
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String color="#0000ff";
                    int num = (Integer) v.getTag();

                    CustomInputDialog dialog = new CustomInputDialog();
                    dialog.setNumber(num);
                    dialog.setDialogListener(FourthLayoutFragment.this);
                    dialog.show(getParentFragmentManager(), "CustomInputDialog");

                }
            });
        }

        // ---------------------------------------> LOAD

        else {
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int num = (Integer) v.getTag();

                    LoadDialog dialog = new LoadDialog(context, (num+1),
                            presetList[num].name,
                            presetList[num].desc,
                            presetList[num].color);
                    dialog.setOnLoadListener(FourthLayoutFragment.this);
                    dialog.show();
                }
            });
        }
    }

    @Override
    public void onDialogPositiveClick(String title, String description, String colorName, int colorValue, int num) {
        presets.save((num+1),title,description, colorValue);
        loadPresets();
        updatePresets();
    }

    @Override
    public void onDialogNegativeClick() {

    }

    @Override
    public void onLoadClicked(int presetNumber) {
        loadPresets();
        updatePresets();
    }
}

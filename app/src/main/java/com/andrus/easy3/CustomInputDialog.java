package com.andrus.easy3;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

public class CustomInputDialog extends DialogFragment {

    public interface DialogListener {
        void onDialogPositiveClick(String title, String description, String colorName, int colorValue, int presetNumber);
        void onDialogNegativeClick();
    }

    private DialogListener listener;
    private EditText titleEditText;
    private EditText descriptionEditText;
    private Spinner colorSpinner;
    private TextView titleCounter;
    private TextView descriptionCounter;
    private int presetNumber;

    public void setNumber(int num) {
        presetNumber=num;
    }

    // Color options for the dropdown
    private final String[] colorNames = new String[] {
            "Mild",
            "Vibratory",
            "Other",
            "Percussive",
            "Spicy",
            "Finisher",
            "Hot",
            "Pain"
    };

    private final int[] colorValues = new int[] {

            Color.parseColor("#33ff33"),
            Color.parseColor("#0000ff"),
            Color.parseColor("#FFFF00"),
            Color.parseColor("#ff66ff"),
            Color.parseColor("#FFA500"),
            Color.parseColor("#00a0a0"),
            Color.parseColor("#FFC0CB"),
            Color.parseColor("#ff0000")
    };

    private int selectedColorPosition = 0;

    // Add setter method for the listener
    public void setDialogListener(DialogListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        if (getActivity() == null) {
            throw new IllegalStateException("Activity cannot be null");
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_custom_input, null);

        titleEditText = view.findViewById(R.id.editTextTitle);
        // descriptionEditText = view.findViewById(R.id.editTextDescription);
        colorSpinner = view.findViewById(R.id.colorSpinner);
        titleCounter = view.findViewById(R.id.titleCounter);
        //descriptionCounter = view.findViewById(R.id.descriptionCounter);

        // Set character limits
        titleEditText.setFilters(new InputFilter[] { new InputFilter.LengthFilter(20) });
        // descriptionEditText.setFilters(new InputFilter[] { new InputFilter.LengthFilter(80) });

        // Set up character counters
        titleEditText.addTextChangedListener(createTextWatcher(titleCounter, 20));
        //descriptionEditText.addTextChangedListener(createTextWatcher(descriptionCounter, 80));

        // Set up color spinner
        ColorSpinnerAdapter adapter = new ColorSpinnerAdapter(requireContext(), colorNames, colorValues);
        colorSpinner.setAdapter(adapter);

        colorSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedColorPosition = position;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Do nothing
            }
        });

        builder.setView(view)
                .setTitle("Save as Preset #"+presetNumber)
                .setPositiveButton("SAVE", null) // Set up later to prevent automatic dismissal
                .setNegativeButton("CANCEL", (dialog, which) -> {
                    if (listener != null) {
                        listener.onDialogNegativeClick();
                    }
                });

        AlertDialog dialog = builder.create();

        // Override the positive button click listener to validate before dismissing
        dialog.setOnShowListener(dialogInterface -> {
            AlertDialog alertDialog = (AlertDialog) dialogInterface;
            alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
                String title = titleEditText.getText().toString().trim();
                // String description = descriptionEditText.getText().toString().trim();

                if (title.isEmpty()) {
                    titleEditText.setError("Title cannot be empty");
                } else {
                    if (listener != null) {
                        listener.onDialogPositiveClick(
                                title,
                                "no description",
                                colorNames[selectedColorPosition],
                                colorValues[selectedColorPosition],
                                presetNumber

                        );
                    }
                    dialog.dismiss();
                }
            });
        });

        return dialog;
    }

    private TextWatcher createTextWatcher(final TextView counterTextView, final int maxLength) {
        return new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // Not needed
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                counterTextView.setText(s.length() + "/" + maxLength);
            }

            @Override
            public void afterTextChanged(Editable s) {
                // Not needed
            }
        };
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        // Only try to find the listener if it hasn't been set manually
        if (listener == null) {
            try {
                // First try to cast the parent fragment
                Fragment parentFragment = getParentFragment();
                if (parentFragment instanceof DialogListener) {
                    listener = (DialogListener) parentFragment;
                } else {
                    // If parent fragment doesn't implement the interface, try with the activity
                    if (context instanceof DialogListener) {
                        listener = (DialogListener) context;
                    }
                    // Don't throw an exception if no listener is found - it might be set later
                }
            } catch (ClassCastException e) {
                // Don't crash, just log the issue
                Log.e("CustomInputDialog", "Could not cast parent to DialogListener", e);
            }
        }
    }

    public static class ColorSpinnerAdapter extends ArrayAdapter<String> {
        private final String[] colorNames;
        private final int[] colorValues;

        public ColorSpinnerAdapter(Context context, String[] colorNames, int[] colorValues) {
            super(context, android.R.layout.simple_spinner_item, colorNames);
            this.colorNames = colorNames;
            this.colorValues = colorValues;
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            View view = super.getView(position, convertView, parent);
            TextView textView = view.findViewById(android.R.id.text1);
            textView.setText(colorNames[position]);
            return view;
        }

        @Override
        public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            View view = super.getDropDownView(position, convertView, parent);
            TextView textView = view.findViewById(android.R.id.text1);
            textView.setText(colorNames[position]);
            textView.setBackgroundColor(colorValues[position]);

            // Set text color to white or black depending on background brightness
            int color = colorValues[position];
            int brightness = (Color.red(color) * 299 + Color.green(color) * 587 + Color.blue(color) * 114) / 1000;
            textView.setTextColor(brightness < 128 ? Color.WHITE : Color.BLACK);

            return view;
        }
    }
}


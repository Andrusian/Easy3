package com.andrus.easy3;

import static com.andrus.easy3.Oscillator.*;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.InputType;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import androidx.annotation.NonNull;

public class OscSettingsDialog extends Dialog {

    // UI Components
    private TextView titleText;
    private Spinner waveformSpinner;
    private EditText frequencyEdit;
    private EditText maxValueEdit;
    private EditText minValueEdit;
    private EditText phaseShiftEdit;
    private Button okButton;
    private Button cancelButton;
    private int oscNumber;
    String purposeComment;
    private TextView commentText;
    String comment;
    private TextView osfreq;
    private TextView osmax;
    private TextView osmin;


    // Data
    private String dialogTitle;
    private String[] waveformOptions = {"Sine", "Square", "Saw", "Tri"};
    private double frequencyLimitHigh = 100f; // Default limit
    private double frequencyLimitLow = .01f; // Default limit
    private double valueLimitHigh = 100.0f;    // Default limit
    private double valueLimitLow = -100.0f;   // Default limit

    public void setNumber(int oscNumber) {
        this.oscNumber=oscNumber;
    }

    //---------------------------------------------------------------------
    // set previous oscillator values in dialog if it has been
    // configured before

    @SuppressLint("DefaultLocale")
    public void setupDialogValues(Oscillator osc) {
        // only set previous values if this has been activated
        // previously

        if (osc.isActive()) {

            // set form spinner

            waveformSpinner = findViewById(R.id.spinner_waveform);

            if (osc.form == SINE) {
                waveformSpinner.setSelection(0);
            } else if (osc.form == SQUARE) {
                 waveformSpinner.setSelection(1);
            } else if (osc.form == SAW) {
                waveformSpinner.setSelection(2);
            } else if (osc.form == TRI) {
                waveformSpinner.setSelection(3);
            }

            // set frequency

            frequencyEdit = findViewById(R.id.edit_frequency);
            frequencyEdit.setText(String.format("%5.2f", osc.freq));

            // set max

            maxValueEdit = findViewById(R.id.edit_max_value);
            maxValueEdit.setText(String.format("%5.2f", osc.maxValue));

            // set min

            minValueEdit = findViewById(R.id.edit_min_value);
            minValueEdit.setText(String.format("%5.2f", osc.minValue));

            // set phase

            phaseShiftEdit = findViewById(R.id.edit_phase_shift);
            phaseShiftEdit.setText(String.format("0.1f", osc.ph));
        }
    }

    //-------------------------------------------------------------------------
    public void setFrequencyLimit(double highfreq, double lowfreq) {
        frequencyLimitHigh= (float) highfreq;
        frequencyLimitLow= (float) lowfreq;
    }

    //-------------------------------------------------------------------------
    public void setValueLimit(double high, double low) {
        valueLimitHigh=high;
        valueLimitLow=low;
    }

    //-------------------------------------------------------------------------
    public void setPurpose(String comment) {
        purposeComment=comment;
        this.comment="Driving: "+comment;
    }

    //-------------------------------------------------------------------------
    // Callback interface
    public interface OnSettingsConfirmedListener {
        void onSettingsConfirmed(int oscNumber, String waveform, float frequency, float maxValue,
                                 float minValue, float phaseShift);
    }

    private OnSettingsConfirmedListener listener;

    //-------------------------------------------------------------------------
    // Constructors
    public OscSettingsDialog(@NonNull Context context, String title) {
        super(context);
        this.dialogTitle = title;
    }

    public OscSettingsDialog(@NonNull Context context, String title,
                             OnSettingsConfirmedListener listener) {
        super(context);
        this.dialogTitle = title;
        this.listener = listener;
    }

    //-------------------------------------------------------------------------
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_osc_settings);

        initializeViews();
        setupSpinner();
        setupEditTextFilters();
        setupButtons();

        titleText.setText(dialogTitle);
    }

    //-------------------------------------------------------------------------
    private void initializeViews() {
        titleText = findViewById(R.id.dialog_title);
        waveformSpinner = findViewById(R.id.spinner_waveform);
        frequencyEdit = findViewById(R.id.edit_frequency);
        maxValueEdit = findViewById(R.id.edit_max_value);
        minValueEdit = findViewById(R.id.edit_min_value);
        phaseShiftEdit = findViewById(R.id.edit_phase_shift);
        okButton = findViewById(R.id.button_ok);
        cancelButton = findViewById(R.id.button_cancel);
        commentText=findViewById(R.id.commentText);
        commentText.setText(comment);
        osfreq=findViewById(R.id.osfreq);
        osmax=findViewById(R.id.osmax);
        osmin=findViewById(R.id.osmin);
        osfreq.setText("Freq: ("+String.format("%5.2f",frequencyLimitLow)+"Hz to "+String.format("%5.2f",frequencyLimitHigh)+"Hz)");
        osmax.setText("Max Value: ( < "+String.format("%5.2f",valueLimitHigh)+")");
        osmin.setText("Min Value: ( > "+String.format("%5.2f",valueLimitLow)+")");
        frequencyEdit.setText("1");
        maxValueEdit.setText(String.format("%5.2f",valueLimitHigh));
        minValueEdit.setText(String.format("%5.2f",valueLimitLow));
        phaseShiftEdit.setText("0");
    }

    //-------------------------------------------------------------------------
    private void setupSpinner() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                getContext(),
                android.R.layout.simple_spinner_item,
                waveformOptions
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        waveformSpinner.setAdapter(adapter);
    }

    //-------------------------------------------------------------------------
    private void setupEditTextFilters() {
        // Set input types for numeric fields
        frequencyEdit.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        maxValueEdit.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL | InputType.TYPE_NUMBER_FLAG_SIGNED);
        minValueEdit.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL | InputType.TYPE_NUMBER_FLAG_SIGNED);
        phaseShiftEdit.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL | InputType.TYPE_NUMBER_FLAG_SIGNED);

        // Custom input filter for phase shift (-0.5 to +0.5)
        phaseShiftEdit.setFilters(new InputFilter[]{new PhaseShiftInputFilter()});
    }

    //-------------------------------------------------------------------------
    private void setupButtons() {
        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (validateInputs()) {
                    if (listener != null) {
                        String waveform = waveformSpinner.getSelectedItem().toString();
                        float frequency = Float.parseFloat(frequencyEdit.getText().toString());
                        float maxValue = Float.parseFloat(maxValueEdit.getText().toString());
                        float minValue = Float.parseFloat(minValueEdit.getText().toString());
                        float phaseShift = Float.parseFloat(phaseShiftEdit.getText().toString());

                        listener.onSettingsConfirmed(oscNumber,waveform, frequency, maxValue, minValue, phaseShift);
                    }
                    dismiss();
                }
            }
        });

        //--------------------------------------------------------------
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
    }

    //--------------------------------------------------------------------
    private boolean validateInputs() {
        try {
            // Validate frequency
            String freqText = frequencyEdit.getText().toString();
            if (freqText.isEmpty()) {
                frequencyEdit.setError("Frequency is required");
                return false;
            }
            float frequency = Float.parseFloat(freqText);
            if (frequency > frequencyLimitHigh) {
                frequencyEdit.setError("Frequency must be between 0 and " + frequencyLimitHigh);
                return false;
            }
            if (frequency < frequencyLimitLow) {
                frequencyEdit.setError("Frequency must be between 0 and " + frequencyLimitLow);
                return false;
            }

            // Validate max value
            String maxText = maxValueEdit.getText().toString();
            if (maxText.isEmpty()) {
                maxValueEdit.setError("Max value is required");
                return false;
            }
            float maxValue = Float.parseFloat(maxText);
            if (maxValue > valueLimitHigh) {
                maxValueEdit.setError("Max value cannot exceed " + valueLimitHigh);
                return false;
            }

            // Validate min value
            String minText = minValueEdit.getText().toString();
            if (minText.isEmpty()) {
                minValueEdit.setError("Min value is required");
                return false;
            }
            float minValue = Float.parseFloat(minText);
            if (minValue < valueLimitLow) {
                minValueEdit.setError("Min value cannot be less than " + valueLimitLow);
                return false;
            }

            // Validate min < max
            if (minValue >= maxValue) {
                minValueEdit.setError("Min value must be less than max value");
                return false;
            }

            // Validate phase shift
            String phaseText = phaseShiftEdit.getText().toString();
            if (phaseText.isEmpty()) {
                phaseShiftEdit.setError("Phase shift is required");
                return false;
            }
            float phaseShift = Float.parseFloat(phaseText);
            if (phaseShift < -0.5f || phaseShift > 0.5f) {
                phaseShiftEdit.setError("Phase shift must be between -0.5 and +0.5");
                return false;
            }

            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    //----------------------------------------------------------
    // Methods to set current values
    public void setCurrentValues(String waveform, float frequency, float maxValue,
                                 float minValue, float phaseShift) {
        // Set waveform spinner
        for (int i = 0; i < waveformOptions.length; i++) {
            if (waveformOptions[i].equals(waveform)) {
                waveformSpinner.setSelection(i);
                break;
            }
        }

        // Set other values
        frequencyEdit.setText(String.valueOf(frequency));
        maxValueEdit.setText(String.valueOf(maxValue));
        minValueEdit.setText(String.valueOf(minValue));
        phaseShiftEdit.setText(String.valueOf(phaseShift));
    }

    //------------------------------------------------------------------------
    public void setOnSettingsConfirmedListener(OnSettingsConfirmedListener listener) {
        this.listener = listener;
    }

    //--------------------------------------------------------------------
    // Custom InputFilter for phase shift
    private static class PhaseShiftInputFilter implements InputFilter {
        @Override
        public CharSequence filter(CharSequence source, int start, int end,
                                   android.text.Spanned dest, int dstart, int dend) {
            try {
                String newVal = dest.toString().substring(0, dstart) +
                        source.toString().substring(start, end) +
                        dest.toString().substring(dend);

                if (newVal.equals("") || newVal.equals("-") || newVal.equals(".") || newVal.equals("-.")) {
                    return null; // Allow these intermediate states
                }

                float value = Float.parseFloat(newVal);
                if (value >= -0.5f && value <= 0.5f) {
                    return null; // Accept the input
                }
            } catch (NumberFormatException e) {
                // Invalid number format
            }
            return ""; // Reject the input
        }
    }
}

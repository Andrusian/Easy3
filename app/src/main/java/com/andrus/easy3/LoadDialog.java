package com.andrus.easy3;

import static com.andrus.easy3.C.presets;
import static com.andrus.easy3.C.sequencer;
import static com.andrus.easy3.C.sequencerFragment;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;

import java.io.File;

public class LoadDialog extends Dialog {

    public interface OnLoadListener {
        void onLoadClicked(int presetNumber);
    }

    private String title;
    private String description;
    private int num;
    private OnLoadListener loadListener;
    private String color;

    public LoadDialog(@NonNull Context context, int num, String title, String description, String color) {
        super(context);
        this.title = title;
        this.description = description;
        this.num=num;
        this.color=color;
    }

    public void setOnLoadListener(OnLoadListener listener) {
        this.loadListener = listener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.load_dialog);

        // Set title
        TextView titleTextView = findViewById(R.id.dialog_title);
        titleTextView.setText(title);

        // Set description
        TextView descriptionTextView = findViewById(R.id.dialog_description);
        descriptionTextView.setText(description);

        // Cancel button
        Button cancelButton = findViewById(R.id.button_cancel);
        cancelButton.setOnClickListener(v -> dismiss());

        // Delete button with confirmation
        Button deleteButton = findViewById(R.id.button_delete);
        if (num<6) {
            deleteButton.setEnabled(false);
        }
        else {
            deleteButton.setOnClickListener(v -> {
                new AlertDialog.Builder(getContext())
                        .setTitle("Confirm DELETE")
                        .setMessage("Are you sure you want to delete?!")
                        .setPositiveButton("Yes", (dialog, which) -> {

                            // Perform delete action
                            Toast.makeText(getContext(), "Item " + num + " Deleted", Toast.LENGTH_SHORT).show();
                            dismiss();
                            String filename = new String(getContext().getFilesDir() + "/preset" + num + ".ini");
                            Log.i("EASY3", "Would delete " + filename + " here.");

                            // Create a File object for the file to delete
                            File fileToDelete = new File(filename);
                            boolean deleted = fileToDelete.delete();


                            if (loadListener != null) {
                                loadListener.onLoadClicked(num);
                            }

                        })
                        .setNegativeButton("No", null)
                        .show();
            });
        }

        // Load button with confirmation
        Button loadButton = findViewById(R.id.button_load);
        loadButton.setOnClickListener(v -> {
            new AlertDialog.Builder(getContext())
                    .setTitle("Confirm Load")
                    .setMessage("Are you sure you want to load?")
                    .setPositiveButton("Yes", (dialog, which) -> {
                        // Perform load action
                        Toast.makeText(getContext(), "Loaded: "+num, Toast.LENGTH_SHORT).show();
                        dismiss();
                        presets.load(num);

                    })
                    .setNegativeButton("No", null)
                    .show();
        });

        // Sequence button with confirmation
        Button seqButton = findViewById(R.id.sequence_button);
        seqButton.setOnClickListener(v -> {
            dismiss();
            sequencer.addStep(60.,num,color);
            sequencerFragment.refreshStepsLayout();
        });
    }
}


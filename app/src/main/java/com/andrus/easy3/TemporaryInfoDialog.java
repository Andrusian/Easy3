package com.andrus.easy3;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Looper;
import android.app.AlertDialog;
import android.os.Handler;


public class TemporaryInfoDialog {

    /**
     * Shows a temporary informational dialog box that automatically dismisses after a specified duration
     *
     * @param context The context (typically your Activity)
     * @param title Dialog title
     * @param message Informational message to display
     * @param durationMs How long to show the dialog in milliseconds
     * @param onDismissListener Optional listener for when dialog is dismissed (can be null)
     */
    public static void show(Context context, String title, String message, long durationMs,
                            DialogInterface.OnDismissListener onDismissListener) {

        // Create the AlertDialog
        AlertDialog.Builder builder = new AlertDialog.Builder(context)
                .setTitle(title)
                .setMessage(message)
                .setCancelable(true);

        // Create and show the dialog
        final AlertDialog dialog = builder.create();
        if (onDismissListener != null) {
            dialog.setOnDismissListener(onDismissListener);
        }
        dialog.show();

        // Set up auto-dismiss after specified duration
        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                if (dialog.isShowing()) {
                    dialog.dismiss();
                }
            }
        }, durationMs);
    }

    // Example usage
    public static void showExample(Context context) {
        show(context,
                "Information",
                "Your action was completed successfully!",
                3000,  // 3 seconds
                null);
    }

    // Example with a dismiss listener
    public static void showExampleWithCallback(Context context) {
        show(context,
                "Process Complete",
                "Data has been saved successfully.",
                2500,  // 2.5 seconds
                new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        // Do something when dialog is dismissed
                        // For example, navigate to another screen
                    }
                });
    }
}


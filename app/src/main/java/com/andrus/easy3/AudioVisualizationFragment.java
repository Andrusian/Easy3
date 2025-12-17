package com.andrus.easy3;

import static com.andrus.easy3.C.padL1;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class AudioVisualizationFragment extends Fragment {
    private ImageView waveformView;
    private ImageView leftSpectrumView;
    private ImageView rightSpectrumView;

    private Bitmap waveformBitmap;
    private Bitmap leftSpectrumBitmap;
    private Bitmap rightSpectrumBitmap;

    private Handler updateHandler;
    private Runnable updateRunnable;

    private Paint leftPaint;
    private Paint rightPaint;
    private Paint bgPaint;
    private Paint gridPaint;
    private Paint textPaint;
    private Paint gridThickPaint;
    private Paint outlinePaint;

    // ADDED: Factory method to create a new instance with position
    public static AudioVisualizationFragment newInstance(int position) {
        AudioVisualizationFragment fragment = new AudioVisualizationFragment();
        Bundle args = new Bundle();
        args.putInt("position", position);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Setup paints
        leftPaint = new Paint();
        leftPaint.setColor(Color.parseColor("#FF00FF00"));
        leftPaint.setAntiAlias(true);

        rightPaint = new Paint();
        rightPaint.setColor(Color.parseColor("#FFFF0000"));
        rightPaint.setStrokeWidth(2);
        rightPaint.setAntiAlias(true);

        bgPaint = new Paint();
        bgPaint.setColor(Color.BLACK);
        bgPaint.setStyle(Paint.Style.FILL);


        gridPaint = new Paint();
        gridPaint.setColor(Color.WHITE);
        gridPaint.setStrokeWidth(1);
        gridPaint.setAntiAlias(true);

        gridThickPaint = new Paint();
        gridThickPaint.setColor(Color.WHITE);
        gridThickPaint.setStrokeWidth(2);
        gridThickPaint.setAntiAlias(true);

        outlinePaint = new Paint();
        outlinePaint.setColor(Color.CYAN);
        outlinePaint.setStrokeWidth(1);
        outlinePaint.setStyle(Paint.Style.STROKE);
        outlinePaint.setAntiAlias(true);

        textPaint = new Paint();
        textPaint.setColor(Color.CYAN);
        textPaint.setTextSize(24);
        textPaint.setAntiAlias(true);
        textPaint.setTextAlign(Paint.Align.CENTER);

        // Setup update handler (100ms intervals)
        updateHandler = new Handler(Looper.getMainLooper());
        updateRunnable = new Runnable() {
            @Override
            public void run() {
                updateVisualization();
                updateHandler.postDelayed(this, 100);
            }
        };
    }

    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {

        waveformView = view.findViewById(R.id.waveformID);
        leftSpectrumView = view.findViewById(R.id.leftSpectrumID);
        rightSpectrumView = view.findViewById(R.id.rightSpectrumID);
    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.frag_visualization, container, false);
    }

    @Override
    public void onResume() {
        super.onResume();
        updateHandler.post(updateRunnable);
    }

    @Override
    public void onPause() {
        super.onPause();
        updateHandler.removeCallbacks(updateRunnable);
    }

    private void updateVisualization() {
        if (!AudioVizData.dataReady) return;

        // Create bitmaps if needed
        if (waveformBitmap == null && waveformView.getWidth() > 0) {
            int w = waveformView.getWidth();
            int h = waveformView.getHeight();
            if (w > 0 && h > 0) {
                waveformBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
                drawWaveform();
            }
        }

        if (leftSpectrumBitmap == null && leftSpectrumView.getWidth() > 0) {
            int w = leftSpectrumView.getWidth();
            int h = leftSpectrumView.getHeight();

            if (w > 0 && h > 0) {
                leftSpectrumBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
            }
            w = rightSpectrumView.getWidth();
            h = rightSpectrumView.getHeight();
            if (w > 0 && h > 0) {
                rightSpectrumBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
            }
            drawSpectrum();
        }

        // Update displays
        if (waveformBitmap != null) {
            drawWaveform();
            waveformView.setImageBitmap(waveformBitmap);
        }

        if ((leftSpectrumBitmap != null)&&(rightSpectrumBitmap!=null)) {
            drawSpectrum();
            leftSpectrumView.setImageBitmap(leftSpectrumBitmap);
            rightSpectrumView.setImageBitmap(rightSpectrumBitmap);
        }
    }

    //------------------------------------------

    private void drawWaveform() {
        Canvas canvas = new Canvas(waveformBitmap);

        // an additional zoom factor because the waveform
        // being smushed together isn't insightful. Just look at
        // first section.

        int zoomFactor=2;

        int w = waveformBitmap.getWidth();
        int h = waveformBitmap.getHeight();

        // Clear background
        canvas.drawRect(0, 0, w, h, bgPaint);

        float centerY = h / 2f;
        float scale = h / 2f *.8f;

        int samples = AudioVizData.waveformSamples/zoomFactor;
        if (samples < 2) return;

        float step = (float) w / samples * zoomFactor;

        // Draw right channel (behind)
        for (int i = 0; i < (samples/zoomFactor) - 1; i++) {
            float x1 = i * step;
            float y1 = centerY - AudioVizData.rightWaveform[i] * scale;
            float x2 = (i + 1) * step;
            float y2 = centerY - AudioVizData.rightWaveform[i + 1] * scale;
            canvas.drawLine(x1, y1, x2, y2, rightPaint);
        }

        // Draw left channel (in front)
        for (int i = 0; i < (samples/zoomFactor) - 1; i++) {
            float x1 = i * step;
            float y1 = centerY - AudioVizData.leftWaveform[i] * scale;
            float x2 = (i + 1) * step;
            float y2 = centerY - AudioVizData.leftWaveform[i + 1] * scale;
            canvas.drawLine(x1, y1, x2, y2, leftPaint);
        }
        // Draw description
        textPaint.setTextAlign(Paint.Align.LEFT);
        textPaint.setTextSize(50);
        canvas.drawText("Waveform", 10, 60, textPaint);
    }

    private void drawSpectrum() {
        drawSpectrumChannel(leftSpectrumBitmap, AudioVizData.leftSpectrum, leftPaint, "Left");
        drawSpectrumChannel(rightSpectrumBitmap, AudioVizData.rightSpectrum, rightPaint, "Right");
    }

    private void drawSpectrumChannel(Bitmap bitmap, float[] data, Paint paint, String label) {
        Canvas canvas = new Canvas(bitmap);
        int w = bitmap.getWidth();
        int h = bitmap.getHeight();

        // Clear background
        canvas.drawRect(0, 0, w, h, bgPaint);

        int numBars = data.length;
        float barWidth = (float) w / numBars;

        // Find max for normalization
        float max = 0.001f;
        for (float v : data) {
            if (v > max) max = v;
        }

        // Draw bars
        for (int i = 0; i < numBars; i++) {
            float barHeight = (data[i] / max) * h;
            float x = i * barWidth;
            float y = h - barHeight;
            canvas.drawRect(x, y, x + barWidth - 1, h, paint);
        }

        // Draw frequency grid lines (250Hz, 500Hz, 1000Hz, 2000Hz)
        final float MIN_FREQ = 125f;
        final float MAX_FREQ = 4000f;
        float[] frequencies = {250f, 500f, 1000f, 2000f};
        String[] labels = {"250", "500", "1k", "2k"};

        for (int i = 0; i < frequencies.length; i++) {
            // Calculate position on logarithmic scale
            float t = (float) (Math.log(frequencies[i] / MIN_FREQ) / Math.log(MAX_FREQ / MIN_FREQ));
            float x = t * w;
            canvas.drawLine(x, 0, x, h, gridPaint);

            // Draw label near top
            textPaint.setTextSize(32);
            canvas.drawText(labels[i], x, 50, textPaint);
        }

        // Draw channel label in upper left corner
        textPaint.setTextAlign(Paint.Align.LEFT);
        textPaint.setTextSize(50);
        canvas.drawText(label, 10, 60, textPaint);

        // Draw outline
        canvas.drawRect(0, 0, w, h, outlinePaint);
    }

}



package com.andrus.easy3;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.widget.SeekBar;

public class LabeledSeekBar extends androidx.appcompat.widget.AppCompatSeekBar {

        private String label = "";
        private Paint labelPaint;
        private Rect textBounds;

        public LabeledSeekBar(Context context) {
            super(context);
            init();
        }

        public LabeledSeekBar(Context context, AttributeSet attrs) {
            super(context, attrs);
            init();
        }

        public LabeledSeekBar(Context context, AttributeSet attrs, int defStyleAttr) {
            super(context, attrs, defStyleAttr);
            init();
        }

        private void init() {
            labelPaint = new Paint();
            labelPaint.setColor(0xFF999900); // #999900
            labelPaint.setTextSize(40);
            labelPaint.setAntiAlias(true);
            labelPaint.setTextAlign(Paint.Align.CENTER);

            textBounds = new Rect();
        }

        public void setLabel(String label) {
            this.label = label != null ? label : "";
            invalidate(); // Trigger a redraw
        }

        public String getLabel() {
            return label;
        }

        @Override
        protected synchronized void onDraw(Canvas canvas) {
            super.onDraw(canvas);

            if (!label.isEmpty()) {
                // Get the bounds of the text
                labelPaint.getTextBounds(label, 0, label.length(), textBounds);

                // Calculate the center position of the SeekBar
                int centerX = getWidth() / 2;
                int centerY = getHeight() / 2;

                // Adjust Y position to center the text vertically
                int textY = centerY + (textBounds.height() / 2);

                // Draw the label in the center
                canvas.drawText(label, centerX, textY, labelPaint);
            }
        }

}

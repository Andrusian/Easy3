package com.andrus.easy3;

import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Shader;
import android.os.Handler;
import android.os.Looper;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.LinearInterpolator;


public class AudioTouchPad extends View {
    // Configurable properties
    private String xAxisLabel;
    private String yAxisLabel;
    private int backgroundStartColor = Color.parseColor("#333333");
    private int backgroundEndColor = Color.parseColor("#222222");
    private int borderColor = Color.parseColor("#00ff00");


    // Current position values (as percentages 0-1)
    public float currentX = 0.5f;
    public float currentY = 0.5f;

    // Time-lagged values (target for animation)
    public float actualX = 0.5f;
    public float actualY = 0.5f;
    private String title=null;
    boolean disabled = false;          // flag for if manipulating this is (npt) meaningful
    private boolean override=false;  // flag for if something else is controlling this
    private OnPositionChangedListener onPositionChangedListener;

    // Paint objects
    private final Paint backgroundPaint = new Paint();
    private final Paint labelPaint = new Paint();
    private final Paint titlePaint = new Paint();
    private final Paint setpointPaint = new Paint();
    private final Paint actualPointPaint = new Paint();
    private final Paint borderPaint = new Paint();

    // Animation properties
    private final ValueAnimator valueAnimator;
    boolean lockX=false;
    boolean lockY=false;
    //----------------------------------------

    public void setTitle(String title) {
        this.title=title;
    }

    //---------------------------------------

    // Interface for position change callback
    public interface OnPositionChangedListener {
        void onPositionChanged(float xPercent, float yPercent);
    }

    // Constructors
    public AudioTouchPad(Context context) {
        this(context, null);
    }

    public AudioTouchPad(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AudioTouchPad(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();

        // Setup animator
        valueAnimator = ValueAnimator.ofFloat(0f, 1f);
        valueAnimator.setDuration(300); // Lag time in ms
        valueAnimator.setInterpolator(new LinearInterpolator());
        valueAnimator.setRepeatCount(ValueAnimator.INFINITE);
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float fraction = (float) animation.getAnimatedValue();
                // Animate actual point toward setpoint
                actualX += (currentX - actualX) * fraction * 0.1f;
                actualY += (currentY - actualY) * fraction * 0.1f;
                invalidate();
            }
        });
        valueAnimator.start();
    }

    public void lockX() {
        lockX=true;
    }
    public void lockY() {
        lockY=true;
    }

    //---------------------------------------------------==============
    // update position moves the current position
    // and the lagged value.

    public void updatePosition(float newX, float newY) {
        // Update the current position
        this.currentX = newX;
        this.currentY = newY;

        // You might need to validate the position is within bounds
        if (currentX < 0) currentX = 0;
        if (currentY < 0) currentY = 0;
        if (currentX > getWidth()) currentX = getWidth();
        if (currentY > getHeight()) currentY = getHeight();

        // Request a redraw to show the new position visually
        invalidate();

        if (onPositionChangedListener != null) {
            onPositionChangedListener.onPositionChanged(currentX, currentY);
        }
    }

    //-------------------------------

    public void gotoPosition(float newX, float newY, boolean update) {
        // Update the current position
        this.actualX = newX;
        this.actualY = newY;
        this.currentX = newX;
        this.currentY = newY;
        // Request a redraw to show the new position visually
        invalidate();

        if ((onPositionChangedListener != null)&&(update)) {
            onPositionChangedListener.onPositionChanged(currentX, currentY);
        }
    }

    public void setLag(long lagx) {
        this.valueAnimator.setDuration(lagx);
    }

    //---------------------------------

    private void init() {
        // Initialize paints
        labelPaint.setColor(Color.parseColor("#999900"));
        labelPaint.setTextSize(40f);
        labelPaint.setTextAlign(Paint.Align.CENTER);

        titlePaint.setColor(Color.parseColor("#777777"));
        titlePaint.setTextSize(100f);
        titlePaint.setTextAlign(Paint.Align.CENTER);

        setpointPaint.setColor(Color.parseColor("#00cccc"));
        setpointPaint.setStyle(Paint.Style.STROKE);

        actualPointPaint.setColor(Color.parseColor("#00cccc"));
        actualPointPaint.setStyle(Paint.Style.FILL);
    }

    // Add these fields to your class
    private Handler borderUpdateHandler = new Handler(Looper.getMainLooper());
    private Runnable borderUpdateRunnable = new Runnable() {
        @Override
        public void run() {
            // This will trigger onDraw to be called again
            invalidate();
            // Schedule the next update
            borderUpdateHandler.postDelayed(this, 500);
        }
    };
    private boolean isBorderUpdateActive = false;

    //-----------------------------------------------------------------

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        float width = getWidth();
        float height = getHeight();

        // Draw background gradient
        LinearGradient gradient = new LinearGradient(
                0f, 0f, width, height,
                backgroundStartColor, backgroundEndColor,
                Shader.TileMode.CLAMP
        );
        backgroundPaint.setShader(gradient);
        canvas.drawRect(0f, 0f, width, height, backgroundPaint);

        drawBorder(canvas, height, width);

        if (title != null) {
            canvas.drawText(title, width / 2, height / 2, titlePaint);
        }

        // Draw axis labels if provided
        if (xAxisLabel != null) {
            canvas.drawText(xAxisLabel, width / 2, height - 20f, labelPaint);
        }

        if (yAxisLabel != null) {
            canvas.save();
            canvas.rotate(-90f, 20f, height / 2-40);
            canvas.drawText(yAxisLabel, 20f, height / 2, labelPaint);
            canvas.restore();
        }

        // Draw current setpoint
        float setpointX = currentX * width;
        float setpointY = (1 - currentY) * height;  // Invert Y for standard coordinate system
        canvas.drawCircle(setpointX, setpointY, 20f, setpointPaint);

        // Draw actual point
        float actualPointX = actualX * width;
        float actualPointY = (1 - actualY) * height;
        canvas.drawCircle(actualPointX, actualPointY, 15f, actualPointPaint);
    }

    //------------------------------------------------------------------
    // Method to start the border updates
    public void startBorderUpdates() {
        if (!isBorderUpdateActive) {
            isBorderUpdateActive = true;
            borderUpdateHandler.post(borderUpdateRunnable);
        }
    }

    // Method to stop the border updates
    public void stopBorderUpdates() {
        if (isBorderUpdateActive) {
            isBorderUpdateActive = false;
            borderUpdateHandler.removeCallbacks(borderUpdateRunnable);
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        // Clean up when view is removed
        stopBorderUpdates();
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        // Start updates when view is attached
        startBorderUpdates();
    }

    //----------------------------------------------------------------------
    // border color changes depending on whether the touchpad is
    // overridden or enabled

    private void drawBorder(Canvas canvas, float height, float width) {

        String colorString;
        if (override) {
            colorString=new String("#66bbff");
        }
        else if (disabled) {
            colorString=new String("#aa6666");
        }
        else {
            colorString=new String("#cccccc");
        }

        borderPaint.setColor(Color.parseColor(colorString));
        canvas.drawRect(0, 0, 4, height, borderPaint);
        canvas.drawRect(0, height-4, width, height, borderPaint);
        canvas.drawRect(width-4, height, width, 0, borderPaint);
        canvas.drawRect(width, 0, 0, 4, borderPaint);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        getParent().requestDisallowInterceptTouchEvent(true);

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_MOVE:
                if (lockX) {
                    currentX=0.5f;
                }
                else {
                    currentX = Math.max(0f, Math.min(event.getX(), getWidth())) / getWidth();
                }
                if (lockY) {
                    currentY=.5f;
                }
                else {
                    currentY = 1 - (Math.max(0f, Math.min(event.getY(), getHeight())) / getHeight());
                }

                if (onPositionChangedListener != null) {
                    onPositionChangedListener.onPositionChanged(currentX, currentY);
                }

                invalidate();
                return true;
        }
        return super.onTouchEvent(event);
    }

    // Getters and setters
    public String getXAxisLabel() {
        return xAxisLabel;
    }

    public void setXAxisLabel(String xAxisLabel) {
        this.xAxisLabel = xAxisLabel;
        invalidate();
    }

    public String getYAxisLabel() {
        return yAxisLabel;
    }

    public void setYAxisLabel(String yAxisLabel) {
        this.yAxisLabel = yAxisLabel;
        invalidate();
    }

    public int getBackgroundStartColor() {
        return backgroundStartColor;
    }

    public void setBackgroundStartColor(int backgroundStartColor) {
        this.backgroundStartColor = backgroundStartColor;
        invalidate();
    }

    public int getBackgroundEndColor() {
        return backgroundEndColor;
    }

    public void setOverride(boolean override) {
        this.override = override;
    }

    public void setBackgroundEndColor(int backgroundEndColor) {
        this.backgroundEndColor = backgroundEndColor;
        invalidate();
    }

    public float getCurrentX() {
        return currentX;
    }

    public float getCurrentY() {
        return currentY;
    }

    public void setDisabled(boolean disabled) {
        this.disabled=disabled;
    }

    public void setOnPositionChangedListener(OnPositionChangedListener listener) {
        this.onPositionChangedListener = listener;
    }
}

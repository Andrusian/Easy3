package com.andrus.easy3;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;

public class AudioDialer extends View {

    // Colors
    private static final int BACKGROUND_COLOR = Color.parseColor("#333333");
    private static final int EDGE_COLOR = Color.parseColor("#cccccc");
    private static final int NEEDLE_COLOR = Color.parseColor("#00cccc");
    private static final int CENTER_COLOR = Color.parseColor("#00cccc");
    private static final int TEXT_COLOR = Color.parseColor("#999999");

    // Paint objects
    private Paint backgroundPaint;
    private Paint edgePaint;
    private Paint needlePaint;
    private Paint centerPaint;
    private Paint textPaint;

    // Dimensions
    private float centerX = 0f;
    private float centerY = 0f;
    private float radius = 0f;
    private float centerRadius;

    // Values
    private float targetAngle = 0f; // -90 to +90 degrees
    private float actualAngle = 0f; // -90 to +90 degrees
    private float targetValue = 0f; // -0.5 to +0.5
    private float actualValue = 0f; // -0.5 to +0.5

    // Animation
    private long lastUpdateTime = System.currentTimeMillis();
    private static final float MAX_ANGULAR_SPEED = 270f; // degrees per second (3x faster)

    // Title
    private String title = "";

    // Touch handling
    private boolean isDragging = false;

    // Animation runnable
    private Runnable animationRunnable;

    public AudioDialer(Context context) {
        super(context);
        init();
    }

    public AudioDialer(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public AudioDialer(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        centerRadius = dpToPx(20f);

        // Initialize paint objects
        backgroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        backgroundPaint.setColor(BACKGROUND_COLOR);
        backgroundPaint.setStyle(Paint.Style.FILL);

        edgePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        edgePaint.setColor(EDGE_COLOR);
        edgePaint.setStyle(Paint.Style.STROKE);
        edgePaint.setStrokeWidth(dpToPx(2f));

        needlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        needlePaint.setColor(NEEDLE_COLOR);
        needlePaint.setStyle(Paint.Style.STROKE);
        needlePaint.setStrokeWidth(dpToPx(3f));
        needlePaint.setStrokeCap(Paint.Cap.ROUND);

        centerPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        centerPaint.setColor(CENTER_COLOR);
        centerPaint.setStyle(Paint.Style.FILL);

        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setColor(TEXT_COLOR);
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setTextSize(dpToPx(14f));
        textPaint.setTypeface(Typeface.DEFAULT_BOLD);

        // Start animation loop
        startAnimation();
    }

    private float dpToPx(float dp) {
        return TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                dp,
                getContext().getResources().getDisplayMetrics()
        );
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        centerX = w / 2f;
        centerY = h/2f + dpToPx(50f); // Position for top semicircle
        radius = Math.min(w, h * 2) / 2f - dpToPx(20f); // Account for padding
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // Draw semi-circle background (top half)
        RectF rect = new RectF(centerX - radius, centerY - radius, centerX + radius, centerY + radius);
        canvas.drawArc(rect, 180f, 180f, true, backgroundPaint);

        // Draw semi-circle edge
        canvas.drawArc(rect, 180f, 180f, false, edgePaint);

        // Draw tick marks at -90, 0, and +90 degrees
        drawTickMarks(canvas);

        // Draw needle for actual position
        // Convert our angle system (-90° left, 0° top, +90° right) to canvas coordinates
        float needleAngle = actualAngle + 270f; // Convert to canvas coordinates (270° = top)
        float needleEndX = centerX + (radius - dpToPx(10f)) * (float) Math.cos(Math.toRadians(needleAngle));
        float needleEndY = centerY + (radius - dpToPx(10f)) * (float) Math.sin(Math.toRadians(needleAngle));
        canvas.drawLine(centerX, centerY, needleEndX, needleEndY, needlePaint);

        // Draw center circle
        canvas.drawCircle(centerX, centerY, centerRadius / 2f, centerPaint);

        // Draw title
        if (!title.isEmpty()) {
            float textY = centerY + dpToPx(30f); // Below the center for top semicircle
            canvas.drawText(title, centerX, textY, textPaint);
        }
    }

    private void drawTickMarks(Canvas canvas) {
        Paint tickPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        tickPaint.setColor(EDGE_COLOR);
        tickPaint.setStyle(Paint.Style.STROKE);
        tickPaint.setStrokeWidth(dpToPx(2f));

        // Tick marks at -90° (left), 0° (top), +90° (right) in our coordinate system
        // Convert to canvas coordinates: left=180°, top=270°, right=0°
        float[] tickAngles = {180f, 270f, 0f}; // left, top, right in canvas coordinates
        float tickLength = dpToPx(10f);

        for (float angle : tickAngles) {
            float startRadius = radius - tickLength;
            float endRadius = radius;

            float startX = centerX + startRadius * (float) Math.cos(Math.toRadians(angle));
            float startY = centerY + startRadius * (float) Math.sin(Math.toRadians(angle));
            float endX = centerX + endRadius * (float) Math.cos(Math.toRadians(angle));
            float endY = centerY + endRadius * (float) Math.sin(Math.toRadians(angle));

            canvas.drawLine(startX, startY, endX, endY, tickPaint);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                float distance = (float) Math.sqrt(Math.pow(event.getX() - centerX, 2) + Math.pow(event.getY() - centerY, 2));
                if (distance <= radius) {
                    isDragging = true;
                    updateTargetFromTouch(event.getX(), event.getY());
                    return true;
                }
                break;

            case MotionEvent.ACTION_MOVE:
                if (isDragging) {
                    updateTargetFromTouch(event.getX(), event.getY());
                    return true;
                }
                break;

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                isDragging = false;
                return true;
        }
        return super.onTouchEvent(event);
    }

    private void updateTargetFromTouch(float x, float y) {
        float dx = x - centerX;
        float dy = y - centerY;

        // Calculate angle in degrees (-180 to +180) from positive X-axis
        float angle = (float) Math.toDegrees(Math.atan2(dy, dx));

        // Convert to our dial coordinate system where:
        // - Left edge = -90°
        // - Top center = 0°
        // - Right edge = +90°
        //
        // atan2 gives us: right=0°, top=-90°, left=180°/-180°, bottom=90°
        // We want: left=-90°, top=0°, right=+90°

        // Rotate by +90° so that top becomes 0°
        angle = angle + 90f;

        // Normalize to (-180, +180) range
        if (angle > 180f) angle -= 360f;
        if (angle < -180f) angle += 360f;

        // Now we have: right=90°, top=0°, left=-90°/270°
        // Convert left from 270° to -90°
        if (angle > 180f) angle -= 360f;

        // Clamp to semi-circle range (-90 to +90) - only allow top half
        angle = Math.max(-90f, Math.min(90f, angle));

        targetAngle = angle;
        targetValue = angleToValue(angle);
    }

    private float angleToValue(float angle) {
        // Map -90° to -0.5, 0° to 0.0, +90° to +0.5
        // Full range is 180° (-90 to +90) mapping to 1.0 (-0.5 to +0.5)
        return angle / 180f;
    }

    private float valueToAngle(float value) {
        // Map -0.5 to -90°, 0.0 to 0°, +0.5 to +90°
        // Full range is 1.0 (-0.5 to +0.5) mapping to 180° (-90 to +90)
        return value * 180f;
    }

    private void startAnimation() {
        animationRunnable = new Runnable() {
            @Override
            public void run() {
                updateActualValue();
                invalidate();
                postDelayed(this, 32);
            }
        };
        post(animationRunnable);
    }

    private void updateActualValue() {
        long currentTime = System.currentTimeMillis();
        float deltaTime = (currentTime - lastUpdateTime) / 1000f; // Convert to seconds
        lastUpdateTime = currentTime;

        float angleDifference = targetAngle - actualAngle;
        float maxAngleChange = MAX_ANGULAR_SPEED * deltaTime;

        if (Math.abs(angleDifference) <= maxAngleChange) {
            actualAngle = targetAngle;
        } else {
            actualAngle += Math.signum(angleDifference) * maxAngleChange;
        }

        actualValue = angleToValue(actualAngle);
    }

    // Public API
    public float getCurrentValue() {
        return actualValue;
    }

    public float getTargetValue() {
        return targetValue;
    }

    public void setTitle(String newTitle) {
        title = newTitle != null ? newTitle : "";
        invalidate();
    }

    public String getTitle() {
        return title;
    }

    public void setValue(float value) {
        float clampedValue = Math.max(-0.5f, Math.min(0.5f, value));
        targetValue = clampedValue;
        targetAngle = valueToAngle(clampedValue);
        actualValue = clampedValue;
        actualAngle = targetAngle;
        invalidate();
    }

    public void setTargetValue(float value) {
        float clampedValue = Math.max(-0.5f, Math.min(0.5f, value));
        targetValue = clampedValue;
        targetAngle = valueToAngle(clampedValue);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        // Clean up animation when view is removed
        if (animationRunnable != null) {
            removeCallbacks(animationRunnable);
        }
    }
}


package com.example.wave;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

public class SemiCircularProgressView extends View {

    private Paint backgroundPaint;
    private Paint progressPaint;
    private Paint percentageTextPaint;
    private Paint labelTextPaint;
    private RectF oval;

    private float progress = 0; // Progress percentage (0-100)
    private final float START_ANGLE = 150f; // Start angle for the arc
    private final float SWEEP_ANGLE = 240f; // Arc sweep angle (~240 degrees)

    public SemiCircularProgressView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context context) {
        boolean isDarkMode = (context.getResources().getConfiguration().uiMode &
                Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES;

        String blue = isDarkMode ? "#708BFF" : "#395EFD";
        // Background arc paint
        backgroundPaint = new Paint();
        backgroundPaint.setColor(Color.parseColor(blue)); // Blue background arc
        backgroundPaint.setStyle(Paint.Style.STROKE);
        backgroundPaint.setStrokeWidth(60f);
        backgroundPaint.setStrokeCap(Paint.Cap.ROUND); // Rounded edges
        backgroundPaint.setAntiAlias(true);

        // Progress arc paint
        progressPaint = new Paint();
        progressPaint.setStyle(Paint.Style.STROKE);
        progressPaint.setStrokeCap(Paint.Cap.ROUND); // Rounded edges for progress
        progressPaint.setStrokeWidth(60f);
        progressPaint.setAntiAlias(true);

        // Percentage text paint
        percentageTextPaint = new Paint();
        percentageTextPaint.setColor(Color.parseColor(blue)); // Blue text
        percentageTextPaint.setTextSize(90f);
        percentageTextPaint.setTextAlign(Paint.Align.CENTER);
        percentageTextPaint.setFakeBoldText(true);
        percentageTextPaint.setAntiAlias(true);

        // Label text paint
        labelTextPaint = new Paint();
        labelTextPaint.setColor(Color.parseColor(blue)); // Blue text
        labelTextPaint.setTextSize(70f);
        labelTextPaint.setTextAlign(Paint.Align.CENTER);
        labelTextPaint.setAntiAlias(true);

        oval = new RectF();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // Define the oval bounds for the arc
        float padding = 40f; // Half of stroke width
        oval.set(padding, padding, getWidth() - padding, getHeight() - padding); // Adjust height ratio for proper arc

        // Draw the background arc
        canvas.drawArc(oval, 140, 260, false, backgroundPaint);

        // Set progress color based on percentage
        if (progress > 75) {
            progressPaint.setColor(Color.parseColor("#FF474D")); // Red
        } else if (progress > 50) {
            progressPaint.setColor(Color.parseColor("#FFC300")); // Orange
        } else {
            progressPaint.setColor(Color.parseColor("#31F572")); // Green
        }

        // Draw the progress arc
        canvas.drawArc(oval, 140, (progress / 100) * 260, false, progressPaint);

        // Draw percentage text
        canvas.drawText((int) progress + "%", getWidth() / 2f, getHeight() / 2f - 20, percentageTextPaint);

        // Draw "Consumed" label below percentage
        canvas.drawText("Consumed", getWidth() / 2f, getHeight() / 2f + 40, labelTextPaint);
    }

    // Method to update progress dynamically
    public void setProgress(float progress) {
        this.progress = progress;
        invalidate(); // Redraw the view
    }
}

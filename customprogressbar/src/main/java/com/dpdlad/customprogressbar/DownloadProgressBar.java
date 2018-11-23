package com.dpdlad.customprogressbar;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.ColorInt;
import android.support.annotation.IntRange;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.graphics.drawable.VectorDrawableCompat;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import java.security.InvalidParameterException;

/**
 * @author Praveen Kumar on 26/06/2017
 */
public class DownloadProgressBar extends View {

    //Default max sweep angle
    private static final int DEFAULT_MAX_SWEEP_ANGLE = 360;

    //Deafult View top origin
    private static final int DEFAULT_ORIGIN_TOP = 40;

    private static final int MINIMUM_MARGIN_ADJUSTMENT = 3;
    private static final int PROGRESS_STARTING_ANGLE = 270; // By default start angle is 270 degree
    // Bundle state flags
    private static final String KEY_INSTANCE_STATE = "KEY_INSTANCE_STATE";
    private static final String KEY_CURRENT_PROGRESS = "KEY_CURRENT_PROGRESS";
    private static final String KEY_MAX_PROGRESS = "KEY_MAX_PROGRESS";
    // Circle stroke width from dimen
    private final int circleStrokeWidth;
    //Sets boundaries to progress circle
    private final RectF progressiveCircleRect = new RectF();
    private final RectF innerCircleRect = new RectF();
    private Paint progressiveArcPaint, nonProgressiveArcPaint, percentageTextPaint, symbolPaint, innerStrokedCirclePaint;
    @IntRange(from = 0, to = DEFAULT_MAX_SWEEP_ANGLE)
    private int mAnimatingSweepAngle;
    // Load attributes
    @ColorInt
    private int primaryProgressBarColor;
    @ColorInt
    private int secondaryProgressBarColor;
    @ColorInt
    private int progressPercentageTextColor;
    @ColorInt
    private int progressBackgroundColor;
    private Drawable downloadDrawable;
    private boolean showDownloadPercentage, showDownloadIndicator;
    private int progress = 0;
    private int maximumProgress = 100; //By default 100

    private int randomAlpha = 0;

    public DownloadProgressBar(Context context) {
        this(context, null);
    }

    public DownloadProgressBar(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DownloadProgressBar(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        circleStrokeWidth = context.getResources().getDimensionPixelSize(R.dimen.progress_circle_radius);

        // loading attributes
        loadAttributes(context, attrs, defStyleAttr);

        reset(); // Reset the all values
    }

    @Nullable
    public Drawable getDownloadDrawable() {
        return downloadDrawable;
    }

    public void setDownloadDrawable(@Nullable Drawable downloadDrawable) {
        this.downloadDrawable = downloadDrawable;
    }

    public boolean isShowDownloadPercentage() {
        return showDownloadPercentage;
    }

    public void setShowDownloadPercentage(boolean showDownloadPercentage) {
        this.showDownloadPercentage = showDownloadPercentage;
    }

    public boolean isShowDownloadIndicator() {
        return showDownloadIndicator;
    }

    public void setShowDownloadIndicator(boolean showDownloadIndicator) {
        this.showDownloadIndicator = showDownloadIndicator;
    }

    private void loadAttributes(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        TypedArray attributes = context.getTheme()
                .obtainStyledAttributes(attrs, R.styleable.BubbledProgressBar, defStyleAttr, 0);

        //Progressbar color
        primaryProgressBarColor = attributes.getColor(R.styleable.BubbledProgressBar_primary_progress_color,
                ContextCompat.getColor(getContext(), android.R.color.white));
        secondaryProgressBarColor = attributes.getColor(R.styleable.BubbledProgressBar_secondary_progress_color,
                ContextCompat.getColor(getContext(), R.color.cloud_progress_get_started_bg_color));
        progressBackgroundColor = attributes.getColor(R.styleable.BubbledProgressBar_progress_background_color,
                ContextCompat.getColor(getContext(), android.R.color.black));
        progress = attributes.getInt(R.styleable.BubbledProgressBar_progress, 0);
        maximumProgress = attributes.getInt(R.styleable.BubbledProgressBar_max, 100);

        downloadDrawable = attributes.getDrawable(R.styleable.BubbledProgressBar_download_icon);
        showDownloadIndicator = attributes.getBoolean(R.styleable.BubbledProgressBar_show_indicator, true);
        showDownloadPercentage = attributes.getBoolean(R.styleable.BubbledProgressBar_show_percentage, true);

        //Text Color
        progressPercentageTextColor = attributes.getColor(R.styleable.BubbledProgressBar_progress_text_color,
                ContextCompat.getColor(getContext(), android.R.color.white));
    }

    /**
     * It sets primary progress color
     *
     * @param primaryProgressBarColor - progressing color
     */
    public void setPrimaryProgressBarColor(int primaryProgressBarColor) {
        this.primaryProgressBarColor = primaryProgressBarColor;
    }

    public int getSecondaryProgressBarColor() {
        return secondaryProgressBarColor;
    }

    /**
     * It sets unanimated progress color
     *
     * @param secondaryProgressBarColor - sets secondary color.
     */
    public void setSecondaryProgressBarColor(int secondaryProgressBarColor) {
        this.secondaryProgressBarColor = secondaryProgressBarColor;
    }

    /**
     * It used set maximum progress value.
     *
     * @param maximumProgress - maximum progress value, by default 100.
     */
    public void setMaxProgress(int maximumProgress) {
        if (maximumProgress > 0) {
            this.maximumProgress = maximumProgress;
        }
    }

    /**
     * It returns maximum progress value
     *
     * @return - maximum progress value
     */
    public int getMaximumProgress() {
        return maximumProgress;
    }

    private void initiatePainters() {
        Resources res = getContext().getResources();
        float staticScoreTextSize = res.getDimensionPixelSize(R.dimen.text_size_progress_circle);
        percentageTextPaint = percentageTextPaint(staticScoreTextSize);
        symbolPaint = percentageTextPaint((float) (staticScoreTextSize / 1.25));
        progressiveArcPaint = getProgressivePaint(primaryProgressBarColor, false);
        nonProgressiveArcPaint = getProgressivePaint(secondaryProgressBarColor, false);
        innerStrokedCirclePaint = innerStrokedCirclePaint();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int minimumSize = Math.min(widthMeasureSpec, heightMeasureSpec);
        if (minimumSize <= 0) {
            Resources res = getResources();
            minimumSize = res.getDimensionPixelSize(R.dimen.progress_circle_h_w);
        }
//        setMeasuredDimension(minimumSize, minimumSize);
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        int width;
        int height;

        //Measure Width
        if (widthMode == MeasureSpec.EXACTLY) {
            //Must be this size
            width = widthSize;
        } else if (widthMode == MeasureSpec.AT_MOST) {
            //Can't be bigger than...
            width = Math.min(minimumSize, widthSize);
        } else {
            //Be whatever you want
            width = minimumSize;
        }

        //Measure Height
        if (heightMode == MeasureSpec.EXACTLY) {
            //Must be this size
            height = heightSize;
        } else if (heightMode == MeasureSpec.AT_MOST) {
            //Can't be bigger than...
            height = Math.min(minimumSize, heightSize);
        } else {
            //Be whatever you want
            height = minimumSize;
        }

        //MUST CALL THIS
        setMeasuredDimension(width, height);
    }

    private void drawCrossOnView(@NonNull Canvas canvas) {
        Paint paint = getDefaultPaint(Paint.Style.STROKE);
        paint.setColor(Color.RED);
        paint.setStrokeWidth(2);
        canvas.drawLine(getWidth() * 0.25f, 0, (getWidth() * 0.25f), getHeight(), paint);
        canvas.drawLine(0, getWidth() * 0.25f, getHeight(), (getWidth() * 0.25f), paint);

        canvas.drawLine(getWidth() * 0.5f, 0, (getWidth() * 0.5f), getHeight(), paint);
        canvas.drawLine(0, getWidth() * 0.5f, getHeight(), (getWidth() * 0.5f), paint);

        canvas.drawLine(getWidth() * 0.75f, 0, (getWidth() * 0.75f), getHeight(), paint);
        canvas.drawLine(0, getWidth() * 0.75f, getHeight(), (getWidth() * 0.75f), paint);
    }

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        canvas.drawColor(Color.TRANSPARENT); // Background color as TRANSPARENT
            drawProgressiveArc(canvas);
        drawFilledStrokedCircle(canvas);
        if (progress >= 0) {
            if (showDownloadPercentage) {
                drawScoreAndPercentageCharacter(canvas);
            }

        } else {
            drawImage(canvas);
        }
//        drawCrossOnView(canvas); // For development purpose only enable it
        super.onDraw(canvas);
    }

    public void startBlink() {
        Animation animBlink = AnimationUtils.loadAnimation(getContext(),
                R.anim.blinkable);

        startAnimation(animBlink);
    }


    private void drawImage(@NonNull Canvas canvas) {
        Resources res = getResources();
        if (null == downloadDrawable) {
            downloadDrawable = VectorDrawableCompat.create(res, R.drawable.ic_cloud_download, getContext().getTheme());
        }
        Bitmap bitmap = getBitmapFromDrawable(downloadDrawable);
        if (null != bitmap) {

            float optimizedCenterValue = getArcCenterValue(); // Find center of the circle
            float positionX = optimizedCenterValue - (bitmap.getWidth() * 0.5f);
            float positionY = optimizedCenterValue - (bitmap.getHeight() * 0.5f);

            canvas.drawBitmap(bitmap, positionX, positionY, getDefaultPaint(Paint.Style.FILL_AND_STROKE));
            if (!bitmap.isRecycled()) bitmap.recycle();
        }
    }

    /**
     * It draws the Assessment score along with "%" character with different text sizes
     *
     * @param canvas
     */
    private void drawScoreAndPercentageCharacter(@NonNull Canvas canvas) {
        final String scoreData = String.valueOf(progress);
        float textHeight = percentageTextPaint.descent() + percentageTextPaint.ascent();
        float symbolTextWidth = symbolPaint.measureText("%");
        float percentageTextWidth = ((getWidth() - percentageTextPaint.measureText(scoreData) - symbolTextWidth) / 2.0f);

//        float yAxis = (float) (getHeight() / 2 - (textHeight * 0.5)); OLD IMPLEMENTATION
        float optimizedCenterValue = getArcCenterValue(); // Find center of the circle

        float yAxis = (float) (optimizedCenterValue - (textHeight * 0.5));
        canvas.drawText(scoreData, percentageTextWidth - MINIMUM_MARGIN_ADJUSTMENT,
                yAxis, percentageTextPaint);
        canvas.drawText("%", percentageTextWidth + percentageTextPaint.measureText(scoreData) + MINIMUM_MARGIN_ADJUSTMENT,
                yAxis, symbolPaint);
    }


    /**
     * Find center of the circle
     *
     * @return returns the arc center point, which is the view center even view size is MIN or MAX. It will align center of the view.
     */
    private float getArcCenterValue() {
        float arcHeight = (progressiveCircleRect.top + progressiveCircleRect.bottom);
        float arcWidth = (progressiveCircleRect.left + progressiveCircleRect.right);
//        float arcHeight = getLayoutParams().height;
//        float arcWidth = getLayoutParams().width;
        float optimizeValue = (arcHeight + arcWidth) / 2; // Find center of the circle
        return optimizeValue / 2;
    }


    /**
     * It draws the progressive arc
     *
     * @param canvas
     */
    private void drawProgressiveArc(@NonNull Canvas canvas) {
        if (progressiveCircleRect.isEmpty()) {
            progressiveCircleRect.set(
                    circleStrokeWidth, // left
                    DEFAULT_ORIGIN_TOP - circleStrokeWidth, //top
                    getWidth() - circleStrokeWidth, // right
                    getHeight() - circleStrokeWidth // bottom
            );
        }
        if (showDownloadIndicator) {
            canvas.drawArc(progressiveCircleRect, 0, DEFAULT_MAX_SWEEP_ANGLE, true, nonProgressiveArcPaint);
            canvas.drawArc(progressiveCircleRect, PROGRESS_STARTING_ANGLE, mAnimatingSweepAngle, false, progressiveArcPaint);
        }
    }


    /**
     * It draws inner filled stroked semi-transparent circle.
     *
     * @param canvas
     */
    private void drawFilledStrokedCircle(@NonNull Canvas canvas) {
        if (innerCircleRect.isEmpty()) {
            innerCircleRect.set(progressiveCircleRect);
            float padding = (float) (circleStrokeWidth * 0.5);
            innerCircleRect.inset(padding, padding);

        }
        canvas.drawOval(innerCircleRect, innerStrokedCirclePaint); // It draws the inner circle with FILL background for star symbol
    }

    /**
     * It provides Default {@link Paint} object.
     *
     * @param style the style to be set as STROKE or FILL_AND_STROKE
     * @return loaded {@link Paint} instance.
     */
    @NonNull
    private Paint getDefaultPaint(Paint.Style style) {
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setStyle(style);
        return paint;
    }

    /**
     * It gives {@link Paint} object for percentage text rendering.
     *
     * @param percentageTextSize - progressed percentage
     * @return - returns paint instance.
     */
    @NonNull
    private Paint percentageTextPaint(float percentageTextSize) {
        Paint mTextPaint = getDefaultPaint(Paint.Style.FILL_AND_STROKE);
        mTextPaint.setColor(progressPercentageTextColor);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mTextPaint.setLetterSpacing(-0.07f);
        }
        mTextPaint.setTextSize((float) (percentageTextSize / 1.25));
        return mTextPaint;
    }

    /**
     * It gives {@link Paint} object for inner stroked and fill circle rendering.
     *
     * @return - returns paint instance.
     */
    @NonNull
    private Paint innerStrokedCirclePaint() {
        Paint paint = getDefaultPaint(Paint.Style.FILL_AND_STROKE);
        paint.setColor(progressBackgroundColor);
        paint.setAlpha(140); //sets transparent level
        return paint;
    }

    /**
     * It provides {@link Paint} instance for draw an arc with progressive feature.
     *
     * @param color                - optional to set according to progressing level.
     * @param isTransparentEnabled
     * @return - It returns the {@link Paint} instance to indicating progressing level to user by given value.
     */
    private Paint getProgressivePaint(int color, boolean isTransparentEnabled) {
        Paint paint = getDefaultPaint(Paint.Style.STROKE);
        paint.setColor(color);
        paint.setStrokeCap(Paint.Cap.ROUND); // gives curve shape to paint
        if (isTransparentEnabled) {
            paint.setAlpha(70);
            paint.setStrokeWidth((float) (circleStrokeWidth * 1.5));
        } else {
            paint.setStrokeWidth((float) (circleStrokeWidth));
        }
        return paint;
    }

    @IntRange(from = 0, to = DEFAULT_MAX_SWEEP_ANGLE)
    public int getSweepAngle() {
        return mAnimatingSweepAngle;
    }

    /**
     * It sets sweep animation value
     *
     * @param mRedSweepAngle - returns animating value
     */
    public void setSweepAngle(int mRedSweepAngle) {
        this.mAnimatingSweepAngle = mRedSweepAngle;
    }

    /**
     * It returns maximum sweeping animation angle.
     *
     * @return returns max sweep angle
     */
    public int getMaxAngle() {
        return DEFAULT_MAX_SWEEP_ANGLE;
    }

    /**
     * It will reset the progress bar state as IN_ACTIVE
     */
    public void reset() {
        //Reset or Assigning the Paints
        initiatePainters();

        //Reset the progress
        setProgress(-1); // add extra work here while user reset progress.

        setSweepAngle(0); // reset the sweep angle.
    }

    @SuppressWarnings("unused")
    private void reInitiateSecondaryProgressColor() {
        secondaryProgressBarColor = ContextCompat.getColor(getContext(), android.R.color.black);
        if (nonProgressiveArcPaint.getColor() == secondaryProgressBarColor) return;
        nonProgressiveArcPaint = getProgressivePaint(secondaryProgressBarColor, true);
    }

    /**
     * It gives current progress value.
     *
     * @return progress - current progress score value
     */
    public int getProgress() {
        return progress;
    }

    /**
     * This method will update the every progress value in {@link Canvas} rendering.
     *
     * @param progress - current progress value
     */
    public void setProgress(int progress) {
        if (progress == -1) {
            this.progress = progress;
        } else if (progress >= 0 && progress <= getMaximumProgress()) {
            this.progress = progress;
//            reInitiateSecondaryProgressColor();
            //this.progress %= getMaximumProgress(); // If values come more than Maximum value
            setSweepAngle((progress * DEFAULT_MAX_SWEEP_ANGLE) / getMaximumProgress());
        } else {
            throw new InvalidParameterException(String.format("Invalid progress(%d) value has been passed!", progress));
        }
        invalidate();
    }

    @Nullable
    @Override
    protected Parcelable onSaveInstanceState() {
        final Bundle bundle = new Bundle();
        bundle.putParcelable(KEY_INSTANCE_STATE, super.onSaveInstanceState());
        bundle.putInt(KEY_CURRENT_PROGRESS, getProgress());
        bundle.putInt(KEY_MAX_PROGRESS, getMaximumProgress());
        return bundle;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        if (state instanceof Bundle) {
            final Bundle bundle = (Bundle) state;
            setProgress(bundle.getInt(KEY_CURRENT_PROGRESS));
            setMaxProgress(bundle.getInt(KEY_MAX_PROGRESS));
            initiatePainters();
            super.onRestoreInstanceState(bundle.getParcelable(KEY_INSTANCE_STATE));
            return;
        }
        super.onRestoreInstanceState(state);
    }

    /**
     * This methods used to create {@link Bitmap} instance from given {@link Drawable} with expected image size.
     *
     * @param drawable - given image drawable.
     * @return - bitmap of given drawable
     */
    private Bitmap getBitmapFromDrawable(@Nullable Drawable drawable) {
        Bitmap bitmap;

        if (drawable == null) {
            return null;
        }

        if (drawable instanceof BitmapDrawable) {
            BitmapDrawable bitmapDrawable = (BitmapDrawable) drawable;
            if (bitmapDrawable.getBitmap() != null) {
                return bitmapDrawable.getBitmap();
            }
        }

        if (drawable.getIntrinsicWidth() <= 0 || drawable.getIntrinsicHeight() <= 0) {
            bitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888); // Single color bitmap will be created of 1x1 pixel
        } else {
            float ratio = 1f; // By default 1f
            int width = (int) (drawable.getIntrinsicWidth() * ratio);
            int height = (int) (drawable.getIntrinsicHeight() * ratio);

            bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        }

        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);

        return bitmap;
    }

}

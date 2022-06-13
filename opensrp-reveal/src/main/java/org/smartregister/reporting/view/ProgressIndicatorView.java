package org.smartregister.reporting.view;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.PorterDuff;
import android.graphics.drawable.ClipDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.annotation.Nullable;
import org.smartregister.reveal.R;
import timber.log.Timber;


public class ProgressIndicatorView extends LinearLayout {

    private int progressBarBackgroundColor;
    private int progressBarForegroundColor;
    private int progressDrawable;
    private String title;
    private String subTitle;
    private int progress;
    private boolean isTitleHidden;
    private boolean isSubTitleHidden;

    private static final String PROGRESSBAR_FOREGROUND_COLOR = "progressbar_foreground_color";
    private static final String PROGRESSBAR_BACKGROUND_COLOR = "progressbar_background_color";
    private static final String PROGRESSBAR_TITLE = "progressbar_title";
    private static final String PROGRESSBAR_SUB_TITLE = "progressbar_sub_title";
    private static final String PROGRESSBAR_PROGRESS = "progressbar_progress";
    private static final String PROGRESSBAR_INSTANCE_STATE = "progressbar_instance_state";
    private static final String PROGRESSBAR_DRAWABLE = "progressbar_drawable";
    private AttributeSet attrs;

    public ProgressIndicatorView(Context context) {
        super(context);
        initView();

    }

    public ProgressIndicatorView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initView();
        setupAttributes(attrs);
    }

    public ProgressIndicatorView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
        setupAttributes(attrs);
    }

    @TargetApi(android.os.Build.VERSION_CODES.LOLLIPOP)
    public ProgressIndicatorView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initView();
        setupAttributes(attrs);
    }

    /**
     * Set init configs for parent layout view
     */
    private void initView() {
        inflate(getContext(), R.layout.progress_indicator_view, this);
        setGravity(Gravity.LEFT);
        setOrientation(VERTICAL);
    }

    /**
     * @param attrs an attribute set styled attributes from theme
     */
    protected void setupAttributes(AttributeSet attrs) {
        this.attrs = attrs;

        TypedArray typedArray = getStyledAttributes();
        try {

            resetLayoutParams(typedArray);

        } finally {

            typedArray.recycle();// We must recycle TypedArray objects as they are shared
        }
    }

    protected TypedArray getStyledAttributes() {
        return getContext().getTheme().obtainStyledAttributes(attrs, R.styleable.ProgressIndicatorView, 0, 0);
    }

    private void resetLayoutParams(TypedArray typedArray) {

        title = TextUtils.isEmpty(title) ? typedArray.getString(R.styleable.ProgressIndicatorView_title) : title;
        title = TextUtils.isEmpty(title) ? progress + "%" : title;

        subTitle = TextUtils.isEmpty(subTitle) ? typedArray.getString(R.styleable.ProgressIndicatorView_subTitle) : subTitle;

        TextView titleTextView = findViewById(R.id.title_textview);
        titleTextView.setText(title);
        titleTextView.setVisibility(isTitleHidden ? View.GONE : View.VISIBLE);

        TextView subTitleTextView = findViewById(R.id.sub_title_textview);
        subTitleTextView.setText(subTitle);
        subTitleTextView.setVisibility(isSubTitleHidden ? View.GONE : View.VISIBLE);

        progress = progress > 0 ? progress : typedArray.getInteger(R.styleable.ProgressIndicatorView_progress, 0);


        setResourceValues(typedArray);

        processProgressBarLayoutReset(progress, progressBarForegroundColor, progressBarBackgroundColor, progressDrawable);

    }

    private void setResourceValues(TypedArray typedArray) {

        progressBarForegroundColor = progressBarForegroundColor != 0 ? progressBarForegroundColor : typedArray.getColor(R.styleable.ProgressIndicatorView_progressBarForegroundColor, 0);
        progressBarBackgroundColor = progressBarBackgroundColor != 0 ? progressBarBackgroundColor : typedArray.getColor(R.styleable.ProgressIndicatorView_progressBarBackgroundColor, 0);
        progressDrawable = progressDrawable != 0 ? progressDrawable : typedArray.getResourceId(R.styleable.ProgressIndicatorView_progressDrawable, 0);

    }

    private void processProgressBarLayoutReset(int progress, int progressBarForegroundColor, int progressBarBackgroundColor, int progressDrawable) {

        ProgressBar progressBarView = findViewById(R.id.progressbar_view);
        progressBarView.setProgressDrawable(progressDrawable > 0 ? getContext().getResources().getDrawable(progressDrawable) : progressBarView.getProgressDrawable());
        progressBarView.setProgress(progress);

        programmaticallyResetProgressBarBackgroundDrawable(progressBarView, progressBarForegroundColor, progressBarBackgroundColor);

    }

    @TargetApi(Build.VERSION_CODES.M)
    protected void programmaticallyResetProgressBarBackgroundDrawable(ProgressBar progressBarView, int progressBarForegroundColor, int progressBarBackgroundColor) {
        try {
            LayerDrawable progressBarDrawable = (LayerDrawable) progressBarView.getProgressDrawable().mutate();
            Drawable backgroundDrawable = progressBarDrawable.getDrawable(0).mutate();
            ClipDrawable clipDrawable = (ClipDrawable) progressBarDrawable.getDrawable(1).mutate();

            if (progressBarBackgroundColor != 0)
                backgroundDrawable.setColorFilter(progressBarBackgroundColor, PorterDuff.Mode.SRC_IN);

            if (progressBarForegroundColor != 0) {

                GradientDrawable gdDefault = new GradientDrawable();
                gdDefault.setColor(progressBarForegroundColor);
                gdDefault.setCornerRadius(10);
                gdDefault.setStroke(2, progressBarBackgroundColor);

                clipDrawable.setDrawable(gdDefault);

            }
        } catch (Exception | NoSuchMethodError e) {
            Timber.d(e);
        }
    }

    @Override
    public Parcelable onSaveInstanceState() {
        Bundle bundle = new Bundle();
        bundle.putParcelable(PROGRESSBAR_INSTANCE_STATE, super.onSaveInstanceState());// Store base view state

        bundle.putInt(PROGRESSBAR_FOREGROUND_COLOR, this.progressBarForegroundColor);
        bundle.putInt(PROGRESSBAR_BACKGROUND_COLOR, this.progressBarBackgroundColor);
        bundle.putString(PROGRESSBAR_TITLE, this.title);
        bundle.putString(PROGRESSBAR_SUB_TITLE, this.subTitle);
        bundle.putInt(PROGRESSBAR_PROGRESS, this.progress);
        bundle.putInt(PROGRESSBAR_DRAWABLE, this.progressDrawable);


        return bundle;
    }

    @Override
    public void onRestoreInstanceState(Parcelable state) {
        Parcelable updatedState = null;

        if (state instanceof Bundle) {
            Bundle bundle = (Bundle) state;
            // Load back our custom view state

            this.progressBarForegroundColor = bundle.getInt(PROGRESSBAR_FOREGROUND_COLOR);
            this.progressBarBackgroundColor = bundle.getInt(PROGRESSBAR_BACKGROUND_COLOR);
            this.title = bundle.getString(PROGRESSBAR_TITLE);
            this.subTitle = bundle.getString(PROGRESSBAR_SUB_TITLE);
            this.progress = bundle.getInt(PROGRESSBAR_PROGRESS);
            this.progressDrawable = bundle.getInt(PROGRESSBAR_DRAWABLE);

            updatedState = bundle.getParcelable(PROGRESSBAR_INSTANCE_STATE);// Load base view state back
        }

        super.onRestoreInstanceState(updatedState != null ? updatedState : state);// Pass base view state to super
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

    }

    private void refreshLayout() {

        TypedArray typedArray = getStyledAttributes();

        try {
            resetLayoutParams(typedArray);
            invalidate();
            requestLayout();
        } finally {
            typedArray.recycle();
        }
    }

    public int getProgressBarBackgroundColor() {
        return progressBarBackgroundColor;
    }

    /**
     * Only works for API 23 and onwards other use a custom drawable xml to customize style
     */
    @TargetApi(Build.VERSION_CODES.M)
    public void setProgressBarBackgroundColor(int progressBarBackgroundColor) {
        this.progressBarBackgroundColor = progressBarBackgroundColor;
        refreshLayout();
    }

    public int getProgressBarForegroundColor() {
        return progressBarForegroundColor;
    }

    /**
     * Only works for API 23 and onwards other use a custom drawable xml to customize style
     */
    @TargetApi(Build.VERSION_CODES.M)
    public void setProgressBarForegroundColor(int progressBarForegroundColor) {
        this.progressBarForegroundColor = progressBarForegroundColor;
        refreshLayout();
    }

    public String getTitle() {
        return title;
    }

    /**
     * Sets title of indicator (top text)
     */
    public void setTitle(String title) {
        this.title = title;
        refreshLayout();
    }

    public String getSubTitle() {
        return subTitle;
    }

    /**
     * Sets subtitle of indicator (top text)
     */
    public void setSubTitle(String subTitle) {
        this.subTitle = subTitle;
        refreshLayout();
    }

    public int getProgress() {
        return progress;
    }

    /**
     * Sets progress of indicator bar
     */
    public void setProgress(int progress) {
        this.progress = progress;
        refreshLayout();
    }

    /**
     * Sets hides title of indicator
     */
    public void hideTitle(boolean isHidden) {
        this.isTitleHidden = isHidden;
        refreshLayout();
    }

    public void hideSubTitle(boolean isHidden) {
        this.isSubTitleHidden = isHidden;
        refreshLayout();
    }

    public void setProgressDrawable(int progressDrawable) {
        this.progressDrawable = progressDrawable;
        refreshLayout();
    }

    public int getProgressDrawable() {
        return progressDrawable;
    }
}

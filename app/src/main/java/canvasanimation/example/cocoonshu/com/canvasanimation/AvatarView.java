package canvasanimation.example.cocoonshu.com.canvasanimation;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Xfermode;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * AvatarView
 * @Auther Cocoonshu
 * @Date 2016-05-16 10:02
 * Copyright (c) 2016 Cocoonshu
 */
public class AvatarView extends View {

    private       float           mStrokeRotate         = 0;
    private       float           mFocusARotate         = 0;
    private       float           mFocusBRotate         = 0;
    private       int             mStrokeColor          = 0xFFFFFFFF;
    private       int             mCircleAColor         = 0xFFFFFF00;
    private       int             mCircleBColor         = 0xFF00FFFF;
    private final float           mStrokeRotateInterval = +1.0f;
    private final float           mFocusARotateInterval = +3.5f;
    private final float           mFocusBRotateInterval = -2.5f;
    private       Paint           mStrokePaint          = null;
    private       Drawable        mAvatarImage          = null;
    private       ExecutorService mAvatarClipExceutor   = Executors.newSingleThreadExecutor();

    public AvatarView(Context context) {
        this(context, null);
    }

    public AvatarView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AvatarView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.AvatarView, defStyleAttr, 0);
        int loopSize = typedArray.getIndexCount();
        for (int i = 0; i < loopSize; i++) {
            int attributeKey = typedArray.getIndex(i);
            switch (attributeKey) {
                case R.styleable.AvatarView_avatar:
                    mAvatarImage = typedArray.getDrawable(attributeKey);
                    if (mAvatarImage != null) {
                        mAvatarImage.setBounds(0, 0, mAvatarImage.getIntrinsicWidth(), mAvatarImage.getIntrinsicHeight());
                        mAvatarImage.setState(ENABLED_STATE_SET);
                        mAvatarClipExceutor.submit(new AvatarClipRunnable(mAvatarImage));
                    }
                    break;
            }
        }
        typedArray.recycle();
        mStrokePaint = new Paint();
        mStrokePaint.setAntiAlias(true);
        mStrokePaint.setDither(true);
        mStrokePaint.setStyle(Paint.Style.STROKE);
        mStrokePaint.setColor(0xFFFFFFFF);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int      widthSpecMode    = MeasureSpec.getMode(widthMeasureSpec);
        int      heightSpecMode   = MeasureSpec.getMode(heightMeasureSpec);
        int      widthSpecSize    = MeasureSpec.getSize(widthMeasureSpec);
        int      heightSpecSize   = MeasureSpec.getSize(heightMeasureSpec);
        int      measuredWidth    = 0;
        int      measuredHeight   = 0;
        int      wantedWidth      = mAvatarImage != null ? mAvatarImage.getIntrinsicWidth() : 0;
        int      wantedHeight     = mAvatarImage != null ? mAvatarImage.getIntrinsicHeight() : 0;

        // Width
        switch (widthSpecMode) {
            case MeasureSpec.UNSPECIFIED:
                measuredWidth = wantedWidth;
                break;
            case MeasureSpec.AT_MOST:
                measuredWidth = wantedWidth < widthSpecSize ? wantedWidth : widthSpecSize;
                break;
            case MeasureSpec.EXACTLY:
                measuredWidth = widthSpecSize;
                break;
        }

        // Height
        switch (heightSpecMode) {
            case MeasureSpec.UNSPECIFIED:
                measuredHeight = wantedHeight;
                break;
            case MeasureSpec.AT_MOST:
                measuredHeight = wantedHeight < heightSpecSize ? wantedHeight : heightSpecSize;
                break;
            case MeasureSpec.EXACTLY:
                measuredHeight = heightSpecSize;
                break;
        }

        setMeasuredDimension(measuredWidth, measuredHeight);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        float viewWidth          = getWidth();
        float viewHeight         = getHeight();
        float limitWidth         = viewWidth * 0.8f;
        float limitHeight        = viewHeight * 0.8f;
        float centerX            = viewWidth * 0.5f;
        float centerY            = viewHeight * 0.5f;
        float avatorWidth        = mAvatarImage != null ? mAvatarImage.getIntrinsicWidth() : 0;
        float avatorHeight       = mAvatarImage != null ? mAvatarImage.getIntrinsicHeight() : 0;
        float scale              = computeImageScale(avatorWidth, avatorHeight, limitWidth, limitHeight);
        float avatorScaleWidth   = avatorWidth * scale;
        float avatorScaleHeight  = avatorHeight * scale;
        float coverSize          = Math.min(avatorScaleWidth, avatorScaleHeight);
        float strokeWidth        = coverSize * 0.02f;
        float focusCircleARadius = coverSize * 0.8f * 0.5f;
        float focusCircleBRadius = coverSize * 0.6f * 0.5f;

        if (mAvatarImage != null) {

            // Draw avatar
            canvas.save();
            canvas.translate((viewWidth - avatorScaleWidth) * 0.5f, (viewHeight - avatorScaleHeight) * 0.5f);
            canvas.scale(scale, scale);
            canvas.rotate(mStrokeRotate, avatorWidth * 0.5f, avatorHeight * 0.5f);
            mAvatarImage.draw(canvas);
            canvas.restore();

            // Draw Stroke
            canvas.save();
            mStrokePaint.setStrokeWidth(strokeWidth);
            mStrokePaint.setStyle(Paint.Style.STROKE);
            mStrokePaint.setColor(mStrokeColor);
            canvas.drawCircle(centerX, centerY, (coverSize - strokeWidth * 0.5f) * 0.5f, mStrokePaint);

            // Draw Circle A
            mStrokePaint.setColor(mCircleAColor);
            canvas.rotate(mFocusARotate, centerX, centerY);
            canvas.drawArc(
                    centerX - focusCircleARadius,
                    centerY - focusCircleARadius,
                    centerX + focusCircleARadius,
                    centerY + focusCircleARadius,
                    0, 90, false, mStrokePaint);
            canvas.drawArc(
                    centerX - focusCircleARadius,
                    centerY - focusCircleARadius,
                    centerX + focusCircleARadius,
                    centerY + focusCircleARadius,
                    180, 90, false, mStrokePaint);
            canvas.drawCircle(
                    centerX, centerY,
                    (focusCircleARadius * 2f - strokeWidth * 1.5f) * 0.5f, mStrokePaint);

            // Draw Circle B
            mStrokePaint.setColor(mCircleBColor);
            canvas.rotate(mFocusBRotate - mFocusARotate, centerX, centerY);
            canvas.drawArc(
                    centerX - focusCircleBRadius,
                    centerY - focusCircleBRadius,
                    centerX + focusCircleBRadius,
                    centerY + focusCircleBRadius,
                    90, 90, false, mStrokePaint);
            canvas.drawArc(
                    centerX - focusCircleBRadius,
                    centerY - focusCircleBRadius,
                    centerX + focusCircleBRadius,
                    centerY + focusCircleBRadius,
                    270, 90, false, mStrokePaint);
            canvas.drawCircle(
                    centerX, centerY,
                    (focusCircleBRadius * 2f - strokeWidth * 1.5f) * 0.5f, mStrokePaint);

            canvas.restore();
        }

        mFocusARotate += mFocusARotateInterval;
        mFocusBRotate += mFocusBRotateInterval;
        mStrokeRotate += mStrokeRotateInterval;
        invalidate();
    }

    private float computeImageScale(float avatorWidth, float avatorHeight, float viewWidth, float viewHeight) {
        if (avatorWidth == 0f || avatorHeight == 0f || viewWidth == 0f || viewHeight == 0f) {
            return 1f;
        }
        if (avatorWidth <= viewWidth && avatorHeight <= viewHeight) {
            return 1f;
        } else {
            float avatorRatio = avatorWidth / avatorHeight;
            float viewRatio   = viewWidth / viewHeight;
            if (avatorRatio > viewRatio) {
                return viewWidth / avatorWidth;
            } else {
                return viewHeight / avatorHeight;
            }
        }
    }

    public void setAvatarImage(final Bitmap bitmap) {
        if (bitmap != null) {
            mAvatarClipExceutor.submit(new AvatarClipRunnable(bitmap));
        }
    }

    private final class AvatarClipRunnable implements Runnable {

        private Bitmap mSrcBitmap = null;

        public AvatarClipRunnable(Bitmap bitmap) {
            mSrcBitmap = bitmap;
        }

        public AvatarClipRunnable(Drawable bitmapBitmap) {
            if (bitmapBitmap instanceof BitmapDrawable) {
                mSrcBitmap = ((BitmapDrawable) bitmapBitmap).getBitmap();
            } else {
                mAvatarImage = bitmapBitmap;
            }
        }

        @Override
        public void run() {
            Bitmap clipBitmap = null;
            if (mSrcBitmap == null) {
                return;
            }
            if (mSrcBitmap.isRecycled()) {
                return;
            }
            if (!mSrcBitmap.isMutable()) {
                clipBitmap = Bitmap.createBitmap(mSrcBitmap.getWidth(), mSrcBitmap.getHeight(), Bitmap.Config.ARGB_8888);
            } else {
                clipBitmap = mSrcBitmap;
            }

            Paint    coverPaint = new Paint();
            Xfermode xfermode   = new PorterDuffXfermode(PorterDuff.Mode.SRC_IN);
            coverPaint.setStyle(Paint.Style.FILL);
            coverPaint.setColor(0xFFFFFFFF);
            coverPaint.setAntiAlias(true);
            coverPaint.setDither(true);

            float bmpWidth  = clipBitmap.getWidth();
            float bmpHeight = clipBitmap.getHeight();
            float centerX   = bmpWidth * 0.5f;
            float centerY   = bmpHeight * 0.5f;
            float minEdge   = Math.min(bmpWidth, bmpHeight);
            Canvas canvas   = new Canvas(clipBitmap);

            coverPaint.setXfermode(null);
            canvas.drawCircle(centerX, centerY, minEdge * 0.5f, coverPaint);
            coverPaint.setXfermode(xfermode);
            canvas.drawBitmap(mSrcBitmap, 0, 0, coverPaint);

            synchronized (mAvatarImage) {
                mAvatarImage = new BitmapDrawable(clipBitmap);
                mAvatarImage.setBounds(0, 0, mAvatarImage.getIntrinsicWidth(), mAvatarImage.getIntrinsicHeight());
                mAvatarImage.setState(View.ENABLED_STATE_SET);
                postInvalidate();
            }
        }
    }
}

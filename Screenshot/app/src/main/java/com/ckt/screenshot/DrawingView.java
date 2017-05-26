package com.ckt.screenshot;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.support.annotation.ColorInt;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

public class DrawingView extends View {
    private static final float TOUCH_TOLERANCE = 4;
    private Bitmap mBitmap;
    private Canvas mCanvas;
    private Path mPath;
    private Paint mBitmapPaint;
    private Paint mPaint;
    private boolean mDrawMode;
    private float mX, mY;
    private float mPenSize = 0;
    private float mEraserSize = 10;
    private float mProportion = 0;
    private float mTranslationalW = 0;
    private float mTranslationalH = 0;

    public DrawingView(Context c) {
        this(c, null);
    }

    public DrawingView(Context c, AttributeSet attrs) {
        this(c, attrs, 0);
    }

    public DrawingView(Context c, AttributeSet attrs, int defStyle) {
        super(c, attrs, defStyle);
        init();
    }

    private void init() {
        mPath = new Path();
        mBitmapPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.DITHER_FLAG);
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setDither(true);
        mPaint.setColor(Color.TRANSPARENT);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeJoin(Paint.Join.ROUND);
        mPaint.setStrokeCap(Paint.Cap.ROUND);
        mPaint.setStrokeWidth(mPenSize);
        mDrawMode = true;
        mPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST));
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if (mBitmap == null) {
            mBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        }
        mCanvas = new Canvas(mBitmap);
        mCanvas.drawColor(Color.TRANSPARENT);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        Matrix matrix = new Matrix();
        float proportion = (float) canvas.getHeight() / mBitmap.getHeight();
        if (proportion < 1) {
            mProportion = proportion;
            mTranslationalW = (canvas.getWidth() - mBitmap.getWidth() * proportion) / 2;
            matrix.postScale(proportion, proportion);
            matrix.postTranslate((canvas.getWidth() - mBitmap.getWidth() * proportion) / 2, 0);
            canvas.drawBitmap(mBitmap, matrix, mBitmapPaint);
        } else {
            mTranslationalW = (canvas.getWidth() - mBitmap.getWidth()) / 2;
            mTranslationalH = (canvas.getHeight() - mBitmap.getHeight()) / 2;
            mProportion = 0;
            canvas.drawBitmap(mBitmap, (canvas.getWidth() - mBitmap.getWidth()) / 2,
                    (canvas.getHeight() - mBitmap.getHeight()) / 2, mBitmapPaint);
        }
    }

    private void touch_start(float x, float y) {
        mPath.reset();
        mPath.moveTo(x, y);
        mX = x;
        mY = y;
        mCanvas.drawPath(mPath, mPaint);
    }

    private void touch_move(float x, float y) {
        float dx = Math.abs(x - mX);
        float dy = Math.abs(y - mY);
        if (dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE) {
            mPath.quadTo(mX, mY, (x + mX) / 2, (y + mY) / 2);
            mX = x;
            mY = y;
        }
        mCanvas.drawPath(mPath, mPaint);
    }

    private void touch_up() {
        mPath.lineTo(mX, mY);
        mCanvas.drawPath(mPath, mPaint);
        mPath.reset();
        if (mDrawMode) {
            mPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SCREEN));
        } else {
            mPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x;
        float y;
        if (mProportion != 0 && mTranslationalW != 0) {
            x = (event.getX() - mTranslationalW) / mProportion;
            y = event.getY() / mProportion;
        } else if (mTranslationalH != 0) {
            x = event.getX() - mTranslationalW;
            y = event.getY() - mTranslationalH;
        } else {
            x = event.getX();
            y = event.getY();
        }
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (mDrawMode) {
                    mPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_OVER));
                } else {
                    mPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
                }
                touch_start(x, y);
                invalidate();
                break;
            case MotionEvent.ACTION_MOVE:
                touch_move(x, y);
                if (!mDrawMode) {
                    mPath.lineTo(mX, mY);
                    mPath.reset();
                    mPath.moveTo(x, y);
                }
                mCanvas.drawPath(mPath, mPaint);
                invalidate();
                break;
            case MotionEvent.ACTION_UP:
                touch_up();
                invalidate();
                break;
        }
        return true;
    }

    public void initializePen() {
        mDrawMode = true;
        mPaint.setAntiAlias(true);
        mPaint.setDither(true);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeJoin(Paint.Join.ROUND);
        mPaint.setStrokeCap(Paint.Cap.ROUND);
        mPaint.setStrokeWidth(mPenSize);
        mPaint.setFilterBitmap(true);
        mPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_OVER));
    }

    public void initializeEraser() {
        mDrawMode = false;
        mPaint.setColor(Color.parseColor("#f4f4f4"));
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(mEraserSize);
        mPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
    }

    @Override
    public void setBackgroundColor(int color) {
        mCanvas.drawColor(color);
        super.setBackgroundColor(color);
    }

    public void setEraserSize(float size) {
        mEraserSize = size;
        initializeEraser();
    }

    public float getEraserSize() {
        return mEraserSize;
    }

    public void setPenSize(float size) {
        mPenSize = size;
        initializePen();
    }

    public float getPenSize() {
        return mPenSize;
    }

    public void setPenColor(@ColorInt int color) {
        mPaint.setColor(color);
    }

    public
    @ColorInt
    int getPenColor() {
        return mPaint.getColor();
    }

    public void setImageBitmap(Bitmap bitmap) {
        mBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);
        mCanvas = new Canvas(mBitmap);
        mCanvas.drawColor(Color.TRANSPARENT);
        bitmap.recycle();
        invalidate();
    }

    public Bitmap getImageBitmap() {
        return mBitmap;
    }
}

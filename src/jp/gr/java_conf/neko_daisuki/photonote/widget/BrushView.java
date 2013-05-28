package jp.gr.java_conf.neko_daisuki.photonote.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint.Style;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Region.Op;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

public class BrushView extends View {

    private int mSelected;

    /**
     * Array of stroke width. Width must be in this array in descendent order.
     */
    private int[] mWidth;

    // drawing data
    private int mBorderWidth = 8;

    public BrushView(Context context) {
        super(context);
        initialize();
    }

    public BrushView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initialize();
    }

    public BrushView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initialize();
    }

    public boolean onTouchEvent(MotionEvent event) {
        // TODO
        return false;
    }

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int size = getCellSize();
        setMeasuredDimension(size, size * mWidth.length);
    }

    protected void onDraw(Canvas canvas) {
        int width = getWidth();
        Rect rect = new Rect(0, 0, width - 1, getHeight() - 1);
        Paint paint = new Paint();
        paint.setColor(Color.WHITE);
        paint.setStyle(Style.FILL);
        canvas.drawRect(rect, paint);

        paint.setColor(Color.BLACK);
        int size = getCellSize();
        float x = (float)width / 2;
        for (int i = 0; i < mWidth.length; i++) {
            float y = (float)(i * size + size / 2);
            canvas.drawCircle(x, y, (float)(mWidth[i] / 2), paint);
        }

        paint.setStrokeWidth(1.0f);
        paint.setStyle(Style.STROKE);
        canvas.drawRect(rect, paint);
        for (int i = 1; i < mWidth.length; i++) {
            float y = (float)(i * size);
            canvas.drawLine(0.0f, y, (float)width, y, paint);
        }

        Rect cellRect = new Rect();
        cellRect.left = 0;
        cellRect.top = mSelected * size;
        cellRect.right = width - 1;
        cellRect.bottom = rect.top + size - 1;
        canvas.clipRect(cellRect, Op.REPLACE);
        rect.left = cellRect.left + mBorderWidth;
        rect.top = cellRect.top + mBorderWidth;
        rect.right = cellRect.right - mBorderWidth;
        rect.bottom = cellRect.bottom - mBorderWidth;
        canvas.clipRect(rect, Op.DIFFERENCE);
        paint.setStyle(Style.FILL);
        canvas.drawRect(cellRect, paint);
    }

    private int getMaxWidth() {
        return mWidth[0];
    }

    private int getCellSize() {
        return 2 * mBorderWidth + getMaxWidth();
    }

    private void initialize() {
        setFocusable(true);
        setFocusableInTouchMode(true);

        mSelected = 0;
        mWidth = new int[] { 96, 48, 32, 24, 16, 8 };
    }
}

/**
 * vim: tabstop=4 shiftwidth=4 expandtab softtabstop=4
 */

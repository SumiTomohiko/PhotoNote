package jp.gr.java_conf.neko_daisuki.photonote.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Region;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

public class PaletteView extends View {

    private static class PaletteColor {

        public int color;
        public int selectedColor;

        public PaletteColor(int color) {
            this.color = color;
            this.selectedColor = Color.BLACK;
        }

        public PaletteColor(int color, int selectedColor) {
            this.color = color;
            this.selectedColor = selectedColor;
        }
    }

    // document
    private PaletteColor[] mColors;
    private int mSelected;

    // drawing data
    private int mBorderWidth = 8;
    private int mSize = 64;

    // stateless helpers
    // TODO

    public PaletteView(Context context) {
        super(context);
        initialize();
    }

    public PaletteView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initialize();
    }

    public PaletteView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initialize();
    }

    public boolean onTouchEvent(MotionEvent event) {
        // TODO
        return false;
    }

    public int getSelectedColor() {
        return mColors[mSelected].color;
    }

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(mSize, mSize * mColors.length);
    }

    protected void onDraw(Canvas canvas) {
        Rect rect = new Rect();
        rect.left = 0;
        rect.right = getWidth() - 1;
        Paint paint = new Paint();
        paint.setStyle(Paint.Style.FILL);
        for (int i = 0; i < mColors.length; i++) {
            rect.top = i * mSize;
            rect.bottom = rect.top + mSize - 1;
            paint.setColor(mColors[i].color);
            canvas.drawRect(rect, paint);
        }

        rect.top = 0;
        rect.bottom = getHeight() - 1;
        paint.setColor(Color.BLACK);
        paint.setStyle(Paint.Style.STROKE);
        canvas.drawRect(rect, paint);

        float x1 = 0;
        float x2 = (float)getWidth();
        for (int i = 1; i < mColors.length; i++) {
            float y = (float)(i * mSize - 1);
            canvas.drawLine(x1, y, x2, y, paint);
        }

        rect.top = mSelected * mSize;
        rect.bottom = rect.top + mSize - 1;
        canvas.clipRect(rect, Region.Op.REPLACE);
        rect.top += mBorderWidth;
        rect.right -= mBorderWidth;
        rect.bottom -= mBorderWidth;
        rect.left += mBorderWidth;
        canvas.clipRect(rect, Region.Op.DIFFERENCE);
        rect.top = mSelected * mSize;
        rect.right = mSize - 1;
        rect.bottom = rect.top + mSize - 1;
        rect.left = 0;
        paint.setColor(mColors[mSelected].selectedColor);
        paint.setStyle(Paint.Style.FILL);
        canvas.drawRect(rect, paint);
    }

    private void initialize() {
        setFocusable(true);
        setFocusableInTouchMode(true);

        mColors = new PaletteColor[] {
            new PaletteColor(Color.BLACK, Color.WHITE),
            new PaletteColor(Color.BLUE),
            new PaletteColor(Color.CYAN),
            new PaletteColor(Color.DKGRAY),
            new PaletteColor(Color.GRAY),
            new PaletteColor(Color.GREEN),
            new PaletteColor(Color.LTGRAY),
            new PaletteColor(Color.MAGENTA),
            new PaletteColor(Color.RED),
            new PaletteColor(Color.WHITE),
            new PaletteColor(Color.YELLOW) };
        mSelected = 0;
    }
}

/**
 * vim: tabstop=4 shiftwidth=4 expandtab softtabstop=4
 */

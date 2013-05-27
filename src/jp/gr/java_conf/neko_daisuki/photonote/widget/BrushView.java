package jp.gr.java_conf.neko_daisuki.photonote.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

public class BrushView extends View {

    // documents
    private int[] mWidth;

    // drawing data
    private int mSize = 48;

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
        setMeasuredDimension(mSize, mSize * mWidth.length);
    }

    protected void onDraw(Canvas canvas) {
        // TODO
    }

    private void initialize() {
        setFocusable(true);
        setFocusableInTouchMode(true);

        mWidth = new int[] { 48 };
    }
}

/**
 * vim: tabstop=4 shiftwidth=4 expandtab softtabstop=4
 */

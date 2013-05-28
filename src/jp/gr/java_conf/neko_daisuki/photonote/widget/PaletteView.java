package jp.gr.java_conf.neko_daisuki.photonote.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Region;
import android.os.Parcel;
import android.os.Parcelable.Creator;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import jp.gr.java_conf.neko_daisuki.android.view.MotionEventDispatcher;

public class PaletteView extends View {

    public interface OnChangeListener {

        public void onChange(PaletteView view);
    }

    private static class FakeListener implements OnChangeListener {

        public void onChange(PaletteView view) {
            // Does nothing.
        }
    }

    private class DownProc implements MotionEventDispatcher.Proc {

        public boolean run(MotionEvent event) {
            int y = (int)event.getY();
            mSelected = y / mSize;
            fireListener();
            invalidate();
            return true;
        }
    }

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

    private static class SavedState extends BaseSavedState {

        private int mSelected;

        public SavedState(Parcel in) {
            super(in);
            mSelected = in.readInt();
        }

        public SavedState(Parcelable superState) {
            super(superState);
        }

        public void writeToParcel(Parcel dest, int flags) {
            super.writeToParcel(dest, flags);
            dest.writeInt(mSelected);
        }

        public int getSelected() {
            return mSelected;
        }

        public void setSelected(int selected) {
            mSelected = selected;
        }
    }

    public static final Creator CREATOR = new Creator() {

        public SavedState createFromParcel(Parcel source) {
            return new SavedState(source);
        }

        public SavedState[] newArray(int size) {
            return new SavedState[size];
        }
    };

    // document
    private PaletteColor[] mColors;
    private int mSelected;
    private OnChangeListener mListener;

    // drawing data
    private int mBorderWidth = 8;
    private int mSize = 64;

    // stateless helpers
    private MotionEventDispatcher mMotionDispatcher;

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

    public void setOnChangeListener(OnChangeListener listener) {
        mListener = listener != null ? listener : new FakeListener();
    }

    public boolean onTouchEvent(MotionEvent event) {
        return mMotionDispatcher.dispatch(event);
    }

    public int getSelectedColor() {
        return mColors[mSelected].color;
    }

    protected Parcelable onSaveInstanceState() {
        SavedState state = new SavedState(super.onSaveInstanceState());
        state.setSelected(mSelected);
        return state;
    }

    protected void onRestoreInstanceState(Parcelable state) {
        if (!(state instanceof SavedState)) {
            return;
        }
        SavedState savedState = (SavedState)state;
        super.onRestoreInstanceState(savedState.getSuperState());
        mSelected = savedState.getSelected();
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

        setOnChangeListener(null);
        mMotionDispatcher = new MotionEventDispatcher();
        mMotionDispatcher.setDownProc(new DownProc());
    }

    private void fireListener() {
        mListener.onChange(this);
    }
}

/**
 * vim: tabstop=4 shiftwidth=4 expandtab softtabstop=4
 */

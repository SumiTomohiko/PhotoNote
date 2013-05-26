package jp.gr.java_conf.neko_daisuki.photonote.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

public class PaintView extends View {

    public interface Adapter {

        public int getLineCount();
        public int getPointCount(int line);
        public PointF getPoint(int line, int n);
        public void beginPaint();
        public void addPoint(PointF point);
    }

    private Adapter mAdapter;

    public PaintView(Context context) {
        super(context);
        initialize();
    }

    public PaintView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initialize();
    }

    public PaintView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initialize();
    }

    public void setAdapter(Adapter adapter) {
        mAdapter = adapter;
    }

    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            mAdapter.beginPaint();
            invalidate();
            return true;
        }
        mAdapter.addPoint(new PointF(event.getX(), event.getY()));
        invalidate();
        return true;
    }

    protected void onDraw(Canvas canvas) {
        Paint paint = new Paint();
        paint.setColor(0xff000000);
        paint.setStrokeWidth(16f);
        paint.setStyle(Paint.Style.STROKE);

        int nLines = mAdapter.getLineCount();
        for (int i = 0; i < nLines; i++) {
            int nPoints = mAdapter.getPointCount(i);
            if (nPoints < 1) {
                continue;
            }

            Path path = new Path();
            PointF point = mAdapter.getPoint(i, 0);
            path.moveTo(point.x, point.y);
            for (int j = 1; j < nPoints; j++) {
                point = mAdapter.getPoint(i, j);
                path.lineTo(point.x, point.y);
            }

            canvas.drawPath(path, paint);
        }
    }

    private void initialize() {
        setFocusable(true);
        setFocusableInTouchMode(true);
    }
}

/**
 * vim: tabstop=4 shiftwidth=4 expandtab softtabstop=4
 */

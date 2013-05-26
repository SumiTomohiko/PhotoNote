package jp.gr.java_conf.neko_daisuki.photonote.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.util.AttributeSet;
import android.util.SparseArray;
import android.view.MotionEvent;
import android.view.View;

public class PaintView extends View {

    private interface MotionEventHandler {

        public void handle(PointF point);
    }

    private class ActionDownEventHandler implements MotionEventHandler {

        public void handle(PointF point) {
            mAdapter.startLine(mColor, mStrokeWidth, point);
        }
    }

    private class DefaultMotionEventHandler implements MotionEventHandler {

        public void handle(PointF point) {
            mAdapter.addPoint(point);
        }
    }

    public interface Adapter {

        public int getLineCount();
        public int getPointCount(int line);
        public int getLineColor(int line);
        public float getStrokeWidth(int line);
        public PointF getPoint(int line, int n);
        public void startLine(int color, float strokeWidth, PointF point);
        public void addPoint(PointF point);
    }

    // documents
    private Adapter mAdapter;

    // stateful helpers
    private Paint mPaint;
    private int mColor;
    private float mStrokeWidth;

    // stateless helpers
    private SparseArray<MotionEventHandler> mMotionEventHandlers;
    private MotionEventHandler mDefaultMotionEventHandler;

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

    public void setColor(int color) {
        mColor = color;
    }

    public void setStrokeWidth(float strokeWidth) {
        mStrokeWidth = strokeWidth;
    }

    public void setAdapter(Adapter adapter) {
        mAdapter = adapter;
    }

    public boolean onTouchEvent(MotionEvent event) {
        int action = event.getAction();
        MotionEventHandler h = mMotionEventHandlers.get(action);
        MotionEventHandler handler = h != null ? h : mDefaultMotionEventHandler;
        handler.handle(new PointF(event.getX(), event.getY()));
        invalidate();
        return true;
    }

    protected void onDraw(Canvas canvas) {
        int nLines = mAdapter.getLineCount();
        for (int i = 0; i < nLines; i++) {
            int nPoints = mAdapter.getPointCount(i);
            if (nPoints < 1) {
                continue;
            }

            mPaint.setColor(mAdapter.getLineColor(i));
            mPaint.setStrokeWidth(mAdapter.getStrokeWidth(i));

            Path path = new Path();
            PointF point = mAdapter.getPoint(i, 0);
            path.moveTo(point.x, point.y);
            for (int j = 1; j < nPoints; j++) {
                point = mAdapter.getPoint(i, j);
                path.lineTo(point.x, point.y);
            }

            canvas.drawPath(path, mPaint);
        }
    }

    private void initialize() {
        setFocusable(true);
        setFocusableInTouchMode(true);

        mColor = Color.BLACK;
        mStrokeWidth = 16f;

        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setStrokeCap(Paint.Cap.ROUND);
        mPaint.setStrokeJoin(Paint.Join.ROUND);
        mPaint.setStyle(Paint.Style.STROKE);

        mMotionEventHandlers = new SparseArray<MotionEventHandler>();
        mMotionEventHandlers.put(
                MotionEvent.ACTION_DOWN,
                new ActionDownEventHandler());
        mDefaultMotionEventHandler = new DefaultMotionEventHandler();
    }
}

/**
 * vim: tabstop=4 shiftwidth=4 expandtab softtabstop=4
 */

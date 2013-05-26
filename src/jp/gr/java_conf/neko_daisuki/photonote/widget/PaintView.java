package jp.gr.java_conf.neko_daisuki.photonote.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

public class PaintView extends View {

    public abstract static class Adapter {
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

    private void initialize() {
        setFocusable(true);
        setFocusableInTouchMode(true);
    }
}

/**
 * vim: tabstop=4 shiftwidth=4 expandtab softtabstop=4
 */

package jp.gr.java_conf.neko_daisuki.photonote;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.graphics.PointF;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;

import jp.gr.java_conf.neko_daisuki.photonote.widget.PaintView;

public class EditActivity extends Activity {

    public enum Extra {
        ORIGINAL_PATH,
        ADDITIONAL_PATH
    }

    private static class Line {

        private List<PointF> mPoints;
        private int mColor;
        private float mStrokeWidth;

        public Line(int color, float strokeWidth) {
            mPoints = new LinkedList<PointF>();
            mColor = color;
            mStrokeWidth = strokeWidth;
        }

        public int getColor() {
            return mColor;
        }

        public float getStrokeWidth() {
            return mStrokeWidth;
        }

        public List<PointF> getPoints() {
            return mPoints;
        }
    }

    private class Adapter implements PaintView.Adapter {

        private List<Line> mLines;

        public Adapter() {
            mLines = new LinkedList<Line>();
        }

        public int getLineCount() {
            return mLines.size();
        }

        public int getPointCount(int line) {
            return mLines.get(line).getPoints().size();
        }

        public PointF getPoint(int line, int n) {
            return mLines.get(line).getPoints().get(n);
        }

        public void startLine(int color, float strokeWidth, PointF point) {
            Line line = new Line(color, strokeWidth);
            line.getPoints().add(point);
            mLines.add(line);
        }

        public void addPoint(PointF point) {
            mLines.get(mLines.size() - 1).getPoints().add(point);
        }

        public float getStrokeWidth(int line) {
            return mLines.get(line).getStrokeWidth();
        }

        public int getLineColor(int line) {
            return mLines.get(line).getColor();
        }
    }

    private Adapter mAdapter;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.edit, menu);
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        setContentView(R.layout.activity_edit);

        Intent i = getIntent();
        setImage(R.id.original, i, Extra.ORIGINAL_PATH);

        mAdapter = new Adapter();
        PaintView view = (PaintView)findViewById(R.id.additional);
        view.setAdapter(mAdapter);
    }

    private void setImage(int view, Intent i, Extra key) {
        ImageView v = (ImageView)findViewById(view);
        v.setImageURI(Uri.fromFile(new File(i.getStringExtra(key.name()))));
    }
}

/**
 * vim: tabstop=4 shiftwidth=4 expandtab softtabstop=4
 */

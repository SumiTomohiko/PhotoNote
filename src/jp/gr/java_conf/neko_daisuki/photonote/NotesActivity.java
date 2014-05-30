package jp.gr.java_conf.neko_daisuki.photonote;

import java.io.File;
import java.io.IOException;
import java.util.List;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import jp.gr.java_conf.neko_daisuki.android.util.ActivityResultHandler;
import jp.gr.java_conf.neko_daisuki.android.util.ActivityUtil;
import jp.gr.java_conf.neko_daisuki.android.util.MenuHandler;
import jp.gr.java_conf.neko_daisuki.photonote.Database.Note;

public class NotesActivity extends ActionBarActivity
        implements NotesFragment.NotesFragmentListener {

    private class AddHandler implements MenuHandler.ItemHandler {

        @Override
        public void handle(MenuItem item) {
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            Uri uri = Uri.fromFile(new File(getTemporaryPath()));
            intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
            startActivityForResult(intent, REQUEST_ADD);
        }
    }

    private interface OnResumedHandler {

        public static class Nop implements OnResumedHandler {

            @Override
            public void onResumed() {
            }
        }

        public static final OnResumedHandler NOP = new Nop();

        public void onResumed();
    }

    private class AddResultHandler
            implements ActivityResultHandler.ResultHandler {

        private class Proc implements OnResumedHandler {

            @Override
            public void onResumed() {
                try {
                    Note note = mDatabase.addNote(mGroup, getTemporaryPath());
                    String fmt = "added a new note: group=%s, note=%s";
                    Log.i(LOG_TAG, String.format(fmt, mGroup, note.getKey()));
                }
                catch (IOException e) {
                    String msg = "Cannot add the new note";
                    ActivityUtil.showException(NotesActivity.this, msg, e);
                }
            }
        }

        @Override
        public void onActivityResult(int requestCode, int resultCode,
                                     Intent data) {
            mOnResumedHandler = new Proc();
        }
    }

    public static final String EXTRA_GROUP = "group";
    private static final int REQUEST_ADD = 0;
    private static final int REQUEST_EDIT = 1;
    private static final String LOG_TAG = "notes_activity";

    // documents
    private Database mDatabase;
    private Database.Group.Key mGroup;

    // helpers
    private MenuHandler mMenuHandler = new MenuHandler();
    private ActivityResultHandler mActivityResultHandler;
    private OnResumedHandler mOnResumedHandler = OnResumedHandler.NOP;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.notes, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        mMenuHandler.handle(item);
        return true;
    }

    @Override
    public List<Note> onRequestNotes(NotesFragment fragment,
                                     Database.Group.Key group) {
        return mDatabase.getNotes(group);
    }

    @Override
    protected void onPause() {
        super.onPause();
        try {
            mDatabase.write();
        }
        catch (IOException e) {
            ActivityUtil.showException(this, "Cannot write the database", e);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        try {
            mDatabase = Database.open();
            mOnResumedHandler.onResumed();
            invalidateViews();
        }
        catch (IOException e) {
            ActivityUtil.showException(this, "Cannot open the database", e);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notes);

        String key = getIntent().getStringExtra(EXTRA_GROUP);
        mGroup = new Database.Group.Key(key);

        mMenuHandler.put(R.id.action_add_note, new AddHandler());
        mActivityResultHandler = new ActivityResultHandler();
        mActivityResultHandler.put(REQUEST_ADD, new AddResultHandler());

        if (savedInstanceState == null) {
            Bundle args = new Bundle();
            args.putString(EXTRA_GROUP, key);
            Fragment fragment = new NotesFragment();
            fragment.setArguments(args);
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, fragment).commit();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode,
                                    Intent data) {
        mActivityResultHandler.handle(requestCode, resultCode, data);
    }

    private void invalidateViews() {
        FragmentManager manager = getSupportFragmentManager();
        int id = R.id.container;
        NotesFragment fragment = (NotesFragment)manager.findFragmentById(id);
        fragment.invalidateViews();
    }

    private String getTemporaryPath() {
        String directory = Application.getTemporaryDirectory();
        return String.format("%s/original.png", directory);
    }
}
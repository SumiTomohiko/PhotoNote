package jp.gr.java_conf.neko_daisuki.photonote;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.DialogFragment;
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
        implements NotesFragment.NotesFragmentListener,
                   RemoveNoteFragment.RemoveNoteFragmentListener {

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

    private class EditResultHandler
            implements ActivityResultHandler.ResultHandler {

        private class Proc implements OnResumedHandler {

            @Override
            public void onResumed() {
                String src = getTemporaryAdditionalPath();
                Database.Note note = mDatabase.getNote(mTargetNote);
                String dest = note.getAdditionalPath();
                try {
                    copyFile(dest, src);
                }
                catch (IOException e) {
                    String fmt = "failed to copy data.json from %s to %s";
                    String msg = String.format(fmt, src, dest);
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

    private Database.Note.Key mTargetNote;

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
    public void onRemoveNote(NotesFragment fragment, Database.Note note) {
        DialogFragment dialog = RemoveNoteFragment.newInstance(note);
        dialog.show(getSupportFragmentManager(), "remove_note");
    }

    @Override
    public List<Note> onRequestNotes(NotesFragment fragment,
                                     Database.Group.Key group) {
        return mDatabase.getNotes(group);
    }

    @Override
    public void onEditNote(NotesFragment fragment, Database.Note.Key note) {
        Database.Note n = mDatabase.getNote(note);

        String additionalPath = n.getAdditionalPath();
        String temporaryPath = getTemporaryAdditionalPath();
        try {
            copyFile(temporaryPath, additionalPath);
        }
        catch (IOException e) {
            String fmt = "failed to copy from %s to %s";
            String msg = String.format(fmt, temporaryPath, additionalPath);
            ActivityUtil.showException(this, msg, e);
            return;
        }

        mTargetNote = n.getKey();

        Intent i = new Intent(this, EditActivity.class);
        i.putExtra(EditActivity.Extra.ORIGINAL_PATH.name(),
                   n.getOriginalPath());
        i.putExtra(EditActivity.Extra.ADDITIONAL_PATH.name(), temporaryPath);
        startActivityForResult(i, REQUEST_EDIT);
    }

    @Override
    public void onRemoveNote(RemoveNoteFragment fragment,
                             Database.Note.Key note) {
        mDatabase.removeNote(note);
        invalidateViews();
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
        mActivityResultHandler.put(REQUEST_EDIT, new EditResultHandler());

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

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        String key = mTargetNote != null ? mTargetNote.toString() : null;
        outState.putString("note", key);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        String key = savedInstanceState.getString("note");
        mTargetNote = key != null ? new Database.Note.Key(key) : null;
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

    private String getTemporaryPath(String name) {
        String parent = Application.getTemporaryDirectory();
        return String.format("%s/%s", parent, name);
    }

    private String getTemporaryAdditionalPath() {
        return getTemporaryPath("data.json");
    }

    private void copyFile(String dest, String src) throws IOException {
        OutputStream out = new FileOutputStream(dest);
        try {
            InputStream in = new FileInputStream(src);
            try {
                byte[] buf = new byte[8192];
                while (0 < in.available()) {
                    int len = in.read(buf);
                    out.write(buf, 0, len);
                }
            }
            finally {
                in.close();
            }
        }
        finally {
            out.close();
        }
    }
}
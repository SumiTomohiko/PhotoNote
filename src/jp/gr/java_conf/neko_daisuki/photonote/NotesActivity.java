package jp.gr.java_conf.neko_daisuki.photonote;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.LinkedList;
import java.util.List;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import jp.gr.java_conf.neko_daisuki.android.util.ActivityUtil;

public class NotesActivity extends ActionBarActivity {

    public static class PlaceholderFragment extends Fragment {

        private class Adapter extends BaseAdapter {

            private class OnClickListener implements View.OnClickListener {

                private Database.Note mNote;

                public OnClickListener(Database.Note note) {
                    mNote = note;
                }

                @Override
                public void onClick(View v) {
                    String additionalPath = mNote.getAdditionalPath();
                    String temporaryPath = getTemporaryAdditionalPath();
                    try {
                        copyFile(temporaryPath, additionalPath);
                    }
                    catch (IOException e) {
                        String fmt = "failed to copy from %s to %s";
                        String msg = String.format(fmt, temporaryPath,
                                                   additionalPath);
                        ActivityUtil.showException(getActivity(), msg, e);
                        return;
                    }

                    //mTargetEntryName = entry.getName();

                    Intent i = new Intent(getActivity(), EditActivity.class);
                    i.putExtra(EditActivity.Extra.ORIGINAL_PATH.name(),
                               mNote.getOriginalPath());
                    i.putExtra(EditActivity.Extra.ADDITIONAL_PATH.name(),
                               temporaryPath);
                    startActivityForResult(i, REQUEST_EDIT);
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

            private List<Database.Note> mNotes;

            public Adapter() {
                mNotes = new LinkedList<Database.Note>();
            }

            @Override
            public void notifyDataSetChanged() {
                super.notifyDataSetChanged();
                mNotes = mDatabase.getNotes(mGroup);
            }

            @Override
            public int getCount() {
                return mNotes.size();
            }

            @Override
            public Object getItem(int position) {
                return null;
            }

            @Override
            public long getItemId(int position) {
                return 0;
            }

            @Override
            public View getView(int position, View convertView,
                                ViewGroup parent) {
                TextView view = new TextView(getActivity());
                Database.Note note = mNotes.get(position);
                view.setOnClickListener(new OnClickListener(note));
                view.setText(note.getName());
                return view;
            }
        }

        // documents
        private Database mDatabase;
        private Database.Group.Key mGroup;

        // views
        private BaseAdapter mAdapter;

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            String s = getArguments().getString(EXTRA_GROUP);
            mGroup = new Database.Group.Key(s);
        }

        @Override
        public void onPause() {
            super.onPause();
            try {
                mDatabase.write();
            }
            catch (IOException e) {
                String msg = "Cannot save the database";
                ActivityUtil.showException(getActivity(), msg, e);
            }
        }

        @Override
        public void onResume() {
            super.onResume();
            try {
                mDatabase = Database.open();
            }
            catch (IOException e) {
                String msg = "Cannot load the database";
                ActivityUtil.showException(getActivity(), msg, e);
            }
            mAdapter.notifyDataSetChanged();
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_notes, container,
                                             false);
            ListView list = (ListView)rootView.findViewById(R.id.notes_list);
            mAdapter = new Adapter();
            list.setAdapter(mAdapter);
            return rootView;
        }
    }

    public static final String EXTRA_GROUP = "group";
    private static final int REQUEST_EDIT = 0;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.notes, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //int id = item.getItemId();
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notes);

        if (savedInstanceState == null) {
            String key = getIntent().getStringExtra(EXTRA_GROUP);
            Bundle args = new Bundle();
            args.putString(EXTRA_GROUP, key);
            Fragment fragment = new PlaceholderFragment();
            fragment.setArguments(args);

            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, fragment).commit();
        }
    }
}
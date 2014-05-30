package jp.gr.java_conf.neko_daisuki.photonote;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.LinkedList;
import java.util.List;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

public class NotesFragment extends Fragment {

    public interface NotesFragmentListener {

        public List<Database.Note> onRequestNotes(NotesFragment fragment,
                                                  Database.Group.Key group);
    }

    private class Adapter extends BaseAdapter {

        private class OnClickListener implements View.OnClickListener {

            private Database.Note mNote;

            public OnClickListener(Database.Note note) {
                mNote = note;
            }

            @Override
            public void onClick(View v) {
                /*
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
                startActivityForResult(i, NotesActivity.REQUEST_EDIT);
                */
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
            mNotes = mListener.onRequestNotes(NotesFragment.this, mGroup);
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
            View view = convertView != null ? convertView : makeView();

            Database.Note note = mNotes.get(position);
            ImageView thumbView = (ImageView)view.findViewById(R.id.thumbnail_image);
            String thumbnailPath = note.getThumbnailPath();
            thumbView.setImageURI(Uri.fromFile(new File(thumbnailPath)));
            TextView nameText = (TextView)view.findViewById(R.id.name_text);
            nameText.setOnClickListener(new OnClickListener(note));
            nameText.setText(note.getName());

            return view;
        }

        private View makeView() {
            LayoutInflater inflater = getActivity().getLayoutInflater();
            return inflater.inflate(R.layout.row_note, null);
        }
    }

    // documents
    private Database.Group.Key mGroup;

    // views
    private BaseAdapter mAdapter;

    private NotesFragmentListener mListener;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mListener = (NotesFragmentListener)activity;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String s = getArguments().getString(NotesActivity.EXTRA_GROUP);
        mGroup = new Database.Group.Key(s);
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

    public void invalidateViews() {
        mAdapter.notifyDataSetChanged();
    }
}
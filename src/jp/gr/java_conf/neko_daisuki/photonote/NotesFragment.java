package jp.gr.java_conf.neko_daisuki.photonote;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

public class NotesFragment extends Fragment {

    public interface NotesFragmentListener {

        public List<Database.Note> onRequestNotes(NotesFragment fragment,
                                                  Database.Group.Key group);
        public void onEditNote(NotesFragment fragment, Database.Note.Key note);
        public void onRemoveNote(NotesFragment fragment, Database.Note note);
    }

    private class Adapter extends BaseAdapter {

        private class RemoveButtonOnClickListener implements View.OnClickListener {

            private Database.Note mNote;

            public RemoveButtonOnClickListener(Database.Note note) {
                mNote = note;
            }

            @Override
            public void onClick(View v) {
                mListener.onRemoveNote(NotesFragment.this, mNote);
            }
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
            nameText.setText(note.getName());
            View removeButton = view.findViewById(R.id.delete_button);
            removeButton.setOnClickListener(new RemoveButtonOnClickListener(note));

            return view;
        }

        private View makeView() {
            LayoutInflater inflater = getActivity().getLayoutInflater();
            return inflater.inflate(R.layout.row_note, null);
        }
    }

    private class OnItemClickListener implements AdapterView.OnItemClickListener {

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position,
                                long id) {
            Database.Note note = mNotes.get(position);
            mListener.onEditNote(NotesFragment.this, note.getKey());
        }
    }

    // documents
    private Database.Group.Key mGroup;
    private List<Database.Note> mNotes = new LinkedList<Database.Note>();

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
        ListView list = (ListView)rootView;
        list.setOnItemClickListener(new OnItemClickListener());
        mAdapter = new Adapter();
        list.setAdapter(mAdapter);
        return rootView;
    }

    public void invalidateViews() {
        mNotes = mListener.onRequestNotes(NotesFragment.this, mGroup);
        mAdapter.notifyDataSetChanged();
    }
}
package jp.gr.java_conf.neko_daisuki.photonote;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

public class RemoveNoteFragment extends DialogFragment {

    public interface RemoveNoteFragmentListener {

        public void onRemoveNote(RemoveNoteFragment fragment,
                                 Database.Note.Key note);
    }

    private class OnClickListener implements DialogInterface.OnClickListener {

        private Database.Note.Key mNote;

        public OnClickListener(Database.Note.Key note) {
            mNote = note;
        }

        @Override
        public void onClick(DialogInterface dialog, int which) {
            mListener.onRemoveNote(RemoveNoteFragment.this, mNote);
        }
    }

    private static final String KEY_KEY = "key";
    private static final String KEY_NAME = "name";

    private RemoveNoteFragmentListener mListener;

    public static DialogFragment newInstance(Database.Note note) {
        RemoveNoteFragment fragment = new RemoveNoteFragment();
        Bundle args = new Bundle();
        args.putString(KEY_KEY, note.getKey().toString());
        args.putString(KEY_NAME, note.getName());
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mListener = (RemoveNoteFragmentListener)activity;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        Resources res = getResources();
        String fmt = res.getString(R.string.delete_entry_dialog_message);
        String positive = res.getString(R.string.positive);
        String negative = res.getString(R.string.negative);
        Bundle args = getArguments();
        String name = args.getString(KEY_NAME);
        Database.Note.Key key = new Database.Note.Key(args.getString(KEY_KEY));

        builder.setMessage(String.format(fmt, name, positive, negative));
        builder.setPositiveButton(positive, new OnClickListener(key));
        builder.setNegativeButton(negative, null);

        return builder.create();
    }
}
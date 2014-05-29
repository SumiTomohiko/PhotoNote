package jp.gr.java_conf.neko_daisuki.photonote;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

public class DeleteGroupFragment extends DialogFragment {

    public interface OnDeleteGroupListener {

        public void onDeleteGroup(DeleteGroupFragment fragment,
                                  Database.Group.Key group);
    }

    private class OnPositiveListener implements DialogInterface.OnClickListener {

        private Database.Group.Key mGroup;

        public OnPositiveListener(Database.Group.Key group) {
            mGroup = group;
        }

        @Override
        public void onClick(DialogInterface dialog, int which) {
            mListener.onDeleteGroup(DeleteGroupFragment.this, mGroup);
        }
    }

    private static final String KEY_KEY = "key";
    private static final String KEY_NAME = "name";

    private OnDeleteGroupListener mListener;

    public static DialogFragment newInstance(Database.Group group) {
        DialogFragment fragment = new DeleteGroupFragment();
        Bundle args = new Bundle();
        args.putString(KEY_KEY, group.getKey().toString());
        args.putString(KEY_NAME, group.getName());
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mListener = (OnDeleteGroupListener)activity;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        Resources res = getResources();
        String fmt = res.getString(R.string.delete_group_dialog_message);
        String positive = res.getString(R.string.positive);
        String negative = res.getString(R.string.negative);

        Bundle args = getArguments();
        String name = args.getString(KEY_NAME);
        builder.setMessage(String.format(fmt, name, positive, negative));
        Database.Group.Key key = new Database.Group.Key(args.getString(KEY_KEY));
        builder.setPositiveButton(positive, new OnPositiveListener(key));
        builder.setNegativeButton(negative, null);

        return builder.create();
    }
}
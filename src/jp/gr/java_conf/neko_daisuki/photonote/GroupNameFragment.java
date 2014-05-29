package jp.gr.java_conf.neko_daisuki.photonote;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

public class GroupNameFragment extends DialogFragment {

    public interface OnNameGivenListener {

        public void onNameGiven(GroupNameFragment fragment, String name);
    }

    private class OnPositiveListener implements DialogInterface.OnClickListener {

        private EditText mEdit;

        public OnPositiveListener(EditText edit) {
            mEdit = edit;
        }

        @Override
        public void onClick(DialogInterface dialog, int which) {
            String name = mEdit.getEditableText().toString();
            mListener.onNameGiven(GroupNameFragment.this, name);
        }
    }

    private OnNameGivenListener mListener;

    public static DialogFragment newInstance() {
        return new GroupNameFragment();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mListener = (OnNameGivenListener)activity;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_group_name, null);
        EditText edit = (EditText)view.findViewById(R.id.name);

        Resources res = getResources();
        String positive = res.getString(R.string.positive);
        String negative = res.getString(R.string.negative);

        builder.setView(view);
        builder.setTitle("Add a new group");
        builder.setPositiveButton(positive, new OnPositiveListener(edit));
        builder.setNegativeButton(negative, null);

        return builder.create();
    }
}
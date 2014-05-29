package jp.gr.java_conf.neko_daisuki.photonote;

import java.util.LinkedList;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class GroupsFragment extends Fragment {

    public interface OnRequestGroupsListener {

        public List<Database.Group> onRequestGroup();
    }

    private class Adapter extends BaseAdapter {

        private class OnClickListener implements View.OnClickListener {

            private Database.Group.Key mGroup;

            public OnClickListener(Database.Group.Key group) {
                mGroup = group;
            }

            @Override
            public void onClick(View v) {
                Context context = getActivity();
                Intent intent = new Intent(context, NotesActivity.class);
                intent.putExtra(NotesActivity.EXTRA_GROUP,
                                mGroup.toString());
                context.startActivity(intent);
            }
        }

        private List<Database.Group> mGroups;

        public Adapter() {
            mGroups = new LinkedList<Database.Group>();
        }

        @Override
        public void notifyDataSetChanged() {
            super.notifyDataSetChanged();
            mGroups = mListener.onRequestGroup();
        }

        @Override
        public int getCount() {
            return mGroups.size();
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
            Database.Group group = mGroups.get(position);
            TextView view = new TextView(getActivity());
            view.setText(group.getName());
            view.setOnClickListener(new OnClickListener(group.getKey()));
            return view;
        }
    }

    private OnRequestGroupsListener mListener;

    // views
    private BaseAdapter mAdapter;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mListener = (OnRequestGroupsListener)activity;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_groups,
                                         container, false);

        ListView list = (ListView)rootView.findViewById(R.id.groups_list);
        mAdapter = new Adapter();
        list.setAdapter(mAdapter);

        return rootView;
    }

    public void invalidate() {
        mAdapter.notifyDataSetChanged();
    }
}
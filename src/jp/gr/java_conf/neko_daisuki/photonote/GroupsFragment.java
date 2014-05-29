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

    public interface GroupsFragmentListener {

        public List<Database.Group> onRequestGroup(GroupsFragment fragment);
        public void onRemoveGroup(GroupsFragment fragment,
                                  Database.Group.Key group);
    }

    private class Adapter extends BaseAdapter {

        private class GroupListener {

            private Database.Group.Key mGroup;

            public GroupListener(Database.Group.Key group) {
                mGroup = group;
            }

            protected Database.Group.Key getGroup() {
                return mGroup;
            }
        }

        private class RemoveButtonListener extends GroupListener implements View.OnClickListener {

            public RemoveButtonListener(Database.Group.Key group) {
                super(group);
            }

            @Override
            public void onClick(View v) {
                mListener.onRemoveGroup(GroupsFragment.this, getGroup());
            }
        }

        private class NameTextListener extends GroupListener implements View.OnClickListener {

            public NameTextListener(Database.Group.Key group) {
                super(group);
            }

            @Override
            public void onClick(View v) {
                Context context = getActivity();
                Intent intent = new Intent(context, NotesActivity.class);
                intent.putExtra(NotesActivity.EXTRA_GROUP,
                                getGroup().toString());
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
            mGroups = mListener.onRequestGroup(GroupsFragment.this);
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
            if (convertView == null) {
                return getView(position, makeView(position), parent);
            }

            Database.Group group = mGroups.get(position);
            Database.Group.Key key = group.getKey();
            TextView text = (TextView)convertView.findViewById(R.id.name_text);
            text.setText(group.getName());
            text.setOnClickListener(new NameTextListener(key));
            View button = convertView.findViewById(R.id.remove_button);
            button.setOnClickListener(new RemoveButtonListener(key));

            return convertView;
        }

        private View makeView(int position) {
            LayoutInflater inflater = getActivity().getLayoutInflater();
            return inflater.inflate(R.layout.row_group, null);
        }
    }

    private GroupsFragmentListener mListener;

    // views
    private BaseAdapter mAdapter;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mListener = (GroupsFragmentListener)activity;
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
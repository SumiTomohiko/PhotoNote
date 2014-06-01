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
import android.widget.AdapterView;
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

        private class RemoveButtonListener implements View.OnClickListener {

            private Database.Group.Key mGroup;

            public RemoveButtonListener(Database.Group.Key group) {
                mGroup = group;
            }

            @Override
            public void onClick(View v) {
                mListener.onRemoveGroup(GroupsFragment.this, mGroup);
            }
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
            View view = convertView != null ? convertView : makeView();

            Database.Group group = mGroups.get(position);
            Database.Group.Key key = group.getKey();
            TextView text = (TextView)view.findViewById(R.id.name_text);
            text.setText(group.getName());
            View button = view.findViewById(R.id.remove_button);
            button.setOnClickListener(new RemoveButtonListener(key));

            return view;
        }

        private View makeView() {
            LayoutInflater inflater = getActivity().getLayoutInflater();
            return inflater.inflate(R.layout.row_group, null);
        }
    }

    private class OnItemClickListener implements AdapterView.OnItemClickListener {

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position,
                                long id) {
            Context context = getActivity();
            Intent intent = new Intent(context, NotesActivity.class);
            intent.putExtra(NotesActivity.EXTRA_GROUP,
                            mGroups.get(position).getKey().toString());
            context.startActivity(intent);
        }
    }

    private GroupsFragmentListener mListener;
    private List<Database.Group> mGroups = new LinkedList<Database.Group>();

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

        ListView list = (ListView)rootView;
        list.setOnItemClickListener(new OnItemClickListener());
        mAdapter = new Adapter();
        list.setAdapter(mAdapter);

        return rootView;
    }

    public void invalidate() {
        mGroups = mListener.onRequestGroup(GroupsFragment.this);
        mAdapter.notifyDataSetChanged();
    }
}
package jp.gr.java_conf.neko_daisuki.photonote;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import android.content.Context;
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

public class GroupsActivity extends ActionBarActivity {

    public static class PlaceholderFragment extends Fragment {

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
                mGroups = mDatabase.getGroups();
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

        // documents
        private Database mDatabase;

        // views
        private BaseAdapter mAdapter;

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
                mDatabase = Database.newInstance();
            }
            mAdapter.notifyDataSetChanged();
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
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.groups, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_groups);

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new PlaceholderFragment()).commit();
        }
    }
}
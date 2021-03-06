package jp.gr.java_conf.neko_daisuki.photonote;

import java.io.IOException;
import java.util.List;

import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import jp.gr.java_conf.neko_daisuki.android.util.ActivityUtil;
import jp.gr.java_conf.neko_daisuki.android.util.MenuHandler;
import jp.gr.java_conf.neko_daisuki.photonote.Database.Group;
import jp.gr.java_conf.neko_daisuki.photonote.Database.Group.Key;

public class GroupsActivity extends ActionBarActivity
        implements DeleteGroupFragment.OnDeleteGroupListener,
                   GroupNameFragment.OnNameGivenListener,
                   GroupsFragment.GroupsFragmentListener {

    private class AddHandler implements MenuHandler.ItemHandler {

        @Override
        public void handle(MenuItem item) {
            GroupNameFragment.newInstance().show(getSupportFragmentManager(),
                                                 "group name");
        }
    }

    private static final String LOG_TAG = "groups_activity";

    private Database mDatabase;
    private MenuHandler mMenuHandler = new MenuHandler();

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.groups, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        mMenuHandler.handle(item);
        return true;
    }

    @Override
    public void onPause() {
        super.onPause();
        try {
            mDatabase.write();
        }
        catch (IOException e) {
            String msg = "Cannot save the database";
            ActivityUtil.showException(this, msg, e);
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        try {
            Application.initialize();
            mDatabase = Database.open();
            invalidateViews();
        }
        catch (IOException e) {
            ActivityUtil.showException(this, "Cannot open the database", e);
        }
    }

    @Override
    public void onNameGiven(GroupNameFragment fragment, String name) {
        if (name.length() == 0) {
            ActivityUtil.showToast(this, "You gave empty group name. No groups are added.");
            return;
        }
        Log.i(LOG_TAG, String.format("Added a new group: %s", name));
        mDatabase.addGroup(name);
        invalidateViews();
    }

    @Override
    public List<Group> onRequestGroup(GroupsFragment fragment) {
        return mDatabase.getGroups();
    }

    @Override
    public void onDeleteGroup(DeleteGroupFragment fragment, Key group) {
        mDatabase.removeGroup(group);
        invalidateViews();
    }

    @Override
    public void onRemoveGroup(GroupsFragment fragment, Database.Group.Key group) {
        Database.Group g = mDatabase.getGroup(group);
        DialogFragment dialog = DeleteGroupFragment.newInstance(g);
        dialog.show(getSupportFragmentManager(), "remove group");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_groups);

        mMenuHandler.put(R.id.action_add_group, new AddHandler());

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new GroupsFragment()).commit();
        }
    }

    private void invalidateViews() {
        FragmentManager manager = getSupportFragmentManager();
        int id = R.id.container;
        GroupsFragment fragment = (GroupsFragment)manager.findFragmentById(id);
        fragment.invalidate();
    }
}
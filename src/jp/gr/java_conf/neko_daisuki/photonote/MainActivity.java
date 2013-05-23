package jp.gr.java_conf.neko_daisuki.photonote;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;

public class MainActivity extends Activity {

    private static class Entry {

        public String name;

        public Entry(String name) {
            this.name = name;
        }
    }

    private static class Group {

        public List<Entry> entries;

        public Group() {
            this.entries = new ArrayList<Entry>();
        }
    }

    private static final String LOG_TAG = "photonote";

    private List<Group> mGroups;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        makeDirectories();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    private void makeDirectories() {
        String[] directories = new String[] {
            getDataDirectory(),
            getEntriesDirectory(),
            getGroupsDirectory() };
        for (String directory: directories) {
            File file = new File(directory);
            if (file.exists()) {
                continue;
            }
            if (file.mkdir()) {
                Log.i(LOG_TAG, String.format("make directory: %s", directory));
                continue;
            }
            Log.e(LOG_TAG, String.format("failed to mkdir: %s", directory));
        }
    }

    protected void onResume() {
        super.onResume();

        mGroups = readGroups(readEntries());
    }

    private String getDataDirectory() {
        return "/mnt/sdcard/.photonote";
    }

    private String getEntriesDirectory() {
        return String.format("%s/entries", getDataDirectory());
    }

    private String getGroupsDirectory() {
        return String.format("%s/groups", getDataDirectory());
    }

    private Map<String, Entry> readEntries() {
        Map<String, Entry> entries = new HashMap<String, Entry>();

        for (String name: new File(getEntriesDirectory()).list()) {
            entries.put(name, new Entry(name));
        }

        return entries;
    }

    private List<Group> readGroups(Map<String, Entry> entries) {
        List<Group> groups = new ArrayList<Group>();

        for (File file: new File(getGroupsDirectory()).listFiles()) {
            String filePath = file.getAbsolutePath();
            try {
                groups.add(readGroup(filePath, entries));
            }
            catch (IOException e) {
                String message = String.format("failed to open %s.", filePath);
                Log.e(LOG_TAG, message);
            }
        }

        return groups;
    }

    private Group readGroup(String path, Map<String, Entry> entries) throws IOException {
        Group group = new Group();

        BufferedReader in = new BufferedReader(new FileReader(path));
        String name;
        while ((name = in.readLine()) != null) {
            group.entries.add(entries.get(name));
        }

        return group;
    }
}

/**
 * vim: tabstop=4 shiftwidth=4 expandtab softtabstop=4
 */

package jp.gr.java_conf.neko_daisuki.photonote;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.View.OnClickListener;
import android.view.View;
import android.widget.ImageView;

public class MainActivity extends Activity {

    private class ShotButtonOnClickListener implements OnClickListener {

        public void onClick(View view) {
            shot();
        }
    }

    private class Entry {

        public String name;

        public Entry(String name) {
            this.name = name;
        }

        public String getDirectory() {
            return String.format("%s/%s", getEntriesDirectory(), name);
        }

        public String getOriginalPath() {
            return String.format("%s/original.png", getDirectory());
        }
    }

    private static class Group {

        public List<Entry> entries;

        public Group() {
            this.entries = new ArrayList<Entry>();
        }
    }

    private static final String LOG_TAG = "photonote";
    private static final int REQUEST_CAPTURE = 42;

    private List<Group> mGroups;

    private SimpleDateFormat mDateFormat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        View shotButton = findViewById(R.id.shot_button);
        shotButton.setOnClickListener(new ShotButtonOnClickListener());

        setupFileTree();

        mDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    private void setupFileTree() {
        makeDirectories();
        makeDefaultGroup();
    }

    private void makeDefaultGroup() {
        boolean done;
        String path = String.format("%s/default", getGroupsDirectory());
        try {
            done = new File(path).createNewFile();
        }
        catch (IOException e) {
            String fmt = "failed to open %s: %s";
            Log.e(LOG_TAG, String.format(fmt, path, e.getMessage()));
            return;
        }
        if (done) {
            Log.i(LOG_TAG, String.format("created: %s", path));
        }
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

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if ((requestCode != REQUEST_CAPTURE) || (resultCode != RESULT_OK)) {
            return;
        }

        Entry entry = new Entry(makeNewEntryName());
        new File(entry.getDirectory()).mkdir();

        Bitmap bitmap = (Bitmap)data.getExtras().get("data");
        String path = entry.getOriginalPath();
        OutputStream out;
        try {
            out = new FileOutputStream(path);
        }
        catch (FileNotFoundException e) {
            String fmt = "failed to open %s: %s";
            Log.e(LOG_TAG, String.format(fmt, path, e.getMessage()));
            return;
        }
        try {
            try {
                bitmap.compress(CompressFormat.PNG, 100, out);
            }
            finally {
                out.close();
            }
        }
        catch (IOException e) {
            String fmt = "failed to close %s: %s";
            Log.e(LOG_TAG, String.format(fmt, path, e.getMessage()));
            return;
        }

        Log.i(LOG_TAG, String.format("added %s.", path));
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

    private void shot() {
        Intent i = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(i, REQUEST_CAPTURE);
    }

    private String makeNewEntryName() {
        return mDateFormat.format(new Date());
    }
}

/**
 * vim: tabstop=4 shiftwidth=4 expandtab softtabstop=4
 */

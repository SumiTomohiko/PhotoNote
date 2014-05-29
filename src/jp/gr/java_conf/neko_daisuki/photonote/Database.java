package jp.gr.java_conf.neko_daisuki.photonote;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import android.util.Log;

public class Database {

    public static class Note {

        public static class Key extends BaseKey {

            public Key(String key) {
                super(key);
            }
        }

        private static class NameComparator implements Comparator<Note> {

            @Override
            public int compare(Note lhs, Note rhs) {
                return lhs.getName().compareTo(rhs.getName());
            }
        }

        private static final Comparator<Note> NAME_COMPARATOR = new NameComparator();

        private Key mKey;

        private Note(String name) {
            mKey = new Key(name);
        }

        public Key getKey() {
            return mKey;
        }

        public String getName() {
            return mKey.toString();
        }

        public String getOriginalPath() {
            return getPath("original.png");
        }

        public String getAdditionalPath() {
            return getPath("additional.json");
        }

        public String getThumbnailPath() {
            return getPath("thumbnail.png");
        }

        private String getPath(String name) {
            return String.format("%s/%s", getDirectory(), name);
        }

        private String getDirectory() {
            String dir = Application.getDataDirectory();
            String key = mKey.toString();
            return String.format("%s/%s", getEntriesDirectory(dir), key);
        }
    }

    public static class Group {

        public static class Key extends BaseKey {

            public Key(String key) {
                super(key);
            }
        }

        private static class NameComparator implements Comparator<Group> {

            @Override
            public int compare(Group lhs, Group rhs) {
                return lhs.getName().compareTo(rhs.getName());
            }
        }

        private static final Comparator<Group> NAME_COMPARATOR = new NameComparator();

        private Database mDatabase;
        private Key mKey;
        private Collection<Note.Key> mNotes = new ArrayList<Note.Key>();

        private Group(Database database, String name) {
            mDatabase = database;
            mKey = new Key(name);
        }

        public void addNote(Note.Key key) {
            mNotes.add(key);
        }

        public Key getKey() {
            return mKey;
        }

        public String getName() {
            return mKey.toString();
        }

        public Collection<Note> getNotes() {
            Collection<Note> c = new ArrayList<Note>();
            for (Note.Key e: mNotes) {
                c.add(mDatabase.mNotes.get(e));
            }
            return c;
        }
    }

    private static class Notes implements Iterable<Note> {

        public Map<Note.Key, Note> mNotes = new HashMap<Note.Key, Note>();

        private Notes() {
        }

        public void add(Note note) {
            mNotes.put(note.getKey(), note);
        }

        public Note get(Note.Key key) {
            return mNotes.get(key);
        }

        @Override
        public Iterator<Note> iterator() {
            return mNotes.values().iterator();
        }
    }

    private static class Groups implements Iterable<Group> {

        private Map<Group.Key, Group> mGroups = new HashMap<Group.Key, Group>();

        private Groups() {
        }

        public void add(Group group) {
            mGroups.put(group.getKey(), group);
        }

        public Group get(Group.Key key) {
            return mGroups.get(key);
        }

        @Override
        public Iterator<Group> iterator() {
            return mGroups.values().iterator();
        }
    }

    private static class BaseKey {

        private String mKey;

        public BaseKey(String key) {
            mKey = key;
        }

        @Override
        public boolean equals(Object o) {
            BaseKey key;
            try {
                key = (BaseKey)o;
            }
            catch (ClassCastException e) {
                return false;
            }
            return mKey.equals(key.toString());
        }

        @Override
        public int hashCode() {
            return mKey.hashCode();
        }

        @Override
        public String toString() {
            return mKey;
        }
    }

    private static final String LOG_TAG = "database";

    private Groups mGroups;
    private Notes mNotes;

    public static Database newInstance() {
        Database database = new Database();
        database.empty();
        return database;
    }

    public static Database open() throws IOException {
        Database database = new Database();
        database.read();
        return database;
    }

    public void addGroup(String name) {
        mGroups.add(new Group(this, name));
    }

    public List<Note> getNotes(Group.Key group) {
        List<Note> l = new ArrayList<Note>();
        for (Note e: mGroups.get(group).getNotes()) {
            l.add(e);
        }
        Collections.sort(l, Note.NAME_COMPARATOR);
        return l;
    }

    public List<Group> getGroups() {
        List<Group> l = new ArrayList<Group>();
        for (Group e: mGroups) {
            l.add(e);
        }
        Collections.sort(l, Group.NAME_COMPARATOR);
        return l;
    }

    public void write() throws IOException {
        String directory = Application.getDataDirectory();
        makeDirectories(directory);

        for (Group group: mGroups) {
            String path = getGroupDirectory(directory, group);
            OutputStream out = new FileOutputStream(path);
            try {
                PrintWriter writer = new PrintWriter(out);
                try {
                    for (Note e: group.getNotes()) {
                        writer.println(e.getKey().toString());
                    }
                }
                finally {
                    writer.close();
                }
            }
            finally {
                out.close();
            }
        }
    }

    private static String getEntriesDirectory(String directory) {
        return String.format("%s/entries", directory);
    }

    private static String getGroupsDirectory(String directory) {
        return String.format("%s/groups", directory);
    }

    private static String getGroupDirectory(String directory, Group group) {
        String name = group.getName();
        return String.format("%s/%s", getGroupsDirectory(directory), name);
    }

    private Group readGroup(String directory, String name) throws IOException {
        Group group = new Group(this, name);

        Reader reader = new FileReader(getGroupDirectory(directory, group));
        try {
            BufferedReader in = new BufferedReader(reader);
            try {
                String noteKey;
                while ((noteKey = in.readLine()) != null) {
                    group.addNote(new Note.Key(noteKey));
                }
            }
            finally {
                in.close();
            }
        }
        finally {
            reader.close();
        }

        return group;
    }

    private void initialize(Groups groups, Notes notes) {
        mGroups = groups;
        mNotes = notes;
    }

    private void makeDirectories(String directory) {
        String[] directories = new String[] {
                directory,
            //getEntriesDirectory(),
                getGroupsDirectory(directory),
            //getTemporaryDirectory()
            };
        for (String d: directories) {
            File file = new File(d);
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

    private void empty() {
        initialize(new Groups(), new Notes());
    }

    private void read() throws IOException {
        String directory = Application.getDataDirectory();

        Notes notes = new Notes();
        for (String name: new File(getEntriesDirectory(directory)).list()) {
            notes.add(new Note(name));
        }
        Groups groups = new Groups();
        for (File file: new File(getGroupsDirectory(directory)).listFiles()) {
            groups.add(readGroup(directory, file.getName()));
        }

        initialize(groups, notes);
    }
}
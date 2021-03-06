package jp.gr.java_conf.neko_daisuki.photonote;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.Reader;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.util.Log;

public class Database {

    public static class Note {

        public static class Key extends BaseKey {

            private static final DateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ROOT);

            public Key(String key) {
                super(key);
            }

            public Key() {
                this(DATE_FORMAT.format(new Date()));
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
            this(new Key(name));
        }

        private Note() {
            this(new Key());
        }

        private Note(Key key) {
            mKey = key;
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

        public void removeNote(Note.Key note) {
            mNotes.remove(note);
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

        public void remove(Note.Key note) {
            mNotes.remove(note);
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

        public void remove(Group.Key key) {
            mGroups.remove(key);
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

    private static class Size {

        public int width;
        public int height;
    }

    private static final String LOG_TAG = "database";

    private Groups mGroups;
    private Notes mNotes;

    public static Database open() throws IOException {
        Database.makeDirectories();
        Database database = new Database();
        database.read();
        return database;
    }

    public void addGroup(String name) {
        mGroups.add(new Group(this, name));
    }

    public Note getNote(Note.Key key) {
        return mNotes.get(key);
    }

    public Group getGroup(Group.Key key) {
        return mGroups.get(key);
    }

    public void removeNote(Note.Key note) {
        for (Group group: mGroups) {
            group.removeNote(note);
        }
        mNotes.remove(note);
    }

    public void removeGroup(Group.Key group) {
        Group g = getGroup(group);
        for (Note note: g.getNotes()) {
            deleteDirectory(note.getDirectory());
        }
        deleteFile(new File(getGroupDirectory(Application.getDataDirectory(), g)));
        mGroups.remove(group);
    }

    public Note addNote(Group.Key group, String imagePath) throws IOException {
        Note note = new Note();

        new File(note.getDirectory()).mkdir();
        String dest = note.getOriginalPath();
        if (!new File(imagePath).renameTo(new File(dest))) {
            String fmt = "failed to move %s to %s.";
            throw new IOException(String.format(fmt, imagePath, dest));
        }
        makeThumbnail(note);
        new File(note.getAdditionalPath()).createNewFile();

        mNotes.add(note);
        mGroups.get(group).addNote(note.getKey());

        return note;
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

    private Size computeThumbnailSize(Bitmap orig) {
        Size size = new Size();

        int width = orig.getWidth();
        int height = orig.getHeight();
        int maxThumbnailWidth = 256;
        int maxThumbnailHeight = maxThumbnailWidth;
        if ((width < maxThumbnailWidth) && (height < maxThumbnailHeight)) {
            size.width = maxThumbnailWidth;
            size.height = maxThumbnailHeight;
            return size;
        }

        if (width < height) {
            float ratio = (float)maxThumbnailHeight / (float)height;
            size.width = (int)(ratio * (float)width);
            size.height = maxThumbnailHeight;
            return size;
        }

        float ratio = (float)maxThumbnailWidth / (float)width;
        size.width = maxThumbnailWidth;
        size.height = (int)(ratio * (float)height);
        return size;
    }

    private void makeThumbnail(Note note) throws IOException {
        String origPath = note.getOriginalPath();
        Bitmap orig = BitmapFactory.decodeFile(origPath);
        if (orig == null) {
            String fmt = "failed to decode image: %s";
            throw new IOException(String.format(fmt, origPath));
        }
        try {
            Size size = computeThumbnailSize(orig);
            Bitmap thumb = Bitmap.createScaledBitmap(orig,
                                                     size.width, size.height,
                                                     false);
            try {
                String thumbPath = note.getThumbnailPath();
                OutputStream out = new FileOutputStream(thumbPath);
                try {
                    if (!thumb.compress(CompressFormat.PNG, 100, out)) {
                        String fmt = "failed to make thumbnail: %s";
                        throw new IOException(String.format(fmt, thumbPath));
                    }
                }
                finally {
                    out.close();
                }
            }
            finally {
                thumb.recycle();
            }
        }
        finally {
            orig.recycle();
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

    private static void makeDirectories() {
        String directory = Application.getDataDirectory();
        String[] directories = new String[] {
                getEntriesDirectory(directory),
                getGroupsDirectory(directory)
        };
        FileUtil.makeDirectories(directories);
    }

    private void read() throws IOException {
        String directory = Application.getDataDirectory();

        mNotes = new Notes();
        for (String name: new File(getEntriesDirectory(directory)).list()) {
            mNotes.add(new Note(name));
        }
        mGroups = new Groups();
        for (File file: new File(getGroupsDirectory(directory)).listFiles()) {
            mGroups.add(readGroup(directory, file.getName()));
        }
    }

    private void deleteDirectory(String directory) {
        deleteDirectory(new File(directory));
    }

    private void deleteDirectory(File directory) {
        for (File file: directory.listFiles()) {
            if (file.isDirectory()) {
                deleteDirectory(file);
            }
            deleteFile(file);
        }
        deleteFile(directory);
    }

    private void deleteFile(File file) {
        file.delete();
        Log.i(LOG_TAG, String.format("deleted: %s", file.getAbsolutePath()));
    }
}
package jp.gr.java_conf.neko_daisuki.photonote;

import java.io.File;

import android.util.Log;

public class FileUtil {

    private static final String LOG_TAG = "file";

    public static void makeDirectories(String[] directories) {
        for (String d: directories) {
            if (new File(d).mkdirs()) {
                Log.i(LOG_TAG, String.format("make directory: %s", d));
            }
        }
    }
}
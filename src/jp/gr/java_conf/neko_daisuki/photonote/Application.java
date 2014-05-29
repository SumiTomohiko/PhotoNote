package jp.gr.java_conf.neko_daisuki.photonote;

import android.os.Environment;

public class Application {

    public static String getDataDirectory() {
        String storage = Environment.getExternalStorageDirectory().getPath();
        return String.format("%s/.photonote", storage);
    }

    public static String getTemporaryDirectory() {
        return String.format("%s/tmp", getDataDirectory());
    }

    public static void initialize() {
        FileUtil.makeDirectories(new String[] { getTemporaryDirectory() });
    }
}
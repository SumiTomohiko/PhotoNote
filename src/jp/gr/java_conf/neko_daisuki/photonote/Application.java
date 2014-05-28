package jp.gr.java_conf.neko_daisuki.photonote;

import android.os.Environment;

public class Application {

    public static String getDataDirectory() {
        String storage = Environment.getExternalStorageDirectory().getPath();
        return String.format("%s/.photonote", storage);
    }
}
package jp.gr.java_conf.neko_daisuki.photonote;

import java.io.File;
import java.io.IOException;

import android.os.Environment;

public class Application {

    public static String getDataDirectory() {
        String storage = Environment.getExternalStorageDirectory().getPath();
        return String.format("%s/.photonote", storage);
    }

    public static String getTemporaryDirectory() {
        return String.format("%s/tmp", getDataDirectory());
    }

    public static void initialize() throws IOException {
        FileUtil.makeDirectories(new String[] { getTemporaryDirectory() });

        String nomedia = String.format("%s/.nomedia", getDataDirectory());
        new File(nomedia).createNewFile();
    }
}
package jp.gr.java_conf.neko_daisuki.android.util;

import java.util.HashMap;
import java.util.Map;

import android.app.Activity;
import android.content.Intent;

public class ActivityResultHandler {

    public interface ResultHandler {

        public static class Nop implements ResultHandler {

            @Override
            public void onActivityResult(int requestCode, int resultCode,
                                         Intent data) {
            }
        }

        public static final ResultHandler NOP = new Nop();

        public void onActivityResult(int requestCode, int resultCode,
                                     Intent data);
    }

    private class Key {

        private int mRequestCode;
        private int mResultCode;

        public Key(int requestCode, int resultCode) {
            mRequestCode = requestCode;
            mResultCode = resultCode;
        }

        public int getRequestCode() {
            return mRequestCode;
        }

        public int getResultCode() {
            return mResultCode;
        }

        public boolean equals(Object o) {
            Key key;
            try {
                key = (Key)o;
            }
            catch (ClassCastException e) {
                return false;
            }
            int requestCode = key.getRequestCode();
            int resultCode = key.getResultCode();
            return (requestCode == mRequestCode) && (resultCode == mResultCode);
        }

        public int hashCode() {
            int n = (mResultCode << 16) + mRequestCode;
            return Integer.valueOf(n).hashCode();
        }
    }

    private Map<Key, ResultHandler> mHandlers;

    public ActivityResultHandler() {
        mHandlers = new HashMap<Key, ResultHandler>();
    }

    public void put(int requestCode, ResultHandler handler) {
        mHandlers.put(new Key(requestCode, Activity.RESULT_OK), handler);
    }

    public void handle(int requestCode, int resultCode, Intent data) {
        ResultHandler handler = mHandlers.get(new Key(requestCode, resultCode));
        ResultHandler h = handler != null ? handler : ResultHandler.NOP;
        h.onActivityResult(requestCode, resultCode, data);
    }
}
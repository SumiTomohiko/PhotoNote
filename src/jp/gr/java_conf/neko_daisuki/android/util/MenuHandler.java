package jp.gr.java_conf.neko_daisuki.android.util;

import android.util.SparseArray;
import android.view.MenuItem;

public class MenuHandler {

    public interface ItemHandler {

        public static class Nop implements ItemHandler {

            @Override
            public void handle(MenuItem item) {
            }
        }

        public static final ItemHandler NOP = new Nop();

        public void handle(MenuItem item);
    }

    private SparseArray<ItemHandler> mHandlers;

    public MenuHandler() {
        mHandlers = new SparseArray<ItemHandler>();
    }

    public void put(int id, ItemHandler handler) {
        mHandlers.put(id, handler);
    }

    public void handle(MenuItem item) {
        ItemHandler handler = mHandlers.get(item.getItemId());
        (handler != null ? handler : ItemHandler.NOP).handle(item);
    }
}
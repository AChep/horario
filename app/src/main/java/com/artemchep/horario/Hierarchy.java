package com.artemchep.horario;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.artemchep.basic.tests.Check;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Artem Chepurnoy
 */
public class Hierarchy {

    /**
     * @author Artem Chepurnoy
     */
    public interface Item {

        /**
         * @return unique key of the item, used to identify it
         * @see #getParentKey()
         */
        @NonNull
        String getKey();

        @NonNull
        String getSortKey();

        @Nullable
        String getParentKey();

    }

    /**
     * @author Artem Chepurnoy
     */
    public static class ItemDecorator implements Item {

        private List<ItemDecorator> mList;
        private ItemDecorator mParent;
        public Item mItem;

        public String sorting;
        public int poo;

        ItemDecorator(@NonNull Item item) {
            mList = new ArrayList<>();
            mItem = item;
        }

        @NonNull
        @Override
        public String getKey() {
            return mItem.getKey();
        }

        @NonNull
        @Override
        public String getSortKey() {
            return mItem.getSortKey();
        }

        @Nullable
        @Override
        public String getParentKey() {
            return mItem.getParentKey();
        }

    }

    /**
     * @author Artem Chepurnoy
     */
    public interface Observer {

        void put(@NonNull ItemDecorator model);

        void remove(@NonNull ItemDecorator model);

    }

    private Observer mObserver;

    private List<ItemDecorator> mList = new ArrayList<>();
    private List<ItemDecorator> mQueued = new ArrayList<>();

    public Hierarchy(Observer observer) {
        mObserver = observer;
    }

    public void put(@NonNull Item item) {
        ItemDecorator cur = new ItemDecorator(item);
        ItemDecorator old = get(mList, item.getKey());

        if (old != null) {
            remove(old);
        }
        {

            // March through children, maybe some
            // are yours?
            int size = mQueued.size();
            for (int i = size - 1; i >= 0; i--) {
                ItemDecorator k = mQueued.get(i);
                if (cur.getKey().equals(k.getParentKey())) {
                    cur.mList.add(k);
                    k.mParent = cur;
                    mQueued.remove(i);
                }
            }
        }

        String parent = cur.getParentKey();
        if (parent == null) {
            mList.add(cur);
            buildBranch(cur);
            notifyBranchPut(cur);
        } else {
            ItemDecorator p = get(mList, parent);
            if (p != null) {
                p.mList.add(cur);
                cur.mParent = p;
                buildBranch(cur);
                notifyBranchPut(cur);
            } else mQueued.add(cur);
        }
    }

    public void remove(@NonNull Item item) {
        ItemDecorator p = get(mList, item.getKey());
        if (p == null) p = get(mQueued, item.getKey());
        if (p != null) {
            notifyBranchRemoved(p);

            if (p.mParent != null) {
                p.mParent.mList.remove(p);
            }

            // Remove children from parent and add them
            // to queue.
            for (ItemDecorator k : p.mList) {
                k.mParent = null;
                mQueued.add(k);
            }
        }
    }

    private void buildBranch(ItemDecorator d) {
        if (d.mParent != null) {
            Check.getInstance().isNonNull(d.mParent.sorting);
            d.sorting = d.mParent.sorting + d.getSortKey();
            d.poo = d.mParent.poo + 1;
        } else {
            d.sorting = d.getSortKey();
            d.poo = 0;
        }

        // Build all children
        for (ItemDecorator i : d.mList) {
            buildBranch(i);
        }
    }

    private void notifyBranchPut(ItemDecorator d) {
        notifyBranch(d, 1);
    }

    private void notifyBranchRemoved(ItemDecorator d) {
        notifyBranch(d, 0);
    }

    private void notifyBranch(ItemDecorator d, int notify) {
        switch (notify) {
            case 0:
                mObserver.remove(d);
                break;
            case 1:
                mObserver.put(d);
                break;
        }

        // Notify about children
        for (ItemDecorator i : d.mList) {
            notifyBranch(i, notify);
        }
    }

    private ItemDecorator get(List<ItemDecorator> list, @NonNull String key) {
        for (ItemDecorator i : list) {
            if (i.getKey().equals(key)) {
                return i;
            }

            ItemDecorator d = get(i.mList, key);
            if (d != null) return d;
        }
        return null;
    }

}

package com.artemchep.horario.utils;

import java.util.List;

/**
 * @author Artem Chepurnoy
 */
public class CompositeList<T> {

    private final List<T>[] mLists;

    public CompositeList(List<T>... lists) {
        mLists = lists;
    }

    public int size() {
        int size = 0;
        for (List<T> list : mLists) size += list.size();
        return size;
    }

    public T get(int i) {
        for (List<T> list : mLists) {
            if (i < list.size()) {
                return list.get(i);
            } else i -= list.size();
        }
        throw new IndexOutOfBoundsException();
    }

    public int get(int i, List<T> target) {
        for (List<T> list : mLists) {
            if (list == target) {
                return i;
            } else i += list.size();
        }
        throw new IllegalArgumentException();
    }

}

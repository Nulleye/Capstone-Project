package com.nulleye.common;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * MapList
 * A map with list functionality
 *
 * @author Cristian Alvarez Planas
 * @version 1
 * 19/2/17
 */
@SuppressWarnings({"WeakerAccess", "unused"})
public class MapList<K,V> implements Iterable<V>, IEmpty {

    public static int NO_POSITION = -1;

    Map<K,WrapperPos> map;
    List<WrapperKey> list;


    private class WrapperPos {
        int position;
        V value;
        WrapperPos(final int position, final V value) {
            this.position = position;
            this.value = value;
        }
    } //WrapperPos


    private class WrapperKey {
        K key;
        V value;
        WrapperKey(final K key, final V value) {
            this.key = key;
            this.value = value;
        }
    } //WrapperKey



    public MapList() {
        map = new HashMap<>();
        list = new ArrayList<>();
    }


    public MapList(final int size) {
        map = new HashMap<>(size);
        list = new ArrayList<>(size);
    }


    public V get(final K key) {
        WrapperPos res = map.get(key);
        return (res != null)? res.value : null;
    }


    public V getAtPosition(final int position) {
        return list.get(position).value;
    }


    public boolean containsKey(final K key) {
        return map.containsKey(key);
    }


    public boolean containsPosition(final int position) {
        return (position > -1) && (list.size() > position);
    }


    public int getPosition(K key) {
        final WrapperPos wpos = map.get(key);
        return (wpos != null)? wpos.position : NO_POSITION;
    }


    public void put(final K key, final V value) {
        map.put(key, new WrapperPos(list.size(), value));
        list.add(new WrapperKey(key, value));
    }


    //Update positions (slow)
    public void putAtPosition(final K key, final int position, final V value) {
        if (containsKey(key)) {
            //Update positions (slow)
            final WrapperPos del = map.get(key);
            list.remove(del.position);
            for (int i = del.position; i < list.size(); i++)
                map.get(list.get(i).key).position--;
        }
        list.add(position, new WrapperKey(key, value));
        map.put(key, new WrapperPos(position, value));
        //Update positions (slow)
        for (int i = position + 1; i < list.size(); i++)
            map.get(list.get(i).key).position++;
    }


    //Update positions (slow)
    public V remove(final K key) {
        final WrapperPos rem = map.remove(key);
        if (rem != null) {
            list.remove(rem.position);
            //Update positions (slow)
            for (int i = rem.position; i < list.size(); i++)
                map.get(list.get(i).key).position--;
            return rem.value;
        }
        return null;
    }


    //Update positions (slow)
    public V removeAtPosition(final int position) {
        final WrapperKey rem = list.remove(position);
        map.remove(rem.key);
        //Update positions (slow)
        for (int i = position; i < list.size(); i++)
            map.get(list.get(i).key).position--;
        return rem.value;
    }


    public int size() {
        return list.size();
    }


    @Override
    public boolean isEmpty() {
        return (list.size() < 1);
    }


    private class ValueIterator implements Iterator<V> {

        Iterator<WrapperKey> wrapKey;

        ValueIterator(Iterator<WrapperKey> wrapKey) {
            this.wrapKey = wrapKey;
        }

        @Override
        public boolean hasNext() {
            return wrapKey.hasNext();
        }

        @Override
        public V next() {
            return wrapKey.next().value;
        }

    } //ValueIterator


    @Override
    public Iterator<V> iterator() {
        return new ValueIterator(list.iterator());
    }

}

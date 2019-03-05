package jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;

public class ListMap<K, V> extends LinkedHashMap<K, List<V>> {

    public V getLast(K key) {
        List<V> list = get(key);
        if (list == null) {
            return null;
        }
        return list.get(list.size() - 1);
    }

    public void add(K key, V value) {
        List<V> list = get(key);
        if (list == null) {
            list = new ArrayList<>();
            put(key, list);
        }
        list.add(value);
    }

    public void putValue(K key, V value) {
        remove(key);
        List<V> list = new ArrayList<>();
        list.add(value);
        put(key, list);
    }

    public boolean removeFromList(K key, V value) {
        List<V> values = get(key);
        if (values == null) {
            return false;
        }
        return values.remove(value);
    }

    public List<V> getAllValues() {
        List<V> v = new ArrayList<>();
        values().forEach((vv) -> v.addAll(vv));
        return v;
    }

    public void removeFromAll(V values) {
        for (List<V> vv : values()) {
            vv.remove(values);
        }
    }

    public void addAll(K key, Collection<? extends V> values) {
        List<V> list = get(key);
        if (list == null) {
            list = new ArrayList<>();
            put(key, list);
        }
        list.addAll(values);
    }

    public void addAll(K key, V... values) {
        this.addAll(key, Arrays.asList(values));
    }

    public List<V> getList(K key) {
        List<V> values = get(key);
        if (values == null) {
            return new ArrayList<>();
        }
        return values;
    }

    public void clearList(K key) {
        List<V> values = get(key);
        if (values != null) {
            values.clear();
        }
    }
}

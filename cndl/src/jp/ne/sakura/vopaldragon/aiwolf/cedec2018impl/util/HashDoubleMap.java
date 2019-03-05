package jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;
import java.util.function.BiConsumer;

public class HashDoubleMap<K> {

    public void forEach(BiConsumer<K, Double> consumer) {
        countMap.values().stream().forEach(c -> consumer.accept(c.key, c.value));
    }

    public double sum() {
        double count = 0;
        for (DoubleCount c : countMap.values()) {
            count += c.value;
        }
        return count;
    }

    public void multiply(double value) {
        for (DoubleCount c : countMap.values()) {
            c.value = c.value * value;
        }
    }

    public HashDoubleMap<K> sort(boolean accend) {
        ArrayList<DoubleCount> counts = new ArrayList<DoubleCount>(countMap.values());
        if (accend) {
            Collections.sort(counts);
        } else {
            Collections.sort(counts, Collections.reverseOrder());
        }
        countMap.clear();
        for (DoubleCount c : counts) {
            countMap.put(c.key, c);
        }
        return this;
    }

    public List<DoubleCount> getValueList() {
        return new ArrayList<>(countMap.values());
    }

    public Set<K> getKeySet() {
        return countMap.keySet();
    }

    public List<K> getKeyList() {
        return new ArrayList<K>(getKeySet());
    }

    public class DoubleCount implements Comparable<DoubleCount> {

        private K key;
        private double value = 0;

        public DoubleCount(K key) {
            this.key = key;
        }

        public K getKey() {
            return key;
        }

        @Override
        public String toString() {
            return key + "/" + Double.toString(value);
        }

        @Override
        public int compareTo(DoubleCount o) {
            return Double.compare(value, o.value);
        }
    }

    public K getKeyAt(int rank) {
        int i = 0;
        for (DoubleCount c : countMap.values()) {
            if (++i == rank) return c.key;
        }
        return null;
    }
    private LinkedHashMap<K, DoubleCount> countMap = new LinkedHashMap<K, DoubleCount>();

    public void removeCount(K key) {
        countMap.remove(key);
    }

    public void modifyValue(K key, double value) {
        if (key == null) {
            return;
        }
        DoubleCount c = countMap.get(key);
        if (c == null) {
            c = new DoubleCount(key);
            countMap.put(key, c);
        }
        c.value += value;
    }

    public double getValue(K key) {
        if (key == null) {
            return 0;
        }
        DoubleCount c = countMap.get(key);
        if (c == null) {
            return 0;
        } else {
            return c.value;
        }
    }

    @Override
    public String toString() {
        return countMap.values().toString();
    }

    public void print() {
        List<DoubleCount> list = getValueList();
//        Collections.sort(list);
        for (DoubleCount c : list) {
            System.out.println(c.key + "\t" + c.value);
        }
    }

}

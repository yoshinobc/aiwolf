package jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.util;

import java.util.OptionalDouble;

public class DoubleListMap<K> extends ListMap<K, Double> {

    public void add(K key, double d) {
        super.add(key, d);
    }

    public OptionalDouble average(K k) {
        return getList(k).stream().mapToDouble(d -> d).average();
    }

    public double sum(K k) {
        return getList(k).stream().mapToDouble(d -> d).sum();
    }

    public double variance(K k) {
        double ave = average(k).orElse(0);
        double sqsum = 0;
        for (Double d : getList(k)) {
            sqsum += Math.pow(d - ave, 2.0);
        }
        return Math.pow(sqsum/getList(k).size(), 0.5);
    }

}

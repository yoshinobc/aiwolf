package jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class DataScoreList<E> extends ArrayList<DataScore<E>> {

    public DataScoreList() {
    }

    public DataScoreList(List<DataScore<E>> list) {
        super(list);
    }

    public Map<E, Double> getScoreMap() {
        return stream().collect(Collectors.toMap(ds -> ds.data, ds -> ds.score));
    }

    public void add(E e, double score) {
        this.add(new DataScore<>(e, score));
    }

    public DataScoreList<E> reverse() {
        Collections.sort(this, Collections.reverseOrder());
        return this;
    }

    public DataScoreList<E> sort() {
        Collections.sort(this);
        return this;
    }

    public DataScoreList<E> sort(boolean asc) {
        if (asc) reverse();
        else sort();
        return this;
    }

    public DataScoreList<E> top(int x) {
        return new DataScoreList<>(subList(0, Math.min(size(), x)));
    }

    public DataScore<E> top() {
        if (isEmpty()) return null;
        return get(0);
    }

    public DataScoreList<E> filter(Predicate<DataScore<E>> p) {
        return new DataScoreList<>(stream().filter(p).collect(Collectors.toList()));
    }

}

package jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.util;

import java.security.SecureRandom;


/**
 * 
 * スコア付きデータ。ソート可能（自然順では、値が大きいほうが前に来る）。なお、値が同値の場合にはランダム（RAND.nextBoolean() ? 1 : -1）。
 * 
 * @param <E> 
 */
public class DataScore<E> implements Comparable<DataScore> {

    public final E data;
    public final double score;

    public DataScore(E data, double score) {
        this.data = data;
        this.score = score;
    }

    private static SecureRandom RAND = new SecureRandom();

    @Override
    public int compareTo(DataScore o) {
        if (o.score == score) return RAND.nextBoolean() ? 1 : -1;
        return Double.compare(o.score, score);
    }

    @Override
    public String toString() {
        return String.format("%s/%.3f", data, score);
    }

}

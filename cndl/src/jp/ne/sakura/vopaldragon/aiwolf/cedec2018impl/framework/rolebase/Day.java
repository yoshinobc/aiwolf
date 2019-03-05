package jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.framework.rolebase;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * FixedTacticTimingで利用する日付条件
 */
public class Day {

    public static Day before(int day) {
        Day d = new Day();
        d.before = day;
        return d;
    }

    public static Day after(int day) {
        Day d = new Day();
        d.after = day;
        return d;
    }

    public static Day on(int day) {
        Day d = new Day();
        d.exact = day;
        return d;
    }

    public static Day on(Integer... day) {
        Day d = new Day();
        d.set = new HashSet<>(Arrays.asList(day));
        return d;
    }

    public static Day any() {
        Day d = new Day();
        return d;
    }

    private Integer before;
    private Integer after;
    private Integer exact;
    private Set<Integer> set;

    private Day() {
    }

    boolean accept(int day) {
        if (exact != null) return day == exact;
        if (after != null) return day >= after;
        if (before != null) return day <= before;
        if (set != null) return set.contains(day);
        return true;
    }

}

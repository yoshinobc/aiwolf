package jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.framework.rolebase;

import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.framework.AbstractStrategy;

/**
 *
 * 指定された日に指定されたプライオリティで動作する戦術実行タイミング。
 */
public class FixedTacticTiming<T extends Tactic> extends TacticTiming<T> {

    public int priority;
    public Day day;
    public boolean isOnceADay = true;

    FixedTacticTiming(T tactic) {
        super(tactic);
    }

    @Override
    public int getPriority(int day, int turn, int skip, int utter, AbstractStrategy strategy) {
        return priority;
    }

    @Override
    public boolean willWork(int day, int turn, int skip, int utter, AbstractStrategy strategy) {
        if (isOnceADay && workedForTheDay) return false;
        if (this.day != null && !this.day.accept(day)) return false;
        return true;
    }

    @Override
    public void startDay() {
        workedForTheDay = false;
    }

    @Override
    public void worked() {
        workedForTheDay = true;
    }

    private boolean workedForTheDay;

}

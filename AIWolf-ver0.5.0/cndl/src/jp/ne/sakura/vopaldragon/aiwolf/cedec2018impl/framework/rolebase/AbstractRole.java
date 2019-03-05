package jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.framework.rolebase;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.framework.AbstractStrategy;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.framework.EventType;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.framework.Game;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.framework.GameAgent;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.framework.GameEvent;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.util.Utils;
import org.aiwolf.client.lib.Content;
import org.aiwolf.client.lib.ContentBuilder;
import org.aiwolf.client.lib.SkipContentBuilder;
import org.aiwolf.common.data.Agent;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.framework.GameEventListener;

/**
 * 具体的な各役職の実装の抽象クラス。このクラスを拡張して役職を実装する。createModelで、利用するGameModelを返す必要がある。
 */
public abstract class AbstractRole<T extends AbstractStrategy> implements GameEventListener {

    private T strategy;

    public AbstractRole(T strategy) {
        this.strategy = strategy;
    }

    @Override
    public void handleEvent(Game g, GameEvent e) {
        if (e.type == EventType.DAYSTART) {
            //一日一回発言のカウンタをリセット
            allTacticTimings.forEach(tt -> tt.startDay());
        }
    }

    private List<TacticTiming> allTacticTimings = new ArrayList<>();

    public enum Repeat {
        ONCE, MULTI
    }

    public class TacticList<T extends Tactic> {

        private List<TacticTiming<T>> tacticTimings = new ArrayList<>();

        /**
         * 優先度最大、日付指定無し、繰り返し無しでTacticを追加する
         */
        public void add(T e) {
            add(e, Integer.MAX_VALUE, null, Repeat.ONCE);
        }

        /**
         * 日付指定、優先度最大、繰り返し無しでTacticを追加する
         */
        public void add(T e, Day day) {
            add(e, Integer.MAX_VALUE, day, Repeat.ONCE);
        }

        /**
         * 優先度指定、日付指定無し、繰り返し無しでTacticを追加する
         */
        public void add(T e, int priority) {
            add(e, priority, null, Repeat.ONCE);
        }

        /**
         * 優先度・日付指定、繰り返し無しでTacticを追加する
         */
        public void add(T e, int priority, Day day) {
            add(e, priority, day, Repeat.ONCE);
        }

        /**
         * 優先度・日付・繰り返しを指定してTacticを追加する
         */
        public void add(T e, int priority, Day day, Repeat repeat) {
            strategy.addEventListener(e);
            FixedTacticTiming<T> timing = new FixedTacticTiming<>(e);
            timing.day = day;
            timing.isOnceADay = repeat == Repeat.ONCE;
            tacticTimings.add(timing);
            allTacticTimings.add(timing);
        }

        /**
         * 優先度順位・起動条件を定める自前クラスでTacticを追加する
         */
        public void add(TacticTiming<T> timing) {
            strategy.addEventListener(timing.tactic);
            tacticTimings.add(timing);
            allTacticTimings.add(timing);
        }

    }

    protected final TacticList<TalkTactic> talkTactics = new TacticList<>();
    protected final TacticList<TalkTactic> whisperTactics = new TacticList<>();
    protected final TacticList<TargetTactic> divineTactics = new TacticList<>();
    protected final TacticList<TargetTactic> voteTactics = new TacticList<>();
    protected final TacticList<TargetTactic> revoteTactics = new TacticList<>();
    protected final TacticList<TargetTactic> attackTactics = new TacticList<>();
    protected final TacticList<TargetTactic> reattackTactics = new TacticList<>();
    protected final TacticList<TargetTactic> guardTactics = new TacticList<>();

    private int skipCount = 0;
    private int utterCount = 0;

    private Content genContent(int turn, TacticList<TalkTactic> talkTactics) {
        int day = strategy.getGame().getDay();
        Collections.sort(talkTactics.tacticTimings, (c1, c2) -> c1.getPriority(day, turn, skipCount, utterCount, strategy) - c2.getPriority(day, turn, skipCount, utterCount, strategy));
        for (TacticTiming<TalkTactic> tt : talkTactics.tacticTimings) {
            try {
                if (tt.willWork(day, turn, skipCount, utterCount, strategy)) {
                    ContentBuilder content = tt.tactic.talk(turn, skipCount, utterCount, strategy);
                    if (content != null) {
                        skipCount = 0;
                        utterCount++;
                        tt.worked();
//                        System.err.println(Utils.join("talk-tactic", day, turn, tt.tactic.getClass().getSimpleName(), new Content(content).getText()));
                        return new Content(content);
                    }
                }
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }
        skipCount++;
        return new Content(new SkipContentBuilder());
    }

    private Agent selectAgent(TacticList<TargetTactic> targetTactics) {
        int day = strategy.getGame().getDay();
        Collections.sort(targetTactics.tacticTimings, (c1, c2) -> c1.getPriority(day, 0, 0, 0, strategy) - c2.getPriority(day, 0, 0, 0, strategy));
        for (TacticTiming<TargetTactic> tt : targetTactics.tacticTimings) {
            try {
                if (tt.willWork(day, 0, 0, 0, strategy)) {
                    GameAgent target = tt.tactic.target(strategy);
                    if (target != null) {
//                        System.err.println(Utils.join("target-tactic", tt.tactic.getClass().getSimpleName(), target));
                        return target.agent;
                    }
                }
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }
        return Utils.getRandom(strategy.getGame().getAliveOthers()).agent;
    }

    Content talk(int turn) {
        return genContent(turn, talkTactics);
    }

    Content whisper(int turn) {
        return genContent(turn, whisperTactics);
    }

    Agent vote() {
        return selectAgent(voteTactics);
    }

    Agent revote() {
        return selectAgent(revoteTactics);
    }

    Agent attack() {
        return selectAgent(attackTactics);
    }

    Agent reattack() {
        return selectAgent(reattackTactics);
    }

    Agent divine() {
        return selectAgent(divineTactics);
    }

    Agent guard() {
        return selectAgent(guardTactics);
    }
}

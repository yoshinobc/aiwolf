package jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.evalmodel;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.CndlStrategy;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.framework.EventType;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.framework.Game;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.framework.GameAction;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.framework.GameBeforeActionListener;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.framework.GameEvent;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.framework.GameEventListener;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.framework.GameVote;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.util.HashCounter;

public class VotePredictionEvalModel implements GameBeforeActionListener, GameEventListener {

    private CndlStrategy strategy;

    public VotePredictionEvalModel(CndlStrategy strategy) {
        this.strategy = strategy;
        this.strategy.addPersistentEventListener(this);
        this.strategy.addPersistentBeforeActionListener(this);
    }

    public Map<String, VoteResult> voteResultMap = new HashMap<>();
    public Map<String, VoteResult> reVoteResultMap = new HashMap<>();

    private static String key(int game, int day) {
        return game + "-" + day;
    }

    public static class VoteResult {

        public int game;
        public int day;
        public HashCounter<Integer> expected = new HashCounter<>();
        public HashCounter<Integer> result = new HashCounter<>();

        public double precision() {
            double success = 0;
            for (Integer agIdx : result.getKeyList()) {
                int r = result.getCount(agIdx);
                int e = expected.getCount(agIdx);
                if (r == e) success += r;
                else success += Math.min(r, e);
            }
            return success / result.totalCount();
        }

        public boolean topSuccess() {
            return !Collections.disjoint(result.sort(false).topCounts(), expected.sort(false).topCounts());
        }

    }

    @Override
    public void handleBeforeAction(Game g, GameAction action) {
        if (action == GameAction.VOTE || action == GameAction.REVOTE) {
            VoteResult vr = new VoteResult();
            vr.day = g.getDay();
            vr.game = strategy.getNumberOfGame();
            if (action == GameAction.VOTE) {
                strategy.voteModel.expectedVote().forEach((to, count) -> vr.expected.countValue(to.getIndex(), count));
                voteResultMap.put(key(vr.game, vr.day), vr);
            } else {
                strategy.voteModel.expectedRevote().forEach((to, count) -> vr.expected.countValue(to.getIndex(), count));
                reVoteResultMap.put(key(vr.game, vr.day), vr);
            }
        }
    }

    @Override
    public void handleEvent(Game g, GameEvent e) {
        if (e.type == EventType.VOTE) {
            VoteResult vr = null;
            if (e.votes.get(0).isReVote) vr = reVoteResultMap.get(key(strategy.getNumberOfGame(), e.day));
            else vr = voteResultMap.get(key(strategy.getNumberOfGame(), e.day));
            if (vr != null) {
                for (GameVote gv : e.votes) {
                    if (!gv.initiator.isSelf) {
//                        System.err.println(Utils.join("actual-vote", e.votes.get(0).isReVote, Utils.AI_MAP.get(gv.initiator.getIndex()), gv.initiator, gv.target));
                        vr.result.countPlus(gv.target.getIndex());
                    }
                }
//                vr.result.sort(false);
//                System.err.println(Utils.join("vote-precision", e.votes.get(0).isReVote, strategy.getNumberOfGame(), e.day, vr.precision(), vr.expected, vr.result));
            }
        }
    }

}

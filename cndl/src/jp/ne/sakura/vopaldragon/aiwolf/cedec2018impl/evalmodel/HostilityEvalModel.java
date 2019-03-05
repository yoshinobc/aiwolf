package jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.evalmodel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.OptionalDouble;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.CndlStrategy;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.framework.Game;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.framework.GameAgent;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.framework.GameEvent;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.framework.GameTalk;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.framework.GameVote;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.framework.MetagameEventListener;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.framework.GameEventListener;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.util.DataScore;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.util.DataScoreList;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.util.DoubleListMap;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.util.HashCounter;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.util.RatioCounter;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.util.VectorMath;
import org.aiwolf.common.data.Role;

public class HostilityEvalModel implements GameEventListener, MetagameEventListener {

    private CndlStrategy strategy;

    public HostilityEvalModel(CndlStrategy strategy) {
        this.strategy = strategy;
        strategy.addMetagameEventListener(this);
        strategy.addPersistentEventListener(this);
    }

    enum HateType {
        VOTE, REVOTE, DIVINE, DEC_VOTE, ESTIMATE_WOLF;
    }

    public static class Hate {

        public String game;
        public int fromAgtIdx;
        public int toAgtIdx;
        public int day;
        public Role fromRole;
        public Role toRole;

        public Role myRole;
        public HateType type;

        public Hate(int fromAgtIdx, int toAgtIdx, HateType type) {
            this.fromAgtIdx = fromAgtIdx;
            this.toAgtIdx = toAgtIdx;
            this.type = type;
        }

    }

    private DoubleListMap<Integer> perc = new DoubleListMap<>();

    /**
     * エージェントの予測精度の一覧。精度が高い順にソート済み。
     *
     */
    public DataScoreList<GameAgent> agentPredictionAccuracy(List<GameAgent> agents) {
        DataScoreList<GameAgent> result = new DataScoreList<>();
        agents.forEach(ag -> {
            OptionalDouble doOpt = perc.average(ag.getIndex());
            if (doOpt.isPresent()) result.add(new DataScore<>(ag, doOpt.getAsDouble()));
        });
        return result.sort();
    }

    public double[] agentPredictionAccuracy() {
        double[] score = new double[strategy.getGame().getVillageSize()];
        Arrays.fill(score, 0.0);
        for (int i = 0; i < score.length; i++) {
            OptionalDouble doOpt = perc.average(i);
            if (doOpt.isPresent()) score[i] = doOpt.getAsDouble();
        }
        VectorMath.normalize(score);
        return score;
    }

    /**
     * 指定したエージェントのほかのエージェントへのHate値を取得する。
     *
     * @return
     */
    public HashCounter<GameAgent> hateScoreOf(GameAgent from) {
        HashCounter<GameAgent> agentCounter = new HashCounter<>();
        for (Hate h : hateListTemp) {
            if (h.fromAgtIdx == from.getIndex()) agentCounter.countPlus(strategy.getGame().getAgentAt(h.toAgtIdx));
        }
        return agentCounter;
    }

    /**
     * 各エージェント自分へのHate値を取得する。
     *
     * @return
     */
    public List<DataScore<GameAgent>> hatedScore() {
        RatioCounter pc = new RatioCounter();
        for (Hate h : hateListTemp) {
            pc.count(Integer.toString(h.fromAgtIdx), h.toAgtIdx == strategy.getGame().getSelf().getIndex());
        }
        List<DataScore<GameAgent>> result = new ArrayList<>();
        for (String key : pc.keySet()) {
            GameAgent ag = strategy.getGame().getAgentAt(Integer.parseInt(key));
            result.add(new DataScore<>(ag, pc.get(key).ratio()));
        }

        Collections.sort(result);
        return result;
    }

    public List<Hate> hateList = new ArrayList<>();
    private List<Hate> hateListTemp = new ArrayList<>();

    @Override
    public void startGame(Game g) {
        hateListTemp = new ArrayList<>();
    }

    @Override
    public void endGame(Game g) {
        RatioCounter predictionAccuracy = new RatioCounter();
        for (Hate h : hateListTemp) {
            h.fromRole = g.getAgentAt(h.fromAgtIdx).role;
            h.toRole = g.getAgentAt(h.toAgtIdx).role;
            h.myRole = g.getSelf().role;
            h.game = g.getGameId();
            hateList.add(h);
            if (h.fromRole != Role.WEREWOLF && h.fromRole != Role.POSSESSED) {
                predictionAccuracy.count("" + h.fromAgtIdx, h.toRole == Role.WEREWOLF);
            }
        }
        for (String pk : predictionAccuracy.keySet()) {
            perc.add(new Integer(pk), predictionAccuracy.get(pk).ratio());
        }
    }

    @Override
    public void handleEvent(Game g, GameEvent e) {
        switch (e.type) {
            case VOTE:
                for (GameVote vote : e.votes) {
                    hateListTemp.add(new Hate(vote.initiator.getIndex(), vote.target.getIndex(), vote.isReVote ? HateType.REVOTE : HateType.VOTE));
                }
                break;
            case TALK:
                for (GameTalk talk : e.talks) {
                    if (talk.isRepeat) continue;
                    switch (talk.getTopic()) {
                        case VOTE:
                            if (talk.getTarget() != null) {
                                hateListTemp.add(new Hate(talk.getTalker().getIndex(), talk.getTarget().getIndex(), HateType.DEC_VOTE));
                            }
                            break;
                        case ESTIMATE:
                            if (talk.getTarget() != null && talk.getRole() == Role.WEREWOLF) {
                                hateListTemp.add(new Hate(talk.getTalker().getIndex(), talk.getTarget().getIndex(), HateType.ESTIMATE_WOLF));
                            }
                            break;
                        case DIVINED:
                            if (talk.getTarget() != null) {
                                hateListTemp.add(new Hate(talk.getTalker().getIndex(), talk.getTarget().getIndex(), HateType.DIVINE));
                            }
                    }
                }
                break;
        }
    }

}

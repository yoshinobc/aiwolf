package jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.evalmodel;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.OptionalDouble;
import java.util.stream.Collectors;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.CndlStrategy;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.framework.Game;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.framework.GameAction;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.framework.GameAgent;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.framework.MetagameEventListener;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.util.ListMap;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.util.Utils;
import org.aiwolf.common.data.Role;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.model.RolePredictor;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.model.RoleProbabilityStruct;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.util.DataScore;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.framework.GameBeforeActionListener;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.util.DataScoreList;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.util.DoubleListMap;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.util.RatioCounter;

public class PredictionEvalModel implements MetagameEventListener, GameBeforeActionListener {

    private CndlStrategy strategy;

    public PredictionEvalModel(CndlStrategy strategy) {
        this.strategy = strategy;
        strategy.addPersistentBeforeActionListener(this);
        strategy.addMetagameEventListener(this);
    }

    @Override
    public void handleBeforeAction(Game g, GameAction action) {
        if (action == GameAction.VOTE) {
            for (String key : predictors.keySet()) {
                RolePredictor rp = predictors.get(key);
                RoleProbabilityStruct rps = rp.getRoleProbabilityStruct(g);
                //プレイヤーのRoleの推定
                for (GameAgent ag : g.getAgents()) {
                    if (ag.isSelf) continue;
                    rolePredictions.add(key, mostLikely(ag, rps, g));
                }
                //役職の推定
                for (Role r : Utils.existingRole(g)) {
//                    if (r == g.getSelf().role || r == Role.VILLAGER) continue;
                    agentPredictions.add(key, topScore(rps, g, r));
                }
            }
        }
    }

    public Prediction mostLikely(GameAgent ag, RoleProbabilityStruct rps, Game g) {
        Prediction p = new Prediction();
        List<DataScore<Role>> rpList = rps.getRoleProbabilityList(ag);
        p.game = strategy.getNumberOfGame();
        p.role = rpList.get(0).data;
        p.p = rpList.get(0).score;
        p.agtIdx = ag.getIndex();
        p.tgtCoRole = ag.coRole;
        p.day = g.getDay();
        p.isDead = !ag.isAlive;
        p.selfRole = g.getSelf().role;
        p.top3 = rpList.stream().limit(2).map(rp -> rp.data).collect(Collectors.toList());
        return p;
    }

    public Prediction topScore(RoleProbabilityStruct rps, Game g, Role role) {
        DataScore<GameAgent> topAgent = rps.topAgent(role, g.getAliveOthers());
        Prediction wp = new Prediction();
        wp.game = strategy.getNumberOfGame();
        wp.agtIdx = topAgent.data.getIndex();
        wp.role = role;
        wp.day = g.getDay();
        wp.p = topAgent.score;
        wp.tgtCoRole = topAgent.data.coRole;
        wp.remNum = g.getAliveSize();
        wp.selfRole = g.getSelf().role;
        return wp;
    }

    @Override
    public void startGame(Game g) {
        agentPredictions.clear();
        rolePredictions.clear();
    }

    private DoubleListMap<String> perc = new DoubleListMap<>();

    /**
     * 指定された役職のエージェント毎の不確定度合いを返す。不確定度合いが高い順にソート済み。
     *
     */
    public DataScoreList<GameAgent> getAgentUnpredictableness(Role role) {
        DataScoreList<GameAgent> result = new DataScoreList<>();
        for (int i = 0; i < this.strategy.getGame().getVillageSize(); i++) {
            OptionalDouble ds = perc.average(i + "_" + role);
            if (ds.isPresent()) {
                result.add(new DataScore<>(this.strategy.getGame().getAgentAt(i), 1 - ds.getAsDouble()));
            }
        }
        return result.sort();
    }

    @Override
    public void endGame(Game g) {
        for (String key : agentPredictions.keySet()) {
            List<Prediction> agentPredictList = agentPredictions.getList(key);
            agentPredictList.forEach(p -> p.trueRole = g.getAgentAt(p.agtIdx).role);
            List<Prediction> rolePredictList = rolePredictions.getList(key);
            RatioCounter predictableCounter = new RatioCounter();
            rolePredictList.forEach(p -> {
                p.trueRole = g.getAgentAt(p.agtIdx).role;
                predictableCounter.count(p.agtIdx + "_" + p.trueRole, p.trueRole == p.role);
            });
            for (String pk : predictableCounter.keySet()) {
                perc.add(pk, predictableCounter.get(pk).ratio());
            }
        }
    }

    public void addPredictor(RolePredictor r) {
        predictors.put(r.getClass().getSimpleName(), r);
    }

    public void addPredictor(String name, RolePredictor r) {
        predictors.put(name, r);
    }

    public ListMap<String, Prediction> getRolePredictions() {
        return rolePredictions;
    }

    public ListMap<String, Prediction> getAgentPredictions() {
        return agentPredictions;
    }

    private Map<String, RolePredictor> predictors = new LinkedHashMap<>();
    private ListMap<String, Prediction> agentPredictions = new ListMap<>();
    private ListMap<String, Prediction> rolePredictions = new ListMap<>();

    public static class Prediction {

        public int game;
        public int day;
        public int remNum;
        public boolean isDead;
        public int agtIdx;
        public Role selfRole;
        public Role tgtCoRole;
        public Role role;
        public Role trueRole;
        public Double p;

        public List<Role> top3;

    }

}

package jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.aiwolf.client.lib.Content;
import org.aiwolf.client.lib.Topic;
import org.aiwolf.common.data.Player;
import org.aiwolf.common.data.Role;
import org.aiwolf.common.data.Species;

import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.CndlStrategy;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.framework.EventType;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.framework.Game;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.framework.GameAction;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.framework.GameAgent;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.framework.GameBeforeActionListener;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.framework.GameEvent;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.framework.GameEventListener;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.framework.MetagameEventListener;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.framework.simulator.AbstractEventGameSimulator;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.framework.simulator.GameLogEvent;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.util.Utils;

/**
 * @author aki
 *
 */
public class FindCndlModel implements GameEventListener, MetagameEventListener, GameBeforeActionListener {

    CndlStrategy strategy;
    Map<Integer, AbstractEventGameSimulator> idxSimMap = new HashMap<>(); // runnerの番号とAgentIdxの対応表
    Map<Integer, Integer[]> cndlSimScore = new HashMap<>(); // 村人時の総トピック一致情報
    Map<Integer, Integer[]> villagerSimScore = new HashMap<>(); // 村人時の総ターゲット一致イベント情報
    Map<Integer, Integer[]> wolfSimScore = new HashMap<>(); // 狼時の総ターゲット一致イベント情報
    Map<Integer, Integer[]> topicSimilarity = new HashMap<>(); // このゲーム内でのトピック一致情報
    Map<Integer, Integer[]> targetSimilarity = new HashMap<>(); // このゲーム内でのターゲット一致情報

    private List<AbstractEventGameSimulator> runners = new ArrayList<>();
    private List<Player> players = new ArrayList<>();

    public FindCndlModel(CndlStrategy strategy) {
        this.strategy = strategy;
        strategy.addPersistentEventListener(this);
        strategy.addMetagameEventListener(this);
        strategy.addPersistentBeforeActionListener(this);
    }

    /* 性能評価用の内部クラス */
    public static class FindCndl {

        public String game;
        public int day;
        public int agtIndex;
        public boolean isCndl;
        public boolean isWolf;
        public double wolfProb;
        public Role role;

    }

//    public List<FindCndl> findCndlList = new ArrayList<>();
    public List<FindCndl> findCndlListTemp = new ArrayList<>();

    /* 性能評価用に VOTE アクションの直前での狼確率を記録 */
    @Override
    public void handleBeforeAction(Game g, GameAction action) {
        if (action == GameAction.VOTE) {
            for (GameAgent ag : g.getOthers()) {
                FindCndl fc = new FindCndl();
                fc.game = g.getGameId();
                fc.day = g.getDay();
                fc.agtIndex = ag.getIndex();
                fc.isCndl = cndlLike(ag);
//                System.err.println(Utils.join(g.getDay(), ag.getIndex(), fc.isCndl));
                fc.wolfProb = wolfProbability(ag);
                findCndlListTemp.add(fc);
            }
        }
    }

    @Override
    public void startGame(Game g) {
        // 最初のゲームでシミュレータを割当。
        if (idxSimMap.size() == 0) {
            g.getAgentStream().filter(x -> !x.isSelf).forEach(agent -> {
                jp.ne.sakura.vopaldragon.aiwolf.CndlPlayer player = new jp.ne.sakura.vopaldragon.aiwolf.CndlPlayer();
                AbstractEventGameSimulator runner = new AbstractEventGameSimulator(player) {
                    @Override
                    protected Species predictSpiecies(int agtIndex) {
                        double[] scores = strategy.evilModel.getRoleProbability(Role.WEREWOLF);
                        List<GameAgent> agents = strategy.getGame().getAgents();
                        Utils.sortByScore(agents, scores, false);
                        for (int i = 0; i < 3; i++) {
                            if (agents.get(i).agent.getAgentIdx() == agtIndex)
                                return Species.WEREWOLF;
                        }
                        return Species.HUMAN;
                    }
                };
                runners.add(runner);
                players.add(player);
                strategy.addPersistentBeforeActionListener(runner);
                strategy.addPersistentEventListener(runner);
                // 類似度の初期化
                int k = agent.getIndex();
                idxSimMap.put(agent.getIndex(), runner);
                topicSimilarity.put(k, new Integer[2]);
                targetSimilarity.put(k, new Integer[2]);
                cndlSimScore.put(k, new Integer[2]);
                villagerSimScore.put(k, new Integer[2]);
                wolfSimScore.put(k, new Integer[2]);
                for (int i = 0; i < 2; i++) {
                    cndlSimScore.get(k)[i] = 0;
                    villagerSimScore.get(k)[i] = 0;
                    wolfSimScore.get(k)[i] = 0;
                }
            });
        }
        Map<Integer, Role> idxRoleMap = new HashMap<>();
        idxSimMap.forEach((k, v) -> {
            // 村人cndl-oldの場合の動きをシミュレート
            idxRoleMap.put(k + 1, Role.VILLAGER);
            v.startGame(g.getSelf().role, k + 1, g.getVillageSize(), idxRoleMap);
            // 1ゲームごとに初期化
            for (int j = 0; j < 2; j++) {
                topicSimilarity.get(k)[j] = 0;
                targetSimilarity.get(k)[j] = 0;
            }
        });
        findCndlListTemp = new ArrayList<>();
    }

    @Override
    public void endGame(Game g) {
        idxSimMap.forEach((k, v) -> {
            v.endGame(g);
            if (g.getAgentAt(k).role == Role.VILLAGER) {
                for (int i = 0; i < 2; i++) {
                    cndlSimScore.get(k)[i] += topicSimilarity.get(k)[i];
                    villagerSimScore.get(k)[i] += targetSimilarity.get(k)[i];
                }
            }
            if (g.getAgentAt(k).role == Role.WEREWOLF) {
                for (int i = 0; i < 2; i++) {
                    wolfSimScore.get(k)[i] += targetSimilarity.get(k)[i];
                }
            }
            if (cndlLike(k)) {
                Utils.log(" Simulality: ", k + 1,
                    String.format("tpcSim: %d/%d", topicSimilarity.get(k)[0], topicSimilarity.get(k)[1]),
                    String.format("trgSim: %d/%d", targetSimilarity.get(k)[0], targetSimilarity.get(k)[1]),
                    getVillagerSimilarity(k), getCndlSimilarity(k));
            }
        });

        /* 性能評価用にゲーム終了時に各エージェントの役割を記録 */
        for (FindCndl fc : findCndlListTemp) {
            fc.role = g.getAgentAt(fc.agtIndex).role;
        }

    }

    @Override
    public void handleEvent(Game g, GameEvent e) {
        switch (e.type) {
            case TALK:
                e.talks.stream().filter(x -> x.getTalker() != g.getSelf() && x.getTopic() != Topic.OVER).forEach(t -> {
                    int agt = t.getTalker().getIndex();
                    GameLogEvent simAction = idxSimMap.get(agt).getLastPlayerAction(EventType.TALK);
                    /* 
					 * GameLogEventのturnには、常に0が入っている
                     */
//                    talkSimScoreN.get(agt).set(1, talkSimScoreN.get(agt).get(1) + 0.5);
                    topicSimilarity.get(agt)[1] += 1;
                    Content simTalk = simAction.talk;
                    if (t.getTopic() == simTalk.getTopic()) {
                        topicSimilarity.get(agt)[0] += 1;
                        if (t.getTarget() != null) {
                            targetSimilarity.get(agt)[1] += 1;
                            if (t.getTarget().agent.getAgentIdx() == simTalk.getTarget().getAgentIdx()) {
                                targetSimilarity.get(agt)[0] += 1;
                            }
                        }
                    }
                    Utils.log("TALK-SIM", agt, t.talkContent(), simAction.talk.getText(), getCndlSimilarity(agt), getVillagerSimilarity(agt));
                });
                break;
        }
    }

    /**
     * 2017年版cndlとの、全ゲームを通じての会話トピックの一致度を返す まだ一度も候補となるイベントが無いなら、0.0を返す。
     *
     * @param agt エージェントID（0始まり）
     * @return
     */
    public double getCndlSimilarity(int agt) {
        return cndlSimScore.containsKey(agt) && cndlSimScore.get(agt)[1] > 0
            ? (double) (cndlSimScore.get(agt)[0]) / cndlSimScore.get(agt)[1]
            : 0.0;
    }

    /**
     * 2017年版cndlとの、このゲームにおける会話ターゲットの一致度を返す まだ一度も候補となるイベントが無いなら、1.0を返す。
     *
     * @param agt エージェントID（0始まり）
     * @return
     */
    public double getVillagerSimilarity(int agt) {
        return targetSimilarity.containsKey(agt) && targetSimilarity.get(agt)[1] > 0
            ? (double) (targetSimilarity.get(agt)[0]) / targetSimilarity.get(agt)[1]
            : 1.0;
    }

    private double toProbability(Integer[] x) {
        return x[1] == 0 ? 0.0 : (double) (x[0]) / x[1];
    }

    public double wolfProbability(GameAgent agt) {
        if (agt.hasCO()) return 0.0; // COしていたら絶対に狼では無い。
        return wolfProbability(agt.getIndex());
    }

    /**
     * cndlベースだった場合には、狼確率を返す。 cndlベースで無いなら、あまり信用できない数字を返す。
     *
     * @param agt エージェントID (0始まり)
     * @return
     */
    private double wolfProbability(int agt) {
        if (villagerSimScore.get(agt)[1] == 0) return 0.0;
        // 2項分布の母数
        //   = {村人, 狼}の場合のtargetSimilarityの平均値
        double[] BiProb = new double[2];
        BiProb[0] = toProbability(villagerSimScore.get(agt));
        // 狼で一度もtargetが一致してないときは、確率が0になってしまうので、適当に数字を入れておく
        BiProb[1] = wolfSimScore.get(agt)[0] == 0 ? 0.2 : toProbability(wolfSimScore.get(agt));
        // 2項分布の計算
        Integer[] sim = targetSimilarity.get(agt);
        double BiCon = 1.0; // 2項係数 n C r
        for (int i = 2; i <= sim[1]; i++) {
            BiCon *= i;
        }
        for (int i = 2; i <= sim[0]; i++) {
            BiCon /= i;
        }
        for (int i = 2; i <= (sim[1] - i); i++) {
            BiCon /= i;
        }
        double[] prob = new double[2];
        prob[0] = BiCon;
        prob[1] = BiCon;
        for (int i = 0; i < sim[0]; ++i) {
            for (int j = 0; j < 2; ++j) {
                prob[j] *= BiProb[j];
            }
        }
        for (int i = 0; i < (sim[1] - sim[0]); ++i) {
            for (int j = 0; j < 2; ++j) {
                prob[j] *= (1.0 - BiProb[j]);
            }
        }
        return prob[1] / (prob[0] + prob[1]);
    }

    public boolean cndlLike(GameAgent agt) {
        return cndlLike(agt.getIndex());
    }

    private boolean cndlLike(int agt) {
        if (villagerSimScore.get(agt) == null) return false;
        if (villagerSimScore.get(agt)[1] == 0) return false;
        if (toProbability(cndlSimScore.get(agt)) > 0.5 && toProbability(villagerSimScore.get(agt)) > 0.3) return true;
        else return false;
    }
}

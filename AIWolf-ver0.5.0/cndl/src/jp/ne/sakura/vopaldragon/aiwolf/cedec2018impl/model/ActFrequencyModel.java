package jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.model;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.CndlPlayer;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.CndlStrategy;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.framework.EventType;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.framework.Game;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.framework.GameAgent;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.framework.GameEvent;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.framework.MetagameEventListener;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.util.HashCounter;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.util.Utils;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.util.VectorMath;
import org.aiwolf.common.data.Role;
import org.aiwolf.common.data.Species;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.framework.GameEventListener;

/**
 * AIの行動をベースにTalkFrequencyModelみたいなことを行うモデル
 */
public class ActFrequencyModel implements GameEventListener, MetagameEventListener, RolePredictor {

    private CndlStrategy cndlStrategy;

    private Game game;
    /* agent の role が r である 確率 */
    private Map<GameAgent, Map<Role, Double>> roleProbability;
    private Map<GameAgent, Map<Integer, Map<Integer, Map<Role, Map<ActionType, Double>>>>> actConditionalProbability = new HashMap<>();


    /* 各役職の人数 */
    private Map<Role, Double> roleNum;
    private List<TopicDetailEvent> events = new ArrayList<>();
    private Map<String, Double> P;
    //metagame要素
    public HashCounter<String> evc = new HashCounter<>();
    public HashCounter<String> evcRole = new HashCounter<>();
    /* エージェント別役職経験回数 */
    private Map<Integer, Map<Role, Integer>> roleCount = new HashMap<>();
    private Map<Integer, Map<Integer, Map<Integer, Map<Role, Map<ActionType, Double>>>>> metaActProbability = new HashMap<>();

    private String dataNaem;

    public ActFrequencyModel(String dataName, CndlStrategy cndlStrategy) {
        this.cndlStrategy = cndlStrategy;
        this.dataNaem = dataName;
        cndlStrategy.addPersistentEventListener(this);
        cndlStrategy.addMetagameEventListener(this);
    }

    public void startGame(Game game) {
        this.game = game;
        /* 役職人数を初期化 */
        roleNum = game.getRoleNum();
        /* エージェント別役職確率を初期化 */
        roleProbability = new HashMap<>();
        Role myRole = game.getSelf().role;
        for (GameAgent ga : game.getAgents()) {
            roleProbability.put(ga, new HashMap<>());
            Map<Role, Double> rp = roleProbability.get(ga);
            if (ga == game.getSelf() || (myRole == Role.WEREWOLF && ga.role == Role.WEREWOLF)) {
                roleNum.keySet().stream().forEach(x -> {
                    if (x == ga.role) {
                        rp.put(x, 1.0);
                    } else {
                        rp.put(x, 0.0);
                    }
                });
            } else {
                int vil = game.getVillageSize() - (myRole == Role.WEREWOLF && game.getVillageSize() == 15 ? 3 : 1);
                roleNum.keySet().stream().forEach(x -> {
                    double num = roleNum.get(x);
                    if (x == myRole) {
                        num -= myRole == Role.WEREWOLF && game.getVillageSize() == 15 ? 3.0 : 1.0;
                    }
                    rp.put(x, num / vil);
                });
            }
            /* 条件付き確率の初期化 */
            actConditionalProbability.put(ga, getTopicProbabilityByIndex(ga.getIndex()));
        }
    }

    public void endGame(Game game) {
        updateTopicProbability(game);
    }

    @Override
    public void handleEvent(Game g, GameEvent e) {
        switch (e.type) {
            case TALK:
            case VOTE:
                double[] divineScore = cndlStrategy.believeSeerModel.getDivineScore();
                double[] evilScore = cndlStrategy.evilModel.getEvilScore();
                double averageEvil = VectorMath.ave(evilScore);
                if (e.type == EventType.TALK) {
                    e.talks.forEach(t -> {
                        if (t.isRepeat) return;
                        switch (t.getTopic()) {
                            case DIVINED:
                            case IDENTIFIED:
                            case COMINGOUT:
                            case OVER:
                            case OPERATOR:
                                ActionType dt = ActionType.valueOf(t.getTopic().name());
                                updateRoleProbability(new TopicDetailEvent(t.getDay(), t.getTurn(), dt, t.getTalker()));
                                break;
                            case VOTE:
                                ActionType dtVote = null;
                                GameAgent target = t.getTarget();
                                if (target != null) {
                                    if (target.coRole == Role.SEER) {
                                        dtVote = ActionType.DEC_VOTE2SEER;
                                    } else if (target.coRole == Role.MEDIUM) {
                                        dtVote = ActionType.DEC_VOTE2MED;
                                    } else if (divineScore[target.getIndex()] == 1.0) {
                                        dtVote = ActionType.DEC_VOTE2BLACK;
                                    } else if (divineScore[target.getIndex()] == -1.0) {
                                        dtVote = ActionType.DEC_VOTE2WHITE;
                                    } else if (evilScore[target.getIndex()] >= averageEvil) {
                                        dtVote = ActionType.DEC_VOTE2EVIL;
                                    } else {
                                        dtVote = ActionType.DEC_VOTE2GOOD;
                                    }
                                    updateRoleProbability(new TopicDetailEvent(t.getDay(), t.getTurn(), dtVote, t.getTalker()));
                                }
                                break;
                            case ESTIMATE:
                                ActionType dtEst = null;
                                if (t.getRole().getSpecies() == Species.WEREWOLF) {
                                    dtEst = ActionType.ESTIMATE_WOLF;
                                } else {
                                    dtEst = ActionType.ESTIMATE_HUM;
                                }
                                updateRoleProbability(new TopicDetailEvent(t.getDay(), t.getTurn(), dtEst, t.getTalker()));
                                break;
                        }
                    });
                } else {
                    e.votes.forEach(v -> {
                        ActionType dtVote = null;
                        GameAgent target = v.target;
                        if (target.coRole == Role.SEER) {
                            dtVote = ActionType.VOTE2SEER;
                        } else if (target.coRole == Role.MEDIUM) {
                            dtVote = ActionType.VOTE2MED;
                        } else if (divineScore[target.getIndex()] == 1.0) {
                            dtVote = ActionType.VOTE2BLACK;
                        } else if (divineScore[target.getIndex()] == -1.0) {
                            dtVote = ActionType.VOTE2WHITE;
                        } else if (evilScore[target.getIndex()] >= averageEvil) {
                            dtVote = ActionType.VOTE2EVIL;
                        } else {
                            dtVote = ActionType.VOTE2GOOD;
                        }
                        updateRoleProbability(new TopicDetailEvent(e.day, v.isReVote ? 1 : 0, dtVote, v.initiator));
                    });
                }
                break;
            case ATTACK:
                updateRoleProbability(e.target, Role.WEREWOLF, false);
                break;
            case GUARD_SUCCESS:
                if (e.target != null) {
                    updateRoleProbability(e.target, Role.WEREWOLF, false);
                }
                break;
            case DIVINE:
            case MEDIUM:
                if (e.species == Species.HUMAN) {
                    updateRoleProbability(e.target, Role.WEREWOLF, false);
                } else {
                    updateRoleProbability(e.target, Role.WEREWOLF, true);
                }
                break;
            default:
                break;
        }
    }

    /**
     * target が role で ある/ない ことが確定した場合に確率を更新する。
     *
     * @param target
     * @param role
     * @param b
     */
    private void updateRoleProbability(GameAgent target, Role role, boolean b) {
        if (target == game.getSelf()) {
            return;
        }
        if (b) {
            for (Map.Entry<Role, Double> entry : roleProbability.get(target).entrySet()) {
                entry.setValue(0.0);
            }
            roleProbability.get(target).replace(role, 1.0);
        } else {
            roleProbability.get(target).put(role, 0.0);
            double sum = roleProbability.get(target).values().stream().mapToDouble(d -> d).sum();
            for (Map.Entry<Role, Double> entry : roleProbability.get(target).entrySet()) {
                entry.setValue(entry.getValue() / sum);
            }
        }
    }

    /**
     * agent のday日目第turnターンの発言のトピックが topic だった時 ベイズの公式に従って確率を更新する。 （事後確率を次の事前確率として使う） P_new (role) = P(role | talk) = (P(talk | role) * P(role))/Sum_for_roles(P(talk | role) * P(role))
     *
     * @param agent
     * @param topic
     * @param day
     * @param turn
     */
    private void updateRoleProbability(TopicDetailEvent event) {
        GameAgent agent = game.getAgentAt(event.initiatorIndex);
        events.add(event);
        if (agent == game.getSelf()) {
            return;
        }
        Map<Role, Double> newP = new HashMap<>();
        double z = 0.0; // 規格化定数
        for (Entry<Role, Double> entry : roleProbability.get(agent).entrySet()) {
            final Role role = entry.getKey();
            final double p = entry.getValue();
            newP.put(role, p * getTopicConditionalProbability(agent, event.day, event.turn, role, event.type));
            z += newP.get(role);
        }
        for (Entry<Role, Double> entry : roleProbability.get(agent).entrySet()) {
            Role role = entry.getKey();
            entry.setValue(newP.get(role) / z);
        }
        newP.clear();
    }

    private double getTopicConditionalProbability(GameAgent agent, int day, int turn, Role role, ActionType topic) {
        Map<Integer, Map<Integer, Map<Role, Map<ActionType, Double>>>> tcp = actConditionalProbability.get(agent);
        if (tcp.get(day) == null) {
            tcp.put(day, new HashMap<>());
        }
        if (tcp.get(day).get(turn) == null) {
            tcp.get(day).put(turn, new HashMap<>());
            for (Role r : Role.values()) {
                if (role == Role.FOX || role == Role.FREEMASON) {
                    continue;
                }
                tcp.get(day).get(turn).put(r, getCommonTCP(day, turn, r));
            }
        }
        return actConditionalProbability.get(agent).get(day).get(turn).get(role).get(topic);
    }

    private Map<ActionType, Double> getCommonTCP(int day, int turn, Role role) {
        /* ファイルから事前分布を読む */
        if (P == null) {
            P = new HashMap<>();
            String filename = game.getVillageSize() == 15 ? dataNaem + "-af-15.txt" : dataNaem + "-af-5.txt";
            try (BufferedReader br = new BufferedReader(new InputStreamReader(ActFrequencyModel.class.getResourceAsStream(filename)))) {
                String line = null;
                while ((line = br.readLine()) != null) {
                    String[] data = line.split("\t");
                    P.put(data[0], Double.parseDouble(data[1]));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        Map<ActionType, Double> ret = new HashMap<>();
        for (ActionType t : ActionType.values()) {
            ret.put(t, P.getOrDefault(String.join(",", "" + role, "" + day, "" + turn, "" + t), 1.0));
        }
        /* 規格化定数 */
        final double z = ret.values().stream().mapToDouble(x -> x).sum();
        ret.entrySet().stream().forEach(x -> x.setValue(x.getValue() / z));
        return ret;
    }

    @Override
    public RoleProbabilityStruct getRoleProbabilityStruct(Game game) {
        RoleProbabilityStruct rps = new RoleProbabilityStruct();
        game.getAgentStream().forEach(ag -> rps.roleProbabilities.put(ag, new HashMap<>(getRoleProbability(ag))));
        return rps;
    }

    public double getRoleProbability(GameAgent agent, Role role) {
        return roleProbability.get(agent).get(role);
    }

    private Map<Role, Double> getRoleProbability(GameAgent agent) {
        return roleProbability.get(agent);
    }

    /**
     * 1ゲーム終了時、そのゲーム中発生した発言に対応する確率を、少し上げる
     *
     * @param g
     */
    private void updateTopicProbability(Game g) {
        g.getAgentStream().filter(x -> !x.isSelf).forEach(x -> {
            if (!roleCount.containsKey(x.getIndex())) {
                roleCount.put(x.getIndex(), new HashMap<>());
            }
            if (roleCount.get(x.getIndex()).containsKey(x.role)) {
                roleCount.get(x.getIndex()).put(x.role, roleCount.get(x.getIndex()).get(x.role) + 1);
            } else {
                roleCount.get(x.getIndex()).put(x.role, 1);
            }
        });
        events.forEach(ev -> {
            GameAgent ga = g.getAgentAt(ev.initiatorIndex);
            if (g.getSelf() != ga) {
                final int day = ev.day;
                final int turn = ev.turn;
                final int agentIndex = ga.getIndex();
                final Role role = ga.role;
                final ActionType topic = ev.type;
                if (!CndlPlayer.PRODUCTION_FLAG) evc.countPlus(Utils.joinWith(",", role.name(), day, turn, topic));
                for (Entry<ActionType, Double> entry : metaActProbability.get(agentIndex).get(day).get(turn).get(role).entrySet()) {
//                    double freq = entry.getValue() * (23.0 + g.getMeta().numberOfGames - 1.0);
//                    double div = 23.0 + g.getMeta().numberOfGames;
                    double freq = entry.getValue() * (12.0 + roleCount.get(agentIndex).get(role));
                    double div = 13.0 + roleCount.get(agentIndex).get(role);
                    entry.setValue(entry.getKey() == topic ? (freq + 1.0) / div : freq / div);
                }
            }
        });
    }

    public Map<Integer, Map<Integer, Map<Role, Map<ActionType, Double>>>> getTopicProbabilityByIndex(int index) {
        Map<Integer, Map<Integer, Map<Integer, Map<Role, Map<ActionType, Double>>>>> tp = metaActProbability;
        if (tp.get(index) == null) {
            tp.put(index, new HashMap<>());
        }
        return tp.get(index);
    }

    public static class TopicDetailEvent {

        int day;
        int turn;
        ActionType type;
        int initiatorIndex;

        @Override
        public String toString() {
            return day + "\t" + turn + "\t" + type;
        }

        public TopicDetailEvent(int day, int turn, ActionType type, GameAgent ag) {
            this.day = Math.min(day, 7);
            this.turn = Math.min(turn, 10);
            this.type = type;
            this.initiatorIndex = ag.getIndex();
        }

    }

    public enum ActionType {

        OVER,
        DEC_VOTE2SEER,
        DEC_VOTE2MED,
        DEC_VOTE2BLACK,
        DEC_VOTE2WHITE,
        DEC_VOTE2EVIL,
        DEC_VOTE2GOOD,
        ESTIMATE_WOLF,
        ESTIMATE_HUM,
        VOTE2SEER,
        VOTE2MED,
        VOTE2BLACK,
        VOTE2WHITE,
        VOTE2EVIL,
        VOTE2GOOD,
        DIVINED,
        IDENTIFIED,
        COMINGOUT,
        OPERATOR
    }

}

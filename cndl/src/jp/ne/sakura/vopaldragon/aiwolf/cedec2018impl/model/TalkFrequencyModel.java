package jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.model;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.HashMap;
import java.util.stream.Stream;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.CndlPlayer;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.CndlStrategy;

import org.aiwolf.client.lib.Topic;
import org.aiwolf.common.data.Role;
import org.aiwolf.common.data.Species;

import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.framework.Game;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.framework.GameAgent;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.framework.GameEvent;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.framework.GameTalk;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.framework.MetagameEventListener;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.util.HashCounter;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.util.Utils;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.framework.GameEventListener;

/**
 * 過去のゲームの発言Topicの頻度分布から役職を予想するモデル
 *
 */
public class TalkFrequencyModel implements GameEventListener, MetagameEventListener, RolePredictor {

    private String name;

    public TalkFrequencyModel(String name, CndlStrategy cndlStrategy) {
        this.name = name;
        cndlStrategy.addPersistentEventListener(this);
        cndlStrategy.addMetagameEventListener(this);
    }

    private Game game;
    /* agent の role が r である 初期確率 */
    private Map<GameAgent, Map<Role, Double>> roleProbability;
    /* agent の role が r である 確率: 出力用 */
    private Map<GameAgent, Map<Role, Double>> roleProbabilityTemp;
    /* game.Day == d, game.Turn == t, agent.getindex == idx, agent.role == r
	 * の時に agentのtalk が　talk.getTopic = topic となる条件付き確率 */
    private Map<GameAgent, Map<Integer, Map<Integer, Map<Role, Map<Topic, Double>>>>> topicConditionalProbability = new HashMap<>();
    /* ファイルから事前分布を読む用Map */
    private Map<String, Double> P;
    /* 各役職の人数 */
    private Map<Role, Double> roleNum;

    @Override
    public void handleEvent(Game g, GameEvent e) {
        switch (e.type) {
            case TALK:
                calcRoleProbability(e.talks);
                break;
            case ATTACK:
                updateRoleProbability(e.target, Role.WEREWOLF, false);
                break;
            case DIVINE:
            case MEDIUM:
                if (e.species == Species.HUMAN) {
                    updateRoleProbability(e.target, Role.WEREWOLF, false);
                } else {
                    updateRoleProbability(e.target, Role.WEREWOLF, true);
                }
                break;
            case GUARD_SUCCESS:
                if (g.getSelf().role == Role.BODYGUARD) {
                    if (e.target != null) {
                        updateRoleProbability(e.target, Role.WEREWOLF, false);
                    }
                } else {
                    game.getAgentStream().filter(x -> !x.isAlive && !x.isSelf).forEach(x -> {
                        updateRoleProbability(x, Role.BODYGUARD, false);
                    });
                }
                break;
            case DAYSTART:
                /* ログを見る */
//                g.getAliveOthers().stream().forEachOrdered(ga -> {
//                    log(ga.getId() + roleProbability.get(ga));
//                });
                break;
            default:
                break;
        }
    }

    private void calcRoleProbability(List<GameTalk> talks) {
        Map<GameAgent, Map<Role, Double>> rpTemp = roleProbabilityTemp;
        if (talks.get(0).getTurn() == 0) {
            rpTemp.entrySet().forEach(e -> {
                rpTemp.get(e.getKey()).entrySet().forEach(e2 -> {
                    e2.setValue(roleProbability.get(e.getKey()).get(e2.getKey()));
                });
            });
        }
        Stream<GameTalk> talkstream = talks.get(0).getTurn() == 0 ? game.getAllTalks() : talks.stream();
        talkstream.filter(x -> x.getTalker() != game.getSelf()).forEach(t -> {
            rpTemp.put(t.getTalker(), calcRoleProbability(t.getTalker(), t.getTopic(), t.getDay(), t.getTurn(), rpTemp.get(t.getTalker())));
        });
        Map<GameAgent, Double> zs = new HashMap<>();
        rpTemp.entrySet().stream().forEach(e -> {
            zs.put(e.getKey(), e.getValue().values().stream().mapToDouble(x -> x).sum());
            e.getValue().entrySet().forEach(e2 -> e2.setValue(e2.getValue() / zs.get(e.getKey())));
        });
        reweight(rpTemp);
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
        }
        reweight(roleProbability);
    }

    /**
     * @param anotherRoleProbability 各エージェントの役職確率を破壊的にリウェイトする。
     */
    private void reweight(Map<GameAgent, Map<Role, Double>> anotherRoleProbability) {
//        log(anotherRoleProbability);
        roleNum.keySet().stream().forEach(role -> {
            /* 各エージェントの役職確率を、役職別に合計し、本来の人数と同じになるようリウェイト */
            double sum = anotherRoleProbability.keySet().stream()
                .mapToDouble(ag -> anotherRoleProbability.get(ag).get(role))
                .sum();
            anotherRoleProbability.keySet().stream().forEach(ag -> {
                anotherRoleProbability.get(ag).put(role, anotherRoleProbability.get(ag).get(role) / sum * roleNum.get(role));
            });
        });
//        log(anotherRoleProbability);
        /* 各エージェントの役職確率がそのエージェント内で足して1になるようリウェイト */
        anotherRoleProbability.keySet().stream().forEach(ag -> {
            Map<Role, Double> rp = anotherRoleProbability.get(ag);
            double sum = rp.entrySet().stream().mapToDouble(e -> e.getValue()).sum();
            rp.entrySet().stream().forEach(e -> e.setValue(e.getValue() / sum));
        });
//        log(anotherRoleProbability);
    }

    /**
     * 事後確率の分子を計算する。<br>
     * （規格化されていない）
     *
     * @param agent
     * @param topic
     * @param day
     * @param turn
     * @param originalP
     * @return
     */
    private Map<Role, Double> calcRoleProbability(GameAgent agent, Topic topic, int day, int turn, Map<Role, Double> originalP) {
        Map<Role, Double> newP = new HashMap<>();
        for (Entry<Role, Double> entry : originalP.entrySet()) {
            final Role role = entry.getKey();
            final double p = entry.getValue();
            newP.put(role, p * getTopicConditionalProbability(agent, day, turn, role, topic));
        }
        return newP;
    }

    private double getTopicConditionalProbability(GameAgent agent, int day, int turn, Role role, Topic topic) {
        Map<Integer, Map<Integer, Map<Role, Map<Topic, Double>>>> tcp = topicConditionalProbability.get(agent);
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
        return topicConditionalProbability.get(agent).get(day).get(turn).get(role).get(topic);
    }

    /**
     * エージェントに依らない条件付き確率の初期値を返す
     *
     * @param day
     * @param turn
     * @param role
     * @return
     */
    private Map<Topic, Double> getCommonTCP(int day, int turn, Role role) {
        /* ファイルから事前分布を読む */
        if (P == null) {
            P = new HashMap<>();
            String filename = game.getVillageSize() == 15 ? name + "-tf-15.txt" : name + "-tf-5.txt";
            try (BufferedReader br = new BufferedReader(new InputStreamReader(TalkFrequencyModel.class.getResourceAsStream(filename)))) {
                String line = null;
                while ((line = br.readLine()) != null) {
                    String[] data = line.split("\t");
                    P.put(data[0], Double.parseDouble(data[1]));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        Map<Topic, Double> ret = new HashMap<>();
        for (Topic t : Topic.values()) {
            if (t != Topic.ATTACK) {
                ret.put(t, P.getOrDefault(String.join(",", "" + role, "" + day, "" + turn, "" + t), 1.0));
            }
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
        return roleProbabilityTemp.get(agent).get(role);
    }

    public Map<Role, Double> getRoleProbability(GameAgent agent) {
        return roleProbabilityTemp.get(agent);
    }

    public Map<GameAgent, Map<Role, Double>> getRoleProbability() {
        return roleProbabilityTemp;
    }

    /* 試合開始時の処理 */
    @Override
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
            topicConditionalProbability.put(ga, getTopicProbabilityByIndex(ga.getIndex()));
        }
        roleProbabilityTemp = new HashMap<>();
        roleProbability.entrySet().forEach(e -> {
            roleProbabilityTemp.put(e.getKey(), new HashMap<>());
            e.getValue().entrySet().forEach(e2 -> roleProbabilityTemp.get(e.getKey()).put(e2.getKey(), e2.getValue()));
        });
    }

    public HashCounter<String> tkc = new HashCounter<>();

    @Override
    public void endGame(Game game) {
        updateTopicProbability(game);
    }

    /* エージェント別発言条件付き確率 */
    private Map<Integer, Map<Integer, Map<Integer, Map<Role, Map<Topic, Double>>>>> topicProbability = new HashMap<>();
    /* エージェント別役職経験回数 */
    private Map<Integer, Map<Role, Integer>> roleCount = new HashMap<>();

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
        g.getAllTalks().forEach(t -> {
            GameAgent ga = t.getTalker();
            if (g.getSelf() != ga) {
                final int day = t.getDay();
                final int turn = t.getTurn();
                final int agentIndex = ga.getIndex();
                final Role role = ga.role;
                final Topic topic = t.getTopic();
                if (!CndlPlayer.PRODUCTION_FLAG) tkc.countPlus(Utils.joinWith(",", role.name(), day, turn, topic));
                for (Entry<Topic, Double> entry : topicProbability.get(agentIndex)
                    .get(day)
                    .get(turn)
                    .get(role)
                    .entrySet()) {
                    double freq = entry.getValue() * (12.0 + roleCount.get(agentIndex).get(role));
                    double div = 13.0 + roleCount.get(agentIndex).get(role);
                    entry.setValue((entry.getKey() == topic ? (freq + 1.0) : freq) / div);
                }
            }
        });
    }

    public Map<Integer, Map<Integer, Map<Role, Map<Topic, Double>>>> getTopicProbabilityByIndex(int index) {
        Map<Integer, Map<Integer, Map<Integer, Map<Role, Map<Topic, Double>>>>> tp = topicProbability;
        if (tp.get(index) == null) {
            tp.put(index, new HashMap<>());
        }
        return tp.get(index);
    }
}

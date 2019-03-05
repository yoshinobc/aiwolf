package jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.model;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.CndlStrategy;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.framework.Game;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.framework.GameAgent;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.framework.GameEvent;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.framework.GameTalk;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.util.ListMap;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.util.VectorMath;
import org.aiwolf.client.lib.Topic;
import org.aiwolf.common.data.Role;
import org.aiwolf.common.data.Species;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.framework.GameEventListener;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.util.DataScore;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.util.DataScoreList;

/**
 * Agentの行動・発言の信頼性をチェックするモデル：信頼性の初期値は100
 */
public class AgentReliabilityModel implements GameEventListener {

    private double[] scores;
    private Game game;
    private GameAgent[] voteDeclare;
    private int size;

    private ListMap<GameAgent, GameAgent> blackDivineList = new ListMap<>();
    private ListMap<GameAgent, GameAgent> whiteDivineList = new ListMap<>();

    public AgentReliabilityModel(CndlStrategy strategy) {
        this.game = strategy.getGame();
        size = game.getVillageSize();
        scores = new double[size];
        voteDeclare = new GameAgent[size];
        Arrays.fill(scores, 100);
        strategy.addEventListener(this);
    }

    private void addScore(GameAgent target, double delta) {
        if (scores[target.getIndex()] == 0) return;
        scores[target.getIndex()] += delta;
        if (scores[target.getIndex()] > 200) scores[target.getIndex()] = 200;
        if (scores[target.getIndex()] < 0) scores[target.getIndex()] = 0;
    }

    /**
     * スコアが高いほど信頼性が高い
     * L2正規化済み（0～1）
     */
    public double[] getScore() {
        return VectorMath.normalize(Arrays.copyOf(scores, size));
    }

    public DataScoreList<GameAgent> getScoreList(List<GameAgent> agents) {
        DataScoreList<GameAgent> list = new DataScoreList<>();
        for (GameAgent ag : agents) {
            list.add(new DataScore<>(ag, scores[ag.getIndex()]));
        }
        return list;
    }

    /**
     * 他のモデルで利用する、各人の発言の信頼性
     */
    public double[] getReliability() {
        double[] s = Arrays.copyOf(scores, size);
        VectorMath.divide(s, 100);
        return s;
    }

    public static final int L3 = 20;
    public static final int L2 = 10;
    public static final int L1 = 5;
    public static final int BAN = -200;

    @Override
    public void handleEvent(Game g, GameEvent e) {
        switch (e.type) {
            case TALK:
                e.talks.forEach(t -> {
                    //同じ発言を二回評価しない
                    if (t.isRepeat) return;
                    //自分の発言は無視
                    if (t.getTalker().isSelf) return;

                    int talkerInd = t.getTalker().getIndex();
                    GameAgent talker = t.getTalker();
                    switch (t.getTopic()) {
                        case COMINGOUT:
                            Optional<GameAgent> coedAgent = game.getAgentStream().filter(ag -> ag.coRole == t.getRole()).findFirst();
                            if (talker.coRole != null) {
                                //すでにCOしてきた人のCO
                                if (talker.coRole != t.getRole()) {
                                    //違う役職でCOしてきたやつはマイナス
                                    addScore(talker, -L2);
                                }
                            } else if (t.getRole() != Role.VILLAGER && t.getRole() == game.getSelf().role) {
                                //自分の役職
                                addScore(talker, -200);
                            } else if (coedAgent.isPresent()) {
                                //他の人がCOしている役職で、1ターン以上後出しの場合
                                Optional<GameTalk> coTalk = coedAgent.get().talkList.stream().filter(co -> co.getTopic() == Topic.COMINGOUT && co.getRole() == t.getRole()).findFirst();
                                if (coTalk.isPresent()) {
                                    GameTalk cot = coTalk.get();
                                    if (cot.getDay() < t.getDay()
                                        || (cot.getDay() == t.getDay() && cot.getTurn() < t.getTurn())) {
                                        switch (t.getRole()) {
                                            case SEER:
                                                addScore(talker, -L2);
                                                break;
                                            case MEDIUM:
                                                addScore(talker, -L1);
                                                break;
                                            case BODYGUARD:
                                                addScore(talker, -L1);
                                                break;
                                        }
                                    }
                                }
                            }
                            if (!(g.getDay() == 1 && t.getTurn() == 0)) {
                                //最初期COでない場合
                                addScore(talker, -L1);
                            }
                            break;
                        case AGREE:
                            break;
                        case DISAGREE:
                            break;
                        case ESTIMATE:
                            break;
                        case IDENTIFIED:
                            if (game.getSelf().role != Role.MEDIUM) {
                                double d = scores[t.getTalker().getIndex()] / 100;
                                game.getUniqueTalks().filter(tk
                                    -> (tk.getTopic() == Topic.DIVINED || tk.getTopic() == Topic.ESTIMATE)
                                    && tk.getTarget() == t.getTarget()).forEach(tk -> {
                                    if (tk.getTopic() == Topic.DIVINED) {
                                        //同定結果と過去の占いの比較
                                        if (t.getResult() != tk.getResult()) addScore(tk.getTalker(), -L3 * d);//偽黒出しないしは白出し
                                        else addScore(tk.getTalker(), L3 * d);//正しい推測は評価
                                    }
                                });
                            }
                            break;
                        case DIVINED:
                            if (game.getSelf().role != Role.SEER) {
                                //変な占いを処罰
                                if (t.getTarget().isExecuted) {
                                    addScore(talker, -L3);
                                }

                                if (t.getTarget() == talker) {
                                    addScore(talker, -L3);
                                }

                                //自分への占い結果を評価
                                if (t.getTarget() == game.getSelf()) {
                                    if (t.getResult() == Species.WEREWOLF) {
                                        if (game.getSelf().role != Role.WEREWOLF) {
                                            addScore(talker, BAN);
                                        } else {
                                            addScore(talker, L3);
                                        }
                                    } else {
                                        if (game.getSelf().role != Role.WEREWOLF) {
                                            addScore(talker, L3);
                                        } else {
                                            addScore(talker, BAN);
                                        }
                                    }
                                }

                                //自称占いが３人未満の時の占いへの黒出しはペナルティ。狂人が占いCOしないで狼がしている可能性は低いため。
                                if (t.getTarget().coRole == Role.SEER && game.getAgentStream().filter(a -> a.coRole == Role.SEER).count() <= 2) {
                                    addScore(talker, -L2);
                                }

                                //占い結果を記録
                                if (t.getResult() == Species.HUMAN) {
                                    whiteDivineList.add(talker, t.getTarget());
                                } else {
                                    blackDivineList.add(talker, t.getTarget());
                                }
                            }
                            break;
                        case DIVINATION:
                            //占い宣言を記録
                            if (t.getTarget().isAlive) {
                                addScore(talker, L1);
                            }
                            break;
                        case VOTE:
                            //投票宣言を記録
                            voteDeclare[t.getTalker().getIndex()] = t.getTarget();
                            break;
                    }

                });
                break;
            case VOTE:
                e.votes.forEach(v -> {
                    //投票先と投票宣言が異なっている場合に評価減
                    if (voteDeclare[v.initiator.getIndex()] != null && voteDeclare[v.initiator.getIndex()] != v.target) {
                        addScore(v.initiator, -L1);
                    }
                });
                break;
            case ATTACK:
                //襲撃後の処理
                game.getUniqueTalks().filter(t
                    -> (t.getTopic() == Topic.DIVINED || t.getTopic() == Topic.ESTIMATE)
                    && t.getTarget() == e.target).forEach(t -> {
                    if (t.getTopic() == Topic.DIVINED) {
                        //襲撃結果と過去の占いの比較
                        if (t.getResult() == Species.WEREWOLF) addScore(t.getTalker(), BAN);
                    }
                });
                //襲撃された人は白
                addScore(e.target, L1);
                break;
            case GUARD_SUCCESS:
                //GoogJob
                if (e.target != null) {
                    addScore(e.target, L3);
                }
                break;
            case MEDIUM:
                //霊媒師のみわかる結果
                if (e.species == Species.WEREWOLF) {
                    addScore(e.target, BAN);
                } else {
                    addScore(e.target, L3);
                }
                GameAgent executed = e.target;
//                game.getLastEventOf(EventType.EXECUTE).target;
                game.getUniqueTalks().filter(tk
                    -> (tk.getTopic() == Topic.DIVINED || tk.getTopic() == Topic.ESTIMATE)
                    && tk.getTarget() == executed).forEach(tk -> {
                    if (tk.getTopic() == Topic.DIVINED) {
                        //同定結果と過去の占いの比較
                        if (e.species != tk.getResult()) addScore(tk.getTalker(), -L2);
                        else addScore(tk.getTalker(), L2);//正しい占いは評価
                    }
                });
                break;
            case DIVINE:
                //占い師のみわかる結果
                if (e.species == Species.WEREWOLF) {
                    addScore(e.target, BAN);
                } else {
                    addScore(e.target, L3);
                }
                break;
            case EXECUTE:
                //追放？
                break;
            case DAYSTART:
                if (game.getDay() > 1) {
                    List<GameAgent> agents = game.getAgents();

                    List<GameAgent> seers = game.getAgentStream().filter(ag -> ag.coRole == Role.SEER).collect(Collectors.toList());
                    List<GameAgent> mediums = game.getAgentStream().filter(ag -> ag.coRole == Role.MEDIUM).collect(Collectors.toList());

                    //COが一人なら信頼度を上げる
                    if (seers.size() == 1) seers.forEach(ag -> addScore(ag, L2));
                    if (mediums.size() == 1) mediums.forEach(ag -> addScore(ag, L2));

                    //仕事をしない占い師と霊媒の評価を下げる
                    seers.stream().forEach(ga -> {
                        if (ga.isAlive
                            && !ga.talkList.stream().filter(t -> t.getDay() == game.getDay() - 1).anyMatch(t -> t.getTopic() == Topic.DIVINED)) {
                            addScore(ga, -L3);
                        }
                    });
                    mediums.stream().forEach(ga -> {
                        if (game.getDay() > 2 && ga.isAlive
                            && !ga.talkList.stream().filter(t -> t.getDay() == game.getDay() - 1).anyMatch(t -> t.getTopic() == Topic.IDENTIFIED)) {
                            addScore(ga, -L3);
                        }
                    });

                    //嘘黒出しが続いた場合のPunish
                    int aliveSize = g.getAliveSize();
                    seers.forEach(ag -> {
                        long aliveWolf = blackDivineList.getList(ag).stream().filter(wolf -> wolf.isAlive).count();
                        //ゲームが終了している
                        if (aliveWolf >= aliveSize / 2) {
                            addScore(ag, BAN);
                        }
                    });

                }
                break;
        }
    }

}

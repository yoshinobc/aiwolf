package jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.model;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.CndlStrategy;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.framework.Game;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.framework.GameAgent;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.framework.GameEvent;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.util.Utils;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.util.VectorMath;
import org.aiwolf.common.data.Role;
import org.aiwolf.common.data.Species;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.framework.GameEventListener;

/**
 * 占い師の言うことを、信頼性をベースに信じるモデル
 */
public class BelieveSeerModel implements GameEventListener {

    private Map<GameAgent, double[]> seerScores = new HashMap<>();
    private Map<GameAgent, double[]> mediumScores = new HashMap<>();
    private Game game;
    private int size;
    private AgentReliabilityModel reliabilityModel;

    public BelieveSeerModel(CndlStrategy strategy, AgentReliabilityModel reliabilityModel) {
        this.game = strategy.getGame();
        strategy.addEventListener(this);
        size = game.getVillageSize();
        this.reliabilityModel = reliabilityModel;
    }

    public double[] getDivineScore() {
        double[] result = new double[size];
        double[] reliability = reliabilityModel.getReliability();
        List<GameAgent> seers = game.getAgentStream().filter(ag -> ag.coRole == Role.SEER).collect(Collectors.toList());
        if (!seers.isEmpty()) {
            //最も信頼性の高い占いを決定
            seers = Utils.getHighestScores(seers, ag -> reliability[ag.getIndex()]);
            for (GameAgent seer : seers) {
                double[] temp = seerScores.getOrDefault(seer, new double[size]);
                result = VectorMath.addAll(result, temp);
            }
        }
        return result;
    }

    public double[] getScore() {
        double[] score = new double[size];
        double[] reliability = reliabilityModel.getReliability();
        if (game.getSelf().role == Role.SEER) {
            double[] result = seerScores.getOrDefault(game.getSelf(), new double[size]);
            VectorMath.normalizeL1(result);
            VectorMath.multiply(result, 2);//強く利かせる
            score = result;
        } else {
            List<GameAgent> seers = game.getAgentStream().filter(ag -> ag.coRole == Role.SEER).collect(Collectors.toList());
            if (!seers.isEmpty()) {
                //最も信頼性の高い占いを決定
                seers = Utils.getHighestScores(seers, ag -> reliability[ag.getIndex()]);
                double[] result = new double[size];
                for (GameAgent seer : seers) {
                    double[] temp = seerScores.getOrDefault(seer, new double[size]);
                    result = VectorMath.addAll(result, temp);
                }
                VectorMath.normalizeL1(result);
                VectorMath.multiply(result, reliability[seers.get(0).getIndex()] / 2);//強く利いてしまうので2で割っとく
                score = result;
            }
        }
        if (game.getSelf().role == Role.MEDIUM) {
            double[] result = mediumScores.getOrDefault(game.getSelf(), new double[size]);
            VectorMath.normalizeL1(result);
            VectorMath.multiply(result, 2);//強く利かせる
            score = VectorMath.addAll(score, result);
        } else {
            List<GameAgent> mediums = game.getAgentStream().filter(ag -> ag.coRole == Role.MEDIUM).collect(Collectors.toList());
            if (!mediums.isEmpty()) {
                //最も信頼性の高い霊媒を決定
                mediums = Utils.getHighestScores(mediums, ag -> reliability[ag.getIndex()]);
                double[] result = new double[size];
                for (GameAgent medium : mediums) {
                    double[] temp = mediumScores.getOrDefault(medium, new double[size]);
                    result = VectorMath.addAll(result, temp);
                }
                VectorMath.normalizeL1(result);
                VectorMath.multiply(result, reliability[mediums.get(0).getIndex()] / 2);//強く利いてしまうので2で割っとく
                score = VectorMath.addAll(score, result);
            }
        }

        return score;
    }

    @Override
    public void handleEvent(Game g, GameEvent e) {
        switch (e.type) {
            case TALK:
                if (g.getSelf().role == Role.SEER) break;
                e.talks.forEach(t -> {
                    //同じ発言を二回評価しない
                    if (t.isRepeat) return;
                    GameAgent talker = t.getTalker();
                    //自分に関する判定は無視
                    if (talker == t.getTarget()) return;
                    double[] score = null;
                    if (talker.coRole == Role.SEER) {
                        score = seerScores.get(talker);
                        if (score == null) {
                            score = new double[size];
                            seerScores.put(talker, score);
                        }
                    } else if (talker.coRole == Role.MEDIUM) {
                        score = mediumScores.get(talker);
                        if (score == null) {
                            score = new double[size];
                            mediumScores.put(talker, score);
                        }
                    } else {
                        return;
                    }
                    switch (t.getTopic()) {
                        case DIVINED:
                            if (t.getResult() == Species.HUMAN) {
                                score[t.getTarget().getIndex()] = -1;
                            } else {
                                score[t.getTarget().getIndex()] = 1;
                            }
                            break;
                        case ESTIMATE:
                            if (talker.coRole == Role.SEER) {
                                if (t.getRole().getSpecies() == Species.HUMAN) {
                                    if (score[t.getTarget().getIndex()] != -1) score[t.getTarget().getIndex()] = -0.1;
                                } else {
                                    if (score[t.getTarget().getIndex()] != 1) score[t.getTarget().getIndex()] = 0.1;
                                }
                            }
                            break;
                        case IDENTIFIED:
                            if (t.getResult() == Species.HUMAN) {
                                score[t.getTarget().getIndex()] = -1;
                            } else {
                                score[t.getTarget().getIndex()] = 1;
                            }
                            break;
                    }
                });
                break;
            case MEDIUM:
                //自分の霊媒結果
                double[] mScore = mediumScores.get(game.getSelf());
                if (mScore == null) {
                    mScore = new double[size];
                    mediumScores.put(game.getSelf(), mScore);
                }
                if (e.species == Species.HUMAN) {
                    mScore[e.target.getIndex()] = -1;
                } else {
                    mScore[e.target.getIndex()] = 1;
                }
                break;
            case DIVINE:
                //自分の占い結果
                double[] sScore = seerScores.get(game.getSelf());
                if (sScore == null) {
                    sScore = new double[size];
                    seerScores.put(game.getSelf(), sScore);
                }
                if (e.species == Species.HUMAN) {
                    sScore[e.target.getIndex()] = -1;
                } else {
                    sScore[e.target.getIndex()] = 1;
                }
                break;
        }
    }

}

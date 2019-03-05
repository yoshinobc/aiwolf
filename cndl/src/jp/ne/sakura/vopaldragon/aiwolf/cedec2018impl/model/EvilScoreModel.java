package jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.model;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.CndlStrategy;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.framework.Game;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.framework.GameAgent;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.util.Utils;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.util.VectorMath;
import org.aiwolf.common.data.Role;

/**
 * 狼の推定モデル
 */
public class EvilScoreModel implements RolePredictor {

    //各種状態
    private Game game;
    private CndlStrategy strategy;

    public EvilScoreModel(CndlStrategy strategy) {
        game = strategy.getGame();
        this.strategy = strategy;
    }

    private boolean once3seer = false;
    private boolean once2medium = false;

    /**
     * 狼らしさを推定するスコア。信頼性を加味した占い・霊媒の情報と役割確率を足し合わせ、いくつかの救済条項を追加。
     */
    public double[] getEvilScore() {

        double[] divinerScore = strategy.believeSeerModel.getScore();

        double[] tfScore = getRoleProbability(Role.WEREWOLF);

        double[] integratedScore = VectorMath.addAll(divinerScore, tfScore);

        if (game.getDay() <= 4) {
            //占いCOが二人までなら、4日目まで占い師は見逃す
            if (game.getSelf().role != Role.SEER && !once3seer) {
                List<GameAgent> seers = game.getAgentStream().filter(ag -> ag.isAlive && ag.coRole == Role.SEER).collect(Collectors.toList());
                if (seers.size() <= 2) {
                    for (GameAgent ag : seers) {
                        integratedScore[ag.getIndex()] = 0;
                    }
                } else {
                    once3seer = true;
                }
            }

            //霊媒COが一人までなら、4日目までは霊媒は見逃す
            if (game.getSelf().role != Role.MEDIUM && !once2medium) {
                List<GameAgent> mediums = game.getAgentStream().filter(ag -> ag.isAlive && ag.coRole == Role.MEDIUM).collect(Collectors.toList());
                if (mediums.size() <= 1) {
                    for (GameAgent ag : mediums) {
                        integratedScore[ag.getIndex()] = 0;
                    }
                } else {
                    once2medium = true;
                }
            }
        }

        //信頼性が一定値以上のAgentはEvilではない()
        double[] relScore = strategy.agentReliabilityModel.getReliability();
        for (GameAgent ag : game.getAliveOthers()) {
            if (relScore[ag.getIndex()] >= 1.3 && ag.coRole == Role.SEER) {
                integratedScore[ag.getIndex()] = 0;
            }
        }

        //自分が狼で無い場合には、スコアを強制的に0に
        if (game.getSelf().role != Role.WEREWOLF) {
            integratedScore[game.getSelf().getIndex()] = 0;
        }
        return VectorMath.normalize(integratedScore);
    }

    /**
     * 信頼性と役割確率を使って、占い師らしさを求める
     */
    public double[] getSeerScore() {
        double[] tfScore = getRoleProbability(Role.SEER);
        double[] relScore = strategy.agentReliabilityModel.getScore();
        double[] score = VectorMath.addAll(tfScore, relScore);
        VectorMath.normalize(score);
        return score;
    }

    /**
     * TFとAFを使って特定の役割の確率っぽいスコアを求める。
     */
    public double[] getRoleProbability(Role role) {
        RoleProbabilityStruct rps = strategy.bestPredictor.getRoleProbabilityStruct(game);
        double[] score1 = new double[game.getVillageSize()];
        game.getAgentStream().filter(ag -> !ag.isSelf).forEach(ag -> score1[ag.getIndex()] = rps.getScore(ag, role));
        return score1;
    }

    public Map<Role, Double> getRoleProbability(GameAgent agent) {
        Map<Role, Double> result = new HashMap<>();
        for (Role r : Utils.existingRole(game)) {
            result.put(r, 0.0);
        }
        result.put(Role.WEREWOLF, getEvilScore()[agent.getIndex()]);
        result.put(Role.SEER, getSeerScore()[agent.getIndex()]);
        return result;
    }

    @Override
    public RoleProbabilityStruct getRoleProbabilityStruct(Game game) {
        RoleProbabilityStruct rps = new RoleProbabilityStruct();
        game.getAgentStream().forEach(ag -> rps.roleProbabilities.put(ag, new HashMap<>(getRoleProbability(ag))));
        return rps;
    }

}

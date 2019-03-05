package jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.target;

import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.CndlStrategy;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.framework.Game;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.framework.GameAgent;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.model.RoleProbabilityStruct;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.util.DataScore;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.util.DataScoreList;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.util.HashCounter;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.util.ListMap;
import static jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.util.Utils.log;
import org.aiwolf.common.data.Role;

public class GuardBasic2 extends CndlTargetTactic {

    @Override
    public GameAgent targetImpl(Game game, CndlStrategy strategy) {

        RoleProbabilityStruct rps = strategy.bestPredictor.getRoleProbabilityStruct(game);

        ListMap<Role, GameAgent> coMap = new ListMap<>();
        game.getAgentStream().filter(ag -> ag.isAlive && ag.hasCO()).forEach(ag -> coMap.add(ag.coRole, ag));
        log("co-map", coMap);
        int seerNum = coMap.getList(Role.SEER).size();
        double[] reliablityScore = strategy.evilModel.getSeerScore();
        double[] evilScore = strategy.evilModel.getEvilScore();

        GameAgent guardTarget = null;
        if (seerNum >= 1) {
            DataScoreList<GameAgent> seerScore = new DataScoreList<>();
            for (GameAgent ag : coMap.getList(Role.SEER)) {
                seerScore.add(ag, rps.getScore(ag, Role.SEER) + reliablityScore[ag.getIndex()]);
            }
            seerScore.sort();
            for (DataScore<GameAgent> agt : seerScore) {
                if (rps.topRole(agt.data).data == Role.WEREWOLF || rps.topRole(agt.data).data == Role.POSSESSED) continue;
                guardTarget = agt.data;
                break;
            }
        }

        if (guardTarget == null) {

            int medNum = coMap.getList(Role.MEDIUM).size();
            if (medNum >= 1) {
                DataScoreList<GameAgent> medScore = new DataScoreList<>();
                for (GameAgent ag : coMap.getList(Role.MEDIUM)) {
                    medScore.add(ag, rps.getScore(ag, Role.MEDIUM) + reliablityScore[ag.getIndex()]);
                }
                medScore.sort();
                for (DataScore<GameAgent> agt : medScore) {
                    if (rps.topRole(agt.data).data == Role.WEREWOLF || rps.topRole(agt.data).data == Role.POSSESSED) continue;
                    guardTarget = agt.data;
                    break;
                }
            }
        }

        //占いも霊媒もいない場合、狼へのHateが高い人を守る
        if (guardTarget == null) {
            DataScoreList<GameAgent> haterList = new DataScoreList<>();
            for (GameAgent ag : game.getAliveOthers()) {
                HashCounter<GameAgent> hateCounter = strategy.hostilityEvalModel.hateScoreOf(ag);
                double score = 0;
                for (GameAgent tgt : hateCounter.getKeyList()) {
                    score += hateCounter.getCount(tgt) * rps.getScore(tgt, Role.WEREWOLF);
                }
                haterList.add(ag, score / hateCounter.totalCount());
            }
            haterList.sort();

            for (DataScore<GameAgent> agt : haterList) {
                if (rps.topRole(agt.data).data == Role.WEREWOLF || rps.topRole(agt.data).data == Role.POSSESSED) continue;
                guardTarget = agt.data;
                break;
            }
        }
        
        if (guardTarget == null) {
            guardTarget = rps.getAgnetProbabilityList(Role.WEREWOLF, game.getAliveOthers()).reverse().get(0).data;//最も狼らしくないやつ
        }

        return guardTarget;
    }

}

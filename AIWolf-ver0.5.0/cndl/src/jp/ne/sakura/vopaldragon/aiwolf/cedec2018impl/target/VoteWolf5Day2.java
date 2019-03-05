package jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.target;

import java.util.List;
import java.util.stream.Collectors;

import org.aiwolf.common.data.Role;

import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.CndlStrategy;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.evalmodel.Village5TacEvalModel.Village5Tactics;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.framework.Game;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.framework.GameAgent;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.model.RoleProbabilityStruct;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.util.DataScore;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.util.DataScoreList;

/**
 *
 * 5人村２日目、生き残っている占いCOの人数によって場合を分ける。
 *
 */
public class VoteWolf5Day2 extends CndlTargetTactic {

    @Override
    public GameAgent targetImpl(Game game, CndlStrategy strategy) {
        GameAgent target = null;
        RoleProbabilityStruct rps = strategy.bestPredictor.getRoleProbabilityStruct(game);

        List<GameAgent> seerCOs = game.getAliveOthers().stream().filter(ag -> ag.coRole == Role.SEER).collect(Collectors.toList());
        if (seerCOs.size() == 1) {
            // 占いCOが1人生存しているなら、COしていないのが狼
            for (GameAgent ag : game.getAliveOthers()) {
                if (!seerCOs.contains(ag)) {
                    return ag;
                }
            }
        } else if (seerCOs.size() == 2) {
            // 占いCOが2人なら、騙り履歴のあるエージェントを狼とする。二人共に騙り履歴がある場合、より狼率が高い方。
            DataScoreList<GameAgent> sortedSeerCOs = rps.getAgnetProbabilityList(Role.WEREWOLF, seerCOs);
            for (DataScore<GameAgent> ag : sortedSeerCOs) {
                Village5Tactics AgTac = strategy.v5TacEvalModel.getTacitcsMap(ag.data);
                if (AgTac.day2HasfakeCoSeer) {
                    return ag.data;
                }
            }
        }

        /* 特定できなければ狼スコアを信じる。 */
        List<DataScore<GameAgent>> candidates = rps.getAgnetProbabilityList(Role.WEREWOLF, game.getAliveOthers());
        target = candidates.get(0).data;

        return target;
    }

}

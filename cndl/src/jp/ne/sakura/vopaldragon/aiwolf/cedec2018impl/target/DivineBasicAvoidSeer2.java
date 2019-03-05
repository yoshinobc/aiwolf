package jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.target;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.CndlStrategy;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.model.EvilScoreModel;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.framework.Game;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.framework.GameAgent;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.model.RoleProbabilityStruct;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.util.DataScore;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.util.DataScoreList;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.util.Utils;
import org.aiwolf.common.data.Role;

/**
 * 占いの基本戦術。Evilスコアが高い順に占うが、SEERCOしている人は飛ばす。DivineWithEvilScoreと併用して、SEER-COのやつは吊り、ステルス人狼を探す
 */
public class DivineBasicAvoidSeer2 extends CndlTargetTactic {

    private Set<GameAgent> divined;

    public DivineBasicAvoidSeer2(Set<GameAgent> divined) {
        this.divined = divined;
    }

    @Override
    public GameAgent targetImpl(Game game, CndlStrategy strategy) {
        List<GameAgent> alives = game.getAliveOthers();
        alives.removeAll(divined);
        if (alives.isEmpty()) return null;

        GameAgent target = null;
        RoleProbabilityStruct rps = strategy.bestPredictor.getRoleProbabilityStruct(game);
        List<DataScore<GameAgent>> stealthList = strategy.predictionEvalModel.getAgentUnpredictableness(Role.WEREWOLF);
        DataScoreList<GameAgent> nonWolfList = rps.getAgnetProbabilityList(Role.WEREWOLF);
        nonWolfList.removeIf(ds -> ds.score < 0.01);//狼確率が著しく低いエージェントは除外
        nonWolfList.reverse();
        stealthList.addAll(nonWolfList);
        stealthList.removeIf(ds -> divined.contains(ds.data) || !ds.data.isAlive);
        long seerCount = game.getAgentStream().filter(ag -> ag.isAlive && ag.coRole == Role.SEER).count();
        long medCount = game.getAgentStream().filter(ag -> ag.isAlive && ag.coRole == Role.MEDIUM).count();
        for (DataScore<GameAgent> targetDs : stealthList) {
            DataScoreList<Role> roles = rps.getRoleProbabilityList(targetDs.data);
            //SEERのCOしているやつは吊ればいいので、占わない
            if (seerCount <= 4 && targetDs.data.coRole == Role.SEER) continue;
            //MEDIUM一人の場合はだいたい本物なので、占わない
            if (medCount == 1 && targetDs.data.coRole == Role.MEDIUM) continue;
            if (roles.get(0).data == Role.WEREWOLF || roles.get(1).data == Role.WEREWOLF) {
                target = targetDs.data;
                break;
            }
        }

        if (target == null) {
            List<GameAgent> aliveOthers = game.getAliveOthers();
            aliveOthers.removeAll(divined);
            target = Utils.getRandom(aliveOthers);
        }
        if (target != null) {
            divined.add(target);
        }
        return target;
    }

}

/*
 * トップメタエージェント（2018年はcnld（2017年版）を優先的に食う）
 */
package jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.talk;

import org.aiwolf.client.lib.AttackContentBuilder;
import org.aiwolf.client.lib.ContentBuilder;
import org.aiwolf.common.data.Role;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.CndlStrategy;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.framework.Game;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.framework.GameAgent;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.model.RoleProbabilityStruct;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.util.DataScoreList;

public class WhisperAttackTopMetaAgent extends CndlTalkTactic {

    @Override
    public ContentBuilder talkImpl(int turn, int skip, int utter, Game game, CndlStrategy strategy) {

        RoleProbabilityStruct rps = strategy.bestPredictor.getRoleProbabilityStruct(game);
        DataScoreList<GameAgent> candidates = rps.getAgnetProbabilityList(Role.POSSESSED).filter(x -> x.data.isAlive && x.data.role != Role.WEREWOLF && strategy.findCndlModel.cndlLike(x.data));
        if (candidates.isEmpty()) return null; // TopMetaっぽい奴がいない
        GameAgent target = candidates.get(0).data;
        return new AttackContentBuilder(target.agent);
    }

}

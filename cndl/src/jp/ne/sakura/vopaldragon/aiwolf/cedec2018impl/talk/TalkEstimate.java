package jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.talk;

import java.util.HashSet;
import java.util.Set;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.CndlStrategy;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.framework.EventType;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.framework.Game;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.framework.GameAgent;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.framework.GameEvent;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.model.RoleProbabilityStruct;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.util.DataScore;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.util.DataScoreList;
import org.aiwolf.client.lib.ContentBuilder;
import org.aiwolf.client.lib.EstimateContentBuilder;
import org.aiwolf.common.data.Role;

public class TalkEstimate extends CndlTalkTactic {

    private Set<GameAgent> estimated = new HashSet<>();

    @Override
    public ContentBuilder talkImpl(int turn, int skip, int utter, Game game, CndlStrategy strategy) {
        RoleProbabilityStruct rps = strategy.bestPredictor.getRoleProbabilityStruct(game);

        DataScoreList<GameAgent> agents = rps.getAgnetProbabilityList(Role.WEREWOLF, game.getAliveOthers());

        for (DataScore<GameAgent> ga : agents) {
            if (estimated.contains(ga.data)) continue;
            DataScore<Role> roleDs = rps.topRole(ga.data);
            if (strategy.voteModel.currentVoteTarget == ga.data && roleDs.data != Role.WEREWOLF) continue;//投票宣言対象に対して白推測は無い
            if (roleDs.data == Role.BODYGUARD) continue;
            if (roleDs.score > 0.5) {
                estimated.add(ga.data);
                return new EstimateContentBuilder(ga.data.agent, roleDs.data);
            }
        }
        return null;
    }

    @Override
    public void handleEvent(Game g, GameEvent e) {
        if (e.type == EventType.DAYSTART) {
            estimated.clear();
        }
    }

}

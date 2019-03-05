package jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.talk;

import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.CndlStrategy;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.framework.Game;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.framework.GameAgent;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.model.RoleProbabilityStruct;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.util.DataScore;
import org.aiwolf.client.lib.ContentBuilder;
import org.aiwolf.client.lib.VoteContentBuilder;
import org.aiwolf.common.data.Role;

public class TalkVoteToWolf1 extends CndlTalkTactic {

    @Override
    public ContentBuilder talkImpl(int turn, int skip, int utter, Game game, CndlStrategy strategy) {
        RoleProbabilityStruct rps = strategy.bestPredictor.getRoleProbabilityStruct(game);
        DataScore<GameAgent> wolf = rps.topAgent(Role.WEREWOLF, game.getAliveOthers());
        if (strategy.voteModel.currentVoteTarget != wolf.data) {
            strategy.voteModel.currentVoteTarget = wolf.data;
            return new VoteContentBuilder(wolf.data.agent);
        }
        return null;
    }

}

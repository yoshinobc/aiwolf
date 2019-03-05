package jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.target;

import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.CndlStrategy;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.framework.Game;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.framework.GameAgent;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.util.HashCounter;
import jp.ne.sakura.vopaldragon.aiwolf.util.Utils;

public class VoteToMjaority extends CndlTargetTactic {

    @Override
    public GameAgent targetImpl(Game game, CndlStrategy strategy) {
        HashCounter<GameAgent> counts = strategy.voteModel.expectedVote();
        if (counts.getKeyList().isEmpty()) counts = strategy.voteModel.getVoteDeclared().getVoteCount();
        counts.sort(false);
        for (GameAgent ag : counts.getKeyList()) {
            if (!ag.isSelf) {
                return ag;
            }
        }
        return Utils.getRandom(game.getAliveOthers());
    }

}

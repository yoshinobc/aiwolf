package jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.target;

import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.CndlStrategy;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.framework.Game;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.framework.GameAgent;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.util.HashCounter;

/**
 * 得票数予想が最大の自分ではないエージェントに投票
 */
public class RevoteToMajority extends CndlTargetTactic {

    @Override
    public GameAgent targetImpl(Game game, CndlStrategy strategy) {
        HashCounter<GameAgent> counts = strategy.voteModel.expectedRevote();
        if (counts.getKeySet().isEmpty()) counts = strategy.voteModel.getVoteActual().getVoteCount();
//        System.err.println("Revote Wolf\t" + counts + "\tLastVote\t" + strategy.voteModel.getVoteActual().getVoteCount());
        counts.sort(false);
        for (GameAgent ag : counts.getKeyList()) {
            if (!ag.isSelf) {
//                System.err.println(ag + "\t" + Utils.ROLE_MAP.get(ag.getIndex()));
                return ag;
            }
        }
        return strategy.voteModel.getVoteActual().whoVoteWhoMap.get(game.getSelf());
    }

}

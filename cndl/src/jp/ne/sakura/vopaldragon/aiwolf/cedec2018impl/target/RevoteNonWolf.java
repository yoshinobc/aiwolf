package jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.target;

import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.CndlStrategy;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.framework.Game;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.framework.GameAgent;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.target.CndlTargetTactic;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.target.VoteNonWolf;

public class RevoteNonWolf extends CndlTargetTactic {

	private VoteNonWolf vnw = new VoteNonWolf();

	@Override
	public GameAgent targetImpl(Game game, CndlStrategy strategy) {
        return vnw.selectTarget(strategy.voteModel.getVoteActual().getVoteCountOfOthers(), game, strategy);
    }

}

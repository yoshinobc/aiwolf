package jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.target;

import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.CndlStrategy;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.framework.Game;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.framework.GameAgent;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.target.CndlTargetTactic;

public class RevoteMayNotWolf extends CndlTargetTactic {
	private VoteMayNotWolf vmnw = new VoteMayNotWolf();
	@Override
	public GameAgent targetImpl(Game game, CndlStrategy strategy) {
		return vmnw.selectTarget(strategy.voteModel.getVoteActual().getVoteCountOfOthers(), game, strategy);
	}

}

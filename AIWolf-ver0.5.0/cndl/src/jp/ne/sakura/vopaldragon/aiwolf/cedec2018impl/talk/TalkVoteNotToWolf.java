package jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.talk;

import org.aiwolf.client.lib.ContentBuilder;
import org.aiwolf.client.lib.VoteContentBuilder;

import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.CndlStrategy;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.framework.Game;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.framework.GameAgent;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.talk.CndlTalkTactic;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.target.VoteNonWolf;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.target.VoteWolf;

public class TalkVoteNotToWolf extends CndlTalkTactic {
	private VoteNonWolf vnw = new VoteNonWolf();

	@Override
	public ContentBuilder talkImpl(int turn, int skip, int utter, Game game, CndlStrategy strategy) {
		GameAgent voteTarget = vnw.target(strategy);
        if (voteTarget != strategy.voteModel.currentVoteTarget) {
            strategy.voteModel.currentVoteTarget = voteTarget;
            return new VoteContentBuilder(voteTarget.agent);
        }
        return null;
	}

}

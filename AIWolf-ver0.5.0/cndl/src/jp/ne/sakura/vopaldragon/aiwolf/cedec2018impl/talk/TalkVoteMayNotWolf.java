package jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.talk;

import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.target.VoteMayNotWolf;
import org.aiwolf.client.lib.ContentBuilder;
import org.aiwolf.client.lib.VoteContentBuilder;

import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.CndlStrategy;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.framework.Game;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.framework.GameAgent;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.talk.CndlTalkTactic;

public class TalkVoteMayNotWolf extends CndlTalkTactic {
	private VoteMayNotWolf vmnw = new VoteMayNotWolf();

	@Override
	public ContentBuilder talkImpl(int turn, int skip, int utter, Game game, CndlStrategy strategy) {
		GameAgent voteTarget = vmnw.target(strategy);
        if (voteTarget != strategy.voteModel.currentVoteTarget) {
            strategy.voteModel.currentVoteTarget = voteTarget;
            return new VoteContentBuilder(voteTarget.agent);
        }
		return null;
	}

}

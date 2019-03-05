package jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.target;

import java.util.ArrayList;
import java.util.List;

import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.CndlStrategy;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.framework.EventType;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.framework.Game;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.framework.GameAgent;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.framework.GameEvent;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.framework.GameVote;

public class RevoteToLive5Wolf extends CndlTargetTactic {

	@Override
	public GameAgent targetImpl(Game game, CndlStrategy strategy) {
		GameAgent myVotes = null;
		List<GameVote> dayVote = new ArrayList<>();
		for (GameEvent voteEvent : game.getEventAtDay(EventType.VOTE, game.getDay())) {
			dayVote.addAll(voteEvent.votes);
		}
		for (GameVote vote : dayVote) {
			if (vote.initiator == game.getSelf()) {
				myVotes = vote.target;
			}
		}

		List<GameAgent> sacrifice = strategy.voteModel.expectedRevote().topCounts();
//		System.out.println("想定される犠牲者：" + sacrifice);

		if (sacrifice != null && myVotes != null) {
			if(sacrifice.contains(myVotes)) {
				return myVotes;
			}
		}
		if(sacrifice != null) {
			for (GameAgent ag : sacrifice) {
				if (!ag.isSelf)
					return ag;
			}
		} else if (myVotes != null) {
			return myVotes;
		}
		return null;
	}

}

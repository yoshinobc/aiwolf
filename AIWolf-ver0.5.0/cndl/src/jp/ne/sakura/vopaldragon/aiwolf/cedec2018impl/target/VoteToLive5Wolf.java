package jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.target;

import static jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.util.Utils.*;

import java.util.List;
import java.util.stream.Collectors;

import org.aiwolf.client.lib.Topic;

import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.CndlStrategy;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.framework.Game;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.framework.GameAgent;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.framework.GameTalk;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.model.VoteStatus;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.util.HashCounter;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.util.ListMap;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.util.Utils;

public class VoteToLive5Wolf extends CndlTargetTactic {

	@Override
	public GameAgent targetImpl(Game game, CndlStrategy strategy) {
		List<GameTalk> myVotes = game.getSelf().talkList.stream()
				.filter(t -> t.getDay() == game.getDay() && t.getTopic() == Topic.VOTE).collect(Collectors.toList());

		VoteStatus vmodel = strategy.voteModel.getVoteDeclared();
		HashCounter<GameAgent> counts = strategy.voteModel.expectedVote();
		ListMap<Integer, GameAgent> listMap = counts.getCounts();

		GameAgent target = null;
		if (!myVotes.isEmpty()) {
			target = myVotes.get(myVotes.size() - 1).getTarget();
			counts.countMinus(target);// 自分の票を抜く
		}

		counts.sort(false);
		int maxVote = counts.topCount();

		log("counts", counts);
		log("who-vote-who", vmodel.whoVoteWhoMap);
		log("maxVote", maxVote);

		List<GameAgent> tops = listMap.getList(maxVote);
		List<GameAgent> seconds = listMap.getList(maxVote -1);
		// 宣言した対象が票を稼いでいるならそいつに投票
		if (tops.contains(target) || seconds.contains(target)) {
			return target;
		}
		// そうでないなら自分以外で得票数が最も高いエージェントに投票
		tops.remove(game.getSelf());
		double[] evilScore = strategy.evilModel.getEvilScore();
		if (!tops.isEmpty()) {
			Utils.sortByScore(tops, evilScore, false);
			return tops.get(0);
		}
		seconds = counts.topNCounts(2);
		seconds.removeAll(tops);
		seconds.remove(game.getSelf());
		if (!seconds.isEmpty()) {
			Utils.sortByScore(seconds, evilScore, false);
			return seconds.get(0);
		}
		return null;
	}

}

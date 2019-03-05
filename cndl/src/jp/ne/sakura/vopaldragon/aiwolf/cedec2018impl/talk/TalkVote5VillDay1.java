package jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.talk;

import static jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.util.Utils.*;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import org.aiwolf.client.lib.ContentBuilder;
import org.aiwolf.client.lib.Topic;
import org.aiwolf.client.lib.VoteContentBuilder;
import org.aiwolf.common.data.Role;

import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.CndlStrategy;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.framework.Game;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.framework.GameAgent;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.framework.GameTalk;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.util.Utils;

public class TalkVote5VillDay1 extends CndlTalkTactic {

	private GameAgent currentTarget;

	@Override
	public ContentBuilder talkImpl(int turn, int skip, int utter, Game game, CndlStrategy strategy) {
		// 占い結果後に発言
		if (game.getDay() == 1) {
			if (currentTarget == null) {
				// 初期、最も弱い人に投票宣言
				List<GameAgent> agents = game.getAliveOthers();
				 currentTarget = strategy.voteModel.currentVoteTarget = strategy.winEvalModel.getAgentWinCount(game.getAliveOthers()).sort().top().data;
				log("Day1:初期殴り先：" + currentTarget);
				return new VoteContentBuilder(currentTarget.agent);
			} else {
				/* SEER としてCOしている者たちの人数によって狼候補の対象を変える。 */
				List<GameAgent> seerCOs = game.getAliveOthers().stream().filter(ag -> ag.coRole == Role.SEER)
						.collect(Collectors.toList());
				List<GameAgent> candidates = game.getAliveOthers();
				if (!seerCOs.isEmpty() && seerCOs.size() < 3) {
					for (GameAgent ag : seerCOs) {
						candidates.remove(ag);
					}
				} else {
					candidates = seerCOs;
				}
				// 占い結果が判ったら、それに合わせて投票先を変える
				List<GameTalk> divine = game.getAllTalks()
						.filter(t -> t.getDay() == game.getDay() && t.getTopic() == Topic.DIVINED)
						.collect(Collectors.toList());
				if (!divine.isEmpty()) {
					GameAgent newTarget = null;
					if (!candidates.isEmpty()) {
						double[] evilScore = strategy.evilModel.getEvilScore();
						Utils.sortByScore(candidates, evilScore, false);
						newTarget = candidates.get(0);
						log("Day1:EvilScore高めの人を殴る");
					}

					if (newTarget != null && newTarget != currentTarget) {
						currentTarget = newTarget;
						log("Day1:新しいターゲット：" + currentTarget);
						return new VoteContentBuilder(currentTarget.agent);
					}
				}
			}
		}
		return null;

	}

}

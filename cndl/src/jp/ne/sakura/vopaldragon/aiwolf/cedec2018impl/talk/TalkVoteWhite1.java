package jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.talk;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.aiwolf.client.lib.ContentBuilder;
import org.aiwolf.client.lib.VoteContentBuilder;
import org.aiwolf.common.data.Role;

import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.CndlStrategy;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.framework.Game;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.framework.GameAgent;

/**
 * @author aki
 * 狼1日目2ターン目用
 * 狼・CO以外から、ランダムに一人を選び、1ターン目でVOTE宣言した相手と違えばVOTE宣言する。
 */
public class TalkVoteWhite1 extends CndlTalkTactic {

	@Override
	public ContentBuilder talkImpl(int turn, int skip, int utter, Game game, CndlStrategy strategy) {
		List<GameAgent> candidates = game.getAliveOthers().stream().filter(x -> x.role != Role.WEREWOLF && !x.hasCO()).collect(Collectors.toList());
		Collections.shuffle(candidates);
		if (candidates.get(0) != strategy.voteModel.currentVoteTarget) {
			strategy.voteModel.currentVoteTarget = candidates.get(0);
			return new VoteContentBuilder(candidates.get(0).agent);
		}
		return null;
	}

}

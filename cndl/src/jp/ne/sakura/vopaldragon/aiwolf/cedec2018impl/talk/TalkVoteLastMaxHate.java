package jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.talk;


import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.CndlStrategy;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.target.VoteNonWolf;

import org.aiwolf.client.lib.ContentBuilder;
import org.aiwolf.client.lib.VoteContentBuilder;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.framework.Game;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.framework.GameAgent;

/**
 * @author aki
 * 2日目以降1ターン目用
 * 基本的には前日の投票を見てできるだけ狼を避けて狂人っぽくない得票数の多い奴にVOTE宣言
 * ESTIMATEが先に発動してた場合は、nullを返して  2ターン目以降用の処理に任す。
 */
public class TalkVoteLastMaxHate extends CndlTalkTactic {
	private VoteNonWolf vnw = new VoteNonWolf();

	@Override
    public ContentBuilder talkImpl(int turn, int skip, int utter, Game game, CndlStrategy strategy) {
		if (turn == 0) {
			GameAgent voteTarget = vnw.selectTarget(strategy.voteModel.getVoteActual().getVoteCountOfOthers(), game, strategy);
			if (voteTarget != strategy.voteModel.currentVoteTarget) {
	            strategy.voteModel.currentVoteTarget = voteTarget;
	            return new VoteContentBuilder(voteTarget.agent);
	        }
		}
		return null;
	}
}

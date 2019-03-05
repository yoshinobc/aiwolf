package jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.talk;

import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.CndlStrategy;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.framework.Game;
import org.aiwolf.client.lib.ContentBuilder;
import org.aiwolf.client.lib.VoteContentBuilder;

/**
 * currentVoteTargetに入った相手に投票宣言。主に占い系で利用
 */
public class TalkVoteCurrentTarget extends CndlTalkTactic {

    @Override
    public ContentBuilder talkImpl(int turn, int skip, int utter, Game game, CndlStrategy strategy) {
        if (strategy.voteModel.currentVoteTarget != null) {
            return new VoteContentBuilder(strategy.voteModel.currentVoteTarget.agent);
        }
        return null;
    }

}

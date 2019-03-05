package jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.talk;

import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.CndlStrategy;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.framework.Game;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.framework.GameAgent;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.target.VoteWolf;
import org.aiwolf.client.lib.ContentBuilder;
import org.aiwolf.client.lib.VoteContentBuilder;

public class TalkVoteToWolf extends CndlTalkTactic {

    private VoteWolf vw = new VoteWolf();

    @Override
    public ContentBuilder talkImpl(int turn, int skip, int utter, Game game, CndlStrategy strategy) {
        GameAgent voteTarget = vw.target(strategy);
        if (voteTarget != strategy.voteModel.currentVoteTarget) {
            strategy.voteModel.currentVoteTarget = voteTarget;
            return new VoteContentBuilder(voteTarget.agent);
        }
        return null;
    }

}

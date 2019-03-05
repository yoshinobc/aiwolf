package jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.talk;

import static jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.util.Utils.*;

import org.aiwolf.client.lib.ContentBuilder;
import org.aiwolf.client.lib.VoteContentBuilder;

import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.CndlStrategy;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.framework.Game;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.framework.GameAgent;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.target.VoteWolf5;

public class TalkVote5VillDay1ver2 extends CndlTalkTactic {

    private GameAgent currentTarget;
    private int votecounter;
    private VoteWolf5 voteTactic = new VoteWolf5();

    @Override
    public ContentBuilder talkImpl(int turn, int skip, int utter, Game game, CndlStrategy strategy) {
        if (currentTarget == null) {
            // 初期、最も強い人に投票宣言
            currentTarget = strategy.voteModel.currentVoteTarget = strategy.winEvalModel.getAgentWinCount(game.getAliveOthers()).sort().top().data;
            log("Day1:初期殴り先：" + currentTarget);
            votecounter += 1;
            return new VoteContentBuilder(currentTarget.agent);
        } else if (votecounter < 4) {
            GameAgent target = voteTactic.target(strategy);
            if (target != null && target != currentTarget) {
                votecounter += 1;
                currentTarget = target;
                return new VoteContentBuilder(currentTarget.agent);
            }
        }
        return null;

    }

}

package jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.talk;

import static jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.util.Utils.*;

import org.aiwolf.client.lib.ContentBuilder;
import org.aiwolf.client.lib.VoteContentBuilder;

import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.CndlStrategy;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.framework.Game;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.framework.GameAgent;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.target.VoteWolf5ByWolf;

public class TalkVote5WolfDay1ver2 extends CndlTalkTactic {

    private GameAgent currentTarget;
    private int votecounter;
    private VoteWolf5ByWolf voteTactic;

    public TalkVote5WolfDay1ver2(VoteWolf5ByWolf voteTactic) {
        this.voteTactic = voteTactic;
    }

    @Override
    public ContentBuilder talkImpl(int turn, int skip, int utter, Game game, CndlStrategy strategy) {
        if (currentTarget == null) { // 初期、最も強い人に投票宣言
            currentTarget = strategy.winEvalModel.getAgentWinCount(game.getAliveOthers()).sort().top().data;
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

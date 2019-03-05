package jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.talk;

import static jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.util.Utils.*;


import org.aiwolf.client.lib.ContentBuilder;
import org.aiwolf.client.lib.VoteContentBuilder;

import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.CndlStrategy;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.framework.Game;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.framework.GameAgent;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.target.VoteWolf5Day2;

public class TalkVote5VillDay2 extends CndlTalkTactic {

    private GameAgent currentTarget;
    private VoteWolf5Day2 voteTactic = new VoteWolf5Day2();

    @Override
    public ContentBuilder talkImpl(int turn, int skip, int utter, Game game, CndlStrategy strategy) {
        GameAgent newTarget = voteTactic.target(strategy);
        if (newTarget != null && newTarget != currentTarget) {
            currentTarget = newTarget;
            log("Day2：新しいターゲット：" + currentTarget);
            return new VoteContentBuilder(currentTarget.agent);
        }
        return null;

    }

}

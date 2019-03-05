package jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.talk;

import java.util.List;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.CndlStrategy;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.framework.Game;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.framework.GameAgent;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.util.Utils;
import org.aiwolf.client.lib.ContentBuilder;
import org.aiwolf.client.lib.VoteContentBuilder;

/**
 * 初日の最初に、最も勝率の高いAgentに対して投票宣言を行う（同数の場合にはランダム）
 */
public class TalkVoteByAI extends CndlTalkTactic {

    @Override
    public ContentBuilder talkImpl(int turn, int skip, int utter, Game game, CndlStrategy strategy) {
        if (game.getDay() == 1 && turn == 0) {
            //最も勝率の高いAgentから一人を抽出
            strategy.voteModel.currentVoteTarget = strategy.winEvalModel.getAgentWinCount(game.getAliveOthers()).sort().top().data;
            return new VoteContentBuilder(strategy.voteModel.currentVoteTarget.agent);
        }
        return null;
    }

}

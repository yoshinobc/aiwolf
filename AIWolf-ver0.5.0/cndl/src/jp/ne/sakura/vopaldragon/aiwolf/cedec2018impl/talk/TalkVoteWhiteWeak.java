package jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.talk;

import java.util.List;
import java.util.stream.Collectors;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.CndlStrategy;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.framework.Game;

import org.aiwolf.client.lib.ContentBuilder;
import org.aiwolf.client.lib.Topic;
import org.aiwolf.client.lib.VoteContentBuilder;
import org.aiwolf.common.data.Role;

import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.framework.GameAgent;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.util.DataScoreList;

/**
 *
 * 人狼、CO以外からメタ勝率が低い奴を vote 対象と宣言
 */
public class TalkVoteWhiteWeak extends CndlTalkTactic {

    @Override
    public ContentBuilder talkImpl(int turn, int skip, int utter, Game game, CndlStrategy strategy) {
        GameAgent me = game.getSelf();
        /* このターン vote していない時のみ発動 */
        if (me.talkList.stream().filter(x -> x.getDay() == game.getDay() && x.getTopic() == Topic.VOTE).collect(Collectors.toList()).isEmpty()) {
            List<GameAgent> agents = game.getAliveOthers();
            agents.removeIf(x -> x.role == Role.WEREWOLF);
            DataScoreList<GameAgent> candidates = strategy.winEvalModel.getAgentWinCount(agents);
            candidates.reverse();
            strategy.voteModel.currentVoteTarget = candidates.top().data;
            return new VoteContentBuilder(candidates.top().data.agent);
        }
        return null;
    }

}

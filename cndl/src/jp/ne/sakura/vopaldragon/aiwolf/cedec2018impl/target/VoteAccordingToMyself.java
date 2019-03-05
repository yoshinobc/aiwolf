package jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.target;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.CndlStrategy;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.model.EvilScoreModel;
import org.aiwolf.client.lib.Topic;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.framework.Game;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.framework.GameAgent;

/**
 *
 * 自分が Vote すると言った相手に Vote
 *
 */
public class VoteAccordingToMyself extends CndlTargetTactic {

    @Override
    public GameAgent targetImpl(Game game, CndlStrategy strategy) {
        GameAgent me = game.getSelf();
        /* 自分が投票すると宣言した先を取得 */
        List<GameAgent> vote_targets = me.talkList.stream().filter(x -> x.getTopic() == Topic.VOTE)
            .map(x -> x.getTarget()).collect(Collectors.toList());
        Collections.reverse(vote_targets); // リストを逆順に

        GameAgent target = null;
        for (GameAgent agent : vote_targets) {
            if (agent != me && agent.isAlive) {
                target = agent;
                break;
            }
        }
        if (target != null) {
            return target;
        }
        return null;
    }
}

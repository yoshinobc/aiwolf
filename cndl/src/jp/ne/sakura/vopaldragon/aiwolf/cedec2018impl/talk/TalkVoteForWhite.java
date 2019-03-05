package jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.talk;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.CndlStrategy;

import org.aiwolf.client.lib.ContentBuilder;
import org.aiwolf.client.lib.Topic;
import org.aiwolf.client.lib.VoteContentBuilder;

import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.framework.Game;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.framework.GameAgent;

public class TalkVoteForWhite extends CndlTalkTactic {

    @Override
    public ContentBuilder talkImpl(int turn, int skip, int utter, Game game, CndlStrategy strategy) {
        GameAgent me = game.getSelf();
        /* 自分の今日の投票宣言先のリスト */
        List<GameAgent> myVoteTargets = me.talkList.stream().filter(x -> x.getDay() == game.getDay() && x.getTopic() == Topic.VOTE).map(x -> x.getTarget()).collect(Collectors.toList());
        GameAgent tar = null;
        if (!myVoteTargets.isEmpty()) {
            tar = myVoteTargets.get(myVoteTargets.size() - 1);
        }

        List<GameAgent> targets = game.getAliveOthers();
        if (tar != null && targets.contains(tar)) {
            return null;
        } else {
            /* evilscoreの小さい生き物に投票 */
            tar = Collections.min(targets, Comparator.comparing(x -> strategy.evilModel.getEvilScore()[x.getIndex()]));
        }
        if (tar != null) {
            return new VoteContentBuilder(tar.agent);
        }
        return null;
    }

}

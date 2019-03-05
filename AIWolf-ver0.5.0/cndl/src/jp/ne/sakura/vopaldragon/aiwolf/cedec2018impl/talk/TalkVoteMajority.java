package jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.talk;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.CndlStrategy;

import org.aiwolf.client.lib.ContentBuilder;
import org.aiwolf.client.lib.Topic;
import org.aiwolf.client.lib.VoteContentBuilder;
import org.aiwolf.common.data.Species;

import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.framework.Game;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.framework.GameAgent;

/**
 *
 * Vote 及び Request(Vote) のターゲットとしてより多くの票が入っているエージェントを Vote対象にする。 すでに自分が3つ黒出しをしているなら、その3人から選ぶ。
 */
public class TalkVoteMajority extends CndlTalkTactic {

    @Override
    public ContentBuilder talkImpl(int turn, int skip, int utter, Game game, CndlStrategy strategy) {
        GameAgent me = game.getSelf();

        /* エージェント別投票宣言数のカウント */
        Map<GameAgent, Integer> votes = new HashMap<>();
        Set<GameAgent> myDivine = me.talkList.stream().filter(x -> x.getTopic() == Topic.DIVINED && x.getResult() == Species.WEREWOLF).map(x -> x.getTarget()).collect(Collectors.toSet());
        if (myDivine.size() >= 3) {
            myDivine.removeIf(x -> !x.isAlive);
            for (GameAgent ga : myDivine) {
                votes.put(ga, 0);
            }
        } else {
            for (GameAgent ga : game.getAliveOthers()) {
                votes.put(ga, 0);
            }
        }
        game.getAllTalks().filter(x -> x.getDay() == game.getDay()).filter(x -> x.getTopic() == Topic.VOTE) // REQUEST に関する処理は未実装
            .map(x -> x.getTarget()).forEach(x -> {
            if (votes.containsKey(x)) {
                votes.put(x, votes.get(x) + 1);
            }
        });

        /* 最大被投票者 */
        GameAgent tar = Collections.max(votes.keySet(), Comparator.comparing(x -> votes.get(x)));
        if (tar != null && tar != strategy.voteModel.currentVoteTarget) {
        		strategy.voteModel.currentVoteTarget = tar;
            return new VoteContentBuilder(tar.agent);
        } else {
            return null;
        }
    }

}

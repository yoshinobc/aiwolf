package jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.talk;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.CndlStrategy;

import org.aiwolf.client.lib.ContentBuilder;
import org.aiwolf.client.lib.Topic;
import org.aiwolf.client.lib.VoteContentBuilder;

import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.framework.EventType;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.framework.Game;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.framework.GameAgent;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.framework.GameEvent;

/**
 *
 * 自分が「Divined WEREWOLF」だと主張した相手からランダムに一人 vote 宣言対象とする 3か目以降は無効
 */
public class TalkVoteForFakeBlack extends CndlTalkTactic {

    private List<GameAgent> divined = new ArrayList<>();

    @Override
    public ContentBuilder talkImpl(int turn, int skip, int utter, Game game, CndlStrategy strategy) {
        if (!divined.isEmpty()) {
            Collections.shuffle(divined);
            GameAgent target = divined.get(0);
            if (target != strategy.voteModel.currentVoteTarget) {
                strategy.voteModel.currentVoteTarget = target;
                return new VoteContentBuilder(target.agent);
            }
        }
        return null;
    }

    @Override
    public void handleEvent(Game g, GameEvent e) {
        if (e.type == EventType.DAYSTART) {
            divined.removeIf(x -> !x.isAlive);
        }
        if (e.type == EventType.TALK) {
            e.talks.stream().filter(x -> x.getTalker().isSelf && x.getTopic() == Topic.DIVINED)
                .forEach(x -> divined.add(x.getTarget()));
        }
    }
}

package jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.talk;

import java.util.Set;
import java.util.stream.Collectors;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.CndlStrategy;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.model.EvilScoreModel;

import org.aiwolf.client.lib.ContentBuilder;
import org.aiwolf.client.lib.DivinedResultContentBuilder;
import org.aiwolf.client.lib.Topic;
import org.aiwolf.common.data.Agent;
import org.aiwolf.common.data.Role;
import org.aiwolf.common.data.Species;

import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.framework.Game;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.framework.GameAgent;

public class TalkDivineBlackSeerDay3 extends CndlTalkTactic {

    @Override
    public ContentBuilder talkImpl(int turn, int skip, int utter, Game game, CndlStrategy strategy) {
        GameAgent me = game.getSelf();
        if (!me.talkList.stream().filter(x -> x.getDay() == game.getDay() && x.getTopic() == Topic.DIVINED).collect(Collectors.toList()).isEmpty()) {
            return null;
        }
        Agent tar = null;
        Set<GameAgent> seers = game.getAliveOthers().stream().filter(ag -> ag.coRole == Role.SEER).collect(Collectors.toSet());
        Set<GameAgent> my_divine = me.talkList.stream().filter(x -> x.getTopic() == Topic.DIVINED && x.getResult() == Species.WEREWOLF)
            .map(x -> x.getTarget()).collect(Collectors.toSet());
        /* 自分以外に複数の占い師COがいて、その中に襲撃で死んだのがいたら
		 * 残りは占うまでもなく人狼と言うしかない。
		 * けど、占い結果として言うよ。
         */
        seers.removeIf(x -> !x.isAlive || my_divine.contains(x));
        for (GameAgent gameAgent : seers) {
            if (!gameAgent.isAttacked && !my_divine.contains(gameAgent)) {
                tar = gameAgent.agent;
                break;
            }
        }
        if (tar != null) {
            return new DivinedResultContentBuilder(tar, Species.WEREWOLF);
        }
        return null;
    }

}

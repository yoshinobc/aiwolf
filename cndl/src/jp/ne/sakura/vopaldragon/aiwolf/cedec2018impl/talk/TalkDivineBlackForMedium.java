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

/**
 * 霊媒師COしている者に偽の黒判定を出す。
 */
public class TalkDivineBlackForMedium extends CndlTalkTactic {

    @Override
    public ContentBuilder talkImpl(int turn, int skip, int utter, Game game, CndlStrategy strategy) {
        GameAgent me = game.getSelf();
        if (!me.talkList.stream().filter(x -> x.getDay() == game.getDay() && x.getTopic() == Topic.DIVINED).collect(Collectors.toList()).isEmpty()) {
            return null;
        }
        Set<GameAgent> mediums = game.getAliveOthers().stream().filter(ag -> ag.coRole == Role.MEDIUM).collect(Collectors.toSet());
        Set<GameAgent> my_divine = me.talkList.stream().filter(x -> x.getTopic() == Topic.DIVINED).map(x -> x.getTarget()).collect(Collectors.toSet());
        Agent ret = null;
        if (mediums.size() == 2 && mediums.stream().filter(x -> x.isAttacked).collect(Collectors.toList()).size() == 1) {
            /* 霊能者COが二人でうち一人が襲撃されていた場合、残りは多分人狼なので避ける */
            return null;
        } else {
            mediums.removeAll(my_divine);
            for (GameAgent gameAgent : mediums) {
                ret = gameAgent.agent;
                break;
            }
        }
        if (ret != null) {
            return new DivinedResultContentBuilder(ret, Species.WEREWOLF);
        }
        return null;
    }

}

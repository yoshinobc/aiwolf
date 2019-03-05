package jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.talk;

import java.util.stream.Collectors;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.CndlStrategy;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.model.EvilScoreModel;

import org.aiwolf.client.lib.ComingoutContentBuilder;
import org.aiwolf.client.lib.ContentBuilder;
import org.aiwolf.client.lib.Topic;
import org.aiwolf.common.data.Role;

import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.framework.Game;

/**
 *
 * 人狼1日目用: 村人CO希望
 */
public class WhisperCOVillager extends CndlTalkTactic {

    @Override
    public ContentBuilder talkImpl(int turn, int skip, int utter, Game game, CndlStrategy strategy) {
        if (game.getSelf().whisperList.stream().filter(x -> x.getTopic() == Topic.COMINGOUT && x.getRole() == Role.VILLAGER).collect(Collectors.toList()).isEmpty()) {
            return new ComingoutContentBuilder(game.getSelf().agent, Role.VILLAGER);
        }
        return null;
    }
}

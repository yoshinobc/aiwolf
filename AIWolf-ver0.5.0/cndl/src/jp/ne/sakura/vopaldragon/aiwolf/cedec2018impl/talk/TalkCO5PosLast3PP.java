package jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.talk;

import org.aiwolf.client.lib.ComingoutContentBuilder;
import org.aiwolf.client.lib.ContentBuilder;
import org.aiwolf.common.data.Role;

import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.CndlStrategy;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.framework.Game;

/**
 * 生存者が3人になったら狂人CO
 */
public class TalkCO5PosLast3PP extends CndlTalkTactic {

    @Override
    public ContentBuilder talkImpl(int turn, int skip, int utter, Game game, CndlStrategy strategy) {
        if (game.getAlives().size() == 3 && game.getSelf().coRole != Role.POSSESSED) {
            return new ComingoutContentBuilder(game.getSelf().agent, Role.POSSESSED);
        }
        return null;
    }

}

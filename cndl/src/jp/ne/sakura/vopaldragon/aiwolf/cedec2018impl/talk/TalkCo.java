package jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.talk;

import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.CndlStrategy;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.model.EvilScoreModel;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.framework.Game;
import org.aiwolf.client.lib.ComingoutContentBuilder;
import org.aiwolf.client.lib.ContentBuilder;
import org.aiwolf.common.data.Role;

/**
 * とりあえずCOする
 */
public class TalkCo extends CndlTalkTactic {

    private Role role;

    public TalkCo(Role role) {
        this.role = role;
    }

    @Override
    public ContentBuilder talkImpl(int turn, int skip, int utter, Game game, CndlStrategy strategy) {
        if (!game.getSelf().hasCO()) {
            return new ComingoutContentBuilder(game.getSelf().agent, role);
        }
        return null;
    }

}

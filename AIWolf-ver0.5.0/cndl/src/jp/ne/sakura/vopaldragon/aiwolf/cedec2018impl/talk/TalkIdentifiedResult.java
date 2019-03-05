package jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.talk;

import org.aiwolf.client.lib.ContentBuilder;
import org.aiwolf.client.lib.IdentContentBuilder;
import org.aiwolf.common.data.Role;
import org.aiwolf.common.data.Species;

import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.CndlStrategy;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.framework.EventType;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.framework.Game;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.framework.GameAgent;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.framework.GameEvent;


/**
 * 霊媒結果をお伝えする発言
 */
public class TalkIdentifiedResult extends CndlTalkTactic {

    @Override
    public ContentBuilder talkImpl(int turn, int skip, int utter, Game game, CndlStrategy strategy) {
        if (game.getSelf().coRole == Role.MEDIUM && target != null) {
            IdentContentBuilder icb = new IdentContentBuilder(target.agent, result);
            target = null;
            result = null;
            return icb;
        }
        return null;
    }

    private GameAgent target;
    private Species result;

    @Override
    public void handleEvent(Game g, GameEvent e) {
        if (e.type == EventType.MEDIUM) {
            this.target = e.target;
            this.result = e.species;
        }

    }

}

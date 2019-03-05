package jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.talk;

import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.CndlStrategy;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.framework.AbstractStrategy;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.framework.Game;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.framework.rolebase.TalkTactic;
import org.aiwolf.client.lib.ContentBuilder;

public abstract class CndlTalkTactic extends TalkTactic {

    @Override
    public ContentBuilder talk(int turn, int skip, int utter, AbstractStrategy strategy) {
        return talkImpl(turn, skip, utter, strategy.getGame(), (CndlStrategy) strategy);
    }

    public abstract ContentBuilder talkImpl(int turn, int skip, int utter, Game game, CndlStrategy strategy);

}

package jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.talk;

import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.CndlStrategy;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.framework.Game;
import org.aiwolf.client.lib.ContentBuilder;
import org.aiwolf.client.lib.SkipContentBuilder;

public class TalkSkip extends CndlTalkTactic {

    @Override
    public ContentBuilder talkImpl(int turn, int skip, int utter, Game game, CndlStrategy strategy) {
        return new SkipContentBuilder();
    }
}

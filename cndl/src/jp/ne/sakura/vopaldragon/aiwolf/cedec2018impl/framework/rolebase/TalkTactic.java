package jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.framework.rolebase;

import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.framework.AbstractStrategy;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.framework.Game;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.framework.GameEvent;
import org.aiwolf.client.lib.ContentBuilder;

/**
 * 会話の戦術
 */
public abstract class TalkTactic implements Tactic {

    /**
     * 会話内容を返す。nullを返すと、次の優先順位のTacticが実行される。
     *
     * @param turn
     * @param skip
     * @param utter
     * @return 会話内容
     */
    public abstract ContentBuilder talk(int turn, int skip, int utter, AbstractStrategy strategy);

    @Override
    public void handleEvent(Game g, GameEvent e) {
    }

}

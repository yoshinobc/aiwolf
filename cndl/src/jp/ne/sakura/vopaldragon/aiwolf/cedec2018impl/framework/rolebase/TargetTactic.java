package jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.framework.rolebase;

import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.framework.AbstractStrategy;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.framework.Game;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.framework.GameAgent;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.framework.GameEvent;

/**
 * 行動のターゲット（投票先、襲撃先、占い先etc）を決定する戦術
 */
public abstract class TargetTactic implements Tactic {

    /**
     * 行動のターゲット（投票先、襲撃先、占い先etc）を返す。nullを返すと、次の優先順位のTacticが実行される。
     *
     * @return 対象のGameAgent
     */
    public abstract GameAgent target(AbstractStrategy strategy);

    @Override
    public void handleEvent(Game g, GameEvent e) {
    }

}

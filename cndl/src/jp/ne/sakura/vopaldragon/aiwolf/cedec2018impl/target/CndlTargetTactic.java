package jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.target;

import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.CndlStrategy;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.framework.AbstractStrategy;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.framework.Game;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.framework.GameAgent;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.framework.rolebase.TargetTactic;

public abstract class CndlTargetTactic extends TargetTactic {

    @Override
    public GameAgent target(AbstractStrategy strategy) {
        return targetImpl(strategy.getGame(), (CndlStrategy) strategy);
    }

    public abstract GameAgent targetImpl(Game game, CndlStrategy strategy);

}

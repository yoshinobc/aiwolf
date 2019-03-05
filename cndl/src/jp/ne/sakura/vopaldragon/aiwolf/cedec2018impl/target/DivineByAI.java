package jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.target;

import java.util.Set;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.CndlStrategy;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.framework.Game;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.framework.GameAgent;

public class DivineByAI extends CndlTargetTactic {

    Set<GameAgent> divined;

    public DivineByAI(Set<GameAgent> divined) {
        this.divined = divined;
    }

    @Override
    public GameAgent targetImpl(Game game, CndlStrategy strategy) {
        if (game.getDay() == 0) {
            GameAgent agent =  strategy.winEvalModel.getAgentWinCount(game.getAliveOthers()).sort().top().data;
            divined.add(agent);
            return agent;
        }
        return null;
    }

}

package jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.target;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.CndlStrategy;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.framework.Game;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.framework.GameAgent;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.util.DataScore;

/**
 * 初日の占い戦術
 */
public class DivineCndlOrWinner extends CndlTargetTactic {

    private Set<GameAgent> divined;

    public DivineCndlOrWinner(Set<GameAgent> divined) {
        this.divined = divined;
    }

    @Override
    public GameAgent targetImpl(Game game, CndlStrategy strategy) {
        if (game.getDay() == 0) {
            GameAgent target = null;
            List<DataScore<GameAgent>> agents = strategy.winEvalModel.getAgentWinCount(game.getAliveOthers());
            //Cndlを探す
            if (strategy.findCndlModel != null) {
                Optional<DataScore<GameAgent>> cndlLike = agents.stream().filter(ds -> strategy.findCndlModel.cndlLike(ds.data)).findFirst();
                if (cndlLike.isPresent()) target = cndlLike.get().data;
            }
            //最も勝率の高いAgentを対象に
            if (target == null) target = agents.get(0).data;
            divined.add(target);
            return target;
        }
        return null;
    }

}

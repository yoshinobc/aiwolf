package jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.target;

import java.util.List;
import java.util.Set;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.CndlStrategy;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.framework.Game;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.framework.GameAgent;
import org.aiwolf.common.data.Role;

/**
 * 占いの基本戦術。Evilスコアが高い順に占う
 */
public class DivineBasic extends CndlTargetTactic {

    private Set<GameAgent> divined;

    public DivineBasic(Set<GameAgent> divined) {
        this.divined = divined;
    }

    @Override
    public GameAgent targetImpl(Game game, CndlStrategy strategy) {
        List<GameAgent> alives = strategy.getGame().getAliveOthers();
        alives.removeAll(divined);
        if (alives.isEmpty()) return null;
        GameAgent target = strategy.bestPredictor.getRoleProbabilityStruct(game).topAgent(Role.WEREWOLF, alives).data;
        divined.add(target);
        return target;
    }

}

package jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.CndlStrategy;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.framework.Game;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.framework.GameAgent;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.util.Utils;
import org.aiwolf.common.data.Role;

public class EnsembleRolePredictotrModel implements RolePredictor {

    private CndlStrategy strategy;
    private List<RolePredictor> predictors;

    public EnsembleRolePredictotrModel(CndlStrategy strategy, RolePredictor... predictors) {
        this.strategy = strategy;
        this.predictors = new ArrayList<>();
        for (RolePredictor rp : predictors) {
            this.predictors.add(rp);
        }
    }

    @Override
    public RoleProbabilityStruct getRoleProbabilityStruct(Game game) {
        RoleProbabilityStruct rps = new RoleProbabilityStruct();
        for (GameAgent ag : game.getAgents()) {
            rps.roleProbabilities.put(ag, new HashMap<>());
        }
        for (RolePredictor rp : predictors) {
            RoleProbabilityStruct subRps = rp.getRoleProbabilityStruct(game);
            for (Role role : Utils.existingRole(game)) {
                for (GameAgent ag : game.getAgents()) {
                    rps.setScore(ag, role, rps.getScore(ag, role) + subRps.getScore(ag, role));
                }
            }
        }
        rps.normalize(strategy.getGame());
        return rps;
    }

}

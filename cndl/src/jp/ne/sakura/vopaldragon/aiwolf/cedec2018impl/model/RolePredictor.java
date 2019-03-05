package jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.model;

import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.framework.Game;

public interface RolePredictor {

    RoleProbabilityStruct getRoleProbabilityStruct(Game game);

}

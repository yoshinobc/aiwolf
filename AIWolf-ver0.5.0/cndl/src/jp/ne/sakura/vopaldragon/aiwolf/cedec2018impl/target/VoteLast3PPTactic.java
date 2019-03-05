package jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.target;

import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.CndlStrategy;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.model.EvilScoreModel;
import org.aiwolf.common.data.Role;

import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.framework.Game;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.framework.GameAgent;

public class VoteLast3PPTactic extends CndlTargetTactic {

    @Override
    public GameAgent targetImpl(Game game, CndlStrategy strategy) {
        if (game.getAlives().size() == 3) {
            for (GameAgent gp : game.getAlives()) {
                if (!gp.isSelf && gp.coRole != Role.WEREWOLF) {
                    return gp;
                }
            }
        }
        return null;
    }

}

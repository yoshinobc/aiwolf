package jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.target;

import java.util.ArrayList;
import java.util.List;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.CndlStrategy;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.framework.Game;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.framework.GameAgent;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.model.RoleProbabilityStruct;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.util.HashCounter;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.util.Utils;
import org.aiwolf.common.data.Role;

public class VoteToMjaorityPreferSeer extends CndlTargetTactic {

    @Override
    public GameAgent targetImpl(Game game, CndlStrategy strategy) {
        HashCounter<GameAgent> counts = strategy.voteModel.expectedVote();
        if (counts.getKeyList().isEmpty()) counts = strategy.voteModel.getVoteDeclared().getVoteCount();

        GameAgent target = null;
        RoleProbabilityStruct rps = strategy.bestPredictor.getRoleProbabilityStruct(game);
        counts.removeCount(game.getSelf());

        List<GameAgent> candidate = counts.getKeyList();
        if (candidate.isEmpty()) candidate = game.getAliveOthers();

        for (GameAgent ag : candidate) {
            if (rps.topRole(ag).data == Role.SEER) {
                target = ag;
                break;
            }
        }
        if (target == null) {
            for (GameAgent ag : candidate) {
                if (rps.topRole(ag).data != Role.POSSESSED) {
                    target = ag;
                    break;
                }
            }
        }
        if (target == null) {
            target = Utils.getRandom(game.getAliveOthers());
        }

        return target;
    }

}

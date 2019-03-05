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

public class VoteToMjaorityAvoidPos extends CndlTargetTactic {

    @Override
    public GameAgent targetImpl(Game game, CndlStrategy strategy) {
        HashCounter<GameAgent> counts = strategy.voteModel.expectedVote();
        if (counts.getKeyList().isEmpty()) counts = strategy.voteModel.getVoteDeclared().getVoteCount();
        counts.sort(false);
        List<GameAgent> topAgents = counts.topCounts();
        topAgents.remove(game.getSelf());
        if (topAgents.isEmpty()) topAgents = counts.topNCounts(2);
        topAgents.remove(game.getSelf());
        List<GameAgent> topAgentsCopy = new ArrayList<>(topAgents);
        if (topAgentsCopy.isEmpty()) topAgentsCopy = game.getAliveOthers();

        RoleProbabilityStruct rps = strategy.bestPredictor.getRoleProbabilityStruct(game);
        topAgents.removeIf(ag -> rps.topRole(ag).data == Role.POSSESSED);
        if (topAgents.isEmpty()) topAgents = topAgentsCopy;

        GameAgent target = rps.topAgent(Role.VILLAGER, topAgents).data;

//        System.err.println("VTMAP\t" + strategy.voteModel.expectedVote() + "\t" + target + "\t" + Utils.ROLE_MAP.get(target.getIndex()));

        return target;
    }

}

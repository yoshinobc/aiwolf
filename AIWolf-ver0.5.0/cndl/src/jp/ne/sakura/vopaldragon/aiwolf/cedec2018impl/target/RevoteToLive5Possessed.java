package jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.target;

import java.util.ArrayList;
import java.util.List;

import org.aiwolf.common.data.Role;

import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.CndlStrategy;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.framework.EventType;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.framework.Game;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.framework.GameAgent;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.framework.GameEvent;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.framework.GameVote;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.model.RoleProbabilityStruct;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.util.DataScore;

public class RevoteToLive5Possessed extends CndlTargetTactic {

    @Override
    public GameAgent targetImpl(Game game, CndlStrategy strategy) {
        GameAgent myVotes = null;
        List<GameVote> dayVote = new ArrayList<>();
        for (GameEvent voteEvent : game.getEventAtDay(EventType.VOTE, game.getDay())) {
            dayVote.addAll(voteEvent.votes);
        }
        for (GameVote vote : dayVote) {
            if (vote.initiator == game.getSelf()) {
                myVotes = vote.target;
            }
        }

        RoleProbabilityStruct rps = strategy.bestPredictor.getRoleProbabilityStruct(game);
        List<DataScore<GameAgent>> wolfrank = rps.getAgnetProbabilityList(Role.WEREWOLF, game.getAliveOthers());
        //自分の投票先が狼っぽいなら外す。
        if (myVotes == wolfrank.get(0).data) myVotes = null;

        List<GameAgent> sacrifice = strategy.voteModel.expectedRevote().topCounts();
        sacrifice.remove(game.getSelf());
        sacrifice.remove(wolfrank.get(0).data);
//		System.out.println("想定される犠牲者：" + sacrifice);

        //候補に自分も投票していたならそのまま投票
        if (myVotes != null) {
            if (sacrifice.contains(myVotes)) {
                return myVotes;
            }
        }
        //候補から狼と自分を抜いて、より狼っぽくない方
        if (!sacrifice.isEmpty()) {
            List<DataScore<GameAgent>> candidates = rps.getAgnetProbabilityList(Role.WEREWOLF, sacrifice);
            return candidates.get(candidates.size() - 1).data;

        //無理なら自分のまま
        } else if (myVotes != null) {
            return myVotes;
        }
        //改めて村
        return wolfrank.get(wolfrank.size() - 1).data;
    }

}

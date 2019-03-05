package jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.target;

import java.util.ArrayList;
import java.util.List;

import org.aiwolf.common.data.Role;

import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.CndlStrategy;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.framework.Game;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.framework.GameAgent;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.model.RoleProbabilityStruct;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.target.CndlTargetTactic;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.util.DataScore;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.util.HashCounter;

/**
 * @author aki
 * 自分以外から1票でも入っている候補から、狼確率の低い生き物を狙う。
 * 自分への投票は気にしない
 */
public class VoteMayNotWolf extends CndlTargetTactic {

    @Override
    public GameAgent targetImpl(Game game, CndlStrategy strategy) {
        return selectTarget(strategy.voteModel.getVoteDeclared().getVoteCountOfOthers(), game, strategy);
    }

    public GameAgent selectTarget(HashCounter<GameAgent> voteCount, Game game, CndlStrategy strategy) {
        GameAgent me = game.getSelf();
        GameAgent target = null;

        int topCount = voteCount.topCount();
        int myCount = voteCount.getCount(me);

        RoleProbabilityStruct rps = strategy.bestPredictor.getRoleProbabilityStruct(game);

        // 一票でも入ってるやつ
        List<GameAgent> candidates = new ArrayList<>();
        for (GameAgent x : voteCount.getKeySet()) {
        		if (x.isSelf) continue;
        		// cndl狼は救う
        		if (strategy.findCndlModel != null && strategy.findCndlModel.cndlLike(x) &&
        				strategy.findCndlModel.wolfProbability(x) > 0.7) continue;
        		//　第一, 第二役職候補が狼なら外す
        		List<DataScore<Role>> roleProbs = rps.getRoleProbabilityList(x);
        		if (roleProbs.get(0).data == Role.WEREWOLF || roleProbs.get(1).data == Role.WEREWOLF) continue;

        		candidates.add(x);
		}

        // 役職COを助ける
        if (game.getDay() <= 3) {
        		candidates.removeIf(x -> x.coRole == Role.SEER || x.coRole == Role.MEDIUM);
		}
        if (candidates.isEmpty()) {
        		candidates = game.getAliveOthers();
            if (game.getDay() <= 3) {
        			candidates.removeIf(x -> x.coRole == Role.SEER || x.coRole == Role.MEDIUM);
            }
		}
        target = rps.getAgnetProbabilityList(Role.WEREWOLF, candidates).get(candidates.size() - 1).data;

        return target;
	}

}

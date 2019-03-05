package jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.target;

import java.util.List;
import java.util.stream.Collectors;

import org.aiwolf.client.lib.Topic;
import org.aiwolf.common.data.Role;

import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.CndlStrategy;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.evalmodel.Village5TacEvalModel.Village5Tactics;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.framework.Game;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.framework.GameAgent;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.model.RoleProbabilityStruct;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.util.DataScore;

public class VoteWithoutWolfDay2 extends CndlTargetTactic {

	@Override
	public GameAgent targetImpl(Game game, CndlStrategy strategy) {

		GameAgent target = null;

		// 初日に占いCOした自分以外のGAリスト
		List<GameAgent> seerCOs = game
				.getAllTalks().filter(t -> t.getDay() == 1 && t.getTopic() == Topic.COMINGOUT
						&& t.getRole() == Role.SEER && !t.getTalker().isSelf)
				.map(t -> t.getTalker()).collect(Collectors.toList());
		// 対抗の占いが2人以上なら、占い騙り履歴がない相手に、投票する。
		if (seerCOs.size() > 1) {
			for (GameAgent ag : seerCOs) {
				Village5Tactics AgTac = strategy.v5TacEvalModel.getTacitcsMap(ag);
				if (!AgTac.day2HasfakeCoSeer && ag.isAlive) {
					target = ag;
					//System.err.println("WSPCO3\t" + target);
					return target;
				}
			}
			// 対抗が1人なら決め打ち
		} else if (seerCOs.size() == 1) {
			target = seerCOs.get(0).isAlive ? seerCOs.get(0) : null;
			if (target != null) {
				//System.err.println("WSP\t" + target);
				return target;
			}
		}
		// CO３以上で占い騙り履歴のない占いがいない or 対抗全滅
		RoleProbabilityStruct rps = strategy.bestPredictor.getRoleProbabilityStruct(game);
		List<DataScore<GameAgent>> wolfrank = rps.getAgnetProbabilityList(Role.WEREWOLF, game.getAliveOthers());
		target = wolfrank.get(wolfrank.size() - 1).data;
		//System.err.println("VPW\t" + target);
		return target;
	}

}

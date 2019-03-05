package jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.talk;

import static jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.util.Utils.*;

import java.util.List;
import java.util.stream.Collectors;

import org.aiwolf.client.lib.ContentBuilder;
import org.aiwolf.client.lib.Topic;
import org.aiwolf.client.lib.VoteContentBuilder;
import org.aiwolf.common.data.Role;

import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.CndlStrategy;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.evalmodel.Village5TacEvalModel.Village5Tactics;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.framework.Game;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.framework.GameAgent;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.model.RoleProbabilityStruct;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.util.DataScore;

public class TalkVote5PossessedDay2 extends CndlTalkTactic {

	private GameAgent currentTarget;

	@Override
	public ContentBuilder talkImpl(int turn, int skip, int utter, Game game, CndlStrategy strategy) {
		//狼でない者への投票を宣言する。
		if (game.getDay() == 2) {
			GameAgent wolf = null;
			RoleProbabilityStruct rps = strategy.bestPredictor.getRoleProbabilityStruct(game);

			List<GameAgent> seerCOs = game.getAllTalks()
					.filter(t -> t.getTopic() == Topic.COMINGOUT && t.getRole() == Role.SEER && !t.getTalker().isSelf)
					.map(t -> t.getTalker()).collect(Collectors.toList());
			List<GameAgent> others = game.getAliveOthers();
			// 対抗が1人ならCOしていないのから判断
			if (seerCOs.size() == 1) {
				others.removeAll(seerCOs);
				List<DataScore<GameAgent>> candidates = rps.getAgnetProbabilityList(Role.WEREWOLF,
						others);
				wolf = candidates.get(0).data;
			// 対抗が2人以上なら、騙り履歴から判断
			} else if (seerCOs.size() > 1) {
				for (GameAgent ag : seerCOs) {
					Village5Tactics AgTac = strategy.v5TacEvalModel.getTacitcsMap(ag);
					if (AgTac.day2HasfakeCoSeer && ag.isAlive) {
						wolf = ag;
					}
				}
			}
			// さもなければスコアで判断
			if (wolf == null) {
				List<DataScore<GameAgent>> candidates = rps.getAgnetProbabilityList(Role.WEREWOLF,
						game.getAliveOthers());
				wolf = candidates.get(0).data;
			}
			GameAgent newTarget = null;
			if (wolf != null) {
				others = game.getAliveOthers();
				others.remove(wolf);
				newTarget = others.get(0);
			}

			if (newTarget != null && newTarget != currentTarget) {
				currentTarget = newTarget;
				log("Day2：新しいターゲット：" + currentTarget);
				return new VoteContentBuilder(currentTarget.agent);
			}
		}

		return null;

	}

}

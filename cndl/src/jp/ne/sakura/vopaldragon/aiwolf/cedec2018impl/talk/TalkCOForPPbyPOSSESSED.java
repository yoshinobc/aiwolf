package jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.talk;

import java.util.List;
import java.util.stream.Collectors;

import org.aiwolf.client.lib.ComingoutContentBuilder;
import org.aiwolf.client.lib.ContentBuilder;
import org.aiwolf.client.lib.Topic;
import org.aiwolf.common.data.Role;

import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.CndlStrategy;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.evalmodel.Village5TacEvalModel.Village5Tactics;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.framework.Game;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.framework.GameAgent;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.model.RoleProbabilityStruct;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.util.DataScore;

public class TalkCOForPPbyPOSSESSED extends CndlTalkTactic {

	@Override
	public ContentBuilder talkImpl(int turn, int skip, int utter, Game game, CndlStrategy strategy) {
		if (game.getSelf().coRole != Role.POSSESSED && game.getSelf().coRole != Role.WEREWOLF) {
			GameAgent wolfcand = null;
			GameAgent vil = null;
			List<GameAgent> Others = game.getAliveOthers();
			List<GameAgent> seerCOs = game.getAllTalks()
					.filter(t -> t.getTopic() == Topic.COMINGOUT && t.getRole() == Role.SEER && !t.getTalker().isSelf)
					.map(t -> t.getTalker()).collect(Collectors.toList());
			// 対抗が二人以上の時、嘘CO履歴のある生存を狼とする。
			if (seerCOs.size() > 1) {
				for (GameAgent ag : seerCOs) {
					Village5Tactics AgTac = strategy.v5TacEvalModel.getTacitcsMap(ag);
					if (AgTac.day2HasfakeCoSeer && ag.isAlive) {
						wolfcand = ag;
						Others.remove(ag);
						vil = Others.get(0);
						break;
					}
				}
				// 対抗が1人で、生存時、対抗を村とする。
			} else if (seerCOs.size() == 1) {
				vil = seerCOs.get(0).isAlive ? seerCOs.get(0) : null;
				if (vil != null) {
					Others.remove(vil);
					wolfcand = Others.get(0);
				}
			}
			// 判断がつかない場合はスコアだより
			if (wolfcand == null) {
				RoleProbabilityStruct rps = strategy.bestPredictor.getRoleProbabilityStruct(game);
				List<DataScore<GameAgent>> wolfrank = rps.getAgnetProbabilityList(Role.WEREWOLF, game.getAliveOthers());
				wolfcand = wolfrank.get(0).data;
				vil = wolfrank.get(wolfrank.size() - 1).data;
			}
			// PPを検討する。
			if (wolfcand != null && vil != null) {
				Village5Tactics WoTac = strategy.v5TacEvalModel.getTacitcsMap(wolfcand);
				Village5Tactics ViTac = strategy.v5TacEvalModel.getTacitcsMap(vil);
				if (WoTac.day2WerewolfHasAvoidWerewolf || !ViTac.day2VillagerHasAvoidWerewolf) {
					// 狼が狼COへの投票を避けるなら票合わせができる。村が狼COへの投票を避けないなら自分を犠牲にできる。
					return new ComingoutContentBuilder(game.getSelf().agent, Role.WEREWOLF);
				} else if (!WoTac.day2WerewolfHasVoteToPossessed) {
					// 狼が単独狂人宣言を相手に投票したことがないなら狂人CO
					return new ComingoutContentBuilder(game.getSelf().agent, Role.POSSESSED);
				} else {
					return null;
				}

			}
		}
		return null;
	}

}

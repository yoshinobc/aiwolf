package jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.talk;

import java.util.List;
import java.util.stream.Collectors;

import org.aiwolf.client.lib.ComingoutContentBuilder;
import org.aiwolf.client.lib.ContentBuilder;
import org.aiwolf.client.lib.Topic;
import org.aiwolf.common.data.Role;
import org.aiwolf.common.data.Species;

import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.CndlStrategy;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.evalmodel.Village5TacEvalModel.Village5Tactics;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.framework.EventType;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.framework.Game;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.framework.GameAgent;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.framework.GameEvent;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.model.RoleProbabilityStruct;

public class TalkCOForPPbySEER extends CndlTalkTactic {

	@Override
	public ContentBuilder talkImpl(int turn, int skip, int utter, Game game, CndlStrategy strategy) {
		if (game.getSelf().coRole != Role.POSSESSED && game.getSelf().coRole != Role.WEREWOLF) {
			GameAgent wolfcand = null;
			GameAgent vil = null;
			List<GameAgent> others = game.getAliveOthers();
			List<GameAgent> seerCOs = game
					.getAllTalks().filter(t -> t.getTopic() == Topic.COMINGOUT && t.getRole() == Role.SEER
							&& !t.getTalker().isSelf && t.getTalker().isAlive)
					.map(t -> t.getTalker()).collect(Collectors.toList());
			if (divineTarget != null && result == Species.WEREWOLF) {
				wolfcand = divineTarget;
				others.remove(wolfcand);
				vil = others.get(0);
			} else {
				if (divineTarget != null && divineTarget.isAlive) {
					vil = divineTarget;
					others.remove(vil);
					wolfcand = others.get(0);
				} else if (seerCOs.size() == 1) {
					vil = seerCOs.get(0);
					others.remove(vil);
					wolfcand = others.get(0);
				} else {
					RoleProbabilityStruct rps = strategy.bestPredictor.getRoleProbabilityStruct(game);
					wolfcand = rps.topAgent(Role.WEREWOLF, others).data;
					vil = others.get(1);
				}
			}
			// SPWのパターンのみ、PPを検討する。
			if (wolfcand != null && vil != null) {
				Village5Tactics WoTac = strategy.v5TacEvalModel.getTacitcsMap(wolfcand);
				Village5Tactics ViTac = strategy.v5TacEvalModel.getTacitcsMap(vil);
				// 対抗が1人生存：SPW、基本的に狂人のふり。
				if (seerCOs.size() == 1) {
					if (WoTac.day2WerewolfHasVoteToPossessed) {
						if (WoTac.day2WerewolfHasAvoidWerewolf || ViTac.day2VillagerHasAvoidWerewolf) {
							// 狂人へ投票する狼でも、狂人または狼が単独狼COへの投票を避けたことがある場合、バグ狙い。
							// どちらを味方につけるかは迷いどころだが、戦績を見ると他狂人の作り込みが甘そうなので狙いとしては狼のふり。
							// System.err.println("SPW:WCO");
							return new ComingoutContentBuilder(game.getSelf().agent, Role.WEREWOLF);
						} else {
							// そうでなければPPは無理
							// System.err.println("SPW:NoneCO");
							return null;
						}
					} else {
						// 原則として狂人COする。狼が狂人COへの投票を躊躇しないならやめる。
						// System.err.println("SPW:PCO");
						return new ComingoutContentBuilder(game.getSelf().agent, Role.POSSESSED);
					}
					// 対抗が2人生存：SPW(CO)、基本的に狼のふり
				} else if (seerCOs.size() == 2) {
					// 狂人が単独狼COへの投票を避けたことがあるなら、狂人を味方につけるべく狼のふり
					if (ViTac.day2VillagerHasAvoidWerewolf) {
						//System.err.println("SPWCO:WCO");
						return new ComingoutContentBuilder(game.getSelf().agent, Role.WEREWOLF);
					}
					// 狼が狂人へ投票をしたことがないなら、狂人のふり。
					if (!WoTac.day2WerewolfHasVoteToPossessed) {
						//System.err.println("SPWCO:PCO");
						return new ComingoutContentBuilder(game.getSelf().agent, Role.POSSESSED);
					}
					//System.err.println("SPWCO:NoneCO");
					return null;
				}
			}
		}
		// System.err.println("SVW:NoneCO");
		return null;
	}

	private GameAgent divineTarget;
	private Species result;

	@Override
	public void handleEvent(Game g, GameEvent e) {
		if (e.type == EventType.DIVINE) {
			divineTarget = e.target;
			result = e.species;
		}
	}

}

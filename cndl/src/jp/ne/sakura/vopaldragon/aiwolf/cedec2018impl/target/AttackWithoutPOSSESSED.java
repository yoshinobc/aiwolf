package jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.target;

import java.util.List;
import java.util.stream.Collectors;

import org.aiwolf.client.lib.Topic;
import org.aiwolf.common.data.Role;
import org.aiwolf.common.data.Species;

import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.CndlStrategy;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.evalmodel.Village5TacEvalModel.Village5Tactics;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.framework.Game;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.framework.GameAgent;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.framework.GameTalk;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.model.RoleProbabilityStruct;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.util.Utils;

public class AttackWithoutPOSSESSED extends CndlTargetTactic {
	/* ２日目を占狂狼、狂村狼、村村狼というパターンだけにする。 */
	@Override
	public GameAgent targetImpl(Game game, CndlStrategy strategy) {
		/* 確定狂人を探す */
		GameAgent Possessedy = null;
		List<GameTalk> divine = game.getAllTalks().filter(t -> t.getTopic() == Topic.DIVINED)
				.collect(Collectors.toList());
		if (!divine.isEmpty()) {
			for (GameTalk t : divine) {
				// 自分に白。真占い時に嘘白をしたことがない。
				if (t.getTarget().isSelf && t.getResult() == Species.HUMAN && t.getTalker().isAlive
						&& !strategy.v5TacEvalModel.getTacitcsMap(t.getTalker()).LiarSeerDivinedFakeWhite) {
					Possessedy = t.getTalker();
					break;
				}
				// 自分以外に黒。真占い時に嘘黒をしたことがない。
				if (!t.getTarget().isSelf && t.getResult() == Species.WEREWOLF && t.getTalker().isAlive
						&& !strategy.v5TacEvalModel.getTacitcsMap(t.getTalker()).LiarSeerDivinedFakeBlack) {
					Possessedy = t.getTalker();
					break;
				}
			}
		}

		List<GameAgent> seerCOs = game.getAllTalks()
				.filter(t -> t.getDay() == 1 && t.getTopic() == Topic.COMINGOUT && t.getRole() == Role.SEER)
				.map(t -> t.getTalker()).collect(Collectors.toList());
		List<GameAgent> aliveseerCOs = game.getAllTalks().filter(t -> t.getTalker().isAlive && t.getDay() == 1
				&& t.getTopic() == Topic.COMINGOUT && t.getRole() == Role.SEER).map(t -> t.getTalker())
				.collect(Collectors.toList());
		List<GameAgent> Others = game.getAliveOthers();
		// 占いCOが2人生存しているなら、残すのが最適だが・・・。
		if (aliveseerCOs.size() == 2) {
/*			//狂人候補が対抗に投票しないようなら占いを襲撃ー＞かえって勝率落ちた。Sの存在はなぜか役立つようだ。
			if (Possessedy == null && seerCOs.size() == 2) {
				RoleProbabilityStruct rps = strategy.bestPredictor.getRoleProbabilityStruct(game);
				Possessedy = rps.topAgent(Role.POSSESSED, seerCOs).data;
			}
			if (Possessedy != null) {
				Village5Tactics PoTac = strategy.v5TacEvalModel.getTacitcsMap(Possessedy);
				if (PoTac.day2PossessedNotVoteToSeer) {
					aliveseerCOs.remove(Possessedy);
					return aliveseerCOs.get(0);
				}
			}*/
			for (GameAgent seer : aliveseerCOs) {
				Others.remove(seer);
			}
			return Others.get(0);

		} else if (aliveseerCOs.size() == 1) {
			GameAgent seer = aliveseerCOs.get(0);
			//論理的に確定できなくてもスコアで判定。localでは勝率アップ
			if (Possessedy == null && seerCOs.size() == 2) {
				RoleProbabilityStruct rps = strategy.bestPredictor.getRoleProbabilityStruct(game);
				Possessedy = rps.topAgent(Role.POSSESSED, seerCOs).data;
			}
			if (seer == Possessedy) {
				Others.remove(seer);
				Village5Tactics PoTac = strategy.v5TacEvalModel.getTacitcsMap(seer);
				/* 狂人が狼COへの投票をしたことない時、狼COする村人を襲う。 */
				if (!PoTac.day2PossessedHasVoteToWerewolf) {
					for (GameAgent ag : Others) {
						Village5Tactics agTac = strategy.v5TacEvalModel.getTacitcsMap(ag);
						if (agTac.day2SeerHasCoWerewolf) {
							return ag;
						}
					}
					/* 狂人が狼COしたことがある時、狼COへの投票を避ける村人を襲う。 */
				} else if (PoTac.day2PossessedHasCoWerewolf) {
					for (GameAgent ag : Others) {
						Village5Tactics agTac = strategy.v5TacEvalModel.getTacitcsMap(ag);
						if (agTac.day2VillagerHasAvoidWerewolf)
							return ag;
					}
				} else {
					/* 狼COへの投票を避けたことのある村人を襲う。 */
					for (GameAgent ag : Others) {
						Village5Tactics agTac = strategy.v5TacEvalModel.getTacitcsMap(ag);
						if (agTac.day2VillagerHasAvoidWerewolf)
							return ag;
					}
					/* もう無理。ESで狼っぽいやつ。 */
					double[] evilScore = strategy.evilModel.getEvilScore();
					Utils.sortByScore(Others, evilScore, false);
					return Others.get(0);
				}
			} else {
				/* 狂人と確定できない生存占いは襲う。 */
				return seer;
			}
		} else {
			// 一番狼っぽいやつを襲う。
			double[] evilScore = strategy.evilModel.getEvilScore();
			Utils.sortByScore(Others, evilScore, true);
			return Others.get(0);

		}
		return null;
	}
}

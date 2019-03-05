package jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.talk;

import static jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.util.Utils.*;

import java.util.List;
import java.util.stream.Collectors;

import org.aiwolf.client.lib.ContentBuilder;
import org.aiwolf.client.lib.Topic;
import org.aiwolf.client.lib.VoteContentBuilder;
import org.aiwolf.common.data.Role;
import org.aiwolf.common.data.Species;

import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.CndlStrategy;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.evalmodel.Village5TacEvalModel.Village5Tactics;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.framework.Game;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.framework.GameAgent;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.framework.GameTalk;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.model.RoleProbabilityStruct;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.util.Utils;

public class TalkVote5WolfDay2 extends CndlTalkTactic {

	private GameAgent currentTarget;

	@Override
	public ContentBuilder talkImpl(int turn, int skip, int utter, Game game, CndlStrategy strategy) {
		if (game.getDay() == 2) {

			GameAgent newTarget = null;
			List<GameAgent> candidates = game.getAliveOthers();

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

			// 初日に占いCOした生存者のリスト
			List<GameAgent> aliveseerCOs = game
					.getAllTalks().filter(t -> t.getDay() == 1 && t.getTopic() == Topic.COMINGOUT
							&& t.getRole() == Role.SEER && t.getTalker().isAlive)
					.map(t -> t.getTalker()).collect(Collectors.toList());
			/* 最終日：狂占狼（占いCO2人）のパターンでPP対応状況から判断 */
			if (Possessedy == null && aliveseerCOs.size() == 2) {
				List<GameAgent> PoCOs = game.getAliveOthers().stream().filter(ag -> ag.coRole == Role.POSSESSED)
						.collect(Collectors.toList());
				List<GameAgent> WolfCOs = game.getAliveOthers().stream().filter(ag -> ag.coRole == Role.WEREWOLF)
						.collect(Collectors.toList());
				int PoCoCount = PoCOs.size();
				int WolfCoCount = WolfCOs.size();
				// 二人ともCOしてない(狂人の時COしない&&[占いの時COする||対抗が狂人の時COする])
				if (PoCoCount == 0 && WolfCoCount == 0) {
					GameAgent VilA = game.getAliveOthers().get(0);
					GameAgent VilB = game.getAliveOthers().get(1);
					Village5Tactics ATac = strategy.v5TacEvalModel.getTacitcsMap(VilA);
					Village5Tactics BTac = strategy.v5TacEvalModel.getTacitcsMap(VilB);
					if ((!ATac.day2PossessedHasCoPossessed && !ATac.day2PossessedHasCoWerewolf)
							&& (ATac.day2SeerHasCoPossessed || ATac.day2SeerHasCoWerewolf)) {
						Possessedy = VilA;
					}
					if ((!ATac.day2PossessedHasCoPossessed && !ATac.day2PossessedHasCoWerewolf)
							&& (BTac.day2PossessedHasCoPossessed || BTac.day2PossessedHasCoWerewolf)) {
						Possessedy = VilA;
					}
					if ((!BTac.day2PossessedHasCoPossessed && !BTac.day2PossessedHasCoWerewolf)
							&& (BTac.day2SeerHasCoPossessed || BTac.day2SeerHasCoWerewolf)) {
						Possessedy = VilB;
					}
					if ((!BTac.day2PossessedHasCoPossessed && !BTac.day2PossessedHasCoWerewolf)
							&& (ATac.day2PossessedHasCoPossessed || ATac.day2PossessedHasCoWerewolf)) {
						Possessedy = VilB;
					}
				}
				// 二人とも狂人CO（狂人の時狂人CO&&[対抗が狂人の時狂人COしない||占いの時狂人COしない]）
				if (PoCoCount == 2 && WolfCoCount == 0) {
					GameAgent VilA = PoCOs.get(0);
					GameAgent VilB = PoCOs.get(1);
					Village5Tactics ATac = strategy.v5TacEvalModel.getTacitcsMap(VilA);
					Village5Tactics BTac = strategy.v5TacEvalModel.getTacitcsMap(VilB);
					if (ATac.day2PossessedHasCoPossessed && !BTac.day2PossessedHasCoPossessed) {
						Possessedy = VilA;
					}
					if (ATac.day2PossessedHasCoPossessed && !ATac.day2SeerHasCoPossessed) {
						Possessedy = VilA;
					}
					if (BTac.day2PossessedHasCoPossessed && !ATac.day2PossessedHasCoPossessed) {
						Possessedy = VilB;
					}
					if (BTac.day2PossessedHasCoPossessed && !BTac.day2SeerHasCoPossessed) {
						Possessedy = VilB;
					}
				}
				// 二人とも狼CO（狂人の時狼CO&&[対抗が狂人の時狼COしない||占いの時狼COしない]）
				if (PoCoCount == 0 && WolfCoCount == 2) {
					GameAgent VilA = WolfCOs.get(0);
					GameAgent VilB = WolfCOs.get(1);
					Village5Tactics ATac = strategy.v5TacEvalModel.getTacitcsMap(VilA);
					Village5Tactics BTac = strategy.v5TacEvalModel.getTacitcsMap(VilB);
					if (ATac.day2PossessedHasCoWerewolf && !BTac.day2PossessedHasCoWerewolf) {
						Possessedy = VilA;
					}
					if (ATac.day2PossessedHasCoWerewolf && !ATac.day2SeerHasCoWerewolf) {
						Possessedy = VilA;
					}
					if (BTac.day2PossessedHasCoWerewolf && !ATac.day2PossessedHasCoWerewolf) {
						Possessedy = VilB;
					}
					if (BTac.day2PossessedHasCoWerewolf && !BTac.day2SeerHasCoWerewolf) {
						Possessedy = VilB;
					}
				}
				// 狂人CO1人、狼COなし
				if (PoCoCount == 1 && WolfCoCount == 0) {
					GameAgent VilA = PoCOs.get(0);
					aliveseerCOs.remove(VilA);
					GameAgent VilB = aliveseerCOs.get(0);
					Village5Tactics ATac = strategy.v5TacEvalModel.getTacitcsMap(VilA);
					Village5Tactics BTac = strategy.v5TacEvalModel.getTacitcsMap(VilB);
					if (ATac.day2PossessedHasCoPossessed && (!ATac.day2SeerHasCoPossessed
							|| BTac.day2PossessedHasCoPossessed || BTac.day2PossessedHasCoWerewolf)) {
						Possessedy = VilA;
					}
					if (!BTac.day2PossessedHasCoPossessed && !BTac.day2PossessedHasCoWerewolf
							&& (BTac.day2SeerHasCoPossessed || BTac.day2SeerHasCoWerewolf
									|| !ATac.day2PossessedHasCoPossessed)) {
						Possessedy = VilB;
					}
				}
				// 狼CO1人、狂人COなし
				if (PoCoCount == 0 && WolfCoCount == 1) {
					GameAgent VilA = WolfCOs.get(0);
					aliveseerCOs.remove(VilA);
					GameAgent VilB = aliveseerCOs.get(0);
					Village5Tactics ATac = strategy.v5TacEvalModel.getTacitcsMap(VilA);
					Village5Tactics BTac = strategy.v5TacEvalModel.getTacitcsMap(VilB);
					if (ATac.day2PossessedHasCoWerewolf && (!ATac.day2SeerHasCoWerewolf
							|| BTac.day2PossessedHasCoPossessed || BTac.day2PossessedHasCoWerewolf)) {
						Possessedy = VilA;
					}
					if (!BTac.day2PossessedHasCoPossessed && !BTac.day2PossessedHasCoWerewolf
							&& (BTac.day2SeerHasCoPossessed || BTac.day2SeerHasCoWerewolf
									|| !ATac.day2PossessedHasCoWerewolf)) {
						Possessedy = VilB;
					}
				}
				// 狂人CO1人、狼CO1人
				if (PoCoCount == 1 && WolfCoCount == 1) {
					GameAgent VilA = WolfCOs.get(0);
					GameAgent VilB = PoCOs.get(0);
					Village5Tactics ATac = strategy.v5TacEvalModel.getTacitcsMap(VilA);
					Village5Tactics BTac = strategy.v5TacEvalModel.getTacitcsMap(VilB);
					if (ATac.day2PossessedHasCoWerewolf
							&& (!ATac.day2SeerHasCoWerewolf || !BTac.day2PossessedHasCoPossessed)) {
						Possessedy = VilA;
					}
					if (BTac.day2PossessedHasCoPossessed
							&& (!BTac.day2SeerHasCoPossessed || !ATac.day2SeerHasCoWerewolf)) {
						Possessedy = VilB;
					}
				}

			}

			// 生存COが1人なら、それはCNDL戦略では狂人
			if (aliveseerCOs.size() == 1) {
				Possessedy = aliveseerCOs.get(0);
			}
			// 狂人と判定できない時はスコア依存
			List<GameAgent> seerCOs = game.getAllTalks()
					.filter(t -> t.getTopic() == Topic.COMINGOUT && t.getRole() == Role.SEER).map(t -> t.getTalker())
					.collect(Collectors.toList());
			if (Possessedy == null && seerCOs.size() == 2) {
				RoleProbabilityStruct rps = strategy.bestPredictor.getRoleProbabilityStruct(game);
				Possessedy = rps.topAgent(Role.POSSESSED, seerCOs).data;
				if (!Possessedy.isAlive)
					Possessedy = null;
			}

			// 狂人を特定できたら、そうでないのをターゲットにする。
			if (Possessedy != null) {
				List<GameAgent> Alives = game.getAliveOthers();
				Alives.remove(Possessedy);
				// System.out.println("狂人特定できたよ。投票先の村:"+ Alives.get(0));
				newTarget = Alives.get(0);
			}
			// 狂人を特定できない占狂狼の場合、村っぽい自称占いをターゲットにする。
			if (newTarget == null && aliveseerCOs.size() == 2) {
				if (!candidates.isEmpty()) {
					double[] evilScore = strategy.evilModel.getEvilScore();
					Utils.sortByScore(candidates, evilScore, false);
					newTarget = candidates.get(candidates.size() - 1);
				}
			}

			// まだ決まらん場合（村村狼か）、得票すうの高い者またはEvilScore高い方を殴
			if (newTarget == null) {
				if (turn < 2) {
					List<GameAgent> sacrifice = strategy.voteModel.expectedVote().topCounts();
					for (GameAgent sf : sacrifice) {
						if (!sf.isSelf) {
							newTarget = sf;
							break;
						}
					}
					if (newTarget == null && !candidates.isEmpty()) {
						double[] evilScore = strategy.evilModel.getEvilScore();
						Utils.sortByScore(candidates, evilScore, false);
						newTarget = candidates.get(0);
					}
				}
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

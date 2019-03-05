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
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.util.Utils;

public class TalkVote5WolfDay1 extends CndlTalkTactic {

	private GameAgent currentTarget;
	private int votecounter;

	@Override
	public ContentBuilder talkImpl(int turn, int skip, int utter, Game game, CndlStrategy strategy) {
		// 占い結果後に発言
		if (game.getDay() == 1) {
//			 if (turn == 0) {
//			 return null;

			if (currentTarget == null) { // 初期、最も弱い人に投票宣言
				currentTarget = strategy.voteModel.currentVoteTarget = strategy.winEvalModel.getAgentWinCount(game.getAliveOthers())
						.sort().top().data;
				log("Day1:初期殴り先：" + currentTarget);
				votecounter += 1;
				return new VoteContentBuilder(currentTarget.agent);

			} else if(votecounter < 3) {
				List<GameAgent> seerCOs = game.getAliveOthers().stream().filter(ag -> ag.coRole == Role.SEER)
						.collect(Collectors.toList());

				GameAgent possessed = null;
				GameAgent newTarget = null;

				// 占い結果が判ったら、それに合わせて投票先を変える
				List<GameTalk> divine = game.getAllTalks()
						.filter(t -> t.getDay() == game.getDay() && t.getTopic() == Topic.DIVINED)
						.collect(Collectors.toList());

				if (!divine.isEmpty()) {
					for (GameTalk t : divine) {
						// 白出し狂人の探索（自分に白出しをしていて、真占い時に嘘白出しをしたことがない。）
						if (t.getTarget().isSelf && t.getResult() == Species.HUMAN) {
							Village5Tactics tac = strategy.v5TacEvalModel.getTacitcsMap(t.getTalker());
							if (!tac.LiarSeerDivinedFakeWhite) {
								possessed = t.getTalker();
								break;
							}
						}
						// 黒出し狂人の探索（自分以外に黒を出していて、真占い時に嘘黒をしたことがない。）
						if (!t.getTarget().isSelf && t.getResult() == Species.WEREWOLF) {
							Village5Tactics tac = strategy.v5TacEvalModel.getTacitcsMap(t.getTalker());
							if (!tac.LiarSeerDivinedFakeBlack) {
								possessed = t.getTalker();
								break;
							}
						}
					}
				}
				if (seerCOs.size() < 3) {
					// 占いCOが2人以下の場合、COしていない村人からES高めの村を対象にする。
					if (newTarget == null) {
						List<GameAgent> candidates = game.getAliveOthers();
						for (GameAgent ag : seerCOs) {
							candidates.remove(ag);
						}
						if (!candidates.isEmpty()) {
							if (!divine.isEmpty()) {
								List<GameAgent> otherblacks = divine.stream().filter(
										t -> t.getTarget() != game.getSelf() && t.getResult() == Species.WEREWOLF)
										.map(t -> t.getTarget()).collect(Collectors.toList());
								List<GameAgent> whites = divine.stream().filter(t -> t.getResult() == Species.HUMAN)
										.map(t -> t.getTarget()).collect(Collectors.toList());
								if (!otherblacks.isEmpty()) {
									GameAgent otherblack = otherblacks.get(0);
									if (candidates.contains(otherblack)) {
										newTarget = otherblacks.get(0);
									}
									if (seerCOs.contains(otherblack)) {
										candidates.removeAll(whites);
										if (!candidates.isEmpty()) {
											newTarget = candidates.get(0);
										}
									}
								}
							} else {
								double[] evilScore = strategy.evilModel.getEvilScore();
								Utils.sortByScore(candidates, evilScore, false);
								newTarget = candidates.get(0);
								log("Day1:EvilScore高めの人を殴る");
							}
						}
					}
				} else {
					// 占いCOが3人以上の場合、COした自称占いからES低めの村を対象にする。確定狂人は除く。
					if (possessed != null) {
						seerCOs.remove(possessed);
					}
					double[] evilScore = strategy.evilModel.getEvilScore();
					Utils.sortByScore(seerCOs, evilScore, false);
					newTarget = seerCOs.get(seerCOs.size() - 1);
					log("Day1:EvilScore低めの人を殴る");
				}
				/*
				 * HashCounter<GameAgent> counts = strategy.voteModel.expectedVote();
				 * ListMap<Integer, GameAgent> listMap = counts.getCounts(); int maxVote =
				 * counts.topCount(); List<GameAgent> tops = listMap.getList(maxVote);
				 * List<GameAgent> seconds = listMap.getList(maxVote - 1); if (newTarget != null
				 * && (tops.contains(newTarget) || seconds.contains(newTarget))) { if (newTarget
				 * != currentTarget) { currentTarget = newTarget; log("Day1:新しいターゲット：" +
				 * currentTarget); return new VoteContentBuilder(currentTarget.agent); } }
				 * seconds = counts.topNCounts(2); seconds.removeAll(tops); if
				 * (tops.contains(game.getSelf())) { if (tops.size() > 1) {
				 * tops.remove(game.getSelf()); double[] evilScore =
				 * strategy.evilModel.getEvilScore(); Utils.sortByScore(tops, evilScore, false);
				 * newTarget = tops.get(tops.size() - 1); } else if(!seconds.isEmpty()){
				 * double[] evilScore = strategy.evilModel.getEvilScore();
				 * Utils.sortByScore(seconds, evilScore, false); newTarget =
				 * seconds.get(seconds.size() - 1); } }
				 */

				if (newTarget != null && newTarget != currentTarget) {
					currentTarget = newTarget;
					log("Day1:新しいターゲット：" + currentTarget);
					votecounter += 1;
					return new VoteContentBuilder(currentTarget.agent);
				}
			}

		}

		return null;

	}

}

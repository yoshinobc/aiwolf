package jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.target;

import java.util.List;
import java.util.stream.Collectors;

import org.aiwolf.client.lib.Topic;
import org.aiwolf.common.data.Role;
import org.aiwolf.common.data.Species;

import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.CndlStrategy;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.framework.Game;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.framework.GameAgent;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.model.RoleProbabilityStruct;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.util.DataScore;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.util.HashCounter;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.util.ListMap;

public class VoteWithoutWolf extends CndlTargetTactic {

	@Override
	public GameAgent targetImpl(Game game, CndlStrategy strategy) {
		List<GameAgent> list = game.getAliveOthers();
		RoleProbabilityStruct rps = strategy.bestPredictor.getRoleProbabilityStruct(game);
		List<DataScore<GameAgent>> candidates = rps.getAgnetProbabilityList(Role.WEREWOLF, list);
		GameAgent wolf = candidates.get(0).data;
		List<GameAgent> divines = game.getSelf().talkList.stream()
				.filter(x -> x.getDay() == game.getDay() && x.getTopic() == Topic.DIVINED
						&& x.getResult() == Species.WEREWOLF)
				.map(x -> x.getTarget()).filter(x -> x.isAlive).collect(Collectors.toList());

		// 自分のターゲットが狼っぽくないならそのまま利用
		GameAgent target = null;
		if (!divines.isEmpty()) {
			if (divines.get(0) != wolf) {
				target = divines.get(0);
			}
		}
		// 村人から借用
		// 自分以外の投票の組み合わせは、a.3-1,b.2-2,c.2-1-1,d.1-1-1-1のいずれか;ただし初日はexpectedVoteは単純に宣言内容を返すので、このパターン以外になることもある。
		HashCounter<GameAgent> voteCount = strategy.voteModel.expectedVote();
		int topCount = voteCount.topCount();
		ListMap<Integer, GameAgent> inversedCountMap = voteCount.getCounts();
		// System.err.println("-----------------" +
		// game.getSelf()+"------------"+game.getAgentStream().map(ag->ag+"/"+Utils.ROLE_MAP.get(ag.getIndex())).collect(Collectors.joining(",
		// ")));
		// if (target != null) System.err.println("InitialTarget\t" + target + "\t" +
		// Utils.ROLE_MAP.get(target.getIndex()));
		// System.err.println(inversedCountMap);
		List<GameAgent> topcounts = inversedCountMap.getList(topCount);
		List<GameAgent> seconds = inversedCountMap.getList(topCount - 1);// cパターンの場合のみ空で無い

		// targetがトップ集団ないしはトップ-1集団にいるなら、とりあず決めたtargetに投票
		if (target != null && (topcounts.contains(target) || seconds.contains(target))) {
			 //System.err.println("OK\t" + target);
			return target;
		}

		if (topCount == 3 && !inversedCountMap.getList(1).isEmpty()) {
			// aパターンの場合、トップが狼でないなら乗っかる。狼が吊られそうなら１票入っている二番手。結果自分だったら無理
			target = topcounts.contains(wolf) ? inversedCountMap.getList(1).get(0) : topcounts.get(0);
			if (target != null && !target.isSelf) {
				 //System.err.println("Pattern A\t" + target);
				return target;
			}
		}
		if (topCount == 2 && topcounts.size() == 2) {
			// bパターンの場合、狼と自分を抜いて、どっちでもいいので乗っかる。
			topcounts.remove(wolf);
			topcounts.remove(game.getSelf());
			if (!topcounts.isEmpty()) {
				target = topcounts.get(0);
				//System.err.println("Pattern B\t" + target);
				return target;
			}
		}
		if (topCount == 2 && !seconds.isEmpty()) {
			// cパターンの場合、トップが狼でも自分でもないなら乗っかる。そうでなければ、2位から自分でも狼でもない相手
			target = topcounts.get(0);
			if (target == wolf || target == game.getSelf()) {
				for (GameAgent sc : seconds) {
					if (sc != wolf && sc != game.getSelf()) {
						target = sc;
						break;
					}
				}
			}
			if (target != null) {
				 //System.err.println("Pattern C\t" + target);
				return target;
			}
		}
		if (topCount == 1) {
			// dパターンの場合、一番狼ではない者
			List<DataScore<GameAgent>> targets = rps.getAgnetProbabilityList(Role.WEREWOLF, topcounts);
			target = targets.get(targets.size() -1).data;
			//System.err.println("Pattern D\t" + target);
			return target;
		}

		// 最悪の場合は一番狼っぽくない相手
		GameAgent wolfCand = candidates.get(candidates.size() - 1).data;
		//System.err.println("Pattern F\t" + target);
		return wolfCand;

	}
}

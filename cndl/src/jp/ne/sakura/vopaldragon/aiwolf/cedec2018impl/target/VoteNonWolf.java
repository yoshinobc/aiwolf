package jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.target;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.aiwolf.common.data.Role;

import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.CndlStrategy;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.framework.Game;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.framework.GameAgent;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.model.RoleProbabilityStruct;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.util.HashCounter;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.util.ListMap;

/**
 * @author aki
 * 狼じゃ無い村民が最大得票しそうなら乗っかる
 * 得票第１位が狼、得票第2位が人間の時、第２位との差が ASOBI 以下なら第2位に投票
 * 自分への投票が最大になりそうなら 仲間を売ってでも生き残る
 * 得票第１位も第２位も味方なら、諦めて最大得票に乗っかる
 * 村の人口が９人以上なら、役職COは避ける。
 *
 * 以上を総合すると、
 * (人口9人以下なら役職COを避けて) 得票1, 2位を選定
 * 得票第１位が狼でかつ得票第２位が狼でない、または得票第1位が自分なら、得票第２位に投票
 * それ以外の場合は得票第1位に投票
 *
 */
public class VoteNonWolf extends CndlTargetTactic {

    @Override
    public GameAgent targetImpl(Game game, CndlStrategy strategy) {
        return selectTarget(strategy.voteModel.getVoteDeclared().getVoteCountOfOthers(), game, strategy);
    }

	public GameAgent selectTarget(HashCounter<GameAgent> voteCount, Game game, CndlStrategy strategy) {
        GameAgent target = null;
        List<GameAgent> candidates;
        RoleProbabilityStruct rps = strategy.bestPredictor.getRoleProbabilityStruct(game);

        // 前日の投票行動を使う場合など、死者への投票行動があれば消しておく。
        for (GameAgent ga : game.getAgents()) {
			if(!ga.isAlive && voteCount.getKeySet().contains(ga)) voteCount.removeCount(ga);
		}
        // voteCount が空になってしまった場合
        if (voteCount.getKeySet().size() == 0) {
        		// TODO 色々考えたけど面倒になったので狼以外から一番狂人っぽくないやつを返すことにした。
        		candidates = game.getAliveOthers();
        		candidates.removeIf(x -> x.role == Role.WEREWOLF);
        		target = rps.getAgnetProbabilityList(Role.POSSESSED, candidates).get(candidates.size() - 1).data;
        		return target;
        }

        int topCount = voteCount.topCount();
        // 投票数別のエージェントのリスト
        ListMap<Integer, GameAgent> listMap = voteCount.getCounts();
        // 得票トップ集団
        List<GameAgent> topAgents = listMap.getList(voteCount.topCount()).stream()
        		.filter(x -> !x.isSelf && x.isAlive).collect(Collectors.toList());
        // 得票2位集団
        int ASOBI = 2;
        int sndCount = topCount;
        List<GameAgent> sndAgents = null;
        for (int i = 0; i < ASOBI; i++) {
	        	sndCount -= 1;
	        	sndAgents = listMap.getList(sndCount).stream()
	        			.filter(x -> !x.isSelf && x.isAlive).collect(Collectors.toList());
	        	if(!sndAgents.isEmpty()) break;
		}
        // 最大得票が少ない場合は2位集団として生き物全員を登録
        if (sndAgents.isEmpty() && topCount <= ASOBI) {
        		sndAgents = game.getAliveOthers();
        		sndAgents.removeIf(x -> voteCount.getCount(x) == topCount);
        }

        GameAgent me = game.getSelf();
		int myCount = voteCount.getCount(me);
		int exceptCOsh = 9; // 役職COを避ける村人口閾値

		candidates = topAgents.stream().filter(x -> x.role != Role.WEREWOLF &&
				(game.getAliveSize() < exceptCOsh || x.coRole == Role.MEDIUM || x.coRole == Role.SEER))
				.collect(Collectors.toList());
		// トップ集団に狼しかいないなら、閾値以下でも役職に投票
		if (candidates.isEmpty() && game.getAliveSize() >= exceptCOsh) {
			candidates = topAgents.stream().filter(x -> x.role != Role.WEREWOLF)
					.collect(Collectors.toList());
		}
		// 2位集団から選ぶ
		if (candidates.isEmpty()) {
			candidates = sndAgents.stream().filter(x -> x.role != Role.WEREWOLF &&
					(game.getAliveSize() < exceptCOsh || x.coRole == Role.MEDIUM || x.coRole == Role.SEER))
					.collect(Collectors.toList());
			if (candidates.isEmpty() && game.getAliveSize() >= exceptCOsh) {
				candidates = sndAgents.stream().filter(x -> x.role != Role.WEREWOLF)
						.collect(Collectors.toList());
			}
		}
		// どう頑張っても仲間が吊られる
		if (candidates.isEmpty()) candidates = topAgents;
		if (candidates.isEmpty()) candidates = sndAgents;
		// ヤケクソ
		if (candidates.isEmpty()) candidates = game.getAliveOthers();

		// 狂人だけをできるだけ避けて投票
		target = rps.getAgnetProbabilityList(Role.POSSESSED, candidates).get(candidates.size() - 1).data;        
        
        return target;
	}

}

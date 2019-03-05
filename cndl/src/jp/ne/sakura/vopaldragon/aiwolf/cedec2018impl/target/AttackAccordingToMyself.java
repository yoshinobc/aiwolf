package jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.target;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.CndlStrategy;

import org.aiwolf.client.lib.AttackContentBuilder;
import org.aiwolf.client.lib.Topic;
import org.aiwolf.common.data.Role;

import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.framework.EventType;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.framework.Game;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.framework.GameAgent;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.model.RoleProbabilityStruct;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.util.DataScore;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.util.Utils;

/**
 *
 * 自分が Vote すると言った相手に Vote
 *
 */
public class AttackAccordingToMyself extends CndlTargetTactic {

    Map<GameAgent, Integer> voteFrom = new HashMap<>();

    @Override
    public GameAgent targetImpl(Game game, CndlStrategy strategy) {
        GameAgent me = game.getSelf();
        /* 自分が襲撃すると宣言した先を取得 */
        List<GameAgent> attack_targets = me.whisperList.stream().filter(x -> x.getDay() == game.getDay()
            && x.getTopic() == Topic.ATTACK).map(x -> x.getTarget()).filter(x -> x.isAlive)
            .collect(Collectors.toList());
        Collections.reverse(attack_targets); // リストを逆順に

        /* 死者は死んだ */
        game.getAgents().stream().filter(x -> !x.isAlive).forEach(x -> voteFrom.remove(x));
        /* 前日の自分たちへの投票 */
        if (game.getLastEventOf(EventType.VOTE) != null) {
            game.getLastEventOf(EventType.VOTE).votes.stream().filter(v -> v.target.role == Role.WEREWOLF && v.initiator.role != Role.WEREWOLF && v.initiator.isAlive).forEach(v -> {
                if (voteFrom.containsKey(v.initiator)) {
                    voteFrom.put(v.initiator, voteFrom.get(v.initiator) + 1);
                } else {
                    voteFrom.put(v.initiator, 1);
                }
            });
        }

        GameAgent target = null;
        for (GameAgent agent : attack_targets) {
            if (agent.role != Role.WEREWOLF) {
                target = agent;
                break;
            }
        }
        if (target != null) return target;
    		/* トップメタを噛む */
    		if (strategy.findCndlModel != null) {
    			RoleProbabilityStruct rps = strategy.bestPredictor.getRoleProbabilityStruct(game);
    			List<DataScore<GameAgent>> candidates = rps.getAgnetProbabilityList(Role.POSSESSED).stream().filter(x -> x.data.isAlive && x.data.role != Role.WEREWOLF && x.data.coRole != Role.SEER && strategy.findCndlModel.cndlLike(x.data)).collect(Collectors.toList());
    			if(!candidates.isEmpty()) {
    				target = Utils.getRandom(Utils.getHighestScores(candidates, (ds -> -ds.score))).data;
    			}
    		}
		if (target != null) return target;
	    /* ヘイトの高いエージェントを探す */
        for (GameAgent agent : voteFrom.keySet()) {
            if (target == null || voteFrom.get(target) < voteFrom.get(agent)) {
                target = agent;
            }
        }
        if (target != null) return target;
        return Utils.getRandom(game.getAliveOthers().stream().filter(x -> x.role != Role.WEREWOLF).collect(Collectors.toList()));
    }

}

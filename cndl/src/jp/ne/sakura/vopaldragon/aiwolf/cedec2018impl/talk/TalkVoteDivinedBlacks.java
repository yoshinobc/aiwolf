package jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.talk;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.aiwolf.client.lib.ContentBuilder;
import org.aiwolf.client.lib.Topic;
import org.aiwolf.client.lib.VoteContentBuilder;
import org.aiwolf.common.data.Role;
import org.aiwolf.common.data.Species;

import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.CndlStrategy;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.framework.EventType;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.framework.Game;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.framework.GameAgent;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.framework.GameEvent;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.model.RoleProbabilityStruct;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.talk.CndlTalkTactic;

/**
 * @author aki
 * 狼2日目以降用
 * 黒出しされた生物にとりあえず一票入れると云ってみる。
 */
public class TalkVoteDivinedBlacks extends CndlTalkTactic {
	private Set<GameAgent> estimated = new HashSet<>();
	private Set<GameAgent> divined = new HashSet<>();
	final int exceptCOsh = 9; // 役職COを避ける村人口閾値
	@Override
	public ContentBuilder talkImpl(int turn, int skip, int utter, Game game, CndlStrategy strategy) {
		// あんまり使い続けるのは怖いので、仲間が一人死んだらやめる
		List<GameAgent> wolves = game.getAlives().stream().filter(x -> x.role == Role.WEREWOLF).collect(Collectors.toList());
		if (wolves.size() == 3 && !divined.isEmpty()) {
			List<GameAgent> candidates = new ArrayList<>();
			// 非狼優先
			divined.stream().filter(x -> x.role != Role.WEREWOLF).forEach(x -> candidates.add(x));
			if (candidates.isEmpty()) {
				divined.stream().forEach(x -> candidates.add(x));
			}
			// 村人口が多いときは役職COは外す
			if (game.getAlives().size() > exceptCOsh) {
				candidates.removeIf(x -> x.hasCO());
			}
			// このターンで Estimate白 してた場合は外す
			candidates.removeIf(x -> estimated.contains(x));
			if (!candidates.isEmpty()) {
				// 狂人だけをできるだけ避けて投票宣言
				RoleProbabilityStruct rps = strategy.bestPredictor.getRoleProbabilityStruct(game);
				GameAgent voteTarget = rps.getAgnetProbabilityList(Role.POSSESSED, candidates).get(candidates.size() - 1).data;
		        if (voteTarget != strategy.voteModel.currentVoteTarget) {
		            strategy.voteModel.currentVoteTarget = voteTarget;
		            return new VoteContentBuilder(voteTarget.agent);
		        }
			}
		}			
		return null;
	}

    @Override
    public void handleEvent(Game g, GameEvent e) {
        if (e.type == EventType.DAYSTART) {
            estimated.clear();
            divined.removeIf(x -> !x.isAlive);
        }
        if (e.type == EventType.TALK) {
        		e.talks.stream().filter(x -> x.getTalker().isSelf && x.getTopic() == Topic.ESTIMATE &&
        				x.getRole() != Role.WEREWOLF && x.getRole() != Role.POSSESSED)
        			.forEach(x -> estimated.add(x.getTarget()));
        		e.talks.stream().filter(x -> x.getTopic() == Topic.DIVINED && x.getResult() == Species.WEREWOLF &&
        				!x.getTarget().isSelf)
        			.forEach(x -> divined.add(x.getTarget()));
        }
    }
}

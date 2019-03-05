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
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.framework.Game;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.framework.GameAgent;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.framework.GameTalk;

public class TalkCOWolfForPPbyVILLAGER extends CndlTalkTactic {

	@Override
	public ContentBuilder talkImpl(int turn, int skip, int utter, Game game, CndlStrategy strategy) {
		if (!game.getSelf().hasCO()) {
			/* 確定狂人を探す */
			GameAgent Possessedy = null;
			List<GameTalk> divine = game.getAllTalks().filter(t -> t.getTopic() == Topic.DIVINED)
					.collect(Collectors.toList());
			if (!divine.isEmpty()) {
				for (GameTalk t : divine) {
					if (t.getTarget().isSelf && t.getResult() == Species.WEREWOLF && t.getTalker().isAlive && !strategy.v5TacEvalModel.getTacitcsMap(t.getTalker()).LiarSeerDivinedFakeBlack) {
						Possessedy = t.getTalker();
						break;
					}
					if (!t.getTarget().isAlive && t.getResult() == Species.WEREWOLF && t.getTalker().isAlive && !strategy.v5TacEvalModel.getTacitcsMap(t.getTalker()).LiarSeerDivinedFakeBlack) {
						Possessedy = t.getTalker();
						break;
					}
				}
			}

			//狂人が確定した時だけPPを検討する。
			if (Possessedy != null) {
				Village5Tactics PoTac = strategy.v5TacEvalModel.getTacitcsMap(Possessedy);
				if(PoTac.day2PossessedHasVoteToWerewolf) {
					return null;
				}else {
					return new ComingoutContentBuilder(game.getSelf().agent, Role.WEREWOLF);
				}

			}
		}
		return null;
	}

}

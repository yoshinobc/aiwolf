package jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.talk;

import java.util.List;
import java.util.stream.Collectors;

import org.aiwolf.client.lib.ContentBuilder;
import org.aiwolf.client.lib.DivinedResultContentBuilder;
import org.aiwolf.common.data.Role;
import org.aiwolf.common.data.Species;

import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.CndlStrategy;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.framework.Game;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.framework.GameAgent;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.util.Utils;

public class TalkDivineBlackAttacktoSeerDay2 extends CndlTalkTactic {

    @Override
    public ContentBuilder talkImpl(int turn, int skip, int utter, Game game, CndlStrategy strategy) {
    	if (game.getSelf().coRole == Role.SEER) {
			// 対抗占いを真占いと推定し、狼判定を出す。対抗がいなければEvilDcore最下位に黒
			List<GameAgent> list = game.getAliveOthers();
			/* SEER としてCOしている者たち */
			List<GameAgent> seerCOs = game.getAliveOthers().stream().filter(ag -> !ag.isSelf && ag.coRole == Role.SEER)
					.collect(Collectors.toList());
			if(seerCOs.size() == 1) {
				return new DivinedResultContentBuilder(seerCOs.get(0).agent, Species.WEREWOLF);
			}

				Utils.sortByScore(list, strategy.evilModel.getEvilScore(), false);
				GameAgent wolfCand = list.get(list.size() - 1);
				return new DivinedResultContentBuilder(wolfCand.agent, Species.WEREWOLF);

		}
		return null;
    }
}

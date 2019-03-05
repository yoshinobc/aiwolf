package jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.talk;

import java.util.Set;
import java.util.stream.Collectors;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.CndlStrategy;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.model.EvilScoreModel;

import org.aiwolf.client.lib.ContentBuilder;
import org.aiwolf.client.lib.DivinedResultContentBuilder;
import org.aiwolf.common.data.Agent;
import org.aiwolf.common.data.Role;
import org.aiwolf.common.data.Species;

import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.framework.Game;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.framework.GameAgent;

/**
 * 
 * 裏切り者初日2ターン目用
 * 他の占い師COが1人 → その占い師に黒出し
 * 他の占い師COが2人以上 → CO以外に黒出し （CO勢から選ぶより、狼をうっかり当てる確率低い）
 *
 */
public class TalkDivineFakeBlackDay1 extends CndlTalkTactic {

    @Override
    public ContentBuilder talkImpl(int turn, int skip, int utter, Game game, CndlStrategy strategy) {
        if (!game.getSelf().hasCO()) {
            return null;
        }
        Agent tar = null;
        /* SEER としてCOしている者たち */
        Set<GameAgent> seerCOs = game.getAliveOthers().stream().filter(ag -> ag.coRole == Role.SEER).collect(Collectors.toSet());
        if (seerCOs.size() == 1) {
            for (GameAgent gameAgent : seerCOs) {
                tar = gameAgent.agent;
            }
        } else {
            /* CO していない者たち */
            Set<GameAgent> noCOs = game.getAliveOthers().stream().filter(ag -> ag.coRole == null).collect(Collectors.toSet());
            for (GameAgent gameAgent : noCOs) {
                tar = gameAgent.agent;
                break;
            }
        }
        if (tar != null) {
            return new DivinedResultContentBuilder(tar, Species.WEREWOLF);
        } else {
            return null;
        }
    }

}

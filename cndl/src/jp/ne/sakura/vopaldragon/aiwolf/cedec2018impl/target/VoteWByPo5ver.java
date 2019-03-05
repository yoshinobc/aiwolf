package jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.target;

import java.util.Set;
import java.util.stream.Collectors;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.CndlStrategy;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.model.EvilScoreModel;

import org.aiwolf.client.lib.Topic;
import org.aiwolf.common.data.Species;

import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.framework.Game;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.framework.GameAgent;

public class VoteWByPo5ver extends CndlTargetTactic {

    @Override
    public GameAgent targetImpl(Game game, CndlStrategy strategy) {
        if (game.getDay() == 1) {
            GameAgent me = game.getSelf();
            Set<GameAgent> targets = me.talkList.stream()
                .filter(x -> x.getDay() == game.getDay() && x.getTopic() == Topic.DIVINED
                && x.getResult() == Species.WEREWOLF)
                .map(x -> x.getTarget()).filter(x -> x.isAlive).collect(Collectors.toSet());
            GameAgent tar = null;
            for (GameAgent gameAgent : targets) {
                tar = gameAgent;
                break;
            }
            if (tar != null) {
                return tar;
            } else {
                return null;
            }
        }
        return null;
    }

}

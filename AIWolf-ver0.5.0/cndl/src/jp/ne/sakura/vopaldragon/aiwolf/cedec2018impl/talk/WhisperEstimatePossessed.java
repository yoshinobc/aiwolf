package jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.talk;

import java.util.List;
import java.util.stream.Collectors;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.CndlStrategy;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.model.EvilScoreModel;

import org.aiwolf.client.lib.ContentBuilder;
import org.aiwolf.client.lib.EstimateContentBuilder;
import org.aiwolf.client.lib.Topic;
import org.aiwolf.common.data.Role;
import org.aiwolf.common.data.Species;

import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.framework.Game;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.framework.GameAgent;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.framework.GameTalk;

public class WhisperEstimatePossessed extends CndlTalkTactic {

    @Override
    public ContentBuilder talkImpl(int turn, int skip, int utter, Game game, CndlStrategy strategy) {
        GameAgent me = game.getSelf();
        if (me.whisperList.stream().filter(x -> x.getTopic() == Topic.ESTIMATE && x.getRole() == Role.POSSESSED).count() > 0) {
            return null;
        }
        /* 当日の占い結果 */
        List<GameTalk> divineResult = game.getAllTalks().filter(x -> x.getDay() == game.getDay() && x.getTalker().role != Role.WEREWOLF && x.getTopic() == Topic.DIVINED).collect(Collectors.toList());
        for (GameTalk gameTalk : divineResult) {
            if ((gameTalk.getTarget().role == Role.WEREWOLF && gameTalk.getResult() == Species.HUMAN)
                || gameTalk.getTarget().role != Role.WEREWOLF && gameTalk.getResult() == Species.WEREWOLF) {
                return new EstimateContentBuilder(gameTalk.getTalker().agent, Role.POSSESSED);
            }
        }
        return null;
    }

}

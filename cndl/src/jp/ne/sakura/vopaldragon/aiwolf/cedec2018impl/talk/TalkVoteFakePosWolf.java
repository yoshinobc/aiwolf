package jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.talk;

import java.util.ArrayList;
import java.util.List;

import org.aiwolf.client.lib.ContentBuilder;
import org.aiwolf.client.lib.VoteContentBuilder;
import org.aiwolf.common.data.Role;
import org.aiwolf.common.data.Species;

import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.CndlStrategy;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.framework.EventType;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.framework.Game;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.framework.GameAgent;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.framework.GameEvent;

public class TalkVoteFakePosWolf extends CndlTalkTactic {

    GameAgent day1Target = null;

    @Override
    public ContentBuilder talkImpl(int turn, int skip, int utter, Game game, CndlStrategy strategy) {
        if (game.getSelf().coRole == Role.POSSESSED || game.getSelf().coRole == Role.WEREWOLF) {
            if (wolf == null) {
                List<GameAgent> others = game.getAliveOthers();
                others.removeAll(nonWolves);
                wolf = strategy.bestPredictor.getRoleProbabilityStruct(game).topAgent(Role.WEREWOLF, others).data;
            }
            List<GameAgent> others = game.getAliveOthers();
            others.remove(wolf);
            GameAgent theOther = others.get(0);
            //狂人のふりをしているときは、狂人への投票を宣言する。
            if (game.getSelf().coRole == Role.POSSESSED) {
                return new VoteContentBuilder(theOther.agent);
            //狼のふりをしているときは、狼への投票を宣言する。
            } else if (game.getSelf().coRole == Role.WEREWOLF) {
                return new VoteContentBuilder(wolf.agent);
            }
        }
        return null;
    }

    private GameAgent divineTarget;
    private Species result;
    private List<GameAgent> nonWolves = new ArrayList<>();
    private GameAgent wolf;

    @Override
    public void handleEvent(Game g, GameEvent e) {
        if (e.type == EventType.DIVINE) {
            divineTarget = e.target;
            result = e.species;
            if (result == Species.HUMAN) nonWolves.add(divineTarget);
            else wolf = divineTarget;
        }
    }

}

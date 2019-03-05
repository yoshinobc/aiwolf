package jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.talk;

import java.util.ArrayList;
import java.util.List;

import org.aiwolf.client.lib.ContentBuilder;
import org.aiwolf.client.lib.DivinedResultContentBuilder;
import org.aiwolf.common.data.Species;

import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.CndlStrategy;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.framework.EventType;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.framework.Game;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.framework.GameAgent;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.framework.GameEvent;
import org.aiwolf.common.data.Role;

/**
 * COしてたら占い結果を言う
 */
public class TalkDivineWithEvilScore5ver extends CndlTalkTactic {

    GameAgent day1Target = null;

    @Override
    public ContentBuilder talkImpl(int turn, int skip, int utter, Game game, CndlStrategy strategy) {
        if (divineTarget != null && game.getSelf().coRole == Role.SEER) {
            if (game.getDay() == 1) {
                GameAgent target = null;
                if (result == Species.HUMAN) {
                    //狼を見つけられなかった場合
                    List<GameAgent> alives = game.getAliveOthers();
                    alives.remove(divineTarget);
                    target = strategy.bestPredictor.getRoleProbabilityStruct(game).topAgent(Role.WEREWOLF, alives).data;
//                    System.err.println("fake");
//                    System.err.println(Utils.join("Talk Fake Divine", "Day1", target, Utils.ROLE_MAP.get(target.getIndex())));
                } else {
                    //狼を見つけられた場合
//                    System.err.println("truth");
                    target = divineTarget;
                }
                strategy.voteModel.currentVoteTarget = target;
                day1Target = target;
                return new DivinedResultContentBuilder(target.agent, Species.WEREWOLF);
            }
            if (game.getDay() == 2) {
                if (wolf == null) {
                    //Wolfが見つかっていない場合、予測してしまう
                    List<GameAgent> others = game.getAliveOthers();
                    others.removeAll(nonWolves);
                    wolf = strategy.bestPredictor.getRoleProbabilityStruct(game).topAgent(Role.WEREWOLF, others).data;
//                    System.err.println(Utils.join("Talk Fake Divine", "Day2", wolf, Utils.ROLE_MAP.get(wolf.getIndex())));
                }
                strategy.voteModel.currentVoteTarget = wolf;

                List<GameAgent> others = game.getAliveOthers();
                others.remove(wolf);
                GameAgent theOther = others.get(0);

                if (theOther.coRole == Role.SEER) {
                    //相手が狂人の場合、投票宣言相手を狂人に向けて、かつ占い発言はしない
                    strategy.voteModel.currentVoteTarget = theOther;
//                    System.err.println("vs madman");
                    return null;
                } else {
                    if (day1Target.isAlive) {
                        //初日の黒出し相手が生きていた
                        if (day1Target == wolf) {
                            //初日に当てていたか、推測が正しかった？ため、初日の黒出し相手が狼だった場合、もう一人に白出し
//                            System.err.println("white");
                            return new DivinedResultContentBuilder(theOther.agent, Species.HUMAN);
                        } else {
                            //一日目の黒出し相手は味方だった（しかも生きてる）場合、慌てて真Wolfに黒出し
//                            System.err.println("alive-black");
                            return new DivinedResultContentBuilder(wolf.agent, Species.WEREWOLF);
                        }
                    } else {
                        //初日の黒出し相手は死んでいるがゲームは続いている。気まずい。
                        //とはいえ、一日目のことは忘れて黒出し
//                        System.err.println("dead-black");
                        return new DivinedResultContentBuilder(wolf.agent, Species.WEREWOLF);
                    }
                }

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

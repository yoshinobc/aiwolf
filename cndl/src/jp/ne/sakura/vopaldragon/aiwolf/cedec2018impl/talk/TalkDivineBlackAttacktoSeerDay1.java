package jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.talk;

import java.util.List;
import java.util.stream.Collectors;

import org.aiwolf.client.lib.ContentBuilder;
import org.aiwolf.client.lib.DivinedResultContentBuilder;
import org.aiwolf.client.lib.Topic;
import org.aiwolf.common.data.Role;
import org.aiwolf.common.data.Species;

import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.CndlStrategy;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.evalmodel.Village5TacEvalModel.Village5Tactics;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.framework.Game;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.framework.GameAgent;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.model.RoleProbabilityStruct;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.util.Utils;

public class TalkDivineBlackAttacktoSeerDay1 extends CndlTalkTactic {
    
    @Override
    public ContentBuilder talkImpl(int turn, int skip, int utter, Game game, CndlStrategy strategy) {
        if (game.getSelf().coRole == Role.SEER) {
            RoleProbabilityStruct rps = strategy.bestPredictor.getRoleProbabilityStruct(game);
            // 対抗占いを真占いと推定し、狼判定を出す。対抗がいなければEvilDcore最下位に黒
            GameAgent seer = null;

            /* SEER としてCOしている者たち */
            List<GameAgent> seerCOs = game.getAliveOthers().stream().filter(ag -> ag.coRole == Role.SEER).collect(Collectors.toList());
            /* 対抗に白判定された者たち */
            List<GameAgent> whites = game.getAllTalks().filter(x -> !x.getTalker().isSelf && x.getTopic() == Topic.DIVINED && x.getResult() == Species.HUMAN).map(x -> x.getTarget()).filter(x -> x.isAlive && !x.isSelf).collect(Collectors.toList());
            /* 対抗に黒判定された者たち */
            List<GameAgent> blacks = game.getAllTalks().filter(x -> !x.getTalker().isSelf && x.getTopic() == Topic.DIVINED && x.getResult() == Species.WEREWOLF).map(x -> x.getTarget()).filter(x -> x.isAlive && !x.isSelf).collect(Collectors.toList());
            GameAgent target = null;
            if (seerCOs.size() == 1) {
                /*対抗が1人のとき、対抗とは反対の占い結果。ただしcndl占いの黒は放置。間に合わなければ対抗以外から黒*/
                if (!whites.isEmpty()) {
                    //白判定がある場合、そいつに黒出し。また、投票対象に設定。
                    target = Utils.getRandom(whites);
                } else {
                    List<GameAgent> aliverOthers = game.getAliveOthers();
                    seer = seerCOs.get(0);
                    aliverOthers.remove(seer);
                    if (!strategy.v5TacEvalModel.getTacitcsMap(seer).LiarSeerDivinedFakeBlack) {
                        //SeerがFakeBlack戦術の使い手で無ければ、黒宣言を抜いた中から黒出し
                        aliverOthers.removeAll(blacks);
                        if (!aliverOthers.isEmpty()) target = rps.getAgnetProbabilityList(Role.WEREWOLF, aliverOthers).reverse().top().data;
                    } else {
                        //狂人の黒特攻に引っ掛かった人が1人以上いるなら、seerに黒出し
//                        System.err.println("kurottoko");
                        int cnt = 0;
                        for (GameAgent ag : aliverOthers) {
                            Village5Tactics tac = strategy.v5TacEvalModel.getTacitcsMap(ag);
                            if (!tac.HaveAvoidFakeBlack) {
                                cnt += 1;
                            }
                        }
                        if (cnt > 1) {
                            target = seer;
                        }
                    }
                }
            }
            if (seerCOs.size() > 1) {
                /*対抗が2人以上のとき、一番狼らしくない対抗に黒*/
                target = rps.getAgnetProbabilityList(Role.WEREWOLF, seerCOs).reverse().top().data;
            }
            if (target == null) {
                target = rps.getAgnetProbabilityList(Role.WEREWOLF, game.getAliveOthers()).reverse().top().data;
            }
            strategy.voteModel.currentVoteTarget = target;
            
//            System.err.println(Utils.join("mad-divine", game.getDay(), target, Utils.ROLE_MAP.get(target.getIndex())));
            
            return new DivinedResultContentBuilder(target.agent, Species.WEREWOLF);
            
        }
        return null;
    }
}

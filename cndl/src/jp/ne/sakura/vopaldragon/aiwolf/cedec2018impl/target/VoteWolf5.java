package jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.target;

import java.util.List;
import java.util.stream.Collectors;

import org.aiwolf.common.data.Role;

import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.CndlStrategy;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.framework.Game;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.framework.GameAgent;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.framework.GameEvent;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.model.RoleProbabilityStruct;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.util.HashCounter;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.util.ListMap;

/**
 *
 * 5人村では初日から狼を狙う。自己防衛はしない。
 *
 */
public class VoteWolf5 extends CndlTargetTactic {

    public GameAgent selectTargetWithRPS(RoleProbabilityStruct rps, Game game, CndlStrategy strategy) {
        GameAgent target = null;

        //とりあえずターゲットを決める
        /* SEER としてCOしている者たち */
        List<GameAgent> seerCOs = game.getAliveOthers().stream().filter(ag -> ag.coRole == Role.SEER).collect(Collectors.toList());
        if (seerCOs.size() < 3) {
            // COが2人以下ならCOしていないのから選ぶ。
            List<GameAgent> nonSeerCOs = game.getAliveOthers().stream().filter(ag -> ag.coRole != Role.SEER).collect(Collectors.toList());
            target = rps.topAgent(Role.WEREWOLF, nonSeerCOs).data;
        } else {
            // COが3人以上ならCOから選ぶ。
            target = rps.topAgent(Role.WEREWOLF, seerCOs).data;
        }

        //自分以外の投票の組み合わせは、a.3-1,b.2-2,c.2-1-1,d.1-1-1-1のいずれか;ただし初日はexpectedVoteは単純に宣言内容を返すので、このパターン以外になることもある。
        HashCounter<GameAgent> voteCount = strategy.voteModel.expectedVote();
        int topCount = voteCount.topCount();
        ListMap<Integer, GameAgent> inversedCountMap = voteCount.getCounts();
//        System.err.println("-----------------" + game.getSelf()+"------------"+game.getAgentStream().map(ag->ag+"/"+Utils.ROLE_MAP.get(ag.getIndex())).collect(Collectors.joining(", ")));
//        if (target != null) System.err.println("InitialTarget\t" + target + "\t" + Utils.ROLE_MAP.get(target.getIndex()));
//        System.err.println(inversedCountMap);
        List<GameAgent> topcounts = inversedCountMap.getList(topCount);
        List<GameAgent> seconds = inversedCountMap.getList(topCount - 1);//cパターンの場合のみ空で無い

        //targetがトップ集団ないしはトップ-1集団にいるなら、とりあず決めたtargetに投票
        if (target != null && (topcounts.contains(target) || seconds.contains(target))) {
//            System.err.println("OK\t" + target);
            return target;
        }

        if (topCount == 3) {
            //aパターンの場合、票を集めているうちましな方に投票
            List<GameAgent> candidate = voteCount.getKeyList();
            candidate.remove(game.getSelf());
            target = candidate.contains(target) ? target : rps.topAgent(Role.WEREWOLF, candidate).data;
//            System.err.println("Pattern A\t" + target);
            return target;
        }
        if (topCount == 2 && topcounts.size() == 2) {
            //bパターンの場合、revote狙いで他の一人に投票
            List<GameAgent> others = game.getAliveOthers();
            others.removeAll(voteCount.getKeyList());
            target = others.contains(target) ? target : rps.topAgent(Role.WEREWOLF, others).data;
//            System.err.println("Pattern B\t" + target);
            return target;
        }
        if (topCount == 2 && !seconds.isEmpty()) {
            //cパターンの場合、revote狙いで1票しか入っていない人のどちらかに投票
            seconds.remove(game.getSelf());
            if (!seconds.isEmpty()) {
                target = rps.topAgent(Role.WEREWOLF, seconds).data;
                return target;
            }
        }
        if (topCount == 1 && target != null) {
            //dパターンの場合（とりあえず決めたtargetと、他の人の投票予想が全部違う場合）、revote狙いでとりあえず決めたtargetに入れれば良い。他の人が変えればそれもよし。
//            System.err.println("Pattern D\t" + target);
            return target;
        }

        //いずれのパターンにも当てはまらなかった場合の番兵
        target = rps.topAgent(Role.WEREWOLF, game.getAliveOthers()).data;
//        System.err.println("Pattern Other\t" + target);
//        if (target.isSelf) System.err.println("target is self!");
        return target;
    }

    @Override
    public GameAgent targetImpl(Game game, CndlStrategy strategy) {
        return selectTargetWithRPS(strategy.bestPredictor.getRoleProbabilityStruct(game), game, strategy);
    }

    @Override
    public void handleEvent(Game g, GameEvent e) {
//        if (e.type == EventType.VOTE) {
//            if (e.day == 1) System.err.println(Utils.join("Vote", g.getSelf(), e.votes.get(0).isReVote, e.votes));
//        }
    }

}

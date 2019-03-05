package jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.target;

import java.util.List;
import java.util.stream.Collectors;

import org.aiwolf.client.lib.Topic;
import org.aiwolf.common.data.Role;
import org.aiwolf.common.data.Species;

import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.CndlStrategy;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.framework.Game;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.framework.GameAgent;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.framework.GameTalk;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.model.RoleProbabilityStruct;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.util.DataScore;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.util.HashCounter;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.util.ListMap;

/**
 * Votewolf5からRevote狙いを抜いたもの
 */
public class RevoteWolf5 extends CndlTargetTactic {

    @Override
    public GameAgent targetImpl(Game game, CndlStrategy strategy) {
        GameAgent target = null;
        RoleProbabilityStruct rps = strategy.bestPredictor.getRoleProbabilityStruct(game);
        /* SEER としてCOしている者たち */
        List<GameAgent> seerCOs = game.getAliveOthers().stream().filter(ag -> ag.coRole == Role.SEER).collect(Collectors.toList());
        // COが2人以下ならCOしていないのから選ぶ。
        if (seerCOs.size() < 3) {
            List<GameAgent> nonSeerCOs = game.getAliveOthers().stream().filter(ag -> ag.coRole != Role.SEER).collect(Collectors.toList());
            List<DataScore<GameAgent>> candidates = rps.getAgnetProbabilityList(Role.WEREWOLF, nonSeerCOs);
            target = candidates.get(0).data;
            // COが3人以上ならCOから選ぶ。
        } else {
            List<DataScore<GameAgent>> candidates = rps.getAgnetProbabilityList(Role.WEREWOLF, seerCOs);
            target = candidates.get(0).data;
        }
        // 初日は1.狼狙い 2.狼が狙えないならセメテ狂人3.できるだけ生き残る
        HashCounter<GameAgent> voteCount = strategy.voteModel.expectedRevote();
        GameAgent me = game.getSelf();
        int topCount = voteCount.topCount();
        ListMap<Integer, GameAgent> listMap = voteCount.getCounts();
        List<GameAgent> topcounts = listMap.getList(topCount);
        List<GameAgent> seconds = listMap.getList(topCount - 1);
        if (target != null && (topcounts.contains(target) || seconds.contains(target))) {
            return target;
        }
        GameAgent possessed = null;
        List<GameTalk> divine = game.getAllTalks().filter(t -> t.getTopic() == Topic.DIVINED).collect(Collectors.toList());
        for (GameTalk t : divine) {
            // 自分か対抗にに黒。真占い時に嘘黒をしたことがない。
            if (t.getResult() == Species.WEREWOLF && t.getTalker().isAlive
                && !strategy.v5TacEvalModel.getTacitcsMap(t.getTalker()).LiarSeerDivinedFakeBlack) {
                if ((seerCOs.size() < 3 && seerCOs.contains(t.getTarget())) || t.getTarget().isSelf)
                    possessed = t.getTalker();
                break;
            }
        }
        if (possessed != null && (topcounts.contains(possessed) || seconds.contains(possessed))) {
            //狂人が見つかってトップか2位集団にいれば狂人に投票
            return possessed;
        }

        if (topcounts.contains(me)) {
            //自分がトップ集団にいる場合、トップ集団か2位集団からましなやつに投票
            topcounts.remove(me);
            if (!topcounts.isEmpty()) {
                target = rps.topAgent(Role.WEREWOLF, topcounts).data;
            } else if (!seconds.isEmpty()) {
                target = rps.topAgent(Role.WEREWOLF, seconds).data;
            }
        }

        if (target == null) {
            target = rps.topAgent(Role.WEREWOLF, voteCount.getKeyList()).data;
        }

        return target;
    }

}

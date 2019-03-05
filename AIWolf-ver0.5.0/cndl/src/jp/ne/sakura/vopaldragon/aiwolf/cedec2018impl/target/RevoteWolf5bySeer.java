package jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.target;

import java.util.List;
import java.util.stream.Collectors;

import org.aiwolf.client.lib.Topic;
import org.aiwolf.common.data.Role;
import org.aiwolf.common.data.Species;

import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.CndlStrategy;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.framework.EventType;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.framework.Game;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.framework.GameAgent;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.framework.GameEvent;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.model.RoleProbabilityStruct;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.model.VoteStatus;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.util.DataScore;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.util.HashCounter;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.util.ListMap;

/**
 * Votewolf5からRevote狙いを抜いたもの
 */
public class RevoteWolf5bySeer extends CndlTargetTactic {

    private GameAgent divineTarget;
    private Species result;

    @Override
    public void handleEvent(Game g, GameEvent e) {
        if (e.type == EventType.DIVINE) {
            divineTarget = e.target;
            result = e.species;
        }
    }

    @Override
    public GameAgent targetImpl(Game game, CndlStrategy strategy) {
        GameAgent target = null;
        GameAgent possessed = null;

        /* SEER としてCOしている他の者たち */
        RoleProbabilityStruct rps = strategy.bestPredictor.getRoleProbabilityStruct(game);
        List<GameAgent> others = game.getAliveOthers();
        List<GameAgent> seerCOs = game.getAgentStream().filter(ag->ag.coRole==Role.SEER).collect(Collectors.toList());
        List<GameAgent> aliveSeerCOs = others.stream().filter(ag->ag.coRole==Role.SEER).collect(Collectors.toList());

        if (seerCOs.size() == 1) {
            possessed = seerCOs.get(0);
            if (!aliveSeerCOs.isEmpty()) {
                others.removeAll(aliveSeerCOs);
            }
            if (!others.isEmpty()) {
                target = rps.topAgent(Role.WEREWOLF, others).data;
            }
        } else if (seerCOs.size() > 1 && !aliveSeerCOs.isEmpty()) {
            target = rps.topAgent(Role.WEREWOLF, aliveSeerCOs).data;
        } else if (!others.isEmpty()) {
            target = rps.topAgent(Role.WEREWOLF, others).data;
        }
        if (target == null) {
            List<DataScore<GameAgent>> candidates = rps.getAgnetProbabilityList(Role.WEREWOLF, others);
            target = candidates.get(0).data;
        }
        //狼を占えていたら、必ず狼を狙う
        if (divineTarget != null && result == Species.WEREWOLF) {
            target = divineTarget;
        }
        // 初日は1.狼狙い 2.狼が狙えないならセメテ狂人3.できるだけ生き残る
        if (game.getDay() == 1) {
            HashCounter<GameAgent> voteCount = strategy.voteModel.expectedRevote();
            GameAgent me = game.getSelf();

            int topCount = voteCount.topCount();
            ListMap<Integer, GameAgent> listMap = voteCount.getCounts();
            List<GameAgent> topcounts = listMap.getList(topCount);
            List<GameAgent> seconds = listMap.getList(topCount - 1);
            if (target != null && (topcounts.contains(target) || seconds.contains(target))) {
                return target;
            }
            if (possessed != null && topcounts.contains(possessed)) {
                return possessed;
            }
            if (topcounts.contains(me)) {
                topcounts.remove(me);
                if (!topcounts.isEmpty()) {
                    List<DataScore<GameAgent>> candidates = rps.getAgnetProbabilityList(Role.WEREWOLF, topcounts);
                    target = candidates.get(0).data;
                }
                if (target == null && !seconds.isEmpty()) {
                    List<DataScore<GameAgent>> candidates = rps.getAgnetProbabilityList(Role.WEREWOLF, seconds);
                    target = candidates.get(0).data;
                }
            }
            if (possessed != null && !seconds.isEmpty() && seconds.contains(possessed)) {
                return possessed;
            }
        }

        return target;
    }

}

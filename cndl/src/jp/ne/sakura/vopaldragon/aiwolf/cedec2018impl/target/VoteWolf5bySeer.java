package jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.target;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.aiwolf.common.data.Role;
import org.aiwolf.common.data.Species;

import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.CndlStrategy;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.framework.EventType;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.framework.Game;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.framework.GameAgent;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.framework.GameEvent;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.model.RoleProbabilityStruct;

/**
 * 1日目は狼と狂人優先、2日目は確実に狼を倒しに行く
 */
public class VoteWolf5bySeer extends CndlTargetTactic {

    private boolean revote;

    public VoteWolf5bySeer(boolean revote) {
        this.revote = revote;
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

    @Override
    public GameAgent targetImpl(Game game, CndlStrategy strategy) {
        GameAgent target = null;
        GameAgent possessed = null;

        /* SEER としてCOしている他の者たち */
        RoleProbabilityStruct rps = strategy.bestPredictor.getRoleProbabilityStruct(game);
        List<GameAgent> others = game.getAliveOthers();
        others.removeAll(nonWolves);
        List<GameAgent> seerCOs = game.getOthers().stream().filter(ag -> ag.coRole == Role.SEER).collect(Collectors.toList());
        List<GameAgent> aliveSeerCOs = game.getAliveOthers().stream().filter(ag -> ag.coRole == Role.SEER).collect(Collectors.toList());

        if (seerCOs.size() == 1) {
            //対抗Seerが一人の場合は狂人
            possessed = seerCOs.get(0);
            others.removeAll(seerCOs);
            if (!others.isEmpty()) {
                target = rps.topAgent(Role.WEREWOLF, others).data;
            }
        } else if (seerCOs.size() > 1 && !aliveSeerCOs.isEmpty()) {
            //対抗Seerが複数人の場合には、その中に狼が混ざっている
            target = rps.topAgent(Role.WEREWOLF, aliveSeerCOs).data;
        } else if (!others.isEmpty()) {
            target = rps.topAgent(Role.WEREWOLF, others).data;
        }
        //※確定ではない狂人を無理して狙いに行くと勝率が下がるので注意

        if (target == null) {
            //基本発生しないけど、念のための番兵
            target = rps.topAgent(Role.WEREWOLF, game.getAliveOthers()).data;
        }

        //狼を占えていたら、必ず狼を狙う
        if (wolf != null) {
            target = wolf;
        }
//        System.err.println(Utils.join("seer vote", "before", game.getDay(), target, Utils.ROLE_MAP.get(target.getIndex())));

        if (game.getDay() == 1) {
            //1日目の場合、狂人狙いや生存を考える
            target = VoteHelper.decideVote(
                Arrays.asList(target, possessed),//優先ターゲット
                Arrays.asList(game.getSelf()),//回避ターゲット
                revote ? strategy.voteModel.expectedRevote() : strategy.voteModel.expectedVote(),//自分を除いた投票予測
                rps, Role.WEREWOLF, target);
        }
//        System.err.println(Utils.join("seer vote", "after", game.getDay(), target, Utils.ROLE_MAP.get(target.getIndex())));
        return target;
    }

}

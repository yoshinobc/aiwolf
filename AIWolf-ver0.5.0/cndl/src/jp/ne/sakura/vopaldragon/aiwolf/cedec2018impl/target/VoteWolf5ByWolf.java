package jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.target;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.aiwolf.common.data.Role;

import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.CndlStrategy;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.framework.EventType;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.framework.Game;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.framework.GameAgent;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.framework.GameEvent;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.framework.GameTalk;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.model.RoleProbabilityStruct;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.util.Utils;
import org.aiwolf.client.lib.Topic;
import org.aiwolf.common.data.Species;

/**
 *
 * 5人村狼が村人の振りをするためのVote思考。
 *
 */
public class VoteWolf5ByWolf extends CndlTargetTactic {

    private VoteWolf5 vw = new VoteWolf5();
    private GameAgent enemy;
    private GameAgent nonEnemy;

    @Override
    public GameAgent targetImpl(Game game, CndlStrategy strategy) {
        //架空のRoleProbabilityStructを使って、村人と同じように思考
        RoleProbabilityStruct fakeRPS = strategy.bestPredictor.getRoleProbabilityStruct(game);
        if (enemy == null) {
            //敵の設定
            List<GameAgent> candidate = game.getAliveOthers().stream().filter(ag -> ag.coRole != Role.SEER).collect(Collectors.toList());
            if (!candidate.isEmpty()) {
                enemy = Utils.getRandom(candidate);
                candidate.remove(enemy);
                if (!candidate.isEmpty()) nonEnemy = candidate.get(0);
            }
        } else {
//            System.err.println(Utils.join("switch_", enemy, nonEnemy, blacks, whites));
            if (nonEnemy != null && (blacks.contains(enemy) || whites.contains(nonEnemy))) {
                enemy = nonEnemy;
                nonEnemy = null;
//                System.err.println(Utils.join("switch", enemy, nonEnemy, blacks, whites));
            }
        }

        fakeRPS.setScore(game.getSelf(), Role.WEREWOLF, 0.0);
        fakeRPS.setScore(game.getSelf(), Role.VILLAGER, 1.0);
        if (enemy != null) {
            fakeRPS.setScore(enemy, Role.WEREWOLF, 1.0);
            fakeRPS.setScore(enemy, Role.VILLAGER, 0.0);
        }
        fakeRPS.normalize(game);
        GameAgent target = vw.selectTargetWithRPS(fakeRPS, game, strategy);
        if (target.isSelf) {
            System.err.println("target is self!" + target);
            System.err.println("enemy is" + enemy);
            fakeRPS.print(game);
        }
        if (fakeRPS.topRole(target).data == Role.POSSESSED) {
            //狂人には優しい
            target = enemy;
        }
        return target;

    }

    private Set<GameAgent> blacks = new HashSet<>();
    private Set<GameAgent> whites = new HashSet<>();

    @Override
    public void handleEvent(Game g, GameEvent e) {
        if (e.type == EventType.TALK) {
            for (GameTalk t : e.talks) {
                if (t.getTopic() == Topic.DIVINED) {
                    if (t.getResult() == Species.WEREWOLF) blacks.add(t.getTarget());
                    else whites.add(t.getTarget());
                }
            }
        }
    }

}

package jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.target;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.CndlStrategy;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.framework.Game;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.framework.GameAgent;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.model.RoleProbabilityStruct;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.util.DataScore;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.util.HashCounter;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.util.ListMap;
import org.aiwolf.common.data.Role;

/**
 *
 * なるべく有効票になり、なるべく狼が最大票になるように投票する
 *
 */
public class VoteWolf extends CndlTargetTactic {

    public DataScore<GameAgent> selectTarget(HashCounter<GameAgent> voteCount, Game game, CndlStrategy strategy) {
        GameAgent me = game.getSelf();

        int topCount = voteCount.topCount();
        int myCount = voteCount.getCount(me);


        DataScore<GameAgent> target = null;
        RoleProbabilityStruct rps = strategy.bestPredictor.getRoleProbabilityStruct(game);
        //自分が吊られるのを回避
        if (myCount > 0 && myCount >= topCount - 1) {
            ListMap<Integer, GameAgent> listMap = voteCount.getCounts();
            //トップ集団
            List<DataScore<GameAgent>> candidates = rps.getAgnetProbabilityList(Role.WEREWOLF, listMap.getList(topCount));
            if (!candidates.isEmpty()) {
                target = candidates.get(0);
            } else {
                //2位集団（自分が単独トップの場合のみ発生）
                candidates = rps.getAgnetProbabilityList(Role.WEREWOLF, listMap.getList(topCount - 1));
                if (!candidates.isEmpty()) {
                    target = candidates.get(0);
                } else {
                    //どうにもならない時には狼スコア最大のエージェントにする
                    candidates = rps.getAgnetProbabilityList(Role.WEREWOLF);
                    target = candidates.get(0);
                }
            }
        } else {

            int ASOBI = 1;
            double cndlWolfTh = 0.7;

            //投票候補の選出
            List<DataScore<GameAgent>> cndls = new ArrayList<>();
            List<DataScore<GameAgent>> wolves = new ArrayList<>();
            List<DataScore<GameAgent>> nonSps = new ArrayList<>();
            List<DataScore<GameAgent>> others = new ArrayList<>();
            Set<Role> exRole = new HashSet<>(Arrays.asList(Role.SEER, Role.BODYGUARD, Role.MEDIUM, Role.POSSESSED));
            exRole.remove(game.getSelf().role);

            for (GameAgent ag : game.getAliveOthers()) {
                List<DataScore<Role>> roleProbs = rps.getRoleProbabilityList(ag);
                //自称占/自称霊を助ける
                boolean spareSeerFlag = true;
                if (game.getDay() > 4) spareSeerFlag = false;
                if (game.getAgentStream().filter(agt -> agt.coRole == Role.SEER).count() >= 3) spareSeerFlag = false;
                if (game.getSelf().role == Role.SEER) spareSeerFlag = false;

                boolean spareMedFlag = true;
                if (game.getDay() > 3) spareMedFlag = false;
                if (game.getAgentStream().filter(agt -> agt.coRole == Role.MEDIUM).count() >= 2) spareMedFlag = false;
                if (game.getSelf().role == Role.MEDIUM) spareMedFlag = false;

                boolean spare = (spareSeerFlag && ag.coRole == Role.SEER) || (spareMedFlag && ag.coRole == Role.MEDIUM);

                if (voteCount.getCount(ag) + ASOBI >= topCount) {
                    if (strategy.findCndlModel != null && strategy.findCndlModel.cndlLike(ag)) {
                        if ((roleProbs.get(0).data == Role.WEREWOLF || roleProbs.get(1).data == Role.WEREWOLF) && strategy.findCndlModel.wolfProbability(ag) > cndlWolfTh) {
                            cndls.add(new DataScore<>(ag, roleProbs.get(0).score));
                        }
                    }
                    if (roleProbs.get(0).data == Role.WEREWOLF) {
                        //第一候補が狼の場合
                        wolves.add(new DataScore<>(ag, roleProbs.get(0).score));
                    } else if (!spare && roleProbs.get(1).data == Role.WEREWOLF) {
                        //第二候補が狼の場合(第二候補なら、猶予あり)
                        wolves.add(new DataScore<>(ag, roleProbs.get(1).score));
                    } else if (!exRole.contains(roleProbs.get(0).data)) {
                        nonSps.add(new DataScore<>(ag, roleProbs.get(0).score));
                    }
                }
                if (!spare) others.add(new DataScore<>(ag, rps.getScore(ag, Role.WEREWOLF)));
            }

//            //Cndl狼がいればそいつを殴る
            if (!cndls.isEmpty()) {
                Collections.sort(cndls);
                target = cndls.get(0);
            }

            //狼がいれば狼を殴る
            if (target == null && !wolves.isEmpty()) {
                Collections.sort(wolves);
                target = wolves.get(0);
            }

            //いなければ、役職の恐れが低い一番判断がつかないやつ
            if (target == null && !nonSps.isEmpty()) {
                Collections.sort(nonSps, Collections.reverseOrder());
                target = nonSps.get(0);
            }

            //いなければ、とにかく一番狼っぽいやつ（死に票になるかもしれないが）
            if (target == null && !others.isEmpty()) {
                Collections.sort(others);
                target = others.get(0);
            }

            if (target == null) {
                target = rps.topAgent(Role.WEREWOLF, game.getAliveOthers());
            }
        }

        return target;
    }

    @Override
    public GameAgent targetImpl(Game game, CndlStrategy strategy) {
        return selectTarget(strategy.voteModel.getVoteDeclared().getVoteCountOfOthers(), game, strategy).data;
    }

}

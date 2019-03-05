package jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.CndlStrategy;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.framework.Game;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.framework.GameAgent;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.framework.GameTalk;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.util.DataScore;
import org.aiwolf.client.lib.Topic;
import org.aiwolf.common.data.Role;
import org.aiwolf.common.data.Species;

public class IntegratedModel implements RolePredictor {

    private CndlStrategy strategy;

    public IntegratedModel(CndlStrategy strategy) {
        this.strategy = strategy;
    }

    @Override
    public RoleProbabilityStruct getRoleProbabilityStruct(Game game) {
        Game g = strategy.getGame();
        RoleProbabilityStruct rps = strategy.ensembleRolePredictotrModel.getRoleProbabilityStruct(g);
        if (g.getSelf().role != Role.MEDIUM) {
            //霊媒情報の判定
            List<DataScore<GameAgent>> mediumScore = new ArrayList<>();
            for (GameAgent ag : g.getAgentStream().filter(ag -> ag.coRole == Role.MEDIUM).collect(Collectors.toList())) {
                double score = 0;
                int count = 0;
                for (GameTalk talk : ag.talkList.stream().filter(t -> t.getTopic() == Topic.IDENTIFIED).collect(Collectors.toList())) {
                    //TODO 同じ予測を無視、矛盾した予測、初日の謎予測等
                    GameAgent tgt = talk.getTarget();
                    List<DataScore<Role>> top2Roles = rps.getRoleProbabilityList(tgt).subList(0, 2);
                    for (DataScore<Role> role : top2Roles) {
                        if (role.data.getSpecies() == talk.getResult()) {
                            score += role.score;
                            count++;
                            break;
                        }
                    }
                }
                score = score / count;
                if (score > 0) mediumScore.add(new DataScore<>(ag, score));
            }
            Collections.sort(mediumScore);
            //判定結果を反映
            for (int i = 0; i < mediumScore.size(); i++) {
                GameAgent ag = mediumScore.get(i).data;
                if (i == 0) {
                    for (GameTalk talk : ag.talkList.stream().filter(t -> t.getTopic() == Topic.IDENTIFIED).collect(Collectors.toList())) {
                        GameAgent tgt = talk.getTarget();
                        if (talk.getResult() == Species.HUMAN) {
                            rps.setScore(tgt, Role.WEREWOLF, 0.0);
                        } else {
                            rps.setScore(tgt, Role.WEREWOLF, 1.0);
                        }
                    }
                } 
            }
        }
        if (g.getSelf().role != Role.SEER) {

            List<DataScore<GameAgent>> seerScore2 = new ArrayList<>();

            double[] relScore = strategy.agentReliabilityModel.getScore();

            for (GameAgent ag : g.getAgentStream().filter(ag -> ag.coRole == Role.SEER).collect(Collectors.toList())) {
                seerScore2.add(new DataScore<>(ag, relScore[ag.getIndex()] + rps.getScore(ag, Role.SEER)));
            }

            Collections.sort(seerScore2);
            //判定結果を反映
            for (int i = 0; i < seerScore2.size(); i++) {
                GameAgent seer = seerScore2.get(i).data;
                if (i == 0) {
                    for (GameTalk talk : seer.talkList.stream().filter(t -> t.getTopic() == Topic.DIVINED).collect(Collectors.toList())) {
                        GameAgent tgt = talk.getTarget();
                        if (talk.getResult() == Species.HUMAN) {
                            rps.setScore(tgt, Role.WEREWOLF, 0.0);
                        } else {
                            rps.setScore(tgt, Role.WEREWOLF, 1.0);
                        }
                    }
                } else {
                    rps.setScore(seer, Role.SEER, 0.0);
                }
            }
        }
        rps.normalize(g);
        return rps;
    }

}

package jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.tools;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.OptionalDouble;

import org.aiwolf.common.data.Role;

import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.CndlPlayer;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.evalmodel.HostilityEvalModel;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.evalmodel.PredictionEvalModel;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.evalmodel.TargetEvalModel;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.evalmodel.VoteEvalModel;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.evalmodel.WinEvalModel;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.framework.EventType;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.model.FindCndlModel;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.target.VoteWolf;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.util.DoubleListMap;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.util.HashCounter;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.util.ListMap;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.util.RatioCounter;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.util.Utils;

public class Evaluator {

    public HashCounter<String> counter = new HashCounter<>();
    public DoubleListMap<String> doubleListMap = new DoubleListMap();

    BufferedWriter voteWriter;
    BufferedWriter roleWriter;
    BufferedWriter agWriter;

    public Evaluator() {
        this(null);
    }

    public Evaluator(Path base) {
        if (base != null) {
            try {
                voteWriter = new BufferedWriter(new FileWriter(base.resolve("vote-eval.tsv").toFile()));
                voteWriter.append(Utils.join("Tactic", "VillageSize", "Game", "Day", "isRevote", "SelfRole", "VoterNum",
                    "VoteRole", "ExecuteRole", "ExecutedVote", "BecomeRevote", "WolfVote", "SpVote", "MadVote", "SurviveVote"));
                voteWriter.newLine();
                roleWriter = new BufferedWriter(new FileWriter(base.resolve("role-prediction.txt").toFile()));
                roleWriter.append(Utils.join("Model", "Size", "Day", "AI", "isDead", "SelfRole", "Predicted",
                    "TrueRole", "Correct", "Predicted3", "Correct3", "Probability"));
                roleWriter.newLine();
                agWriter = new BufferedWriter(new FileWriter(base.resolve("agnet-prediction.txt").toFile()));
                agWriter.append(Utils.join("Model", "Size", "Day", "AI", "remHum", "SelfRole", "TargetRole", "TrueRole",
                    "Correct", "Probability"));
                agWriter.newLine();
            } catch (IOException ex) {
                ex.printStackTrace();
            }

        }
    }

    public void printResult() {
        List<String> keyList = new ArrayList<>(doubleListMap.keySet());
        Collections.sort(keyList);
        for (String key : keyList) {
            System.out.println(key + "\t" + doubleListMap.average(key).orElse(0) + "\t" + doubleListMap.variance(key));
        }
//        counter.sort((s1, s2) -> s1.compareTo(s2), true).print();

//        VoteWolf.actionCounter.print();
        close();
    }

    public void close() {
        try {
            if (voteWriter != null) {
                voteWriter.close();
                roleWriter.close();
                agWriter.close();
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    RatioCounter ratio100Counter = new RatioCounter();
    DoubleListMap<String> double100ListMap = new DoubleListMap();

    public void evaluate(CndlPlayer cndlPlayer, int villageSize, Map<Integer, String> aiMap) {
        try {
            // 投票評価
            if (cndlPlayer.getStrategy().voteEvalModel != null) {
                for (VoteEvalModel.Vote vote : cndlPlayer.getStrategy().voteEvalModel.getVoteList()) {
                    if (voteWriter != null) {
                        voteWriter.append(Utils.join(vote.name, villageSize, vote.game, vote.day, vote.isRevote,
                            vote.myRole, vote.voteNum, vote.voteRole, vote.executeRole, vote.executedVote, vote.becomeRevote,
                            vote.wolfVote, vote.spVote, vote.madVote, vote.surviveVote));
                        voteWriter.newLine();
                    }
                    if (vote.myRole == Role.WEREWOLF || vote.myRole == Role.POSSESSED)
                        ratio100Counter.count(Utils.joinWith("-", "03_vote", villageSize, "wolf-sp", vote.name),
                            vote.spVote == VoteEvalModel.VoteEval.HIT);
                    if (vote.myRole == Role.WEREWOLF || vote.myRole == Role.POSSESSED)
                        ratio100Counter.count(Utils.joinWith("-", "03_vote", villageSize, "wolf-mad", vote.name),
                            vote.madVote == VoteEvalModel.VoteEval.HIT);
                    if (vote.myRole != Role.WEREWOLF && vote.myRole != Role.POSSESSED
                        && vote.wolfVote != VoteEvalModel.VoteEval.NO_CHOICE)
                        ratio100Counter.count(Utils.joinWith("-", "03_vote", villageSize, "village-wolf", vote.name),
                            vote.wolfVote == VoteEvalModel.VoteEval.HIT);
                    if (vote.myRole != Role.WEREWOLF && vote.myRole != Role.POSSESSED
                        && vote.spVote != VoteEvalModel.VoteEval.NO_CHOICE)
                        ratio100Counter.count(Utils.joinWith("-", "03_vote", villageSize, "village-sp", vote.name),
                            vote.spVote == VoteEvalModel.VoteEval.HIT);
                }
            }

            // 判定評価
            if (cndlPlayer.getStrategy().predictionEvalModel != null) {
                ListMap<String, PredictionEvalModel.Prediction> rolePredictions = cndlPlayer
                    .getStrategy().predictionEvalModel.getRolePredictions();
                for (String key : rolePredictions.keySet()) {
                    for (PredictionEvalModel.Prediction pr : rolePredictions.getList(key)) {
                        if (roleWriter != null) {
                            roleWriter.append(Utils.join(key, villageSize, pr.day, aiMap.get(pr.agtIdx), pr.isDead,
                                pr.selfRole, pr.role, pr.trueRole, pr.role == pr.trueRole, pr.top3,
                                pr.top3.contains(pr.trueRole), pr.p));
                            roleWriter.newLine();
                        }
                        ratio100Counter.count(Utils.joinWith("\t", "04_pred", villageSize, pr.day, pr.selfRole, "role", key),
                            pr.role == pr.trueRole);
                    }
                }
                ListMap<String, PredictionEvalModel.Prediction> agtPredictions = cndlPlayer
                    .getStrategy().predictionEvalModel.getAgentPredictions();
                for (String key : agtPredictions.keySet()) {
                    for (PredictionEvalModel.Prediction pr : agtPredictions.getList(key)) {
                        if (agWriter != null) {
                            agWriter.append(Utils.join(key, villageSize, pr.day, aiMap.get(pr.agtIdx), pr.remNum,
                                pr.selfRole, pr.role, pr.trueRole, pr.role == pr.trueRole, pr.p));
                            agWriter.newLine();
                        }
                        ratio100Counter.count(Utils.joinWith("\t", "04_pred", villageSize, pr.day, pr.selfRole, "agent", key),
                            pr.role == pr.trueRole);
                        for (Role role : Role.values()) {
                            if (pr.selfRole != role && pr.role == role) {
                                ratio100Counter.count(Utils.joinWith("\t", "04_pred", villageSize, pr.day, pr.selfRole, role, key),
                                    pr.trueRole == role);
                            }
                        }
                        if (pr.selfRole != Role.WEREWOLF && pr.role == Role.WEREWOLF) {
                            ratio100Counter.count(Utils.joinWith("-", "04_pred", villageSize, "gobaku", key),
                                (pr.trueRole == Role.SEER || pr.trueRole == Role.MEDIUM));
                        }
                    }
                }
                // List<DataScore<Integer>> unpred =
                // cndlPlayer.getStrategy().predictionEvalModel.unpredictableAgent(Role.WEREWOLF);
                // for (DataScore<Integer> ds : unpred) {
                // System.out.println(aiMap.get(ds.data) + "\t" +
                // cndlPlayer.getStrategy().getGame().getAgentAt(ds.data).role + "\t" +
                // ds.score);
                // }
            }
            // Hate評価
            if (cndlPlayer.getStrategy().hostilityEvalModel != null) {
                for (HostilityEvalModel.Hate h : cndlPlayer.getStrategy().hostilityEvalModel.hateList) {
                    ratio100Counter.count(Utils.joinWith("-", "hate", villageSize, h.type, h.myRole),
                        aiMap.get(h.toAgtIdx).equals("cndl"));
                    ratio100Counter.count(Utils.joinWith("-", "hate", villageSize, "total"),
                        aiMap.get(h.toAgtIdx).equals("cndl"));
                }
                cndlPlayer.getStrategy().hostilityEvalModel
                    .agentPredictionAccuracy(cndlPlayer.getStrategy().getGame().getAgents()).forEach(ag -> {
                    double100ListMap.add("wolf-find-" + aiMap.get(ag.data.getIndex()), ag.score);
                });
            }
            // ターゲット評価
            if (cndlPlayer.getStrategy().targetEvalModel != null) {
                TargetEvalModel tem = cndlPlayer.getStrategy().targetEvalModel;
                cndlPlayer.getStrategy().targetEvalModel.getActions().getAllValues().forEach(t -> {
                    counter.countPlus(Utils.joinWith("-", "05_TGT", t.type, t.toRole));
                    if (t.isDead)
                        counter.countPlus(Utils.joinWith("-", "05_TGT", t.type, "BUG_DEAD"));
                });
                if (tem.getGuardSuccessRate() != -1)
                    double100ListMap.add("guard-rate", tem.getGuardSuccessRate());
            }

            // 勝率評価
            if (cndlPlayer.getStrategy().winEvalModel != null) {
                for (WinEvalModel.Win win : cndlPlayer.getStrategy().winEvalModel.lastWinList) {
                    ratio100Counter.count(Utils.joinWith("-", "01_win", villageSize, "total", aiMap.get(win.agtIdx)),
                        win.win);
                    ratio100Counter.count(Utils.joinWith("-", "01_win", villageSize, win.role, aiMap.get(win.agtIdx)),
                        win.win);
                }
            }
            // Cndl発見評価
            if (cndlPlayer.getStrategy().findCndlModel != null) {
                for (FindCndlModel.FindCndl fc : cndlPlayer.getStrategy().findCndlModel.findCndlListTemp) {
                    ratio100Counter.count(Utils.joinWith("-", "fcndl", villageSize, "find-", aiMap.get(fc.agtIndex)),
                        fc.isCndl);

                    for (int i = 5; i <= 9; i++) {
                        if (fc.wolfProb > i / 10.0 && fc.isCndl)
                            ratio100Counter.count(Utils.joinWith("-", "fcndl", villageSize, "is-wolf"),
                                fc.role == Role.WEREWOLF);
                    }
                }
            }

            if (cndlPlayer.getStrategy().getGame().getSelf().role == Role.WEREWOLF && villageSize == 5) {
                System.out.print("ぐふっ");
                ratio100Counter.count("wolf-one-day-kill", cndlPlayer.getStrategy().getGame().getEventAtDay(EventType.EXECUTE, 1).get(0).target.isSelf);
            }

            if (cndlPlayer.getStrategy().getGame().getSelf().role == Role.VILLAGER && villageSize == 5) {
                ratio100Counter.count("villager-one-day-kill", cndlPlayer.getStrategy().getGame().getEventAtDay(EventType.EXECUTE, 1).isEmpty());
            }

            // 他人の投票分析
//            if (cndlPlayer.getStrategy().othersVoteEvalModel != null) {
//                for (Boolean flag : cndlPlayer.getStrategy().othersVoteEvalModel.getPredicCountList()) {
//                    ratio100Counter.count(Utils.joinWith("-", "PredictTarget", villageSize, "Done"), flag);
//                }
//                for (Boolean flag : cndlPlayer.getStrategy().othersVoteEvalModel.getPredictList()) {
//                    ratio100Counter.count(Utils.joinWith("-", "PredictTarget", villageSize, "Vote", "Success"), flag);
//                }
//                for (Boolean flag : cndlPlayer.getStrategy().othersVoteEvalModel.getRePredicCountList()) {
//                    ratio100Counter.count(Utils.joinWith("-", "RepredictTarget", villageSize, "ReDone"), flag);
//                }
//                for (Boolean flag : cndlPlayer.getStrategy().othersVoteEvalModel.getRePredictList()) {
//                    ratio100Counter.count(Utils.joinWith("-", "RepredictTarget", villageSize, "ReVote", "Success"),
//                        flag);
//                }
//                for (Boolean flag : cndlPlayer.getStrategy().othersVoteEvalModel.getPredicCountWithRoleList()) {
//                    ratio100Counter.count(Utils.joinWith("-", "PredictTargetWithRole", villageSize, "Done"), flag);
//                }
//                for (Boolean flag : cndlPlayer.getStrategy().othersVoteEvalModel.getPredictWithRoleList()) {
//                    ratio100Counter.count(Utils.joinWith("-", "PredictTargetWithRole", villageSize, "Vote", "Success"), flag);
//                }
//                for (Boolean flag : cndlPlayer.getStrategy().othersVoteEvalModel.getRePredicCountWithRoleList()) {
//                    ratio100Counter.count(Utils.joinWith("-", "RepredictTargetWithRole", villageSize, "ReDone"), flag);
//                }
//                for (Boolean flag : cndlPlayer.getStrategy().othersVoteEvalModel.getRePredictWithRoleList()) {
//                    ratio100Counter.count(Utils.joinWith("-", "RepredictTargetWithRole", villageSize, "ReVote", "Success"),
//                        flag);
//                }
//            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void finisheOneset(CndlPlayer cndlPlayer) {
        //pred評価
        if (cndlPlayer.getStrategy().votePredectionEvalModel != null) {
            cndlPlayer.getStrategy().votePredectionEvalModel.voteResultMap.values().forEach(vr -> {
                if (!Double.isNaN(vr.precision())) {
                    this.doubleListMap.add("vote-prediction-precision", vr.precision());
                    this.ratio100Counter.count("vote-prediction-top", vr.topSuccess());
                }
            });
            cndlPlayer.getStrategy().votePredectionEvalModel.reVoteResultMap.values().forEach(vr -> {
                if (!Double.isNaN(vr.precision())) {
                    this.doubleListMap.add("revote-prediction-precision", vr.precision());
                    this.ratio100Counter.count("revote-prediction-top", vr.topSuccess());
                }
            });
        }
        for (String key : ratio100Counter.keySet()) {
            if (ratio100Counter.get(key).total > 0)
                this.doubleListMap.add(key, ratio100Counter.get(key).ratio());
            counter.countValue(key, (int) ratio100Counter.get(key).total);
        }
        for (String key : double100ListMap.keySet()) {
            OptionalDouble d = double100ListMap.average(key);
            if (d.isPresent())
                this.doubleListMap.add(key, d.getAsDouble());
        }
        ratio100Counter = new RatioCounter();
        double100ListMap = new DoubleListMap<>();
    }

}

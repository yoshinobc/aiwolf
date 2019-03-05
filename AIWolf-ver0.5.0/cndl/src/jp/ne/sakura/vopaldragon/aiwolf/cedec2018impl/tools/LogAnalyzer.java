package jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.tools;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.framework.EventType;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.framework.simulator.GameLog;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.framework.simulator.GameLogEvent;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.tools.logs.LogReader;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.tools.logs.LogReaderYosen;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.util.HashCounter;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.util.Utils;
import org.aiwolf.client.lib.Topic;
import org.aiwolf.common.data.Role;

/**
 * スプレッドシート用の各種データを出力するアナライザー
 */
public class LogAnalyzer {

    public static void main(String[] args) throws Exception {
//        LogReader reader = new LogReaderCedec();
        LogReader reader = new LogReaderYosen();
//        LogReader reader = new LogReaderLocal(Arrays.asList(Paths.get("local-log", "180813184610").toFile().listFiles()));
        LogAnalyzer logAnalyzer = new LogAnalyzer("cndl");
        for (LogReader.LogSet ls : reader) {
            if (Integer.parseInt(ls.rev) < 600) continue;
            List<GameLog> logList = ls.logs;
//            if (logList.get(0).villageSize != 5) continue; // 5人村のログのみ
            if (logList.get(0).roleOf("cndl") != null) {
                for (GameLog log : logList) {
                    logAnalyzer.analyze(ls.rev, ls.name, log);
                }
                System.out.println(ls.name + "\t" + logList.size() + "\t" + (logList.get(0).agents.length - 1));
            }
        }
//           logAnalyzer.write(Paths.get("分析", "ローカルログ分析"));
        logAnalyzer.write(Paths.get("分析", "予選ログ分析"));
    }

    private String agentName;

    public LogAnalyzer(String agentName) {
        this.agentName = agentName;
    }

    private HashCounter<String> talkC15 = new HashCounter<>();
    private HashCounter<String> voteC15 = new HashCounter<>();
    private HashCounter<String> gameC15 = new HashCounter<>();
    private HashCounter<String> talkC5 = new HashCounter<>();
    private HashCounter<String> voteC5 = new HashCounter<>();
    private HashCounter<String> gameC5 = new HashCounter<>();
    private HashCounter<String> divineC5 = new HashCounter<>();
    private HashCounter<String> divineC15 = new HashCounter<>();
    private HashCounter<String> voteD15 = new HashCounter<>();
    private HashCounter<String> voteD5 = new HashCounter<>();
    private HashCounter<String> agentC15 = new HashCounter<>();
    private HashCounter<String> agentC5 = new HashCounter<>();
    private HashCounter<String> stateC5 = new HashCounter<>(); // 5人村状態判定
    private HashCounter<String> stateC15 = new HashCounter<>(); // 15人村状態判定

    public void analyze(String rev, String gameName, GameLog log) throws IOException {
        Role role = log.roleOf(agentName);
        HashCounter<String> talkC = log.villageSize == 5 ? talkC5 : talkC15;
        HashCounter<String> voteC = log.villageSize == 5 ? voteC5 : voteC15;
        HashCounter<String> gameC = log.villageSize == 5 ? gameC5 : gameC15;
        HashCounter<String> voteD = log.villageSize == 5 ? voteD5 : voteD15;
        HashCounter<String> divineC = log.villageSize == 5 ? divineC5 : divineC15;
        HashCounter<String> agentC = log.villageSize == 5 ? agentC5 : agentC15;
        HashCounter<String> stateC = log.villageSize == 5 ? stateC5 : stateC15;
        StringBuilder game = new StringBuilder();
        StringBuilder atk = new StringBuilder();
        StringBuilder divine = new StringBuilder();
        StringBuilder bg = new StringBuilder();
        String diedOf = "";
        List<String> readableLog = new ArrayList<>();
        StringBuilder oldState = new StringBuilder(); // それまでの死亡者役職
        for (GameLogEvent l : log.allLog) {
            GameLog.GameLogAgent ag = log.agents[l.agtIdx];
            GameLog.GameLogAgent tgt = log.agents[l.tgtIdx];
            if (l.type == EventType.DAYSTART) {
//                        if (!game.toString().endsWith("|")) game.append("|");
            } else if (l.type == EventType.EXECUTE) {
                if (tgt != null && agentName.equals(tgt.ai)) diedOf = "exe";
                game.append(log.agents[l.tgtIdx].role.name().substring(0, 1));
                if (tgt != null) readableLog.add(String.format("%s\t%s\t%s\t%s", l.day, "execute", tgt.ai, tgt.role));
                // 5人村状態判定
                if (tgt != null) {
                    oldState.append(tgt.role.name().substring(0, 1));
                    stateC.countPlus(String.join("\t",
                        rev,
                        role.name(),
                        oldState
                    ));
                }
            } else if (l.type == EventType.ATTACK) {
                atk.append(log.agents[l.tgtIdx].role.name().substring(0, 1));
                if (!l.atkSuccess) {
                    atk.append("!");
                } else {
                    if (tgt != null && agentName.equals(tgt.ai)) diedOf = "atk";
                }
                readableLog.add(String.format("%s\t%s\t%s\t%s\t%s", l.day, "attack", tgt.ai, tgt.role, l.atkSuccess ? "" : "guarded"));
                // 5人村状態判定
                if (l.atkSuccess) {
                    oldState.append(tgt.role.name().substring(0, 1));
                } else {
                    oldState.append("!");
                }
                stateC.countPlus(String.join("\t",
                    rev,
                    role.name(),
                    oldState
                ));
            } else if (l.type == EventType.DIVINE) {
                if (tgt != null) {
                    divine.append(log.agents[l.tgtIdx].role.name().substring(0, 1));
                    readableLog.add(String.format("%s\t%s\t%s\t%s\t%s\t%s", l.day, "divine", ag.ai, ag.role, tgt.ai, tgt.role));
                    divineC.countPlus(String.join("\t",
                        rev,
                        "" + (log.winner == role.getTeam()),
                        "" + log.gameDay,
                        "" + l.day,
                        ag.ai,
                        tgt.ai,
                        tgt.role.name()
                    ));
                }

            } else if (l.type == EventType.GUARD_TARGET) {
                bg.append(log.agents[l.tgtIdx].role.name().substring(0, 1));
                readableLog.add(String.format("%s\t%s\t%s\t%s\t%s\t%s", l.day, "guard", ag.ai, ag.role, tgt.ai, tgt.role));
            } else if (l.type == EventType.TALK) {
                if (l.talk.getTopic() == Topic.VOTE) {
                    if (l.talk.getTarget().getAgentIdx() != 0) {
                        voteC.countPlus(String.join("\t",
                            rev,
                            "" + (log.winner == role.getTeam()),
                            "" + log.gameDay,
                            "" + l.day,
                            role.name(),
                            "declare",
                            ag.ai,
                            ag.role.name(),
                            log.agents[l.talk.getTarget().getAgentIdx()].ai,
                            log.agents[l.talk.getTarget().getAgentIdx()].role.name()
                        ));
                    }
                }
                talkC.countPlus(String.join("\t",
                    rev,
                    "" + (log.winner == role.getTeam()),
                    "" + l.day,
                    "" + l.turn,
                    ag.ai,
                    ag.role.name(),
                    l.talk.getTopic() == Topic.OPERATOR ? l.talk.getOperator().name() : l.talk.getTopic().name(),
                    Objects.toString(l.talk.getRole(), "-")
                ));

                GameLog.GameLogAgent talkTgt = null;
                if (l.talk.getTarget() != null) talkTgt = log.agents[l.talk.getTarget().getAgentIdx()];
                readableLog.add(String.format("%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s", l.day, "talk", l.turn, l.tgtIdx, ag.ai, ag.role,
                    l.talk.getTopic() == Topic.OPERATOR ? l.talk.getOperator().name() : l.talk.getTopic().name(),
                    Objects.toString(l.talk.getRole(), Objects.toString(l.talk.getResult(), "-")),
                    talkTgt != null ? talkTgt.ai : "",
                    talkTgt != null ? talkTgt.role : ""
                ));
            } else if (l.type == EventType.VOTE) {
                voteD.countPlus(String.join("\t",
                    rev,
                    "" + (log.winner == role.getTeam()),
                    "" + l.day,
                    ag.ai,
                    ag.role.name(),
                    tgt.ai,
                    tgt.role.name()
                ));
                voteC.countPlus(String.join("\t",
                    rev,
                    "" + (log.winner == role.getTeam()),
                    "" + log.gameDay,
                    "" + l.day,
                    role.name(),
                    "vote",
                    ag.ai,
                    ag.role.name(),
                    tgt.ai,
                    tgt.role.name()
                ));
                readableLog.add(String.format("%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s", l.day, "vote", l.agtIdx, ag.ai, ag.role, l.tgtIdx, tgt.ai, tgt.role));
            }
        }

        String div = Arrays.stream(log.agents).filter(ag -> ag != null && ag.role == Role.SEER).findFirst().get().ai;
        String pos = Arrays.stream(log.agents).filter(ag -> ag != null && ag.role == Role.POSSESSED).findFirst().get().ai;
        String med = log.villageSize == 5 ? "" : Arrays.stream(log.agents).filter(ag -> ag != null && ag.role == Role.MEDIUM).findFirst().get().ai;
        String bdg = log.villageSize == 5 ? "" : Arrays.stream(log.agents).filter(ag -> ag != null && ag.role == Role.BODYGUARD).findFirst().get().ai;
        String wlvs = Arrays.stream(log.agents).filter(ag -> ag != null && ag.role == Role.WEREWOLF).map(ww -> ww.ai).collect(Collectors.joining(","));

//        String day2Rem = log.log.getList(EventType.DAYSTART).stream().filter(e -> e.day == 2 && e.status == Status.ALIVE).map(e -> log.agents[e.agtIdx].role.toString().substring(0, 1)).collect(Collectors.joining());
        int seerDied = -1;
        int bgDied = -1;
        int medDied = -1;
        int posDied = -1;

        for (int i = 1; i <= log.villageSize; i++) {
            Role r = log.roleOf(i);
            switch (r) {
                case SEER:
                    seerDied = log.died[i];
                    break;
                case BODYGUARD:
                    bgDied = log.died[i];
                    break;
                case MEDIUM:
                    medDied = log.died[i];
                    break;
                case POSSESSED:
                    posDied = log.died[i];
                    break;
            }

        }

        gameC.countPlus(Utils.join(
            rev,
            gameName,
            log.name,
            Objects.toString(log.winner, "err"),
            (log.winner == role.getTeam()),
            role,
            log.getAgtIdxOf(agentName),
            log.gameDay,
            log.remWolf,
            log.died[log.getAgtIdxOf(agentName)],
            diedOf,
            game,
            game.substring(0, Math.min(game.length(), 1)),
            atk,
            atk.substring(0, Math.min(atk.length(), 1)),
            divine,
            divine.substring(0, Math.min(divine.length(), 1)),
            bg,
            bg.substring(0, Math.min(bg.length(), 1)),
            seerDied,
            medDied,
            bgDied,
            posDied,
            div,
            pos,
            med,
            bdg,
            wlvs)
        );
        Arrays.stream(log.agents).filter(ag -> ag != null).forEach(ag -> {
            agentC.countPlus(Utils.join(
                rev,
                role,
                (log.winner == role.getTeam()),
                ag.ai,
                ag.role,
                (log.winner == ag.role.getTeam()))
            );
        });
    }

    public void write(Path base) throws IOException {
        List<String> headers = Arrays.asList("Rev", "Win", "Day", "Turn", "AI", "Role", "Talk", "TalkRole", "count");
        write(talkC5, base.resolve("talk5.txt"), headers);
        write(talkC15, base.resolve("talk15.txt"), headers);
        headers = Arrays.asList("Rev", "Win", "Fin", "Day", "MyRole", "action", "AI(from)", "Role(from)", "AI(to)", "Role(to)", "count");
        write(voteC5, base.resolve("vote5.txt"), headers);
        write(voteC15, base.resolve("vote15.txt"), headers);
        headers = Arrays.asList("Rev", "Game", "File", "Winner", "Win", "Role","AgtIdx", "GameDay", "RemWolf", "Died", "DiedOf", "Execute", "E1", "Attack", "A1", "Divine", "D1", "BodyGuard", "B1", "SeerDied", "BGDied", "MedDied", "PosDied", "Seer", "Poss", "Med", "BdyGur", "Wolves", "count");
        write(gameC5, base.resolve("game5.txt"), headers);
        write(gameC15, base.resolve("game15.txt"), headers);
        headers = Arrays.asList("Rev", "Win", "Gd", "day", "AI", "TgtAI", "Role", "Cnt");
        write(divineC5, base.resolve("divine5.txt"), headers);
        write(divineC15, base.resolve("divine15.txt"), headers);
        headers = Arrays.asList("Rev", "Win", "Day", "AI", "Role(from)", "AI to", "Role(to)", "count");
        write(voteD5, base.resolve("voted5.txt"), headers);
        write(voteD15, base.resolve("voted15.txt"), headers);
        headers = Arrays.asList("Rev", "MyRole", "IWin", "AI", "HisRole", "HeWin", "Count");
        write(agentC5, base.resolve("agent5.txt"), headers);
        write(agentC15, base.resolve("agent15.txt"), headers);
        // 5人村状態判定
        headers = Arrays.asList("Rev", "MyRole", "State（死者）", "Count");
        write(stateC5, base.resolve("state5.txt"), headers);
        write(stateC15, base.resolve("state15.txt"), headers);
    }

    private void write(HashCounter<String> counter, Path file, List<String> headers) throws IOException {

        try (BufferedWriter result = Files.newBufferedWriter(file)) {
            result.write(String.join("\t", headers));
            result.newLine();
            for (String key : counter.getKeyList()) {
                result.append(key + "\t" + counter.getCount(key));
                result.newLine();
            }
        }
    }

}

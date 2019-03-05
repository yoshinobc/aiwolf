package jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.framework.simulator;

import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.framework.EventType;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.util.ListMap;
import org.aiwolf.client.lib.Content;
import org.aiwolf.common.data.Role;
import org.aiwolf.common.data.Status;
import org.aiwolf.common.data.Team;

/**
 * 配布された人狼知能プラットフォームから生成されるログファイルをパースするクラス
 */
public class GameLog {
    // 自分のチームの名

    public GameLog(String name, List<String> lines) {
        this.name = name;

        if (lines.stream().limit(15).map(l -> l.split(",")[1]).allMatch(s -> s.equals("status"))) {
            villageSize = 15;
        } else {
            villageSize = 5;
        }
        agents = new GameLogAgent[villageSize + 1];
        died = new int[villageSize + 1];
        Arrays.fill(died, -1);

        diedOf = new String[villageSize + 1];
        Arrays.fill(diedOf, "");

        for (String line : lines) {
            String[] data = line.split(",");
            GameLogEvent l = new GameLogEvent();
            l.day = Integer.parseInt(data[0]);
            int i = Integer.parseInt(data[2]);
            switch (data[1]) {
                case "status":
                    if (agents[i] == null) {
                        GameLogAgent ag = new GameLogAgent();
                        ag.role = Role.valueOf(data[3]);
                        ag.ai = data[5].startsWith("Dummy") ? "Dummy" : data[5];
                        agents[i] = ag;
                    }
                    l.type = EventType.DAYSTART;
                    l.agtIdx = i;
                    l.status = Status.valueOf(data[4]);
                    if (died[i] == -1 && l.status == Status.DEAD) {
                        died[i] = l.day - 1;
                    }
                    break;
                case "whisper":
                    l.type = EventType.WHISPER;
                case "talk":
                    if (l.type == null) l.type = EventType.TALK;
                    l.talkId = i;
                    l.turn = Integer.parseInt(data[3]);
                    l.agtIdx = Integer.parseInt(data[4]);
                    l.talk = new Content(data[5]);
                    break;
                case "attack":
                    l.type = EventType.ATTACK;
                    l.tgtIdx = i;
                    l.atkSuccess = Boolean.parseBoolean(data[3]);
                    if (l.atkSuccess) diedOf[i] = "attacked";
                    break;
                case "execute":
                    l.type = EventType.EXECUTE;
                    l.tgtIdx = i;
                    diedOf[i] = "executed";
                    break;
                case "guard":
                    l.type = EventType.GUARD_TARGET;
                case "divine":
                    if (l.type == null) l.type = EventType.DIVINE;
                case "vote":
                    if (l.type == null) l.type = EventType.VOTE;
                case "attackVote":
                    if (l.type == null) l.type = EventType.ATTACK_VOTE;
                    l.agtIdx = i;
                    l.tgtIdx = Integer.parseInt(data[3]);
                    break;
                case "result":
                    gameDay = l.day;
                    winner = Team.valueOf(data[4]);
                    remHuman = i;
                    remWolf = Integer.parseInt(data[3]);
                    break;
            }
            if (l.type != null) {
                log.add(l.type, l);
                allLog.add(l);
            } else {
                if (!data[1].equals("result")) {
                    System.out.println("NoType\t" + line);
                }
            }
        }
    }

    public int villageSize;

    /**
     * ログの名前
     */
    public String name;
    /**
     * エージェントが死んだ日。最後まで生き残った場合は-1
     */
    public int[] died;

    public String[] diedOf;

    public boolean hasWon(int agtIdx) {
        return agents[agtIdx].role.getTeam() == winner;
    }

    public GameLog(File logFile) throws Exception {
        this(logFile.getName(), Files.readAllLines(logFile.toPath()));
    }

    public int getAgtIdxOf(String ai) {
        for (int i = 1; i < agents.length; i++) {
            if (agents[i].ai.equals(ai)) return i;
        }
        return -1;
    }

    public Role roleOf(int agtIdx) {
        return agents[agtIdx].role;
    }

    public Role roleOf(String ai) {
        GameLogAgent agent = Arrays.stream(agents).filter(ag -> ag != null && ag.ai.equals(ai)).findFirst().orElse(null);
        if (agent == null) return null;
        return agent.role;
    }

    public GameLogAgent[] agents;
    public ListMap<EventType, GameLogEvent> log = new ListMap<>();
    public List<GameLogEvent> allLog = new ArrayList<>();

    /**
     * 終了時に生き残った村人数
     */
    public int remHuman;
    /**
     * 終了時に生き残った狼数
     */
    public int remWolf;
    /**
     * ゲーム終了時の日数
     */
    public int gameDay;
    /**
     * ゲームに勝利したチーム
     */
    public Team winner;

    public static class GameLogAgent {

        public String ai;
        public Role role;

        @Override
        public String toString() {
            return ai + "/" + role;
        }

    }

}

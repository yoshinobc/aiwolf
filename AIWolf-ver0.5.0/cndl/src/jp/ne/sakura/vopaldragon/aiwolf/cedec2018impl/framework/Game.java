package jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.framework;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.util.ListMap;
import static jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.util.Utils.log;
import org.aiwolf.client.lib.Topic;
import org.aiwolf.common.data.Agent;
import org.aiwolf.common.data.Role;
import org.aiwolf.common.data.Talk;
import org.aiwolf.common.data.Team;
import org.aiwolf.common.data.Vote;
import org.aiwolf.common.net.GameInfo;
import org.aiwolf.common.net.GameSetting;

/**
 * ゲーム全体の情報を保持する構造体
 */
public class Game {

    private List<GameAgent> gameAgents = new ArrayList<>();
    private Map<Agent, GameAgent> agentPlayerMap = new HashMap<>();
    private GameAgent self;
    private String gameId;

    public String getGameId() {
        return gameId;
    }

    /**
     * GameAgent全員（のコピー）を返す
     *
     */
    public List<GameAgent> getAgents() {
        return new ArrayList<>(gameAgents);
    }

    public List<GameAgent> getAlives() {
        return gameAgents.stream().filter(gp -> gp.isAlive).collect(Collectors.toList());
    }

    public List<GameAgent> getAliveOthers() {
        return gameAgents.stream().filter(gp -> gp.isAlive && gp != self).collect(Collectors.toList());
    }

    public List<GameAgent> getOthers() {
        return gameAgents.stream().filter(gp -> gp != self).collect(Collectors.toList());
    }

    /**
     * 指定されたIndex（0始まり、GameAgent.getIndexに対応）のGameAgentを返す
     */
    public GameAgent getAgentAt(int index) {
        return gameAgents.get(index);
    }

    /**
     * GameAgent全員（のコピー）のStreamを返す
     *
     */
    public Stream<GameAgent> getAgentStream() {
        return new ArrayList<>(gameAgents).stream();
    }

    /**
     * 村の初期サイズを返す（5or15）
     *
     * @return
     */
    public int getVillageSize() {
        return gameAgents.size();
    }

    /**
     * 生き残っている村人の数を返す
     *
     * @return
     */
    public int getAliveSize() {
        return this.getAlives().size();
    }

    public GameAgent getSelf() {
        return self;
    }

    public Team getTeam() {
        return team;
    }

    private Team team;
    private AbstractStrategy strategy;
    private GameSetting gameSetting;

    Game(AbstractStrategy strategy, GameInfo gameInfo, GameSetting gameSetting) {
        this.gameId = "" + strategy.getNumberOfGame();
        this.strategy = strategy;
        this.gameSetting = gameSetting;

        //GameAgentの作成
        gameInfo.getAgentList().forEach(agent -> {
            GameAgent gp = new GameAgent(this, agent);
            if (agent == gameInfo.getAgent()) {
                gp.isSelf = true;
                gp.role = gameInfo.getRole();
                this.team = gp.role.getTeam();
                this.self = gp;
            }
            if (gameInfo.getRoleMap().containsKey(agent)) {
                // 人狼なら最初に役職わかる他人がいる。
                gp.role = gameInfo.getRoleMap().get(agent);
            }
            agentPlayerMap.put(agent, gp);
            gameAgents.add(gp);
        });
//        System.out.println("-----" + this.gameId + "\t" + this.self.role);
        log("I am " + this.self.role);
        for (GameAgent ga : gameAgents) {
            log("Agent", ga, ga.role);
        }

        Map<Role, Double> roleNum = new HashMap<>();
        if (getVillageSize() == 5) {
            roleNum.put(Role.VILLAGER, 2.0);
            roleNum.put(Role.SEER, 1.0);
            roleNum.put(Role.WEREWOLF, 1.0);
            roleNum.put(Role.POSSESSED, 1.0);
        } else {
            roleNum.put(Role.VILLAGER, 8.0);
            roleNum.put(Role.SEER, 1.0);
            roleNum.put(Role.MEDIUM, 1.0);
            roleNum.put(Role.BODYGUARD, 1.0);
            roleNum.put(Role.WEREWOLF, 3.0);
            roleNum.put(Role.POSSESSED, 1.0);
        }
        this.roleNum = Collections.unmodifiableMap(roleNum);

    }

    private Map<Role, Double> roleNum;

    public Map<Role, Double> getRoleNum() {
        return roleNum;
    }

    public GameSetting getGameSetting() {
        return gameSetting;
    }

    GameAgent toGameAgent(Agent agent) {
        return agentPlayerMap.get(agent);
    }

    /**
     * 今が何日目かを返す
     *
     * @return
     */
    public int getDay() {
        return day;
    }

    void dayStart() {
        log("### DAY START" + getDay() + "######");
        this.talk_turn = 0;
        this.whisper_turn = 0;
    }

    /**
     * 指定されたイベント種別のリストを返す
     *
     * @param type イベント種別
     * @return ゲーム開始時からの当該種別のイベント（時系列順）
     */
    public List<GameEvent> getEventsOf(EventType type) {
        return events.getList(type);
    }

    public GameEvent getLastEventOf(EventType type) {
        return events.getLast(type);
    }

    public List<GameEvent> getEventAtDay(EventType type, int day) {
        return events.getList(type).stream().filter(ev -> ev.day == day).collect(Collectors.toList());
    }

    private ListMap<EventType, GameEvent> events = new ListMap<>();
    private int day = 0;

    private Map<String, GameTalk> idTalkMap = new LinkedHashMap<>();
    private ListMap<String, GameTalk> contentTalkMap = new ListMap<>();

    public GameTalk getTalkById(int day, int id) {
        return idTalkMap.get(day + "_" + id);
    }

    private Map<String, GameTalk> idWhisperMap = new LinkedHashMap<>();

    public GameTalk getWhisperById(int day, int id) {
        return idWhisperMap.get(day + "_" + id);
    }

    /**
     * 指定された発話と同内容の発話の一覧を返す
     *
     * @param talk
     * @return
     */
    public List<GameTalk> getSameTalk(GameTalk talk) {
        return contentTalkMap.getList(talk.talkContent());
    }

    /**
     * 全ての発話のStreamを返す
     *
     * @return
     */
    public Stream<GameTalk> getAllTalks() {
        return idTalkMap.values().stream();
    }

    /**
     * 同発言者、同内容の発話をDistinctしたStreamを返す
     *
     * @return
     */
    public Stream<GameTalk> getUniqueTalks() {
        return getAllTalks().collect(Collectors.toMap(t -> (t.getTalker().getId() + "_" + t.talkContent()), t -> t, (p, q) -> p)).values().stream();
    }

    private Set<Integer> talkHashCodes = new HashSet<>();

    private void addEvent(GameEvent e) {
        events.add(e.type, e);
        //会話の記録
        if (e.type == EventType.TALK) e.talks.forEach(t -> {
                idTalkMap.put(t.talkUniqueId(), t);
//                System.out.println("Add Talk "+idTalkMap.size());
                contentTalkMap.add(t.talkContent(), t);
                int talkHash = (t.getTalker() + "_" + t.talkContent()).hashCode();
                if (talkHashCodes.contains(talkHash)) t.isRepeat = true;
                else talkHashCodes.add(talkHash);

                if (t.getTopic() == Topic.COMINGOUT) {
                    t.getTalker().coRole = t.getRole();
                }
            });
        //ささやきの記録
        if (e.type == EventType.WHISPER) e.talks.forEach(t -> {
                idWhisperMap.put(t.talkUniqueId(), t);
            });
//        log("event", e);
        //Strategyにイベントを通知
        strategy.publishEvent(e);
    }

    private Map<String, String> lastGameInfoValue = new LinkedHashMap<>();
    private List<String> fields = Arrays.asList("day", "executedAgent", "latestExecutedAgent", "attackedAgent", "lastDeadAgentList", "guardedAgent", "mediumResult", "divineResult", "voteList", "latestVoteList", "attackVoteList", "latestAttackVoteList", "talkList", "whisperList"
    //   "cursedFox",  "agent",   "statusMap", "roleMap",        "remainTalkMap", "remainWhisperMap", "existingRoleList"
    );

    void updateWorld(GameInfo gameInfo) {
        if (game_end) return;
        //生存状況の更新
        gameAgents.forEach(gp -> gp.isAlive = gameInfo.getAliveAgentList().contains(gp.agent));

        //ゲーム終了時の役割情報開示
        gameAgents.forEach(gp -> {
            if (gameInfo.getRoleMap().get(gp.agent) != null) gp.role = gameInfo.getRoleMap().get(gp.agent);
        });
//        System.out.println("whisper\t"+gameInfo.getWhisperList());

        for (String field : fields) {
            try {
                Field f = GameInfo.class.getDeclaredField(field);
                f.setAccessible(true);
                String value = Objects.toString(f.get(gameInfo));
                String old = Objects.toString(lastGameInfoValue.get(field));
                if (!value.equals(old)) {
                    log(field, value);
//                    log(field, old+"->"+value);
                    if (!value.equals("null")) {
                        switch (field) {
                            case "day":
                                log("<<<<<<<<<<<Day " + value + ">>>>>>>>>>>");
                                day = gameInfo.getDay();
                                addEvent(new GameEvent(EventType.DAYSTART, day));
                                //Guardイベント
                                if (day > 1 && gameInfo.getLastDeadAgentList() != null && gameInfo.getLastDeadAgentList().isEmpty()) {
                                    if (gameInfo.getGuardedAgent() != null) addEvent(new GameEvent(EventType.GUARD_SUCCESS, day - 1, toGameAgent(gameInfo.getGuardedAgent())));
                                    else addEvent(new GameEvent(EventType.GUARD_SUCCESS, day - 1));
                                }
                                lastGameInfoValue.put("guardedAgent", null);
                                lastGameInfoValue.put("executedAgent", null);
                                lastGameInfoValue.put("latestExecutedAgent", null);
                                lastGameInfoValue.put("attackedAgent", null);
                                break;
                            case "mediumResult":
                                addEvent(new GameEvent(EventType.MEDIUM, this, gameInfo.getMediumResult()));
                                break;
                            case "divineResult":
                                addEvent(new GameEvent(EventType.DIVINE, this, gameInfo.getDivineResult()));
                                break;
                            case "executedAgent":
                                updateExecuted(day - 1, gameInfo.getExecutedAgent());
                                break;
                            case "latestExecutedAgent":
                                updateExecuted(day, gameInfo.getLatestExecutedAgent());
                                break;
                            case "attackedAgent":
                                addEvent(new GameEvent(EventType.VICTIM_DECIDED, day - 1, toGameAgent(gameInfo.getAttackedAgent())));
                                break;
                            case "lastDeadAgentList":
                                if (gameInfo.getLastDeadAgentList().size() >= 1) {
                                    Agent lastAttacked = gameInfo.getLastDeadAgentList().get(0);
                                    addEvent(new GameEvent(EventType.ATTACK, day - 1, toGameAgent(lastAttacked)));
                                    toGameAgent(lastAttacked).isAttacked = true;
                                }
                                break;
                            case "guardedAgent":
                                addEvent(new GameEvent(EventType.GUARD_TARGET, day - 1, toGameAgent(gameInfo.getGuardedAgent())));
                                break;
                            case "voteList":
                                updateVotes(gameInfo.getVoteList());
                                break;
                            case "latestVoteList":
                                updateVotes(gameInfo.getLatestVoteList());
                                break;
                            case "attackVoteList":
                                updateAttackVotes(gameInfo.getAttackVoteList());
                                break;
                            case "latestAttackVoteList":
                                updateAttackVotes(gameInfo.getLatestAttackVoteList());
                                break;
                            case "talkList":
                                List<GameTalk> talks = new ArrayList<>();
                                for (Talk talk : gameInfo.getTalkList()) {
                                    if (talk.getTurn() == talk_turn) {
                                        talks.add(new GameTalk(talk, this, false));
                                    }
                                }
                                if (!talks.isEmpty()) {
                                    this.addEvent(new GameEvent(EventType.TALK, talks));
                                    talk_turn++;
                                }
                                break;
                            case "whisperList":
                                List<GameTalk> whispers = new ArrayList<>();
                                for (Talk talk : gameInfo.getWhisperList()) {
                                    if (talk.getTurn() == whisper_turn) {
                                        whispers.add(new GameTalk(talk, this, true));
                                    }
                                }
                                if (!whispers.isEmpty()) {
                                    this.addEvent(new GameEvent(EventType.WHISPER, whispers));
                                    whisper_turn++;
                                }
                                break;
                            //do nothing
                            case "cursedFox":
                            case "agent":
                            case "statusMap":
                            case "roleMap":
                            case "remainTalkMap":
                            case "remainWhisperMap":
                            case "existingRoleList":
                        }
                    }
                    lastGameInfoValue.put(field, value);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    private void updateExecuted(int day, Agent agent) {
        if (!toGameAgent(agent).isExecuted) {
            addEvent(new GameEvent(EventType.EXECUTE, day, toGameAgent(agent)));
            toGameAgent(agent).isExecuted = true;
        }
    }

    private boolean game_end = false;
    private int talk_turn = 0;
    private int whisper_turn = 0;

    private void updateVotes(List<Vote> votes) {
        if (votes != null && !votes.isEmpty()) {
            int vDay = votes.get(0).getDay();
            if (vDay >= voteDay) {
//                System.out.println(vDay + "\t" + votes);
                if (vDay > voteDay) there_was_revote_flag = false;
                addEvent(new GameEvent(EventType.VOTE, vDay,
                    votes.stream().map(v -> new GameVote(v.getDay(), there_was_revote_flag, toGameAgent(v.getAgent()), toGameAgent(v.getTarget()))).collect(Collectors.toList())
                ));
                voteDay++;
                there_was_revote_flag = false;
            }
        }
    }

    private void updateAttackVotes(List<Vote> votes) {
        if (votes != null && !votes.isEmpty()) {
            int vDay = votes.get(0).getDay();
            if (vDay >= attackVoteDay) {
                if (vDay > attackVoteDay) there_was_attack_revote_flag = false;
                addEvent(new GameEvent(EventType.ATTACK_VOTE, vDay,
                    votes.stream().map(v -> new GameVote(v.getDay(), there_was_attack_revote_flag, toGameAgent(v.getAgent()), toGameAgent(v.getTarget()))).collect(Collectors.toList())
                ));
                attackVoteDay++;
                there_was_attack_revote_flag = false;
            }
        }
    }

    private boolean there_was_revote_flag = false;
    private boolean there_was_attack_revote_flag = false;
    private int voteDay = 1;
    private int attackVoteDay = 1;

    void notifyAtkRevote() {
        there_was_attack_revote_flag = true;
        attackVoteDay--;
        lastGameInfoValue.put("attackVoteList", "[]");
        lastGameInfoValue.put("latestAttackVoteList", "[]");
    }

    void notifyRevote() {
//        System.out.println("There was a revote");
        there_was_revote_flag = true;
        voteDay--;
        lastGameInfoValue.put("voteList", "[]");
        lastGameInfoValue.put("latestVoteList", "[]");
    }

    private Team wonTeam;

    public Team getWonTeam() {
        return wonTeam;
    }

    void finish() {
        if (!game_end) {
            log("<<<<<<<<<<<Game End>>>>>>>>>>>");
            game_end = true;
            log("roles", gameAgents);
            boolean allWolfDead = getAgentStream().filter(a -> a.role == Role.WEREWOLF).allMatch(a -> !a.isAlive);
            wonTeam = allWolfDead ? Team.VILLAGER : Team.WEREWOLF;
            log(wonTeam == getSelf().role.getTeam() ? "I won" : "I lost");
        }
    }

}

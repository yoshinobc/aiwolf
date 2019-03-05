package jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.framework.simulator;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.framework.EventType;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.util.ListMap;
import org.aiwolf.client.lib.Content;
import org.aiwolf.common.data.Agent;
import org.aiwolf.common.data.Judge;
import org.aiwolf.common.data.Player;
import org.aiwolf.common.data.Role;
import org.aiwolf.common.data.Species;
import org.aiwolf.common.data.Status;
import org.aiwolf.common.data.Talk;
import org.aiwolf.common.data.Vote;
import org.aiwolf.common.net.GameInfo;
import org.aiwolf.common.net.GameSetting;

public abstract class AbstractGameSimulator {

    private Player player;
    private boolean zombieMode = true;

    public AbstractGameSimulator(Player player) {
        this.player = player;
        this.prepareStates();
    }

    public void setZombieMode(boolean zombieMode) {
        this.zombieMode = zombieMode;
    }

    public enum State {
        DAY_START, FIRST_TALK, TALK, DAY_FINISH, VOTE_FIRST, REVOTE, DIVINE, WHISPER_FIRST, WHISPER, GUARD, ATTACK_VOTE_FIRST, ATTACK_VOTE;

        public State next() {
            int ord = this.ordinal() + 1;
            if (ord >= State.values().length) ord = 0;
            return State.values()[ord];
        }
    }

    private enum StateControl {
        SKIP, NEXT, WAIT, LOOP;
    }

    private State state;
    private Map<State, Supplier<StateControl>> stateActions;
    private int talkTurn = 0;

    private void prepareStates() {
        stateActions = new HashMap<>();
        //dayStart
        stateActions.put(State.DAY_START, () -> {
            if (!continueDayFlag) return StateControl.WAIT;
            if (day > 0 && role == Role.SEER && divineJudge == null) return StateControl.WAIT;
            if (day > 1) {
                if (role == Role.MEDIUM && mediumJudge == null) return StateControl.WAIT;
                if (role == Role.BODYGUARD && guardedAgent == null) return StateControl.WAIT;
                if (role == Role.WEREWOLF && (attackedAgent == null || attackVotes == null)) return StateControl.WAIT;
                if (executed == null || votes == null || lastDeadAgentList == null) return StateControl.WAIT;
            }
            setDayStartInfo();
            if (isAlive()) {
                setGameInfo("day", day);
                player.update(info);
                player.dayStart();
            }
            talkTurn = 0;
            continueDayFlag = false;
            return StateControl.NEXT;
        });

        //first talk
        stateActions.put(State.FIRST_TALK, () -> {
            if (day == 0) return StateControl.SKIP;
            if (continueTalkFlag == null || !continueTalkFlag) return StateControl.WAIT;
            if (isAlive()) {
                player.update(info);
                String talkStr = player.talk();
                if (talkStr != null) {
                    Content talk = new Content(talkStr);
                    GameLogEvent ev = new GameLogEvent();
                    ev.turn = talkTurn++;
                    ev.day = day;
                    ev.agtIdx = self.getAgentIdx();
                    ev.type = EventType.TALK;
                    ev.talk = talk;
                    playerActions.add(EventType.TALK, ev);
                }
            }
            continueTalkFlag = null;
            return StateControl.NEXT;
        });
        //second talk
        stateActions.put(State.TALK, () -> {
            if (day == 0) return StateControl.SKIP;
            if (continueTalkFlag == null) return StateControl.WAIT;
            if (!continueTalkFlag) {
                continueTalkFlag = null;
                return StateControl.NEXT;
            }
            if (tempTalkList == null) return StateControl.WAIT;
            if (isAlive()) {
                talkList.addAll(tempTalkList);
                setGameInfo("talkList", talkList);
                tempTalkList = null;
                player.update(info);
                Content talk = new Content(player.talk());
                GameLogEvent ev = new GameLogEvent();
                ev.day = day;
                ev.turn = talkTurn++;
                ev.agtIdx = self.getAgentIdx();
                ev.type = EventType.TALK;
                ev.talk = talk;
                playerActions.add(EventType.TALK, ev);
            }
            continueTalkFlag = null;
            return StateControl.LOOP;
        });
        //dayFinish
        stateActions.put(State.DAY_FINISH, () -> {
            if (day > 0 && tempTalkList == null) return StateControl.WAIT;
            if (isAlive()) {
                if (day > 0) {
                    talkList.addAll(tempTalkList);
                    setGameInfo("talkList", talkList);
                    tempTalkList = null;
                }
                player.update(info);
            }
            return StateControl.NEXT;
        });
        //first vote
        stateActions.put(State.VOTE_FIRST, () -> {
            if (day == 0) return StateControl.SKIP;
            if (continueVoteFlag == null || !continueVoteFlag) return StateControl.WAIT;
            if (isAlive()) {
                player.update(info);
                Agent divTgt = player.vote();
                GameLogEvent ev = new GameLogEvent();
                ev.day = day;
                ev.agtIdx = self.getAgentIdx();
                ev.type = EventType.VOTE;
                ev.tgtIdx = divTgt.getAgentIdx();
                playerActions.add(EventType.VOTE, ev);
            }
            continueVoteFlag = null;
            return StateControl.NEXT;
        });
        //second vote
        stateActions.put(State.REVOTE, () -> {
            if (day == 0) return StateControl.SKIP;
            if (continueVoteFlag == null) return StateControl.WAIT;
            if (!continueVoteFlag) {
                continueVoteFlag = null;
                return StateControl.NEXT;
            }
            if (votes == null) return StateControl.WAIT;
            if (isAlive()) {
                setGameInfo("latestVoteList", new ArrayList(votes));
                votes = null;
                player.update(info);
                Agent divTgt = player.vote();
                GameLogEvent ev = new GameLogEvent();
                ev.day = day;
                ev.agtIdx = self.getAgentIdx();
                ev.type = EventType.VOTE;
                ev.tgtIdx = divTgt.getAgentIdx();
                playerActions.add(EventType.VOTE, ev);
            }
            continueVoteFlag = null;
            return StateControl.LOOP;
        });
        //divine
        stateActions.put(State.DIVINE, () -> {
            if (role != Role.SEER) return StateControl.SKIP;
            if (!continueDivineFlag) return StateControl.WAIT;
            if (day != 0) {
                if (votes == null || executed == null) return StateControl.WAIT;
                setNightExecuteResult();
            }
            if (isAlive()) {
                player.update(info);
                Agent divTgt = player.divine();
                GameLogEvent ev = new GameLogEvent();
                ev.day = day;
                ev.agtIdx = self.getAgentIdx();
                ev.type = EventType.DIVINE;
                ev.tgtIdx = divTgt.getAgentIdx();
                playerActions.add(EventType.DIVINE, ev);
            }
            continueDivineFlag = false;
            return StateControl.NEXT;
        });
        //first whisper
        stateActions.put(State.WHISPER_FIRST, () -> {
            if (role != Role.WEREWOLF) return StateControl.SKIP;
            if (getAliveWolfSize() == 1) return StateControl.SKIP;
            if (day != 0 && (votes == null || executed == null)) return StateControl.WAIT;
            if (continueWhisperFlag == null || !continueWhisperFlag) return StateControl.WAIT;
            if (isAlive()) {
                if (day != 0) {
                    setNightExecuteResult();
                }
                player.update(info);
                Content whisper = new Content(player.whisper());
                GameLogEvent ev = new GameLogEvent();
                ev.day = day;
                ev.agtIdx = self.getAgentIdx();
                ev.type = EventType.WHISPER;
                ev.talk = whisper;
                playerActions.add(EventType.WHISPER, ev);
            }
            continueWhisperFlag = null;
            return StateControl.NEXT;
        });
        //second whisper
        stateActions.put(State.WHISPER, () -> {
            if (role != Role.WEREWOLF) return StateControl.SKIP;
            if (getAliveWolfSize() == 1) return StateControl.SKIP;
            if (continueWhisperFlag == null) return StateControl.WAIT;
            if (!continueWhisperFlag) {
                continueWhisperFlag = null;
                return StateControl.NEXT;
            }
            if (tempWhisperList == null) return StateControl.WAIT;
            if (isAlive()) {
                whisperList.addAll(tempWhisperList);
                tempWhisperList = null;
                setGameInfo("whisperList", whisperList);
                player.update(info);
                Content whisper = new Content(player.whisper());
                GameLogEvent ev = new GameLogEvent();
                ev.day = day;
                ev.agtIdx = self.getAgentIdx();
                ev.type = EventType.WHISPER;
                ev.talk = whisper;
                playerActions.add(EventType.WHISPER, ev);
            }
            continueWhisperFlag = null;
            return StateControl.LOOP;
        });
        //first attack vote
        stateActions.put(State.ATTACK_VOTE_FIRST, () -> {
            if (role != Role.WEREWOLF || day == 0) return StateControl.SKIP;
            if (continueAttackVoteFlag == null || !continueAttackVoteFlag) return StateControl.WAIT;
            if (getAliveWolfSize() > 1) {
                if (getAliveWolfSize() > 1 && tempWhisperList == null) return StateControl.WAIT;
                whisperList.addAll(tempWhisperList);
                tempWhisperList = null;
                setGameInfo("whisperList", whisperList);
            } else {
                //生き残りが1人だとWhisperが無いので、votes/executedの更新が必要になる
                if (votes == null || executed == null) return StateControl.WAIT;
                setNightExecuteResult();
            }
            if (isAlive()) {
                player.update(info);
                Agent atkTgt = player.attack();
                GameLogEvent ev = new GameLogEvent();
                ev.day = day;
                ev.agtIdx = self.getAgentIdx();
                ev.type = EventType.ATTACK_VOTE;
                ev.tgtIdx = atkTgt.getAgentIdx();
                playerActions.add(EventType.ATTACK_VOTE, ev);
            }
            continueAttackVoteFlag = null;
            return StateControl.NEXT;
        });
        //second attack vote
        stateActions.put(State.ATTACK_VOTE, () -> {
            if (role != Role.WEREWOLF || day == 0) return StateControl.SKIP;
            if (continueAttackVoteFlag == null) return StateControl.WAIT;
            if (!continueAttackVoteFlag) {
                continueAttackVoteFlag = null;
                return StateControl.NEXT;
            }
            if (attackVotes == null) return StateControl.WAIT;
            if (isAlive()) {
                setGameInfo("latestAttackVoteList", attackVotes);
                attackVotes = null;
                player.update(info);
                Agent atkTgt = player.attack();
                GameLogEvent ev = new GameLogEvent();
                ev.day = day;
                ev.agtIdx = self.getAgentIdx();
                ev.type = EventType.ATTACK_VOTE;
                ev.tgtIdx = atkTgt.getAgentIdx();
                playerActions.add(EventType.ATTACK_VOTE, ev);
            }
            continueAttackVoteFlag = null;
            return StateControl.LOOP;
        });
        //guard
        stateActions.put(State.GUARD, () -> {
            if (role != Role.BODYGUARD || day == 0) return StateControl.SKIP;
            if (!continueGuardFlag) return StateControl.WAIT;
            if (votes == null || executed == null) return StateControl.WAIT;
            if (isAlive()) {
                setNightExecuteResult();
                player.update(info);
                Agent guardTgt = player.guard();
                GameLogEvent ev = new GameLogEvent();
                ev.day = day;
                ev.agtIdx = self.getAgentIdx();
                ev.type = EventType.GUARD_TARGET;
                ev.tgtIdx = guardTgt.getAgentIdx();
                playerActions.add(EventType.GUARD_TARGET, ev);
            }
            continueGuardFlag = false;
            return StateControl.NEXT;
        });
    }

    private void setDayStartInfo() {
        //情報セット
        setGameInfo("divineResult", divineJudge);
        setGameInfo("mediumResult", mediumJudge);
        setGameInfo("guardedAgent", guardedAgent);
        setGameInfo("attackedAgent", attackedAgent);
        setGameInfo("executedAgent", executed);
        if (votes != null) setGameInfo("voteList", new ArrayList(votes));
        if (attackVotes != null) setGameInfo("attackVoteList", new ArrayList<>(attackVotes));
        if (lastDeadAgentList != null) setGameInfo("lastDeadAgentList", new ArrayList<>(lastDeadAgentList));

        //クリア処理
        setGameInfo("latestExecutedAgent", null);
        if (!info.getLatestVoteList().isEmpty()) {
            setGameInfo("latestVoteList", new ArrayList());
        }
        if (!info.getLatestAttackVoteList().isEmpty()) {
            setGameInfo("latestAttackVoteList", new ArrayList());
        }
        if (!info.getTalkList().isEmpty()) {
            talkList.clear();
            setGameInfo("talkList", talkList);
        }
        if (!info.getWhisperList().isEmpty()) {
            whisperList.clear();
            setGameInfo("whisperList", whisperList);
        }
        votes = null;
        attackVotes = null;
        executed = null;
        lastDeadAgentList = null;
        attackedAgent = null;
        guardedAgent = null;
        divineJudge = null;
        mediumJudge = null;
    }

    private void setNightExecuteResult() {
        setGameInfo("latestExecutedAgent", executed);
        setGameInfo("latestVoteList", new ArrayList(votes));
    }

    private void run() {
        for (int limit = 0; limit < State.values().length; limit++) {//無限ループ回避
            //アクションに失敗するまで次から次にアクションを続けていく
            StateControl result = stateActions.get(state).get();
//            System.out.println("***RUN***\t" + state + " " + result);
            if (result == StateControl.SKIP || result == StateControl.NEXT) {
                state = state.next();
            } else if (result == StateControl.LOOP) {
                //同じアクションの繰り返し
            } else if (result == StateControl.WAIT) {
                break;
            }
        }
    }

    //現在のゲーム情報
    private Agent[] agents;
    private Agent self;
    private GameInfo info;
    private Role role;
    private Map<Agent, Status> statusMap;
    private Map<Agent, Role> roleMap;
    private List<Talk> talkList;
    private List<Talk> tempTalkList;
    private List<Talk> whisperList;
    private List<Talk> tempWhisperList;
    private List<Vote> votes;
    private List<Vote> attackVotes;

    private Agent executed;
    private List<Agent> lastDeadAgentList;

    private Agent attackedAgent;
    private Agent guardedAgent;

    private Judge divineJudge;
    private Judge mediumJudge;

    private int day;

    private Boolean continueTalkFlag;
    private Boolean continueWhisperFlag;
    private Boolean continueAttackVoteFlag;
    private Boolean continueVoteFlag;
    private boolean continueGuardFlag;
    private boolean continueDayFlag;
    private boolean continueDivineFlag;

    protected void continueTalk(boolean flag) {
        continueTalkFlag = flag;
        run();
    }

    protected void continueWhisper(boolean flag) {
        continueWhisperFlag = flag;
        run();
    }

    protected void continueAttackVote(boolean flag) {
        continueAttackVoteFlag = flag;
        run();
    }

    protected void continueGuard() {
        continueGuardFlag = true;
        run();
    }

    protected void continueDivine() {
        continueDivineFlag = true;
        run();
    }

    protected void continueDay() {
        continueDayFlag = true;
        run();
    }

    protected void continueVote(boolean flag) {
        continueVoteFlag = flag;
        run();
    }
    private ListMap<EventType, GameLogEvent> playerActions;

    protected Agent[] getAgents() {
        return agents;
    }

    protected int getAliveAgentSize() {
        return this.info.getAliveAgentList().size();
    }

    protected int getAliveWolfSize() {
        return (int) info.getAliveAgentList().stream().filter(ag -> roleMap.get(ag) == Role.WEREWOLF).count();
    }

    protected boolean isAlive() {
        return zombieMode || statusMap.get(self) == Status.ALIVE;
    }

    public Role getRole() {
        return role;
    }

    public Agent getSelf() {
        return self;
    }

    public List<GameLogEvent> getPlayerActions(EventType type) {
        return playerActions.getList(type);
    }

    public GameLogEvent getLastPlayerAction(EventType type) {
        List<GameLogEvent> list = playerActions.getList(type);
        if (list.isEmpty()) return null;
        return list.get(list.size() - 1);
    }

    /**
     *
     * @param villageSize 村のサイズ
     * @param selfIdx プレイヤーのIndex（1始まり）
     * @param idxRoleMap 初期に判明しているRoleの一覧
     */
    protected void startGame(int villageSize, int selfIdx, Map<Integer, Role> idxRoleMap) {
        //初期化
        info = new GameInfo();
        GameSetting setting = new GameSetting();
        statusMap = new HashMap<>();
        roleMap = new HashMap<>();
        playerActions = new ListMap<>();
        talkList = new ArrayList<>();
        tempTalkList = null;
        whisperList = new ArrayList<>();
        tempWhisperList = null;
        votes = Collections.EMPTY_LIST;
        attackVotes = Collections.EMPTY_LIST;
        executed = null;
        lastDeadAgentList = Collections.EMPTY_LIST;

        attackedAgent = null;
        guardedAgent = null;

        divineJudge = null;
        mediumJudge = null;

        continueTalkFlag = null;
        continueWhisperFlag = null;
        continueAttackVoteFlag = null;
        continueVoteFlag = null;
        continueGuardFlag = false;
        continueDayFlag = false;
        continueDivineFlag = false;

        //エージェント初期化
        agents = new Agent[villageSize + 1];
        for (int i = 1; i <= villageSize; i++) {
            try {
                Constructor<Agent> ag = Agent.class.getDeclaredConstructor(int.class);
                ag.setAccessible(true);
                Agent agent = ag.newInstance(i);
                agents[i] = agent;
                if (i == selfIdx) {
                    setGameInfo("agent", agent);
                    role = idxRoleMap.get(i);
                    self = agent;
                }
                    roleMap.put(agent, idxRoleMap.get(i));
                statusMap.put(agent, Status.ALIVE);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
//        //狼だった場合の初期開示
//        if (role == Role.WEREWOLF) {
//            for (int i = 1; i <= villageSize; i++) {
//                if (idxRoleMap.get(i) == Role.WEREWOLF) {
//                    roleMap.put(agents[i], Role.WEREWOLF);
//                }
//            }
//        }

        setGameInfo("roleMap", roleMap);
        setGameInfo("statusMap", statusMap);
        player.initialize(info, setting);
        continueDayFlag = true;
        day = 0;
        state = State.DAY_START;
        run();
    }

    protected void finishGame(int finishDay, Map<Integer, Role> idxRoleMap, Map<Integer, Status> idxAliveMap) {
        if (role == Role.WEREWOLF && attackVotes == null) attackVotes = Collections.EMPTY_LIST;
        setDayStartInfo();
        setGameInfo("day", finishDay);
        idxRoleMap.forEach((i, r) -> roleMap.put(agents[i], r));
        setGameInfo("roleMap", roleMap);
        idxAliveMap.forEach((i, s) -> statusMap.put(agents[i], s));
        setGameInfo("statusMap", statusMap);
        player.update(info);
        player.finish();
    }

    protected void updateDay(int day) {
        this.day = day;
        run();
    }

    protected void updateTalk(List<Talk> talks) {
        tempTalkList = talks;
        run();
    }

    protected void updateVote(List<Vote> votes) {
        this.votes = votes;
        run();
    }

    protected void updateExecute(int day, int tgtIdx) {
        executed = agents[tgtIdx];
        statusMap.put(agents[tgtIdx], Status.DEAD);
        run();
    }

    protected void updateAttack(int tgtIndex) {
        lastDeadAgentList = Arrays.asList(agents[tgtIndex]);
        statusMap.put(agents[tgtIndex], Status.DEAD);
        run();
    }

    protected void updateAttackFail() {
        lastDeadAgentList = Collections.EMPTY_LIST;
        run();
    }

    protected void updateWhisper(List<Talk> whisper) {
        if (role == Role.WEREWOLF) {
            tempWhisperList = whisper;
            run();
        }
    }

    protected void updateAttackVote(List<Vote> votes) {
        if (role == Role.WEREWOLF) {
            this.attackVotes = votes;
            run();
        }
    }

    protected void updateVictim(int tgtIndex) {
        if (role == Role.WEREWOLF) {
            attackedAgent = agents[tgtIndex];
            run();
        }
    }

    protected void updateDivine(int day, int tgtIndex, Species species) {
        if (role == Role.SEER) {
            Agent targetAgent = agents[tgtIndex];
            divineJudge = new Judge(day + 1, self, targetAgent, species);
            run();
        }
    }

    protected void updateMedium(int day, int tgtIdx, Species tgtSpecies) {
        if (role == Role.MEDIUM) {
            mediumJudge = new Judge(day + 1, self, agents[tgtIdx], tgtSpecies);
            run();
        }
    }

    protected void updateGuard(int tgtIndex) {
        if (role == Role.BODYGUARD) {
            guardedAgent = this.agents[tgtIndex];
            run();
        }
    }

    private void setGameInfo(String fieldName, Object o) {
        try {
            Field f = GameInfo.class.getDeclaredField(fieldName);
            f.setAccessible(true);
            f.set(info, o);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

}

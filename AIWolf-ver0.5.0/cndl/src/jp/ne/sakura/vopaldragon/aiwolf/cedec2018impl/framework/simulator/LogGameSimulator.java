package jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.framework.simulator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.aiwolf.common.data.Role;
import org.aiwolf.common.data.Vote;

import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.framework.EventType;
import org.aiwolf.common.data.Player;
import org.aiwolf.common.data.Status;
import org.aiwolf.common.data.Talk;

/**
 * ゲームログを読み込ませ、当該試合を疑似体験させるツール。
 */
public class LogGameSimulator extends AbstractGameSimulator {

    public static boolean OPTION_OPEN_ROLES = false;

    public LogGameSimulator(Player player) {
        super(player);
    }

    public void run(int playerIdx, GameLog log) throws Exception {
        //初期化
        Map<Integer, Role> allRoleMap = new HashMap();
        Map<Integer, Role> initialRoleMap = new HashMap();
        for (int j = 1; j < log.agents.length; j++) {
            if (OPTION_OPEN_ROLES || j == playerIdx || (log.agents[playerIdx].role == Role.WEREWOLF && log.agents[j].role == Role.WEREWOLF)) {
                initialRoleMap.put(j, log.agents[j].role);
            }
            allRoleMap.put(j, log.agents[j].role);
        }
        startGame(log.villageSize, playerIdx, initialRoleMap);

        //開始
        List<GameLogEvent> buffer = new ArrayList<>();
        EventType lastType = null;
        int turn = 0;
        int day = 0;
        boolean attackedFlag = false;
        for (GameLogEvent e : log.allLog) {
            if (e.day != day) {
                //日が変わった
                day = e.day;
                if (day == log.gameDay) {
                    if (!attackedFlag) updateAttackFail();//狼が最終日に吊られるとATTACKが記録されないので。
                    Map<Integer, Status> idxAliveMap = new HashMap<>();
                    int baseIndex = log.allLog.size() - (log.villageSize + 1);
                    for (int i = 1; i < log.villageSize; i++) {
                        idxAliveMap.put(i, log.allLog.get(baseIndex + i).status);
                    }
                    finishGame(day, allRoleMap, idxAliveMap);
                    break;
                } else {
                    attackedFlag = false;
                    updateDay(day);
                    continueDay();
                }
            }
            if (lastType != null && lastType != e.type) {
                switch (lastType) {
                    case WHISPER:
                        //最後のささやきログが終わったら起動
                        continueWhisper(true);
                        if (!buffer.stream().allMatch(w -> w.day == 0)) {//初日の最終ささやきは流れる
                            updateWhisper(toTalks(buffer));
                        } else {
                            buffer.clear();
                        }
                        continueWhisper(false);
                        turn = 0;
                        break;
                    case TALK:
                        //最後の会話ログが終わったら起動
                        continueTalk(true);
                        updateTalk(toTalks(buffer));
                        continueTalk(false);
                        turn = 0;
                        break;
                    case VOTE:
                        continueVote(true);
                        //すべての投票ログが終わったら最後の結果を記録して投票終了
                        List<Vote> votes = toVote(buffer);
                        updateVote(votes);
                        buffer.clear();
                        continueVote(false);
                        break;
                    case ATTACK_VOTE:
                        //すべての投票ログが終わったら最後の結果を記録して投票終了
                        continueAttackVote(true);
                        List<Vote> attackVotes = toVote(buffer);
                        updateAttackVote(attackVotes);
                        continueAttackVote(false);
                        buffer.clear();
                        break;
                }
            }

            switch (e.type) {
                case DIVINE:
                    continueDivine();
                    updateDivine(day, e.tgtIdx, allRoleMap.get(e.tgtIdx).getSpecies());
                    break;
                case EXECUTE:
                    //追放情報
                    updateExecute(day, e.tgtIdx);
                    updateMedium(day, e.tgtIdx, allRoleMap.get(e.tgtIdx).getSpecies());
                    break;
                case VOTE:
                    int size = getAliveAgentSize();
                    if (buffer.size() == size) {
                        continueVote(true);
                        //VOTE が1巡り以上している場合は再投票
                        List<Vote> votes = buffer.stream().map(ev -> new Vote(ev.day, getAgents()[ev.agtIdx], getAgents()[ev.tgtIdx])).collect(Collectors.toList());
                        updateVote(votes);
                        buffer.clear();
                    }
                    buffer.add(e);
                    break;
                case ATTACK_VOTE:
                    int wolfSize = getAliveWolfSize();
                    if (buffer.size() == wolfSize) {
                        //ATTACKVOTE が1巡り以上している場合は再投票
                        List<Vote> votes = buffer.stream().map(ev -> new Vote(ev.day, getAgents()[ev.agtIdx], getAgents()[ev.tgtIdx])).collect(Collectors.toList());
                        updateAttackVote(votes);
                        continueAttackVote(true);
                        buffer.clear();
                    }
                    buffer.add(e);
                    break;
                case TALK:
                    if (e.turn != turn) {
                        continueTalk(true);
                        updateTalk(toTalks(buffer));
                    }
                    //ためる
                    buffer.add(e);
                    turn = e.turn;
                    break;
                case WHISPER:
                    if (e.turn != turn) {
                        continueWhisper(true);
                        updateWhisper(toTalks(buffer));
                    }
                    //ためる
                    buffer.add(e);
                    turn = e.turn;
                    break;
                case ATTACK:
                    attackedFlag = true;
                    updateVictim(e.tgtIdx);
                    if (e.atkSuccess) {
                        updateAttack(e.tgtIdx);
                    } else {
                        updateAttackFail();
                    }
                    break;
                case GUARD_TARGET:
                    continueGuard();
                    updateGuard(e.tgtIdx);
            }
            lastType = e.type;
        }
    }

    private List<Talk> toTalks(List<GameLogEvent> buffer) {
        List<Talk> talks = buffer.stream().map(ev -> {
            Talk talk = new Talk(ev.talkId, ev.day, ev.turn, getAgents()[ev.agtIdx], ev.talk.getText());
//            if (talk.getAgent().getAgentIdx() == getSelf().getAgentIdx()) {
//                GameLogEvent gle = getLastPlayerAction(EventType.TALK);
//                if (gle != null) talk = new Talk(ev.talkId, ev.day, ev.turn, getAgents()[ev.agtIdx], gle.talk.getText());
//            }
            return talk;
        }).collect(Collectors.toList());
        buffer.clear();
        return talks;
    }

    private List<Vote> toVote(List<GameLogEvent> buffer) {
        List<Vote> votes = buffer.stream().map(ev -> {
            Vote vote = new Vote(ev.day, getAgents()[ev.agtIdx], getAgents()[ev.tgtIdx]);
//            if (vote.getAgent().getAgentIdx() == getSelf().getAgentIdx()) {
//                GameLogEvent gle = getLastPlayerAction(EventType.VOTE);
//                if (gle != null) vote = new Vote(ev.day, getAgents()[ev.agtIdx], getAgents()[gle.tgtIdx]);
//            }
            return vote;
        }).collect(Collectors.toList());
        buffer.clear();
        return votes;
    }

}

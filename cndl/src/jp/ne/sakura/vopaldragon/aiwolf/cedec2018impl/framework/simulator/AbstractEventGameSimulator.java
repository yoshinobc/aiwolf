package jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.framework.simulator;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.framework.EventType;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.framework.Game;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.framework.GameAction;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.framework.GameAgent;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.framework.GameEvent;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.framework.GameTalk;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.framework.GameVote;
import org.aiwolf.client.lib.Content;
import org.aiwolf.client.lib.SkipContentBuilder;

import org.aiwolf.common.data.Role;
import org.aiwolf.common.data.Vote;

import org.aiwolf.common.data.Player;
import org.aiwolf.common.data.Species;
import org.aiwolf.common.data.Status;
import org.aiwolf.common.data.Talk;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.framework.GameEventListener;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.framework.GameBeforeActionListener;

public abstract class AbstractEventGameSimulator extends AbstractGameSimulator implements GameEventListener, GameBeforeActionListener {

    public AbstractEventGameSimulator(Player player) {
        super(player);
    }

    private Role masterRole = null;
    private List<Integer> wolves = new ArrayList<>();

    public void startGame(Role masterRole, int playerIdx, int villageSize, Map<Integer, Role> idxRoleMap) {
        this.masterRole = masterRole;
        if (idxRoleMap.get(playerIdx) == Role.WEREWOLF) {
            wolves = new ArrayList<>(idxRoleMap.keySet());
        }
        startGame(villageSize, playerIdx, idxRoleMap);
    }

    public void endGame(Game g) {
        Map<Integer, Role> idxRoleMap = new HashMap<>();
        Map<Integer, Status> idxStatusMap = new HashMap<>();
        for (GameAgent a : g.getAgents()) {
            idxRoleMap.put(a.agent.getAgentIdx(), a.role);
            idxStatusMap.put(a.agent.getAgentIdx(), a.isAlive ? Status.ALIVE : Status.DEAD);
        }
        finishGame(g.getDay(), idxRoleMap, idxStatusMap);
    }

    @Override
    public void handleBeforeAction(Game g, GameAction action) {
        switch (action) {
            case TALK:
                super.continueTalk(true);
                break;
            case VOTE:
                super.continueTalk(false);
                super.continueVote(true);
                break;
            case REVOTE:
                super.continueVote(true);
                break;
            case GUARD:
                super.continueVote(false);
                super.continueGuard();
                break;
            case DIVINE:
                super.continueVote(false);
                super.continueDivine();
                break;
            case WHISPER:
                super.continueVote(false);
                super.continueWhisper(true);
                break;
            case ATTACKVOTE:
                super.continueWhisper(false);
                super.continueAttackVote(true);
                break;
            case ATTACKREVOTE:
                super.continueAttackVote(true);
                break;
            case DAYSTART:
                if (masterRole != Role.SEER && getRole() == Role.SEER) {
                    super.continueDivine();
                    if (g.getDay() > 0) {
                        GameLogEvent ev = super.getLastPlayerAction(EventType.DIVINE);
                        updateDivine(g.getDay() - 1, ev.tgtIdx, predictSpiecies(ev.tgtIdx));
                    }
                }
                if (masterRole != Role.BODYGUARD && getRole() == Role.BODYGUARD && g.getDay() > 1) {
                    super.continueGuard();
                    GameLogEvent ev = super.getLastPlayerAction(EventType.GUARD_TARGET);
                    updateGuard(ev.tgtIdx);
                }
                if (masterRole != Role.WEREWOLF && getRole() == Role.WEREWOLF) {
                    List<Talk> dummyWhisper = new ArrayList<>();
                    int idx = 0;
                    for (Integer wolfIdx : wolves) {
                        if (g.getAgentAt(wolfIdx).isAlive)
                            dummyWhisper.add(new Talk(idx++, g.getDay(), 0, getAgents()[wolfIdx], new Content(new SkipContentBuilder()).getText()));
                    }
                    if (!dummyWhisper.isEmpty()) {
                        super.continueWhisper(true);
                        super.updateWhisper(dummyWhisper);
                    }
                    super.continueWhisper(false);
                    if (g.getDay() > 1) {
                        super.continueAttackVote(true);
                        List<Vote> dummyVote = new ArrayList<>();
                        for (Integer wolfIdx : wolves) {
                            if (g.getAgentAt(wolfIdx).isAlive)
                                dummyVote.add(new Vote(g.getDay(), getAgents()[wolfIdx], getAgents()[3]));
                        }
                        super.updateAttackVote(dummyVote);
                        super.continueAttackVote(false);
                        GameLogEvent gle = getLastPlayerAction(EventType.ATTACK_VOTE);
                        if (gle != null) {
                            updateVictim(gle.tgtIdx);
                        } else {
                            updateVictim(5);
                        }
                    }
                }
                if (g.getDay() > 0) {
                    continueDay();
                }
                break;
        }
    }

    protected abstract Species predictSpiecies(int agtIndex);

    @Override
    public void handleEvent(Game g, GameEvent e) {

        switch (e.type) {
            case DAYSTART:
                super.continueWhisper(false);
                super.continueVote(false);
                super.continueAttackVote(false);
                updateDay(e.day);
                break;
            case ATTACK:
                updateAttack(e.target.agent.getAgentIdx());
                break;
            case GUARD_SUCCESS:
                updateAttackFail();
                break;
            case ATTACK_VOTE:
                updateAttackVote(toVote(e.votes));
                break;
            case DIVINE:
                updateDivine(e.day, e.target.agent.getAgentIdx(), e.species);
                break;
            case EXECUTE:
                updateExecute(e.day, e.target.agent.getAgentIdx());
                if (masterRole != Role.MEDIUM && getRole() == Role.MEDIUM) {
                    updateMedium(e.day, e.target.agent.getAgentIdx(), predictSpiecies(e.target.agent.getAgentIdx()));
                }
                break;
            case GUARD_TARGET:
                updateGuard(e.target.agent.getAgentIdx());
                break;
            case MEDIUM:
                updateMedium(e.day, e.target.agent.getAgentIdx(), e.species);
                break;
            case TALK:
                updateTalk(toTalks(e.talks));
                break;
            case VICTIM_DECIDED:
                updateVictim(e.target.agent.getAgentIdx());
                break;
            case VOTE:
                updateVote(toVote(e.votes));
                break;
            case WHISPER:
                updateWhisper(toTalks(e.talks));
                break;
        }
    }

    private List<Talk> toTalks(List<GameTalk> list) {
        return list.stream().map(ev -> {
            //自分の発言内容に上書き
            if (ev.getTalker().agent.getAgentIdx() == getSelf().getAgentIdx() && getLastPlayerAction(EventType.TALK) != null) {
                GameLogEvent gev = getLastPlayerAction(EventType.TALK);
                return new Talk(ev.getId(), ev.getDay(), ev.getTurn(), getAgents()[ev.getTalker().agent.getAgentIdx()], gev.talk.getText());
            }
            return new Talk(ev.getId(), ev.getDay(), ev.getTurn(), getAgents()[ev.getTalker().agent.getAgentIdx()], ev.talkContent());
        }).collect(Collectors.toList());
    }

    private List<Vote> toVote(List<GameVote> list) {
        return list.stream().map(ev -> new Vote(ev.day, getAgents()[ev.initiator.agent.getAgentIdx()], getAgents()[ev.target.agent.getAgentIdx()])).collect(Collectors.toList());
    }
}

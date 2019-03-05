package jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.evalmodel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.CndlStrategy;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.framework.EventType;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.framework.Game;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.framework.GameAction;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.framework.GameAfterActionListener;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.framework.GameAgent;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.framework.GameEvent;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.framework.GameEventListener;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.framework.GameVote;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.framework.MetagameEventListener;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.target.CndlTargetTactic;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.util.HashCounter;
import org.aiwolf.common.data.Role;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.framework.GameBeforeActionListener;
import org.aiwolf.client.lib.Content;

public class VoteEvalModel implements MetagameEventListener, GameBeforeActionListener, GameAfterActionListener, GameEventListener {

    private CndlStrategy strategy;

    public VoteEvalModel(CndlStrategy strategy) {
        this.strategy = strategy;
        strategy.addMetagameEventListener(this);
        strategy.addPersistentBeforeActionListener(this);
        strategy.addPersistentAfterActionListener(this);
        strategy.addPersistentEventListener(this);
    }

    @Override
    public void handleEvent(Game g, GameEvent e) {
        if (e.type == EventType.VOTE) {
            for (GameVote gv : e.votes) {
                if (gv.isReVote) continue;
                if (gv.initiator.isSelf) {
                    Vote v = new Vote();
                    v.name = "event-vote";
                    v.game = g.getGameId();
                    v.voteIndex = gv.target.getIndex();
                    v.day = gv.day;
                    v.isRevote = false;
                    v.myRole = g.getSelf().role;
                    tempLis.add(v);
                }
            }
        }
    }

    List<Vote> tempLis = new ArrayList<>();

    @Override
    public void handleBeforeAction(Game g, GameAction action) {
        if (action == GameAction.VOTE) {
            if (g.getSelf().isAlive) {
                for (Entry<String, CndlTargetTactic> ctt : voteTactic.entrySet()) {
                    Vote v = new Vote();
                    v.name = ctt.getKey();
                    v.game = g.getGameId();
                    v.voteIndex = ctt.getValue().target(strategy).getIndex();
                    v.day = g.getDay();
                    v.isRevote = false;
                    v.myRole = g.getSelf().role;
                    tempLis.add(v);
                }
            }
        }
    }

    @Override
    public void handleAfterAction(Game g, GameAction action, Content talk, GameAgent target) {
        if (action == GameAction.VOTE) {
            if (g.getSelf().isAlive) {
                Vote v = new Vote();
                v.name = "actual-vote";
                v.game = g.getGameId();
                v.voteIndex = target.getIndex();
                v.day = g.getDay();
                v.isRevote = false;
                v.myRole = g.getSelf().role;
                tempLis.add(v);
            }
        }
    }

    @Override
    public void startGame(Game g) {
        tempLis = new ArrayList<>();
//        put("VoteWolf", new VoteWolf());
    }

    private Map<String, CndlTargetTactic> voteTactic = new LinkedHashMap<>();

    public void put(String key, CndlTargetTactic tactic) {
        voteTactic.put(key, tactic);
    }

    @Override
    public void endGame(Game g) {
        for (Vote v : tempLis) {
            //System.out.println("====" + v.name + "====");
            //System.out.println(g.getGameId() + "-" + v.day);
            List<GameEvent> voteEvs = g.getEventAtDay(EventType.VOTE, v.day);
            if (voteEvs.isEmpty()) {
//                System.out.println("Empty\t"+v.name+"\t"+v.day);
//                System.out.println(g.getEventsOf(EventType.VOTE));
                continue;
            }
            GameEvent voteEv = voteEvs.get(0);
            
//            System.out.println(v.day+" "+v.name);
//            System.out.println(voteEv);
            HashCounter<GameAgent> voteCount = new HashCounter<>();
            for (GameVote gv : voteEv.votes) {
                if (!gv.initiator.isSelf) voteCount.countPlus(gv.target);
            }
            if (!g.getEventAtDay(EventType.EXECUTE, voteEv.day).isEmpty()) {
                GameAgent myVote = g.getAgentAt(v.voteIndex);
                v.myRole = g.getSelf().role;
                v.voteNum = voteEv.votes.size();
                v.voteRole = myVote.role;
                GameAgent exe = g.getEventAtDay(EventType.EXECUTE, voteEv.day).get(0).target;
                v.executeIndex = exe.getIndex();
                v.executeRole = exe.role;
                v.executedVote = v.voteIndex == v.executeIndex;
                List<HashCounter<GameAgent>.Count> counts = voteCount.getCountList();
                Collections.sort(counts, Collections.reverseOrder());

                if (voteCount.topCounts().size() >= 2) v.becomeRevote = true;

                //System.out.println("Count:" + counts);
                v.wolfVote = killRoleVote(myVote, counts, Role.WEREWOLF);
                //System.out.println(v.wolfVote);
                v.spVote = killRoleVote(myVote, counts, Role.SEER, Role.BODYGUARD, Role.MEDIUM);
                //System.out.println(v.spVote);
                v.madVote = killRoleVote(myVote, counts, Role.POSSESSED);
                //System.out.println(v.madVote);
                v.surviveVote = surviveVote(myVote, counts);
                //System.out.println(v.surviveVote);
                voteList.add(v);

//                ListMap<GameAgent, GameAgent> votedBy = new ListMap<>();
//                for (GameVote gv : voteEvs.get(0).votes) {
//                    votedBy.add(gv.target, gv.initiator);
//                }
                //System.out.println(votedBy);
                //System.out.println("Executed:" + exe);
            }
        }
    }

    public static enum VoteEval {
        NO_CHOICE, HIT, MISS
    }

    private VoteEval killRoleVote(GameAgent voted, List<HashCounter<GameAgent>.Count> counts, Role... roles) {
        Set<GameAgent> result = new HashSet<>();
        int maxVoteCount = counts.get(0).getCount();
        Set<Role> tgtRoles = new HashSet<>(Arrays.asList(roles));
        //System.out.println("Kill-" + tgtRoles + " top=" + maxVoteCount);
        for (int i = 0; i < counts.size(); i++) {
            HashCounter<GameAgent>.Count count = counts.get(i);
            if (count.getCount() + 1 >= maxVoteCount && tgtRoles.contains(count.getKey().role)) {
                result.add(count.getKey());
            }
        }
        //System.out.println(result);
        if (result.isEmpty()) return VoteEval.NO_CHOICE;
        return result.contains(voted) ? VoteEval.HIT : VoteEval.MISS;
    }

    private VoteEval surviveVote(GameAgent voted, List<HashCounter<GameAgent>.Count> counts) {
        Set<GameAgent> result = new HashSet<>();
        int maxVoteCount = counts.get(0).getCount();
        //System.out.println("Survive top=" + maxVoteCount);
        boolean iAmInDanger = false;
        for (int i = 0; i < counts.size(); i++) {
            HashCounter<GameAgent>.Count count = counts.get(i);
            if (count.getKey().isSelf) {
                if (count.getCount() != maxVoteCount) return VoteEval.NO_CHOICE;
                iAmInDanger = true;
            } else if (count.getCount() + 1 >= maxVoteCount) {
                result.add(count.getKey());
            }
        }
        if (!iAmInDanger) return VoteEval.NO_CHOICE;
        if (result.isEmpty()) return VoteEval.NO_CHOICE;
        return result.contains(voted) ? VoteEval.HIT : VoteEval.MISS;
    }

    private List<Vote> voteList = new ArrayList<>();

    public List<Vote> getVoteList() {
        return voteList;
    }

    public static class Vote {

        public String name;
        public String game;
        public int day;
        public int voteNum;
        public Role myRole;
        public boolean isRevote;
        public int voteIndex;
        public int executeIndex;
        public Role voteRole;
        public Role executeRole;
        public boolean executedVote;
        public boolean becomeRevote;
        public VoteEval wolfVote;
        public VoteEval spVote;
        public VoteEval madVote;
        public VoteEval surviveVote;
    }

}

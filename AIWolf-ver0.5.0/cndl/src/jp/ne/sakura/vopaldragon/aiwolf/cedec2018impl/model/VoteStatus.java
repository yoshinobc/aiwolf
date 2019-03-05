package jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.framework.GameAgent;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.util.HashCounter;

public class VoteStatus {

    public Map<GameAgent, GameAgent> whoVoteWhoMap = new LinkedHashMap<>();

    public VoteStatus copy() {
        VoteStatus newvs = new VoteStatus();
        newvs.whoVoteWhoMap.putAll(this.whoVoteWhoMap);
        return newvs;
    }

    public void set(GameAgent voter, GameAgent target) {
        whoVoteWhoMap.put(voter, target);
    }

    public HashCounter<GameAgent> getVoteCount() {
        HashCounter<GameAgent> counter = new HashCounter<>();
        whoVoteWhoMap.values().forEach((ag) -> counter.countPlus(ag));
        return counter;
    }

    public HashCounter<GameAgent> getVoteCountOfOthers() {
        HashCounter<GameAgent> counter = new HashCounter<>();
        whoVoteWhoMap.values().forEach((ag) -> {
            if (!ag.isSelf) counter.countPlus(ag);
        });
        return counter;
    }

    public HashCounter<GameAgent> getVoteCountOfOthers(GameAgent agent) {
        HashCounter<GameAgent> counter = new HashCounter<>();
        whoVoteWhoMap.values().forEach((ag) -> {
            if (ag != agent) counter.countPlus(ag);
        });
        return counter;
    }

    public List<GameAgent> voterFor(GameAgent target) {
        List<GameAgent> list = new ArrayList<>();
        whoVoteWhoMap.forEach((from, to) -> {
            if (to == target) list.add(from);
        });
        return list;
    }

    public static class VoteGroup implements Comparable<VoteGroup> {

        public VoteGroup(GameAgent target, double evilScore) {
            this.target = target;
            this.score = evilScore;
        }

        @Override
        public String toString() {
            return "Vg{" + target + "=" + score + '}';
        }
        public GameAgent target;
        public double score;

        @Override
        public int compareTo(VoteGroup o) {
            return Double.compare(o.score, score);
        }
    }

}

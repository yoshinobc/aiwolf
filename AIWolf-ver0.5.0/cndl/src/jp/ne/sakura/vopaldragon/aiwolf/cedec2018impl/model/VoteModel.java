package jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.model;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.CndlStrategy;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.framework.Game;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.framework.GameAgent;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.framework.GameEvent;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.framework.GameTalk;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.util.HashCounter;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.framework.GameEventListener;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.framework.MetagameEventListener;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.util.DataScore;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.util.HashDoubleMap;
import org.aiwolf.client.lib.Topic;
import org.aiwolf.common.data.Role;
import org.aiwolf.common.data.Team;

/**
 * 現在の投票宣言状態を表現するモデル
 */
public class VoteModel implements GameEventListener, MetagameEventListener {

    /**
     * 現在の自分の投票宣言対象
     */
    public GameAgent currentVoteTarget;

    /**
     * 現在のみんなの投票宣言状況
     */
    private VoteStatus voteAnnounce;
    private HashCounter<GameAgent> cumulativeVoteAnnounce;
    private VoteStatus vote;
    private VoteStatus revote;
    private CndlStrategy strategy;

    public VoteModel(CndlStrategy strategy) {
        this.strategy = strategy;
        strategy.addPersistentEventListener(this);
        strategy.addMetagameEventListener(this);
    }

    public VoteStatus getVoteDeclared() {
        return voteAnnounce;
    }

    public VoteStatus getVoteActual() {
        return vote;
    }

    public VoteStatus getVoteRevoted() {
        return revote;
    }

    public double[] voteScore() {
        HashCounter<GameAgent> count = voteAnnounce.getVoteCount();
        double[] score = new double[strategy.getGame().getVillageSize()];
        for (GameAgent ag : count.getKeyList()) {
            score[ag.getIndex()] = count.getCount(ag);
        }
        return score;
    }

    public static class VoteRecord {

        public void add(VoteRecord other) {
            this.revoteEqualVoteCount += other.revoteEqualVoteCount;
            this.revoteOther += other.revoteOther;
            this.revoteToMajorityCount += other.revoteToMajorityCount;
            this.totalReVoteCount += other.totalReVoteCount;
            this.totalVoteCount += other.totalVoteCount;
            this.voteOther += other.voteOther;
            this.voteToMajorityCount += other.voteToMajorityCount;
            this.voteToSecondMajorityCount += other.voteToSecondMajorityCount;
            this.voteToMajorityFoolCount += other.voteToMajorityFoolCount;
            this.voteToPlanedCount += other.voteToPlanedCount;
        }

        /**
         * VOTE合計
         */
        public double totalVoteCount;
        /**
         * 投票宣言どおりに投票した数
         */
        public int voteToPlanedCount;
        /**
         * 人気（各エージェント最後の宣言ベース）に投票した数
         */
        public int voteToMajorityCount;
        /**
         * 人気第二位（各エージェント最後の宣言ベース）に投票した数
         */
        public int voteToSecondMajorityCount;
        /**
         * 人気（全Vote宣言ベース）に投票した数
         */
        public int voteToMajorityFoolCount;
        /**
         * その他
         */
        public int voteOther;

        /**
         * REVOTE合計
         */
        public double totalReVoteCount;
        /**
         * VOTEと変えずに投票した数
         */
        public int revoteEqualVoteCount;
        /**
         * VOTEの人気先に投票した数
         */
        public int revoteToMajorityCount;
        /**
         * その他
         */
        public int revoteOther;
    }

    private Map<Integer, VoteRecord> cumulativeVoteRecord = new HashMap<>();
    private Map<GameAgent, VoteRecord> currentVoteRecord = new HashMap<>();
    
    public VoteRecord getVoteRecord(GameAgent ag){
        return cumulativeVoteRecord.getOrDefault(ag, new VoteRecord());
    }

    public HashCounter<GameAgent> expectedVote() {
//        if (true) 
        if (strategy.getNumberOfGame() == 1 && strategy.getGame().getDay() == 1)
            return getVoteDeclared().getVoteCountOfOthers();

        HashDoubleMap<GameAgent> dCounter = new HashDoubleMap();

        RoleProbabilityStruct rps = strategy.bestPredictor.getRoleProbabilityStruct(strategy.getGame());

        for (GameAgent ag : strategy.getGame().getAliveOthers()) {

            HashDoubleMap<GameAgent> agDCounter = new HashDoubleMap<>();

            HashCounter<GameAgent> voteCounter = getVoteDeclared().getVoteCountOfOthers(ag);
            List<GameAgent> topVote = voteCounter.topCounts();
            topVote.remove(ag);
            List<GameAgent> secondTopVote = voteCounter.topNCounts(2);
            secondTopVote.removeAll(topVote);
            secondTopVote.remove(ag);
            List<GameAgent> cumulativeTopVote = cumulativeVoteAnnounce.topCounts();
            cumulativeTopVote.remove(ag);
            GameAgent declaredVote = getVoteDeclared().whoVoteWhoMap.get(ag);
            List<GameAgent> other = strategy.getGame().getAlives();
            other.remove(ag);
            other.removeAll(topVote);
            other.removeAll(secondTopVote);
            other.remove(declaredVote);
//            System.err.println(Utils.join("vote-candidates", ag, declaredVote, topVote, secondTopVote, cumulativeTopVote, other));

            boolean sortAsc = rps.topRole(ag).data.getTeam() == Team.WEREWOLF;

            VoteRecord rec = new VoteRecord();
            rec.add(currentVoteRecord.getOrDefault(ag, new VoteRecord()));
            rec.add(cumulativeVoteRecord.getOrDefault(ag.getIndex(), new VoteRecord()));

            if (declaredVote != null) {
                dCounter.modifyValue(declaredVote, rec.voteToPlanedCount / rec.totalVoteCount);
                agDCounter.modifyValue(declaredVote, rec.voteToPlanedCount / rec.totalVoteCount);
            }

            DataScore<GameAgent> ctvTopWolf = rps.getAgnetProbabilityList(Role.WEREWOLF, cumulativeTopVote).sort(sortAsc).top();
            if (ctvTopWolf != null) {
                dCounter.modifyValue(ctvTopWolf.data, rec.voteToMajorityFoolCount / rec.totalVoteCount);
                agDCounter.modifyValue(ctvTopWolf.data, rec.voteToMajorityFoolCount / rec.totalVoteCount);
            }

            DataScore<GameAgent> topTopWolf = rps.getAgnetProbabilityList(Role.WEREWOLF, topVote).sort(sortAsc).top();
            if (topTopWolf != null) {
                dCounter.modifyValue(topTopWolf.data, rec.voteToMajorityCount / rec.totalVoteCount);
                agDCounter.modifyValue(topTopWolf.data, rec.voteToMajorityCount / rec.totalVoteCount);
            }

            DataScore<GameAgent> secondTopWolf = rps.getAgnetProbabilityList(Role.WEREWOLF, secondTopVote).sort(sortAsc).top();
            if (secondTopWolf != null) {
                dCounter.modifyValue(secondTopWolf.data, rec.voteToSecondMajorityCount / rec.totalVoteCount);
                agDCounter.modifyValue(secondTopWolf.data, rec.voteToSecondMajorityCount / rec.totalVoteCount);
            }

            DataScore<GameAgent> otherTopWolf = rps.getAgnetProbabilityList(Role.WEREWOLF, other).sort(sortAsc).top();
            if (otherTopWolf != null) {
                dCounter.modifyValue(otherTopWolf.data, rec.voteOther / rec.totalVoteCount);
                agDCounter.modifyValue(otherTopWolf.data, rec.voteOther / rec.totalVoteCount);
            }
//            System.err.println(Utils.join("pred-agent", Utils.AI_MAP.get(ag.getIndex()), ag, declaredVote, agDCounter.sort(false)));

        }
        HashCounter<GameAgent> result = new HashCounter<>();
        int total = strategy.getGame().getAliveSize() - 1;
        dCounter.sort(false);
//        System.err.println(Utils.join("vote-prediction", strategy.getGame().getSelf().role, dCounter));
        for (GameAgent ag : dCounter.getKeyList()) {
            int count = (int) Math.round(dCounter.getValue(ag));
            if (count > total) count = total;
            if (count == 0) count = 1;
            result.countValue(ag, count);
            total -= count;
            if (total == 0) break;
        }

        return result;
    }

    public HashCounter<GameAgent> expectedRevote() {
//       getVoteActual().getVoteCountOfOthers();

        HashDoubleMap<GameAgent> dCounter = new HashDoubleMap();

        RoleProbabilityStruct rps = strategy.bestPredictor.getRoleProbabilityStruct(strategy.getGame());

        for (GameAgent ag : strategy.getGame().getAliveOthers()) {

            HashDoubleMap<GameAgent> agDCounter = new HashDoubleMap<>();

            HashCounter<GameAgent> voteCounter = getVoteActual().getVoteCountOfOthers(ag);
            List<GameAgent> topVote = voteCounter.topCounts();
            topVote.remove(ag);
            List<GameAgent> secondTopVote = voteCounter.topNCounts(2);
            secondTopVote.removeAll(topVote);
            secondTopVote.remove(ag);
            GameAgent declaredVote = getVoteActual().whoVoteWhoMap.get(ag);
            List<GameAgent> other = strategy.getGame().getAlives();
            other.remove(ag);
            other.removeAll(topVote);
            other.removeAll(secondTopVote);
            other.remove(declaredVote);
//            System.err.println(Utils.join("vote-candidates", ag, declaredVote, topVote, secondTopVote, cumulativeTopVote, other));

            boolean sortAsc = rps.topRole(ag).data.getTeam() == Team.WEREWOLF;

            VoteRecord rec = new VoteRecord();
            rec.add(currentVoteRecord.getOrDefault(ag, new VoteRecord()));
            rec.add(cumulativeVoteRecord.getOrDefault(ag.getIndex(), new VoteRecord()));
            if (rec.totalReVoteCount == 0) continue;

            if (declaredVote != null) {
                dCounter.modifyValue(declaredVote, rec.revoteEqualVoteCount / rec.totalReVoteCount);
                agDCounter.modifyValue(declaredVote, rec.revoteEqualVoteCount / rec.totalReVoteCount);
            }

            DataScore<GameAgent> topTopWolf = rps.getAgnetProbabilityList(Role.WEREWOLF, topVote).sort(sortAsc).top();
            if (topTopWolf != null) {
                dCounter.modifyValue(topTopWolf.data, rec.revoteToMajorityCount / rec.totalReVoteCount);
                agDCounter.modifyValue(topTopWolf.data, rec.revoteToMajorityCount / rec.totalReVoteCount);
            }

            DataScore<GameAgent> otherTopWolf = rps.getAgnetProbabilityList(Role.WEREWOLF, other).sort(sortAsc).top();
            if (otherTopWolf != null) {
                dCounter.modifyValue(otherTopWolf.data, rec.revoteOther / rec.totalReVoteCount);
                agDCounter.modifyValue(otherTopWolf.data, rec.revoteOther / rec.totalReVoteCount);
            }
//            System.err.println(Utils.join("pred-agent", Utils.AI_MAP.get(ag.getIndex()), ag, declaredVote, agDCounter.sort(false)));

        }
        HashCounter<GameAgent> result = new HashCounter<>();
        int total = strategy.getGame().getAliveSize() - 1;
        dCounter.sort(false);
//        System.err.println(Utils.join("vote-prediction", strategy.getGame().getSelf().role, dCounter));
        for (GameAgent ag : dCounter.getKeyList()) {
            int count = (int) Math.round(dCounter.getValue(ag));
            if (count > total) count = total;
            if (count == 0) count = 1;
            result.countValue(ag, count);
            total -= count;
            if (total == 0) break;
        }

        return result;
    }

    @Override
    public void startGame(Game g) {
        //そのゲーム回中に使うVoteRecord
        currentVoteRecord = new LinkedHashMap<>();
        for (GameAgent ag : g.getAgents()) {
            currentVoteRecord.put(ag, new VoteRecord());
        }
        voteAnnounce = new VoteStatus();
        revote = new VoteStatus();
        vote = new VoteStatus();
        cumulativeVoteAnnounce = new HashCounter<>();
    }

    @Override
    public void endGame(Game g) {
        //累積VoteRecordへの記録
        if (cumulativeVoteRecord.isEmpty()) {
            for (GameAgent ag : g.getAgents()) {
                cumulativeVoteRecord.put(ag.getIndex(), new VoteRecord());
            }
        }
        currentVoteRecord.forEach((ag, rec) -> {
            cumulativeVoteRecord.get(ag.getIndex()).add(rec);
        });
    }

    private int lastDay = 0;

    @Override
    public void handleEvent(Game g, GameEvent e) {
        switch (e.type) {
            case DAYSTART:
                currentVoteTarget = null;
                break;
            case TALK:
                if (lastDay != e.day) {
                    lastDay = e.day;
                    voteAnnounce = new VoteStatus();
                    revote = new VoteStatus();
                    vote = new VoteStatus();
                    cumulativeVoteAnnounce = new HashCounter<>();
                }
                e.talks.forEach(t -> {
                    switch (t.getTopic()) {
                        case AGREE:
                            GameTalk tgtTalk = t.getTargetTalk();
                            if (tgtTalk != null && tgtTalk.getTopic() == Topic.VOTE) {
                                voteAnnounce.set(t.getTalker(), tgtTalk.getTarget());
                            }
                            break;
                        case VOTE:
                            //投票宣言
                            voteAnnounce.set(t.getTalker(), t.getTarget());
                            cumulativeVoteAnnounce.countPlus(t.getTarget());
                            break;
                    }
                });
                break;
            case VOTE:
                boolean isRevote = e.votes.get(0).isReVote;
                e.votes.forEach(v -> {
                    if (isRevote) revote.set(v.initiator, v.target);
                    else vote.set(v.initiator, v.target);
                });

                VoteStatus baseVote = isRevote ? vote : voteAnnounce;
                VoteStatus evalVote = isRevote ? revote : vote;

                for (GameAgent ag : evalVote.whoVoteWhoMap.keySet()) {
                    HashCounter<GameAgent> voteCounter = baseVote.getVoteCountOfOthers(ag);
                    List<GameAgent> topVote = voteCounter.topCounts();
                    List<GameAgent> secondTopVote = voteCounter.topNCounts(2);
                    secondTopVote.removeAll(topVote);
                    List<GameAgent> cumulativeTopVote = cumulativeVoteAnnounce.topCounts();
                    GameAgent lastVote = baseVote.whoVoteWhoMap.get(ag);
                    GameAgent actualVote = evalVote.whoVoteWhoMap.get(ag);

                    VoteRecord record = currentVoteRecord.get(ag);
                    if (isRevote) {
                        record.totalReVoteCount++;
                        if (lastVote == actualVote) {
                            record.revoteEqualVoteCount++;
                        } else if (topVote.contains(actualVote)) {
                            record.revoteToMajorityCount++;
                        } else {
                            record.revoteOther++;
                        }
//                        System.err.println(Utils.join("revote-type", record.totalReVoteCount, record.revoteToMajorityCount, record.revoteEqualVoteCount, record.revoteOther));
                    } else {
                        record.totalVoteCount++;
                        if (lastVote == actualVote) {
                            record.voteToPlanedCount++;
                        } else if (cumulativeTopVote.contains(actualVote) && !topVote.contains(actualVote)) {
                            record.voteToMajorityFoolCount++;
                        } else if (topVote.contains(actualVote)) {
                            record.voteToMajorityCount++;
                        } else if (secondTopVote.contains(actualVote)) {
                            record.voteToSecondMajorityCount++;
                        } else record.voteOther++;
//                        System.err.println(baseVote.getVoteCountOfOthers(ag));
//                        System.err.println(Utils.join("vote-type", ag.getIndex() + 1, record.totalVoteCount, record.voteToPlanedCount, record.voteToMajorityCount, record.voteToMajorityFoolCount, record.voteOther));
                    }
                }
                break;
        }
    }

}

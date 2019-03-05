package jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.talk;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.CndlStrategy;

import org.aiwolf.client.lib.ContentBuilder;
import org.aiwolf.client.lib.Topic;
import org.aiwolf.client.lib.VoteContentBuilder;
import org.aiwolf.common.data.Role;
import org.aiwolf.common.data.Species;

import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.framework.Game;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.framework.GameAgent;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.framework.GameTalk;

/**
 *
 * vote先をカウント最多得票先が人狼なら、他に誰も票をいれてない村人にvote宣言そうでなければ乗っかる。
 */
public class TalkVoteWhiteMajority extends CndlTalkTactic {

    @Override
    public ContentBuilder talkImpl(int turn, int skip, int utter, Game game, CndlStrategy strategy) {
        GameAgent me = game.getSelf();
        /* 自分のこれまでの投票宣言先のリスト */
        List<GameAgent> myVoteTargets = me.talkList.stream().filter(x -> x.getDay() == game.getDay() && x.getTopic() == Topic.VOTE).map(x -> x.getTarget()).collect(Collectors.toList());
        GameAgent myLastVote = null;
        if (!myVoteTargets.isEmpty()) {
            myLastVote = myVoteTargets.get(myVoteTargets.size() - 1);
        }
        Stream<GameTalk> todaysVoteStream = game.getAllTalks().filter(x -> x.getDay() == game.getDay() && x.getTopic() == Topic.VOTE);
        /* エージェント別最終投票宣言先 */
        Map<GameAgent, GameAgent> voteTo = new HashMap<>();
        todaysVoteStream.forEach(x -> voteTo.put(x.getTalker(), x.getTarget()));

        /* エージェント別投票宣言数のカウント（人狼からの票は無効票） */
        // REQUEST に関する処理は未実装
        Map<GameAgent, Integer> votes = new HashMap<>();
        for (GameAgent ga : game.getAlives()) {
            /* 2日目までは CO勢には投票しない */
            if (ga == game.getSelf() || (game.getDay() <= 2 && ga.coRole != null)) {
                continue;
            }
            votes.put(ga, 0);
        }
        voteTo.keySet().stream().filter(x -> votes.containsKey(voteTo.get(x))).forEach(x -> {
            GameAgent target = voteTo.get(x);
            votes.put(target, votes.get(target) + 1);
        });

        /* 裏切り者っぽい者 */
        GameAgent possessed = null;
        List<GameTalk> divineResult = game.getAllTalks().filter(x -> x.getTalker().role != Role.WEREWOLF && x.getTopic() == Topic.DIVINED).collect(Collectors.toList());
        for (GameTalk gameTalk : divineResult) {
            if ((gameTalk.getTarget().role == Role.WEREWOLF && gameTalk.getResult() == Species.HUMAN)
                || gameTalk.getTarget().role != Role.WEREWOLF && gameTalk.getResult() == Species.WEREWOLF) {
                possessed = gameTalk.getTalker();
            }
        }

        /* 最多得票者 & 第２位 */
        GameAgent tar = Collections.max(votes.keySet(), Comparator.comparing(x -> votes.get(x)));
        GameAgent tar2nd = null;
        for (GameAgent ga : votes.keySet()) {
            if (ga != tar && (tar2nd == null || votes.get(tar2nd) < votes.get(ga))) {
                tar2nd = ga;
            }
        }
        /* 最多得票が人狼陣営の場合 */
        if (tar.role == Role.WEREWOLF || (possessed != null && tar == possessed)) {
            if (game.getAlives().stream().filter(x -> x.role == Role.WEREWOLF).count() < 3) {
                /* 残り人狼人数が少ないなら 最多得票村人に投票 */
                game.getAgents().stream().filter(x -> x.role == Role.WEREWOLF).forEach(x -> votes.remove(x));
                if (possessed != null) {
                    votes.remove(possessed);
                }
                tar = Collections.max(votes.keySet(), Comparator.comparing(x -> votes.get(x)));
            } else if ((turn <= 2 || (tar2nd != null && votes.get(tar) - votes.get(tar2nd) <= 1))
                && tar2nd.role != Role.WEREWOLF) {
                /* 私の一票で世界が変わるなら変える。 狂人には犠牲になって頂く */
                tar = tar2nd;
            }
        }
        if (tar == myLastVote) {
            return null;
        } else {
            return new VoteContentBuilder(tar.agent);
        }
    }

}

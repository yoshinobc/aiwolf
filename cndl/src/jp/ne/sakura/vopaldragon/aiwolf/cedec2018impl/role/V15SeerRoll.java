package jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.role;

import java.util.HashSet;
import java.util.Set;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.CndlStrategy;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.framework.GameAgent;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.talk.TalkCo;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.talk.TalkDivineWithEvilScore;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.talk.TalkVoteCurrentTarget;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.framework.rolebase.Day;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.talk.TalkEstimate;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.talk.TalkVoteToWolf;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.target.DivineBasicAvoidSeer2;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.target.DivineCndlOrWinner;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.target.RevoteWolf;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.target.VoteWolf;
import org.aiwolf.common.data.Role;

public class V15SeerRoll extends CndlBaseRole {

    private Set<GameAgent> divined = new HashSet<>();

    public V15SeerRoll(CndlStrategy strategy) {
        super(strategy);
        //会話戦略
        //とりあえず初日の1ターン目にCO
        talkTactics.add(new TalkCo(Role.SEER), 10000, Day.on(1));
        //占い結果を最速でお伝えする
        talkTactics.add(new TalkDivineWithEvilScore(), 1000, Day.any());
        //占いで黒を発見したらVote宣言
        talkTactics.add(new TalkVoteCurrentTarget(), 1000, Day.any(), Repeat.ONCE);
        //周りの投票を見ながら、狼らしい人への投票宣言
        talkTactics.add(new TalkVoteToWolf(), 500, Day.any(), Repeat.MULTI);
        //Estimateしてみる
        talkTactics.add(new TalkEstimate(), (int) (Math.random() * 1000), Day.after(2), Repeat.ONCE);

        //投票戦略
        //その段階で最も狼らしい票を得ている人に投票
        voteTactics.add(new VoteWolf());

        //投票結果を踏まえて最も狼らしい票を得ている人に投票
        revoteTactics.add(new RevoteWolf());

        //占い戦術
        //0日目はCndlか勝率が高い人を占う
        divineTactics.add(new DivineCndlOrWinner(divined), Day.on(0));
        //1日目夜以降は、狼らしく、占いCOでないできればステルス目の人を占う
        divineTactics.add(new DivineBasicAvoidSeer2(divined), Day.after(1));
    }

}

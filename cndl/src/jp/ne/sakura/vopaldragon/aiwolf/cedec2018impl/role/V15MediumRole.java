package jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.role;

import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.CndlStrategy;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.talk.TalkCo;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.talk.TalkIdentifiedResult;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.framework.rolebase.Day;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.talk.TalkEstimate;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.talk.TalkVoteToWolf;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.target.RevoteWolf;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.target.VoteWolf;
import org.aiwolf.common.data.Role;

public class V15MediumRole extends CndlBaseRole {

    public V15MediumRole(CndlStrategy strategy) {
        super(strategy);
        //会話戦略
        //とりあえず初日の1ターン目にCO
        talkTactics.add(new TalkCo(Role.MEDIUM), 1500, Day.on(1));
        //二日目以降、霊媒結果を最速でお伝えする
        talkTactics.add(new TalkIdentifiedResult(), 1500, Day.after(2));
        //周りの投票を見ながら、狼らしい人への投票宣言
        talkTactics.add(new TalkVoteToWolf(), 500, Day.any(), Repeat.MULTI);
        //Estimateしてみる
        talkTactics.add(new TalkEstimate(), (int) (Math.random() * 1000), Day.after(2), Repeat.ONCE);

        //投票戦略
        //その段階で最も狼らしい票を得ている人に投票
        voteTactics.add(new VoteWolf());

        //投票結果を踏まえて最も狼らしい票を得ている人に投票
        revoteTactics.add(new RevoteWolf());

    }

}

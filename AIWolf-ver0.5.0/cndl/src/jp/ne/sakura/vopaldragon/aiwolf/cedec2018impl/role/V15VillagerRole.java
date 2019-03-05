package jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.role;

import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.CndlStrategy;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.framework.rolebase.Day;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.talk.TalkEstimate;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.talk.TalkVoteByAI;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.talk.TalkVoteToWolf;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.talk.TalkVoteToWolf1;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.target.RevoteWolf;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.target.VoteWolf;

public class V15VillagerRole extends CndlBaseRole {

    public V15VillagerRole(CndlStrategy strategy) {
        super(strategy);

        //会話戦略
        //初日最初は勝率の高いAIに投票宣言。ただし、その後必ずWolfらしいAIに言い直す（変わらなければ無言）。
        talkTactics.add(new TalkVoteByAI(), 1000, Day.on(1), Repeat.ONCE);
        talkTactics.add(new TalkVoteToWolf1(), 900, Day.on(1), Repeat.ONCE);
        //最も狼らしい人に投票宣言
        talkTactics.add(new TalkVoteToWolf1(), 750, Day.after(2), Repeat.ONCE);
        //周りの投票を見ながら、狼らしい人への投票宣言
        talkTactics.add(new TalkVoteToWolf(), 500, Day.any(), Repeat.MULTI);
        //Estimateしてみる。Estimateの優先順位はランダム
        talkTactics.add(new TalkEstimate(), (int) (Math.random() * 1000), Day.after(2), Repeat.ONCE);

        //投票戦略
        //その段階で最も狼らしい票を得ている人に投票
        voteTactics.add(new VoteWolf());

        //投票結果を踏まえて最も狼らしい票を得ている人に投票
        revoteTactics.add(new RevoteWolf());

    }

}

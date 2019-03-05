package jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.role;

import org.aiwolf.common.data.Role;

import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.CndlStrategy;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.framework.rolebase.Day;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.talk.TalkCOForPPbyPOSSESSED;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.talk.TalkCo;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.talk.TalkDivineBlackAttacktoSeerDay1;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.talk.TalkVote5PossessedDay2;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.talk.TalkVoteCurrentTarget;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.target.RevoteToLive5Possessed;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.target.VoteWithoutWolf;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.target.VoteWithoutWolfDay2;

public class V5PossessedRole extends CndlBaseRole {

    public V5PossessedRole(CndlStrategy strategy) {
        super(strategy);
        // 最初のターン、占いCO
        talkTactics.add(new TalkCo(Role.SEER), 10000, Day.on(1));
        // COの次ターン、対抗占いに黒。対抗COがない場合は狼ランキング最下位に黒。
        talkTactics.add(new TalkDivineBlackAttacktoSeerDay1(), 9000, Day.on(1));
        //黒判定を宣言したエージェントへVote宣言をする。
        talkTactics.add(new TalkVoteCurrentTarget(), 8000, Day.on(1));

        // 最終日狼CO
        talkTactics.add(new TalkCOForPPbyPOSSESSED(), 10000, Day.on(2));
        talkTactics.add(new TalkVote5PossessedDay2(), 8000, Day.on(2));
        //talkTactics.add(new TalkDivineBlackAttacktoSeerDay2(), 9000, Day.on(2));

        // 狼ランキングトップを避けて投票。
        voteTactics.add(new VoteWithoutWolf(), 9000, Day.on(1));
        revoteTactics.add(new RevoteToLive5Possessed(), 9000, Day.on(1));
        voteTactics.add(new VoteWithoutWolfDay2(), 9000, Day.on(2));
        revoteTactics.add(new VoteWithoutWolfDay2(), 9000, Day.on(2));

    }

}

package jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.role;

import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.CndlStrategy;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.framework.rolebase.Day;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.talk.TalkCO5WolfForPP;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.talk.TalkVote5WolfDay1ver2;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.talk.TalkVote5WolfDay2;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.target.AttackLastDay;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.target.AttackWithoutPOSSESSED;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.target.RevoteToMajority;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.target.RevoteWolf5Day2;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.target.VoteToMjaorityAvoidPos;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.target.VoteToMjaorityPreferSeer;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.target.VoteWolf5ByWolf;

public class V5WerewolfRole extends CndlBaseRole {

    public V5WerewolfRole(CndlStrategy strategy) {
        super(strategy);

        //初日、疑似村人行動
        VoteWolf5ByWolf voteWolf5ByWolf = new VoteWolf5ByWolf();
        talkTactics.add(new TalkVote5WolfDay1ver2(voteWolf5ByWolf), 9000, Day.on(1), Repeat.MULTI);
        voteTactics.add(new VoteToMjaorityAvoidPos(), Day.on(1));
        revoteTactics.add(new RevoteToMajority(), Day.on(1));

        //２日目狂人が生存していれば狼COを検討
        talkTactics.add(new TalkCO5WolfForPP(), 10000, Day.on(2));
        talkTactics.add(new TalkVote5WolfDay2(), 9000, Day.on(2), Repeat.MULTI);
        voteTactics.add(new VoteToMjaorityPreferSeer(), Day.on(2));
        revoteTactics.add(new RevoteWolf5Day2());

        //特定裏切者を避けて襲撃初日
        attackTactics.add(new AttackWithoutPOSSESSED(), Day.on(1));
        //もうなんでもいいから噛む。襲撃２日目
        attackTactics.add(new AttackLastDay(), Day.on(2));

    }
}

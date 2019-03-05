package jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.role;

import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.CndlStrategy;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.framework.rolebase.Day;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.talk.TalkCOWolfForPPbyVILLAGER;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.talk.TalkVote5VillDay1ver2;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.talk.TalkVote5VillDay2;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.target.RevoteWolf5;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.target.VoteAccordingToMyself;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.target.VoteWolf5;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.target.VoteWolf5Day2;

public class V5VillagerRole extends CndlBaseRole {

    public V5VillagerRole(CndlStrategy strategy) {
        super(strategy);
        //Vote戦略
        talkTactics.add(new TalkVote5VillDay1ver2(), 10000, Day.on(1), Repeat.MULTI);
        voteTactics.add(new VoteWolf5(), Day.on(1));
        revoteTactics.add(new RevoteWolf5(), Day.on(1));

        // 投票戦略
        //PP返し
        talkTactics.add(new TalkCOWolfForPPbyVILLAGER(), 10000, Day.on(2));
        talkTactics.add(new TalkVote5VillDay2(), 9000, Day.on(2), Repeat.MULTI);
        voteTactics.add(new VoteAccordingToMyself(), Day.on(2));
        revoteTactics.add(new VoteWolf5Day2(), Day.on(2));
    }
}

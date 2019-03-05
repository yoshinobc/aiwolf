package jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.role;

import java.util.HashSet;
import java.util.Set;

import org.aiwolf.common.data.Role;

import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.CndlStrategy;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.framework.GameAgent;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.framework.rolebase.Day;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.talk.TalkCOForPPbySEER;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.talk.TalkCo;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.talk.TalkDivineWithEvilScore5ver;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.talk.TalkVoteCurrentTarget;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.talk.TalkVoteFakePosWolf;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.target.DivineBasic;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.target.DivineCndlOrWinner;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.target.VoteWolf5bySeer;

public class V5SeerRoll extends CndlBaseRole {

    private Set<GameAgent> divined = new HashSet<>();

    public V5SeerRoll(CndlStrategy strategy) {
        super(strategy);

        //初日CO
        talkTactics.add(new TalkCo(Role.SEER), 10000, Day.on(1));
        //PP履歴を参照し、PP返しをする。
        talkTactics.add(new TalkCOForPPbySEER(), 10000, Day.on(2));

        //白判定時、自分のモデルを信じて黒として伝える。（自分が占いCO状態のときのみ発動）
        talkTactics.add(new TalkDivineWithEvilScore5ver(), 9000, Day.any());
        //黒出しした人に投票宣言。1回のみ。
        talkTactics.add(new TalkVoteCurrentTarget(), 8000, Day.any());
        //二日目にFakeCOしたときの投票行動
        talkTactics.add(new TalkVoteFakePosWolf(), 8000, Day.on(2));

        //0日目は勝率の高いAIを占う
        divineTactics.add(new DivineCndlOrWinner(divined), Day.on(0));
        //1日目以降はEvilScoreの高いエージェントを占う
        divineTactics.add(new DivineBasic(divined), Day.after(1));

        // もっとも狼らしいエージェントに投票
        voteTactics.add(new VoteWolf5bySeer(false));
        revoteTactics.add(new VoteWolf5bySeer(true));
    }

}

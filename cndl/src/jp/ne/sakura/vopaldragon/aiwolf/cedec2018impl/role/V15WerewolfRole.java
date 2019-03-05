package jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.role;

import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.talk.TalkVoteDivinedBlacks;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.talk.TalkVoteNotToWolf;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.talk.TalkVoteWhite1;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.target.RevoteNonWolf;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.CndlStrategy;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.framework.Game;

import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.talk.TalkVoteLastMaxHate;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.talk.TalkVoteWhiteMajority;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.talk.TalkVoteByAI;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.talk.WhisperAttackHateCount;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.talk.WhisperAttackNotSeerMedium;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.talk.WhisperAttackTopMetaAgent;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.talk.WhisperCOVillager;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.talk.WhisperEstimatePossessed;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.target.AttackAccordingToMyself;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.target.VoteAccordingToMyself;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.target.VoteNonWolf;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.framework.rolebase.Day;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.talk.TalkEstimateOfWerewolf;

public class V15WerewolfRole extends CndlBaseRole {

    public V15WerewolfRole(CndlStrategy strategy) {
        super(strategy);

        /* Whisper */
        // 0日目以降: [2] 村人CO宣言
        whisperTactics.add(new WhisperCOVillager(), 10000, Day.on(0));
        // TODO 0日目: 占い師を騙って人狼を占うという仲間がいたら、DISAGREE する。
        // 身内以外に黒出し / 身内に白出しした占い師がいたら estimate POSSESSED
        whisperTactics.add(new WhisperEstimatePossessed(), 9000, Day.any());
        if (strategy.findCndlModel != null) whisperTactics.add(new WhisperAttackTopMetaAgent(), 8500, Day.after(1));
        // TODO 1日目: メタ読みで狩人っぽい奴がいたら、 estimate 狩人
        // TODO 人狼陣営へのvote を行ってくるエージェントのヘイトをカウントしてattack宣言
        whisperTactics.add(new WhisperAttackHateCount(), 8000, Day.any());
        // 狩人 > 占い師・霊媒師CO以外から狩人率高い者へattack宣言
        whisperTactics.add(new WhisperAttackNotSeerMedium(), 7000, Day.any());

        /* Attck */
        // 基本的に宣言通りにAttack
        // 人狼が1人の時はWhisperが発生しないので、
        // これまで自分たちへのvoteカウントの大きかった生き物attack
        attackTactics.add(new AttackAccordingToMyself());
        reattackTactics.add(new AttackAccordingToMyself());


        /* Talk */
        /*  村人エージェントに似せる */
        // 初日最初は人狼以外からメタ勝率が低い奴を vote 対象と宣言
        // その後、人狼・CO以外にからランダムに投票先を選ぶ。同じのが選ばれたら無言。
        talkTactics.add(new TalkVoteByAI(), 1000, Day.on(1), Repeat.ONCE);
        talkTactics.add(new TalkVoteWhite1(), 900, Day.on(1), Repeat.ONCE);
        // 2日目以降1ターン目: それまでにDIVINED 黒を出された非COがいればVOTE宣言しておく
        talkTactics.add(new TalkVoteDivinedBlacks(), 800, Day.after(2), Repeat.ONCE);
        // 2日目以降1ターン目: 前日の投票情報を使ってVoteNonWolfと同じやり方でVote先を選ぶ
        talkTactics.add(new TalkVoteLastMaxHate(), 750, Day.after(2), Repeat.ONCE);
        // 2ターン目以降:
        /*
         * 身内以外のvote先をカウント。
         * 最多得票先が人狼なら、票が入っている村人にvote宣言、そうでなければ乗っかる。
		 */
        talkTactics.add(new TalkVoteNotToWolf(), 500, Day.any(), Repeat.MULTI);
        //Estimateしてみる。Estimateの優先順位はランダム
        talkTactics.add(new TalkEstimateOfWerewolf(), (int) (Math.random() * 1000), Day.after(2), Repeat.ONCE);

        /* Vote */
        // 最多投票に乗っかりつつ、無理のない範囲で狼から票を外す
        voteTactics.add(new VoteNonWolf());
        revoteTactics.add(new RevoteNonWolf());
    }

}

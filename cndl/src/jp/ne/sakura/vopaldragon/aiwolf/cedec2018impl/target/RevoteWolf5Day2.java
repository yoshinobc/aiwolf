package jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.target;

import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.CndlStrategy;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.framework.Game;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.framework.GameAgent;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.model.VoteModel;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.model.VoteStatus;

/**
 * 狼二日目Revote、他の人の再投票行動の確率に応じて行動
 */
public class RevoteWolf5Day2 extends CndlTargetTactic {

    @Override
    public GameAgent targetImpl(Game game, CndlStrategy strategy) {
        VoteStatus vote = strategy.voteModel.getVoteActual();
        //Revoteが発生するのは1,1,1割れ。場合分けしていくと、2人とも投票を変更するタイプの場合、自分は変えない方が良い。そうで無い場合、自分は変えた方が良い。

        GameAgent ag1 = game.getAliveOthers().get(0);
        GameAgent ag2 = game.getAliveOthers().get(1);
        GameAgent myTarget = vote.whoVoteWhoMap.get(game.getSelf());
        GameAgent theOtherTtarget = ag1 == myTarget ? ag2 : ag1;

        VoteModel.VoteRecord vrAg1 = strategy.voteModel.getVoteRecord(ag1);
        double changeRateAg1 = vrAg1.totalReVoteCount == 0 ? 0 : 1 - vrAg1.revoteEqualVoteCount / vrAg1.totalReVoteCount;
        VoteModel.VoteRecord vrAg2 = strategy.voteModel.getVoteRecord(ag2);
        double changeRateAg2 = vrAg2.totalReVoteCount == 0 ? 0 : 1 - vrAg2.revoteEqualVoteCount / vrAg2.totalReVoteCount;

        if (changeRateAg1 > 0.5 && changeRateAg2 > 0.5) {
            return myTarget;
        } else {
            return theOtherTtarget;
        }

    }

}

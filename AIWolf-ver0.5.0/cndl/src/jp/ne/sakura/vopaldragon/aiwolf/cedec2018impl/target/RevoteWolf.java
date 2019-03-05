package jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.target;

import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.CndlStrategy;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.framework.Game;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.framework.GameAgent;

public class RevoteWolf  extends CndlTargetTactic {

  private  VoteWolf vw = new VoteWolf();
    
    @Override
    public GameAgent targetImpl(Game game, CndlStrategy strategy) {
        return vw.selectTarget(strategy.voteModel.getVoteActual().getVoteCountOfOthers(), game, strategy).data;
    }

}

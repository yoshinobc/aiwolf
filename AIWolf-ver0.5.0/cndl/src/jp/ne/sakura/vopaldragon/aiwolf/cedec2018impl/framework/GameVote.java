package jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.framework;

/**
 * 投票行動を示すクラス
 */
public class GameVote {

    public final int day;
    public final boolean isReVote;
    public final GameAgent initiator;
    public final GameAgent target;

    GameVote(int day, boolean isReVote, GameAgent initiator, GameAgent target) {
        this.day = day;
        this.isReVote = isReVote;
        this.initiator = initiator;
        this.target = target == null ? initiator : target;
        initiator.voteList.add(this);
    }

    @Override
    public String toString() {
        return initiator.getId() + ">" + target.getId() + (isReVote ? "!" : "");
    }

}

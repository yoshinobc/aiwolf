package jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.framework;

import java.util.List;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.util.Utils;
import org.aiwolf.common.data.Judge;
import org.aiwolf.common.data.Species;

/**
 * 発生するイベント情報を保持する構造体
 */
public class GameEvent {

    /**
     * イベント種別
     *
     * @see EventType
     */
    public final EventType type;
    /**
     * イベントの起きた日
     */
    public final int day;
    /**
     * イベントの対象エージェントを返す（ATTACK/EXECUTE/DIVINE/MEDIUM/VICTIM_DECIDED/GUARD;ATTACK/EXECUTE以外は当該役職以外の人には判らない）。
     */
    public final GameAgent target;
    /**
     * 判定結果（DIVINE/MEDIUM）。
     */
    public final Species species;
    /**
     * 投票内容（VOTE/ATTACK_VOTE）
     */
    public final List<GameVote> votes;
    /**
     * 会話内容（TALK/WHISPER）
     */
    public final List<GameTalk> talks;

    GameEvent(EventType type, int day) {
        this.type = type;
        this.day = day;
        this.talks = null;
        this.target = null;
        this.species = null;
        this.votes = null;
    }

    GameEvent(EventType type, int day, List<GameVote> votes) {
        this.type = type;
        this.day = day;
        this.votes = votes;
        this.talks = null;
        this.target = null;
        this.species = null;
    }

    GameEvent(EventType type, List<GameTalk> talks) {
        this.type = type;
        this.day = talks.get(0).getDay();
        this.talks = talks;
        this.votes = null;
        this.target = null;
        this.species = null;

    }

    GameEvent(EventType type, Game game, Judge judge) {
        this.type = type;
        this.day = judge.getDay() - 1;
        this.species = judge.getResult();
        this.target = game.toGameAgent(judge.getTarget());
        this.talks = null;
        this.votes = null;
    }

    GameEvent(EventType type, int day, GameAgent target) {
        this.type = type;
        this.day = day;
        this.target = target;
        this.talks = null;
        this.species = null;
        this.votes = null;
    }

    @Override
    public String toString() {
        return Utils.toString(this);
    }

}

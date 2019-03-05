package jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.framework.simulator;

import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.framework.EventType;
import org.aiwolf.client.lib.Content;
import org.aiwolf.common.data.Role;
import org.aiwolf.common.data.Status;

public class GameLogEvent {

    public int day;
    /**
     * 備考：DAYSTARTは現在のプレイヤー状態を示す
     */
    public EventType type;
    public int agtIdx;
    public int tgtIdx;
    public int talkId;
    public int turn;
    public Status status;
    public Content talk;
    public Role role;
    public boolean atkSuccess;

    @Override
    public String toString() {
        return "EventLog{" + "day=" + day + ", type=" + type + ", agtIdx=" + agtIdx + ", tgtIdx=" + tgtIdx + ", talkId=" + talkId + ", turn=" + turn + ", status=" + status + ", talk=" + talk + ", role=" + role + ", atkSuccess=" + atkSuccess + '}';
    }

}

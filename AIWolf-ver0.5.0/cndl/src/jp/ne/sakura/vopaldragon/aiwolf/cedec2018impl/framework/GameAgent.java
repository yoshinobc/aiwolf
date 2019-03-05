package jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.framework;

import java.util.ArrayList;
import java.util.List;
import org.aiwolf.common.data.Agent;
import org.aiwolf.common.data.Role;

/**
 * Agentのラッパ。1ゲーム中は同じインスタンスが使い回されるため「==」が有効。ゲームをまたぐと変わるため、MetagameModelでの利用にはIndexを使うこと。
 */
public class GameAgent {

    /**
     * 公式のエージェント
     */
    public Agent agent;
    /**
     * 自分か
     */
    public boolean isSelf;
    /**
     * 生存しているか
     */
    public boolean isAlive;
    /**
     * 襲撃されたか
     */
    public boolean isAttacked;
    /**
     * 追放されたか
     */
    public boolean isExecuted;

    /**
     * システム的に確定した役職。ゲーム終了時には全てのエージェントに値が入る。
     */
    public Role role;
    /**
     * COした役職。二回以上COしている場合、最新の値が入る。
     */
    public Role coRole;

    public List<GameVote> voteList = new ArrayList<>();
    public List<GameTalk> talkList = new ArrayList<>();
    public List<GameTalk> whisperList = new ArrayList<>();

    private Game game;

    GameAgent(Game game, Agent agent) {
        this.agent = agent;
        this.game = game;
    }

    public String getId() {
        return String.format("%02d", agent.getAgentIdx());
    }

    public int getIndex() {
        return agent.getAgentIdx() - 1;
    }

    public boolean hasCO() {
        return coRole != null;
    }

    @Override
    public String toString() {
        String s = String.format("%02d", agent.getAgentIdx())
            + (role == null ? "?" : role.name().substring(0, 1))
            + (isAlive ? " " : "*");
        if (isSelf) return "<" + s + ">";
        return s;
    }

}

package jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.evalmodel;

import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.CndlStrategy;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.framework.EventType;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.framework.Game;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.framework.GameAction;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.framework.GameAfterActionListener;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.framework.GameAgent;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.framework.GameEvent;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.framework.GameEventListener;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.framework.MetagameEventListener;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.util.ListMap;
import org.aiwolf.client.lib.Content;
import org.aiwolf.common.data.Role;

public class TargetEvalModel implements GameAfterActionListener, MetagameEventListener, GameEventListener {

    private CndlStrategy strategy;

    public TargetEvalModel(CndlStrategy strategy) {
        this.strategy = strategy;
        strategy.addPersistentAfterActionListener(this);
        strategy.addMetagameEventListener(this);
        strategy.addPersistentEventListener(this);
    }

    private ListMap<GameAction, TargetAction> actions;

    public ListMap<GameAction, TargetAction> getActions() {
        return actions;
    }

    @Override
    public void handleAfterAction(Game g, GameAction action, Content talk, GameAgent target) {
        switch (action) {
            case ATTACKVOTE:
            case DIVINE:
            case GUARD:
            case ATTACKREVOTE:
                actions.add(action, new TargetAction(g.getGameId(), g.getDay(), action, target.getIndex(), !target.isAlive));
        }
    }

    public double getGuardSuccessRate() {
        if (guardCount == 0) return -1;
        return guardSuccessCount / guardCount;
    }

    private double guardCount = 0;
    private double guardSuccessCount = 0;

    @Override
    public void handleEvent(Game g, GameEvent e) {
        if (e.type == EventType.GUARD_SUCCESS) {
            guardSuccessCount++;
        } else if (e.type == EventType.GUARD_TARGET) {
            guardCount++;
        }
    }

    @Override
    public void startGame(Game g) {
        actions = new ListMap<>();
        guardCount = 0;
        guardSuccessCount = 0;
    }

    @Override
    public void endGame(Game g) {
        for (TargetAction ta : actions.getAllValues()) {
            ta.toRole = g.getAgentAt(ta.toAgtIdx).role;
        }
    }

    public static class TargetAction {

        public String game;
        public int day;
        public GameAction type;
        public int toAgtIdx;
        public Role toRole;
        public boolean isDead;

        public TargetAction(String game, int day, GameAction type, int toAgtIdx, boolean isDead) {
            this.game = game;
            this.day = day;
            this.type = type;
            this.toAgtIdx = toAgtIdx;
            this.isDead = isDead;
        }

    }

}

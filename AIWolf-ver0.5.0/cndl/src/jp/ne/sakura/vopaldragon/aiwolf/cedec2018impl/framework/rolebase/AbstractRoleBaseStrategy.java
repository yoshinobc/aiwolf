package jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.framework.rolebase;

import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.framework.Game;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.framework.AbstractStrategy;
import org.aiwolf.client.lib.Content;
import org.aiwolf.common.data.Agent;

public abstract class AbstractRoleBaseStrategy extends AbstractStrategy{

    private RoleSelector roleSelector;
    public AbstractRoleBaseStrategy(RoleSelector roleSelector) {
        this.roleSelector = roleSelector;
    }

    
    private AbstractRole<? extends AbstractRoleBaseStrategy> currentRole;
    
    @Override
    public void startGameHook(Game g) {
        currentRole = roleSelector.selectRole(this);
        this.addEventListener(currentRole);
    }
    
    @Override
    public Agent attack() {
        return currentRole.attack();
    }

    @Override
    public Agent divine() {
        return currentRole.divine();
    }

    @Override
    public Agent guard() {
        return currentRole.guard();
    }

    @Override
    public Agent reattack() {
        return currentRole.reattack();
    }

    @Override
    public Agent revote() {
        return currentRole.revote();
    }

    @Override
    public Content talk(int turn) {
        return currentRole.talk(turn);
    }

    @Override
    public Agent vote() {
        return currentRole.vote();
    }

    @Override
    public Content whisper(int turn) {
        return currentRole.whisper(turn);
    }
     
    
}

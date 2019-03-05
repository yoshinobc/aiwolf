package jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.framework;

import org.aiwolf.client.lib.Content;

public interface GameAfterActionListener {

    void handleAfterAction(Game g, GameAction action, Content talk, GameAgent target);
}

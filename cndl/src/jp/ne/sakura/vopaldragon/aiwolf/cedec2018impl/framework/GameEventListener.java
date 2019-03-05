package jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.framework;

/**
 * 様々なGameEventを受け取るリスナー
 */
public interface GameEventListener {

    void handleEvent(Game g, GameEvent e);

}

package jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.framework;

/**
 * ゲーム終了と開始のイベントを受け取るリスナー
 */
public interface MetagameEventListener {

    /**
     * ゲーム終了時に1回呼ばれる。GameAgentでは全ての役職が判るようになっている。
     *
     * @param g
     */
   default void endGame(Game g){};

    /**
     * ゲーム開始時に1回呼ばれる。
     *
     * @param g
     */
  default void startGame(Game g){};
}

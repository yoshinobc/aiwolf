package jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.framework;

import java.util.ArrayList;
import java.util.List;
import org.aiwolf.client.lib.Content;
import org.aiwolf.common.data.Agent;

public abstract class AbstractStrategy {

    private int numberOfGame = 0;

    public AbstractStrategy() {
    }

    /**
     * 何回戦かを返す
     *
     * @return 現在の試合数
     */
    public int getNumberOfGame() {
        return numberOfGame;
    }

    private Game currentGame;

    /**
     * 現在のゲーム情報を返す。
     *
     * @return 現在のゲーム情報
     */
    public Game getGame() {
        return currentGame;
    }

    private List<MetagameEventListener> metagameListener = new ArrayList<>();

    /**
     * MetagameEventListenerを追加するとstartGameとendGameを受け取れるようになる。
     *
     * @param 追加するリスナー
     */
    public void addMetagameEventListener(MetagameEventListener mel) {
        metagameListener.add(mel);
    }

    void startGame(Game g) {
        numberOfGame++;
        currentGame = g;
        startGameHook(currentGame);
        for (MetagameEventListener mm : metagameListener) {
            mm.startGame(g);
        }
    }

    protected void startGameHook(Game g) {
    }

    void finishGame(Game g) {
        finishGameHook(g);
        for (MetagameEventListener mm : metagameListener) {
            mm.endGame(g);
        }
        //ゲーム単位のListenerの削除
        beforeActionListeners.clear();
        eventListenrs.clear();
    }

    protected void finishGameHook(Game g) {
    }

    private List<GameBeforeActionListener> beforeActionListeners = new ArrayList<>();
    private List<GameBeforeActionListener> persistentBeforeActionListeners = new ArrayList<>();

    /**
     *
     * 各アクションの実行前に呼び出されます。ゲーム毎にクリアされます。
     *
     * @param listener
     */
    public void addBeforeActionListener(GameBeforeActionListener listener) {
        beforeActionListeners.add(listener);
    }

    /**
     *
     * 各アクションの実行前に呼び出されます。ゲーム毎にクリアされません。
     *
     * @param listener
     */
    public void addPersistentBeforeActionListener(GameBeforeActionListener listener) {
        persistentBeforeActionListeners.add(listener);
    }

    void publishActionBefore(GameAction action) {
        for (GameBeforeActionListener listener : beforeActionListeners) {
            try {
                listener.handleBeforeAction(currentGame, action);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        for (GameBeforeActionListener listener : persistentBeforeActionListeners) {
            try {
                listener.handleBeforeAction(currentGame, action);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private List<GameAfterActionListener> afterActionListeners = new ArrayList<>();
    private List<GameAfterActionListener> persistentAfterActionListeners = new ArrayList<>();

    /**
     *
     * 各アクションの実行後に呼び出されます。ゲーム毎にクリアされます。
     *
     * @param listener
     */
    public void addAfterActionListener(GameAfterActionListener listener) {
        afterActionListeners.add(listener);
    }

    /**
     *
     * 各アクションの実行後に呼び出されます。ゲーム毎にクリアされません。
     *
     * @param listener
     */
    public void addPersistentAfterActionListener(GameAfterActionListener listener) {
        persistentAfterActionListeners.add(listener);
    }

    void publishActionAfter(GameAction action, Content talk, Agent agent) {
        for (GameAfterActionListener listener : afterActionListeners) {
            try {
                listener.handleAfterAction(currentGame, action, talk, getGame().toGameAgent(agent));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        for (GameAfterActionListener listener : persistentAfterActionListeners) {
            try {
                listener.handleAfterAction(currentGame, action, talk, getGame().toGameAgent(agent));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private List<GameEventListener> eventListenrs = new ArrayList<>();
    private List<GameEventListener> persistentEventListenrs = new ArrayList<>();

    /**
     *
     * イベント（情報の更新）時に呼び出されます。ゲーム毎にクリアされます。
     *
     */
    public void addEventListener(GameEventListener gel) {
        eventListenrs.add(gel);
    }

    /**
     *
     * イベント（情報の更新）時に呼び出されます。ゲーム毎にクリアされません。
     *
     */
    public void addPersistentEventListener(GameEventListener gel) {
        persistentEventListenrs.add(gel);
    }

    void publishEvent(GameEvent ev) {
        for (GameEventListener listenr : eventListenrs) {
            try {
                listenr.handleEvent(currentGame, ev);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        for (GameEventListener listenr : persistentEventListenrs) {
            try {
                listenr.handleEvent(currentGame, ev);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    /**
     * 会話の応答を返す
     *
     * @param turn 現在の会話ターン
     * @return
     */
    public abstract Content talk(int turn);

    public abstract Content whisper(int turn);

    public abstract Agent vote();

    public abstract Agent revote();

    public abstract Agent attack();

    public abstract Agent reattack();

    public abstract Agent divine();

    public abstract Agent guard();

}

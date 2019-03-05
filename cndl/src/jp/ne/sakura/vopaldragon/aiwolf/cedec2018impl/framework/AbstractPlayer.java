package jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.framework;

import static jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.util.Utils.*;

import org.aiwolf.common.data.Agent;
import org.aiwolf.common.data.Player;
import org.aiwolf.common.net.GameInfo;
import org.aiwolf.common.net.GameSetting;

import org.aiwolf.client.lib.Content;
import org.aiwolf.client.lib.SkipContentBuilder;

/**
 * Playerクラスの抽象実装。これを継承したクラスを作成し、Runnerのクラス名として指定するようにする。
 */
public abstract class AbstractPlayer<T extends AbstractStrategy> implements Player {

    private T strategy;

    public AbstractPlayer() {
        this.strategy = createStrategy();
    }

    protected abstract T createStrategy();

    public T getStrategy() {
        return strategy;
    }

    @Override
    public abstract String getName();

    private Game game;
    private boolean vote_flag = false;
    private boolean attack_vote_flag = false;
    private int talkTurn = 0;
    private int whisperTurn = 0;

    @Override
    public void initialize(GameInfo gi, GameSetting gs) {
        try {
            log("[INITIALIZE]");
            game = new Game(strategy, gi, gs);
            strategy.startGame(game);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void update(GameInfo gi) {
        try {
            log("[UPDATE]");
            game.updateWorld(gi);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void dayStart() {
        try {
            strategy.publishActionBefore(GameAction.DAYSTART);
            log("[DAYSTART]");
            game.dayStart();
            this.talkTurn = 0;
            this.whisperTurn = 0;
            this.vote_flag = false;
            this.attack_vote_flag = false;
            this.gameFinished = false;
            strategy.publishActionAfter(GameAction.DAYSTART, null, null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public String talk() {
        try {
            strategy.publishActionBefore(GameAction.TALK);
            Content msg = strategy.talk(talkTurn);
            log("[TALK]", talkTurn, msg.getText());
            talkTurn++;
            strategy.publishActionAfter(GameAction.TALK, msg, null);
            return msg.getText();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new Content(new SkipContentBuilder()).getText();
    }

    @Override
    public String whisper() {
        try {
            strategy.publishActionBefore(GameAction.WHISPER);
            Content msg = strategy.whisper(whisperTurn);
            log("[WHISPER]", whisperTurn, msg.getText());
            whisperTurn++;
            strategy.publishActionAfter(GameAction.WHISPER, msg, null);
            return msg.getText();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new Content(new SkipContentBuilder()).getText();
    }

    @Override
    public Agent vote() {
        try {
            Agent target;
            if (vote_flag) {
                strategy.publishActionBefore(GameAction.REVOTE);
                target = strategy.revote();
                game.notifyRevote();
                log("[REVOTE]", target);
                strategy.publishActionAfter(GameAction.REVOTE, null, target);
            } else {
                strategy.publishActionBefore(GameAction.VOTE);
                target = strategy.vote();
                vote_flag = true;
                log("[VOTE]", target);
                strategy.publishActionAfter(GameAction.VOTE, null, target);
            }
            return target;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public Agent attack() {
        try {
            Agent target;
            if (attack_vote_flag) {
                strategy.publishActionBefore(GameAction.ATTACKREVOTE);
                target = strategy.reattack();
                game.notifyAtkRevote();
                log("[REATTACK]", target);
                strategy.publishActionAfter(GameAction.ATTACKREVOTE, null, target);
            } else {
                strategy.publishActionBefore(GameAction.ATTACKVOTE);
                target = strategy.attack();
                attack_vote_flag = true;
                log("[ATTACK]", target);
                strategy.publishActionAfter(GameAction.ATTACKVOTE, null, target);
            }
            return target;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public Agent divine() {
        try {
            strategy.publishActionBefore(GameAction.DIVINE);
            Agent target = strategy.divine();
            log("[DIVINE]", target);
            strategy.publishActionAfter(GameAction.DIVINE, null, target);
            return target;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public Agent guard() {
        try {
            strategy.publishActionBefore(GameAction.GUARD);
            Agent target = strategy.guard();
            log("[GUARD]", target);
            strategy.publishActionAfter(GameAction.GUARD, null, target);
            return target;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private boolean gameFinished = false;

    @Override
    public void finish() {
        try {
            if (!gameFinished) {
                log("[FINISH]");
                this.game.finish();
                strategy.finishGame(this.game);
                gameFinished = true;
            } else {
                log("!!!Game has finished");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}

package jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.framework;

import java.util.List;
import org.aiwolf.client.lib.Content;
import org.aiwolf.client.lib.Operator;
import org.aiwolf.client.lib.SkipContentBuilder;
import org.aiwolf.client.lib.Topic;
import org.aiwolf.common.data.Role;
import org.aiwolf.common.data.Species;
import org.aiwolf.common.data.Talk;

/**
 * Talkのラッパクラス
 */
public class GameTalk {

    private Talk talk;
    private Game game;
    private Content content;
    private boolean isWhisper;
    public boolean isRepeat;

    GameTalk(Talk talk, Game game, boolean isWhisper) {
        this.talk = talk;
        this.game = game;
        content = new Content(talk.getText());

        //発話内容チェック
        boolean hasError = false;
        if (content.getTopic() == Topic.VOTE) {
            if (getTarget() == null || !getTarget().isAlive) hasError = true;
        } else if (content.getTopic() == Topic.AGREE || content.getTopic() == Topic.DISAGREE) {
            if (getTargetTalk() == null) hasError = true;
        } else if (content.getTopic() == Topic.DIVINED||content.getTopic() == Topic.IDENTIFIED) {
            if (getTarget() == null) hasError = true;
        }

        if (hasError) content = new Content(new SkipContentBuilder());

        if (isWhisper) game.toGameAgent(talk.getAgent()).whisperList.add(this);
        else game.toGameAgent(talk.getAgent()).talkList.add(this);
        this.isWhisper = isWhisper;
    }

    public boolean isWhisper() {
        return isWhisper;
    }

    public String talkContent() {
        return content.getText();
    }

    public String talkUniqueId() {
        return talk.getDay() + "_" + talk.getIdx();
    }

    @Override
    public String toString() {
        return getTalker().getId() + "=" + talk.getText() + "@" + (talk.getDay() + "/" + talk.getIdx()) + (content.getTalkID() != -1 ? "=>" + getTargetTalk() : "");
    }

    public Operator getOperator() {
        return content.getOperator();
    }

    /**
     * 発話の主語
     *
     * @return
     */
    public GameAgent getSubject() {
        return game.toGameAgent(content.getSubject());
    }

    /**
     * 発話の目的語（Agree/Disagreeを除く）
     *
     * @return
     */
    public GameAgent getTarget() {
        return game.toGameAgent(content.getTarget());
    }

    public Topic getTopic() {
        return content.getTopic();
    }

    /**
     * CO/Estimateの時の役割
     *
     * @return
     */
    public Role getRole() {
        return content.getRole();
    }

    /**
     * 占い・霊媒結果の時の判定
     *
     * @return
     */
    public Species getResult() {
        return content.getResult();
    }

    /**
     * AGREE/DISAGREEの対象となる発話
     */
    public GameTalk getTargetTalk() {
        if (isWhisper) return game.getWhisperById(content.getTalkDay(), content.getTalkID());
        else return game.getTalkById(content.getTalkDay(), content.getTalkID());
    }

    public List<Content> getContentList() {
        return content.getContentList();
    }

    /**
     * 発話のあった日
     */
    public int getDay() {
        return talk.getDay();
    }

    /**
     * 発話のあったターン
     */
    public int getTurn() {
        return talk.getTurn();
    }

    /**
     * 発話ID（1日ごとに0にリセットされるため、厳密にはIDではない）
     *
     * @return
     */
    public int getId() {
        return talk.getIdx();
    }

    /**
     * 発話者
     *
     * @return
     */
    public GameAgent getTalker() {
        return game.toGameAgent(talk.getAgent());
    }

    public boolean isSkip() {
        return talk.isSkip();
    }

    public boolean isOver() {
        return talk.isOver();
    }

}

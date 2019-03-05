package jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.evalmodel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.CndlStrategy;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.framework.Game;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.framework.GameAgent;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.framework.MetagameEventListener;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.util.DataScore;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.util.DataScoreList;
import org.aiwolf.common.data.Role;

public class WinEvalModel implements MetagameEventListener {

    public WinEvalModel(CndlStrategy strategy) {
        strategy.addMetagameEventListener(this);
    }

    public static class Win {

        public String game;
        public int agtIdx;
        public Role role;
        public Role myRole;
        public int finDay;
        public boolean win;

    }

    private double[] winCount;

    public double[] getWinCount() {
        return winCount;
    }

    public DataScoreList<GameAgent> getAgentWinCount(List<GameAgent> target) {
        return new DataScoreList<>(target.stream().map(tgt -> new DataScore<>(tgt, winCount[tgt.getIndex()])).collect(Collectors.toList())).sort();
    }

    public List<Win> winList = new ArrayList<>();
    public List<Win> lastWinList = new ArrayList<>();

    @Override
    public void startGame(Game g) {
        if (winCount == null) {
            winCount = new double[g.getVillageSize()];
            Arrays.fill(winCount, 0.0d);
        }
    }

    @Override
    public void endGame(Game g) {
        lastWinList = new ArrayList<>();
        for (GameAgent ag : g.getAgents()) {
            Win win = new Win();
            win.agtIdx = ag.getIndex();
            win.role = ag.role;
            win.myRole = g.getSelf().role;
            win.finDay = g.getDay();
            win.win = g.getWonTeam() == ag.role.getTeam();
            if (win.win) winCount[ag.getIndex()] += 1;
            winList.add(win);
            lastWinList.add(win);
        }
    }

}

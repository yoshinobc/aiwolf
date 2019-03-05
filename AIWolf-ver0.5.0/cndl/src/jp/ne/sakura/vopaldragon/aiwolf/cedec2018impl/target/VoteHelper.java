package jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.target;

import java.util.List;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.framework.GameAgent;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.model.RoleProbabilityStruct;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.util.HashCounter;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.util.ListMap;
import org.aiwolf.common.data.Role;

public class VoteHelper {

    static GameAgent decideVote(List<GameAgent> tgtList, List<GameAgent> avoidList, HashCounter<GameAgent> voteCount, RoleProbabilityStruct rps, Role baseRole, GameAgent fallBack) {

        int topCount = voteCount.topCount();
        ListMap<Integer, GameAgent> listMap = voteCount.getCounts();
        List<GameAgent> topcounts = listMap.getList(topCount);
        List<GameAgent> seconds = listMap.getList(topCount - 1);

        //候補がトップカウントかセカンドトップカウントかどうかを順に確認。
        for (GameAgent ag : tgtList) {
            if (ag != null && (topcounts.contains(ag) || seconds.contains(ag))) {
                return ag;
            }
        }

        //候補の場合、なるべく回避
        for (GameAgent ag : avoidList) {
            topcounts = listMap.getList(topCount);
            if (topcounts.contains(ag)) {
                topcounts.remove(ag);
                if (!topcounts.isEmpty()) {
                    return rps.topAgent(baseRole, topcounts).data;
                }
                if (!seconds.isEmpty()) {
                    return rps.topAgent(baseRole, seconds).data;
                }
            }
        }
        
        return fallBack;
    }

}

package jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.tools;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.CndlPlayer;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.framework.simulator.GameLog;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.framework.simulator.LogGameSimulator;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.tools.logs.LogReader;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.tools.logs.LogReaderYosen;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.util.Utils;

public class RunGameLog {

    public static void main(String[] args) throws Exception {
        Path base = Paths.get("分析", "予選ログ分析");
        Evaluator ev = new Evaluator(base);
        LogGameSimulator.OPTION_OPEN_ROLES = true;
        int count = 0;
        int villageSize = 5;
        LogReader reader = new LogReaderYosen(Paths.get("予選ログ", "zip"), Arrays.asList("723","726","731", "708", "709"));
//            LogReader reader = new LogReaderLocal(Arrays.asList(new File("log")));
//        LogReader reader = new LogReaderYosen(new File(Paths.get("..","Log2018").toAbsolutePath().toString()));
//        Set<Role> targetRoles = new HashSet<>(Arrays.asList(Role.VILLAGER, Role.SEER, Role.BODYGUARD, Role.MEDIUM));

        for (LogReader.LogSet ls : reader) {
            List<GameLog> logList = ls.logs;
            if (logList.get(0).roleOf("cndl") != null && logList.get(0).villageSize == villageSize) {
                CndlPlayer player = new CndlPlayer();
//            if (logList.get(0).roleOf("cndl") != null) {
                System.out.println(ls.name);
                LogGameSimulator glr = new LogGameSimulator(player);
                Map<Integer, String> aiMap = new HashMap<>();
                for (int i = 1; i < logList.get(0).agents.length; i++) {
                    aiMap.put(i - 1, logList.get(0).agents[i].ai);
                    Utils.AI_MAP.put(i - 1, logList.get(0).agents[i].ai);
                }
                for (GameLog log : logList) {
                    int plIdx = log.getAgtIdxOf("cndl");
//                    if (log.roleOf(plIdx) != Role.WEREWOLF) continue;
                    for (int i = 1; i <= log.villageSize; i++) {
                        Utils.ROLE_MAP.put(i - 1, log.roleOf(i));
                    }
                    glr.run(plIdx, log);
                    ev.evaluate(player, logList.get(0).villageSize, aiMap);
                }
                ev.finisheOneset(player);
//                if (count++ > 5)
//                    break;
            }
        }
        ev.printResult();
    }

}

package jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.tools;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.util.Utils;
import org.aiwolf.common.data.Agent;
import org.aiwolf.common.data.Player;
import org.aiwolf.common.data.Role;
import org.aiwolf.common.net.GameSetting;
import org.aiwolf.server.AIWolfGame;
import org.aiwolf.server.net.DirectConnectServer;
import org.aiwolf.server.util.GameLogger;

public class RunLocal {

    public static void main(String[] args) throws IOException {

        Evaluator ev = new Evaluator();

        int villageSize = 5;
        int SET_NUM = 50;
        int BATTLE_NUM = 100;
        Role cndlRole = null;
        boolean log = false;
        Path logDir = Paths.get("local-log", new SimpleDateFormat("yyMMddHHmmss").format(new Date()));

        for (int j = 0; j < SET_NUM; j++) {
            Utils.disableSout();
            List<Player> players = new ArrayList<>();
            jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.CndlPlayer cndlPlayer = new jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.CndlPlayer();
            for (int i = 0; i < 14; i++) {
                players.add(new org.aiwolf.sample.player.SampleRoleAssignPlayer());
            }
            if (villageSize == 5) {
                Collections.shuffle(players);
                players = players.subList(0, 4);
            }
            players.add(cndlPlayer);

            Collections.shuffle(players);

            Map<Integer, String> aiMap = new HashMap<>();
            for (int i = 0; i < players.size(); i++) {
                aiMap.put(i, players.get(i).getName());
                Utils.AI_MAP.put(i, players.get(i).getName());
            }

            Map<Player, Role> roleMap = new LinkedHashMap<>();
            for (Player pl : players) {
                if (pl.getName().equals("cndl")) roleMap.put(pl, cndlRole);
                else roleMap.put(pl, null);
            }

            GameSetting setting = GameSetting.getCustomGame(Paths.get("AIWolf-ver0.4.12", "SampleSetting.cfg").toAbsolutePath().toString(), villageSize);
            DirectConnectServer dcs = new DirectConnectServer(roleMap);
            for (int i = 0; i < BATTLE_NUM; i++) {
                final AIWolfGame aiWolfGame = new AIWolfGame(setting, dcs) {
                    @Override
                    protected void init() {
                        super.init();
                        for (Agent ag : getGameData().getAgentList()) {
                            Utils.ROLE_MAP.put(ag.getAgentIdx() - 1, getGameData().getRole(ag));
                        }
                    }
                };
                aiWolfGame.setShowConsoleLog(false);
                if (log) {
                    File logFile = logDir.resolve(String.format("%03d", j)).resolve(String.format("%03d.log", i)).toFile();
                    logFile.getParentFile().mkdirs();
                    aiWolfGame.setLogFile(logFile);
                } else {
                    aiWolfGame.setGameLogger(EMPTY_LOGGER);
                }
                aiWolfGame.start();

                Utils.enableSout();
                ev.evaluate(cndlPlayer, villageSize, aiMap);
                System.out.print(".");
                Utils.disableSout();
            }
            Utils.enableSout();
            System.out.println("");
            ev.finisheOneset(cndlPlayer);
        }
        ev.printResult();
    }

    private static GameLogger EMPTY_LOGGER = new GameLogger() {
        @Override
        public void log(String log) {
        }

        @Override
        public void flush() {
        }

        @Override
        public void close() {
        }
    };

}

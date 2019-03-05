package jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.tools;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.CndlPlayer;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.framework.GameAgent;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.framework.simulator.GameLog;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.framework.simulator.LogGameSimulator;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.tools.logs.LogReader;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.tools.logs.LogReaderYosen;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.util.HashCounter;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.util.ListMap;

public class WriteModelData {

    public static void main(String[] args) throws Exception {
//        write("gat", 5, 1, new LogReaderGAT());
        write("yosen", 15, -1, new LogReaderYosen());
//        write("cedec", 5, -1, new LogReaderCedec());
//        write("yosen-old", 15, -1, new LogReaderYosen(Paths.get("過去大会ログ", "c2017-yosen").toFile()));
    }

    public static void write(String fileName, int vSize, int plIdx, LogReader reader) {
        System.out.println(fileName);
        int count = 0;
        try {
            HashCounter<String> actFreq = new HashCounter<>();
            HashCounter<String> talkFreq = new HashCounter<>();
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(new File(fileName + "-at-" + vSize + ".txt")))) {
loop:           for (LogReader.LogSet ls : reader) {
                    List<GameLog> logList = ls.logs;
                    if (logList.get(0).villageSize == vSize) {
                        System.out.println(ls.name);
                        CndlPlayer player = new CndlPlayer();
                        LogGameSimulator glr = new LogGameSimulator(player);
                        for (GameLog log : logList) {
                            try {
                                if (plIdx == -1) plIdx = log.getAgtIdxOf("cndl");
                                if (plIdx == -1) continue loop;
                                glr.run(plIdx, log);
                                //act-text
                                ListMap<GameAgent, String> actions = player.getStrategy().actTextModel.events;
                                for (GameAgent key : actions.keySet()) {
                                    if (key.role != null) {
                                        writer.append("__label__" + key.role + " " + actions.get(key).stream().collect(Collectors.joining(" ")));
                                        writer.newLine();
                                    }
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                        //act-freq
                        HashCounter<String> evc = player.getStrategy().actFrequencyModel.evc;
                        for (String key : evc.getKeyList()) {
                            actFreq.countValue(key, evc.getCount(key));
                        }
                        //talk-freq
                        HashCounter<String> tkc = player.getStrategy().talkFrequencyModel.tkc;
                        for (String key : tkc.getKeyList()) {
                            talkFreq.countValue(key, tkc.getCount(key));
                        }
                        writer.flush();
                    }
                }
            }
            Files.write(Paths.get(fileName + "-af-" + vSize + ".txt"),
                actFreq.getKeyList().stream().map(k -> k + "\t" + actFreq.getCount(k)).collect(Collectors.toList()), StandardCharsets.UTF_8);
            Files.write(Paths.get(fileName + "-tf-" + vSize + ".txt"),
                talkFreq.getKeyList().stream().map(k -> k + "\t" + talkFreq.getCount(k)).collect(Collectors.toList()), StandardCharsets.UTF_8);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

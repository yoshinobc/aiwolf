package jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.tools.logs;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.framework.simulator.GameLog;

public class LogReaderLocal implements LogReader {

    private Iterator<File> logDir;

    public LogReaderLocal(List<File> logDir) {
        this.logDir = logDir.iterator();
    }

    private LogSet next;

    @Override
    public boolean hasNext() {
        if (!logDir.hasNext()) return false;
        File dir = logDir.next();
        List<GameLog> logList = new ArrayList<>();
        for (File log : dir.listFiles()) {
            try {
                List<String> lines = Files.readAllLines(log.toPath());
                String name = log.getName().replace(".log", "");
                GameLog log1 = new GameLog(name, lines);
                logList.add(log1);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        next = new LogSet();
        next.logs = logList;
        next.rev = "local";
        next.name = dir.getName();
        return true;
    }

    @Override
    public LogSet next() {
        return next;
    }

}

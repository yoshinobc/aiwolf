package jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.tools.logs;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.GZIPInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.framework.simulator.GameLog;

public class LogReaderCedec implements LogReader {

    private ZipInputStream zis;

    public LogReaderCedec() {
        try {
            zis = new ZipInputStream(new FileInputStream(Paths.get("過去大会ログ","cedec2017Log.zip").toFile()));
        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
        }
    }

    private LogSet next;

    private List<GameLog> logList = new ArrayList<>();
    private String lastName;

    @Override
    public boolean hasNext() {
        next = null;
        while (next == null) {

            try {
                ZipEntry entry = zis.getNextEntry();
                if (entry == null && logList.isEmpty()) return false;
                if (entry == null || (lastName != null && !entry.getName().split("/")[1].equals(lastName))) {
                    next = new LogSet();
                    next.name = lastName;
                    next.rev = "cedec2017";
                    next.logs = logList;
                    logList = new ArrayList<>();
                }
                if (entry != null) {
                    String[] name = entry.getName().split("/");
                    BufferedReader br = new BufferedReader(new InputStreamReader(new GZIPInputStream(zis)));
                    String line = null;
                    List<String> lines = new ArrayList<>();
                    while ((line = br.readLine()) != null) {
                        lines.add(line);
                    }
                    GameLog log = new GameLog(name[2].replace(".log.gz", ""), lines);
                    logList.add(log);
                    lastName = name[1];
                }
            } catch (IOException ex) {
            }
        }
        return true;
    }

    @Override
    public LogSet next() {
        return next;
    }
}

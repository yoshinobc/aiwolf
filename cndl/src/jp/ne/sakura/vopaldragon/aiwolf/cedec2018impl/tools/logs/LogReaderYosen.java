package jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.tools.logs;

import java.io.BufferedReader;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.zip.ZipFile;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.framework.simulator.GameLog;

public class LogReaderYosen implements LogReader {

    private Iterator<String> revisions;

    private File file;

    public LogReaderYosen(File file) {
        this.file = file;
        revisions = Arrays.stream(file.listFiles()).map(f -> f.getName()).collect(Collectors.toList()).iterator();
    }

    public LogReaderYosen() {
        this(new File(Paths.get("予選ログ", "zip").toAbsolutePath().toString()));
    }

    public LogReaderYosen(Path path, List<String> revName) {
        this(path.toFile());
        revisions = revName.iterator();
    }

    private LogSet next;
    private String rev;

    @Override
    public boolean hasNext() {

        next = null;
        while (next == null) {
            if (logZipFiles == null || !logZipFiles.hasNext()) {
                if (!revisions.hasNext()) return false;
                rev = revisions.next();
//                System.out.println("Next Rev " + rev);
                logZipFiles = Arrays.asList(new File(Paths.get(file.getAbsolutePath().toString(), rev).toAbsolutePath().toString()).listFiles(new FilenameFilter() {
                    @Override
                    public boolean accept(File dir, String name) {
                        return name.startsWith("gameLog");
                    }
                })).iterator();
            } else {
                try {
                    File zip = logZipFiles.next();
//                    System.out.println("Next Zip " + zip);

                    ZipFile zf = new ZipFile(zip);
                    if (zf.size() != 100 && zf.size() != 50 && zf.size() != 20) {
//                        System.out.println("Less than 100 " + zf.size() + "\t" + zip);
                        continue;
                    }

                    List<GameLog> logList = new ArrayList<>();
                    zf.stream().forEach(z -> {
                        if (!z.isDirectory()) {
                            String logName = z.getName().replace("game/", "").replace(".log", "");
                            try (BufferedReader reader = new BufferedReader(new InputStreamReader(zf.getInputStream(z)))) {
                                List<String> lines = reader.lines().collect(Collectors.toList());
                                GameLog log = new GameLog(logName, lines);
                                logList.add(log);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    });

                    next = new LogSet();
                    next.logs = logList;
                    next.name = zip.getName().replace(".zip", "").replace("gameLog", "");
                    next.rev = rev;
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        }
        return true;

    }

    private Iterator<File> logZipFiles;

    @Override
    public LogSet next() {
        return next;
    }

}

package jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.tools.logs;

import java.util.Iterator;
import java.util.List;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.framework.simulator.GameLog;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.tools.logs.LogReader.LogSet;

public interface LogReader extends Iterator<LogSet>,Iterable<LogSet> {

    @Override
    public default Iterator<LogSet> iterator() {
        return this;
    }

    
    
    
    public static class LogSet {

        public String rev;
        public String name;
        public List<GameLog> logs;

    }

}

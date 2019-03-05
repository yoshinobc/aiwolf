package jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.Stack;
import java.util.function.Function;
import java.util.stream.Collectors;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.framework.Game;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.framework.GameAgent;
import org.aiwolf.common.data.Role;

public class Utils {

    public static Map<Integer, Role> ROLE_MAP = new LinkedHashMap<>();
    public static Map<Integer, String> AI_MAP = new LinkedHashMap<>();

    public static Role[] EXISTING_ROLE = new Role[]{Role.VILLAGER, Role.SEER, Role.WEREWOLF, Role.MEDIUM, Role.BODYGUARD, Role.POSSESSED};
    public static Role[] EXISTING_ROLE5 = new Role[]{Role.VILLAGER, Role.SEER, Role.WEREWOLF, Role.POSSESSED};

    public static Role[] existingRole(Game game) {
        if (game.getVillageSize() == 15) return EXISTING_ROLE;
        return EXISTING_ROLE5;
    }

    /**
     * 最もスコアの高いものを取得する。複数同着の場合には全て返す
     */
    public static <E> List<E> getHighestScores(List<E> list, Function<E, Double> f) {
        double max = Double.NEGATIVE_INFINITY;
        List<E> result = new ArrayList<>();
        for (E e : list) {
            double score = f.apply(e);
            if (score > max) {
                max = score;
                result.clear();
                result.add(e);
            } else if (score == max) {
                result.add(e);
            }
        }
        return result;
    }

    static Random rand = new SecureRandom("seed".getBytes());

    public static <T extends Object> T getRandom(List<T> list) {
        if (list.isEmpty()) return null;
        else return list.get(rand.nextInt(list.size()));
    }

    private static PrintStream ps;
    private static PrintStream nullStream = new PrintStream(new OutputStream() {
        public void write(int b) throws IOException {
        }
    });

    public static void disableSout() {
        ps = System.out;
        System.setOut(nullStream);
    }

    public static void enableSout() {
        System.setOut(ps);
    }

    private static PrintStream logOut;

    public static void fileSout(File f) {
        try {
            logOut = new PrintStream(f, "UTF-8");
        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
        } catch (UnsupportedEncodingException ex) {
            ex.printStackTrace();
        }
    }

    public static void switchSout() {
        if (ps != null) {
            if (System.out == nullStream) {
                System.setOut(ps);
            } else {
                System.setOut(nullStream);
            }
        }
    }

    //TODO ここをfalseにすると、ログが出力される。
    private static final boolean IS_Production = true;

    static int index = 0;

    public static void log(Object... logs) {
        if (!IS_Production) {
            switchSout();
            StackTraceElement ste[] = Thread.currentThread().getStackTrace();
            String clazz = "UK";
            boolean isSim = false;
            if (ste.length > 2) {
                clazz = ste[2].getClassName().replaceAll("^.+\\.", "") + "-" + ste[2].getMethodName();
                for (StackTraceElement s : ste) {
                    if (s.getClassName().contains("EventGameSimulator")) {
                        isSim = true;
                        break;
                    }
                }
            }
            String log = clazz + "\t" + Arrays.stream(logs).map(o -> Objects.toString(o).replaceAll("\t", " ")).collect(Collectors.joining("\t"));
//            System.out.println("***LOG***\t" + log);
            if (isSim) {
//                simu.add(log);
                System.out.println("***SIM***\t" + (simu.size() - 1) + "\t" + log);
            } else {
//                    real.add(log);
                if (logOut != null) {
                    logOut.println("***LOG***\t" + (real.size() - 1) + "\t" + log);
                } else {
                    System.out.println("***LOG***\t" + (real.size() - 1) + "\t" + log);
                }
            }
//            if (simu.size() > index && real.size() > index) {
//                System.out.println("+++TEST+++\t" + index + "\t" + (simu.get(index).equals(real.get(index))) + "\t" + real.get(index).replaceAll("\t", " ") + "\t" + simu.get(index).replaceAll("\t", " "));
//                index++;
//            }
            switchSout();
        }
    }

    public static void printLogs() {
        switchSout();
        int max = Math.min(Utils.real.size(), Utils.simu.size());
        for (int i = 0; i < max; i++) {
            if (!Utils.real.get(i).equals(Utils.simu.get(i))) {
                System.out.print("***merge***\t" + i + "\t" + Utils.real.get(i).equals(Utils.simu.get(i)) + "\t" + Utils.real.get(i).replaceAll("\t", " "));
                System.out.println("\t" + Utils.simu.get(i).replaceAll("\t", " "));
            }
        }
        real.clear();
        simu.clear();
        switchSout();
    }

    public static List<String> simu = new ArrayList<>();
    public static List<String> real = new ArrayList<>();

    public static String toString(Object obj) {
        List<String> values = new ArrayList<>();
        for (Field f : obj.getClass().getDeclaredFields()) {
            try {
                f.setAccessible(true);
                Object val = f.get(obj);
                if (val != null) {
                    values.add(f.getName() + "=" + Objects.toString(val));
                }
            } catch (Exception e) {
            }
        }
        return obj.getClass().getSimpleName() + " {" + String.join(", ", values) + "}";
    }

    public static String join(Object... values) {
        return joinWith("\t", values);
    }

    public static String join(double... values) {
        return Arrays.stream(values).mapToObj(o -> Objects.toString(o)).collect(Collectors.joining("\t"));
    }

    public static String joinWith(String v, Object... values) {
        return Arrays.stream(values).map(o -> o instanceof Role ? Objects.toString(o).substring(0, 1) : Objects.toString(o)).collect(Collectors.joining(v));
    }

    public static void sortByScore(List<GameAgent> agents, double[] score, boolean asc) {
        agents.sort(Comparator.comparing((GameAgent ag) -> (asc ? 1 : -1) * score[ag.getIndex()]));
    }

    private static Stack<Time> times = new Stack();

    private static class Time {

        String loc;
        long time;
        String v;

        public Time(String loc, String v, long time) {
            this.v = v;
            this.loc = loc;
            this.time = time;
        }

    }

    private static final boolean TIME = false;

    public static void startStopwatch(String value) {
        if (TIME) {
            StackTraceElement ste[] = Thread.currentThread().getStackTrace();
            String clazz = "UK";
            boolean isSim = false;
            if (ste.length > 2) {
                clazz = ste[2].getClassName().replaceAll("^.+\\.", "") + "-" + ste[2].getMethodName() + "-" + ste[2].getLineNumber();
                for (StackTraceElement s : ste) {
                    if (s.getClassName().contains("GameSimulator")) {
                        isSim = true;
                        break;
                    }
                }
            }
            if (!isSim) times.push(new Time(clazz, value, System.currentTimeMillis()));
        }
    }

    public static void stopStopwatch() {
        if (TIME) {
            if (!times.isEmpty()) {
                Time time = times.pop();
                System.out.printf("***TIME***\t%s\t%s\t%s\n", time.loc, time.v, (System.currentTimeMillis() - time.time));
            }
        }
    }

}

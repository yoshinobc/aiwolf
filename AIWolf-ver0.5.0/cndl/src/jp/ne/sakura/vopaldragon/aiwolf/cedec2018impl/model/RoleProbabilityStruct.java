package jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.model;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.framework.Game;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.framework.GameAgent;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.util.DataScore;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.util.DataScoreList;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.util.Utils;
import org.aiwolf.common.data.Role;

public class RoleProbabilityStruct {

    public static void normalizeL1(Map<Role, Double> map) {
        double d = 0;
        for (double dd : map.values()) {
            d += dd;
        }
        if (d != 0) {
            double dsq = d;
            map.entrySet().forEach(es -> es.setValue(es.getValue() / dsq));
        }
    }

    public static void normalizeL2(Map<Role, Double> map) {
        double d = 0;
        for (double dd : map.values()) {
            d += dd*dd;
        }
        if (d != 0) {
            double dsq = Math.sqrt(d);
            map.entrySet().forEach(es -> es.setValue(es.getValue() / dsq));
        }
    }

    public void print(Game game) {
        StringBuilder sb = new StringBuilder();
        for (Role role : Utils.existingRole(game)) {
            sb.append("\t").append(role);
        }
        sb.append("\n");
        for (GameAgent ag : game.getAgents()) {
            sb.append(ag.getIndex());
            for (Role role : Utils.existingRole(game)) {
                sb.append("\t").append(String.format("%.3f", getScore(ag, role)));
            }
            sb.append("\n");
        }
        System.err.println(sb);
    }

    public Map<GameAgent, Map<Role, Double>> roleProbabilities = new HashMap<>();
    private Map<GameAgent, DataScoreList<Role>> roleProbabilityListCache = new HashMap<>();
    private Map<Role, DataScoreList<GameAgent>> agentProbabilityListCache = new HashMap<>();

    public void normalize(Game game) {
        normalizeInRole(game);
        normalizeInAgent();
    }

    public void normalizeInAgent() {
        roleProbabilities.values().forEach(v -> RoleProbabilityStruct.normalizeL1(v));
    }

    public void normalizeInRole(Game game) {
        Map<Role, Double> roleNum = game.getRoleNum();

        for (Role role : Utils.existingRole(game)) {
            double total = 0;
            for (GameAgent ag : game.getAgents()) {
                total += getScore(ag, role);
            }
            total = roleNum.get(role) / total;
            for (GameAgent ag : game.getAgents()) {
                setScore(ag, role, getScore(ag, role) * total);
            }
        }
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 19 * hash + Objects.hashCode(this.roleProbabilities);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        final RoleProbabilityStruct other = (RoleProbabilityStruct) obj;
        if (!Objects.equals(this.roleProbabilities, other.roleProbabilities)) return false;
        return true;
    }

    public double getScore(GameAgent agent, Role role) {
        return roleProbabilities.getOrDefault(agent, Collections.emptyMap()).getOrDefault(role, 0.0);
    }

    public void setScore(GameAgent agent, Role role, Double score) {
        roleProbabilities.get(agent).put(role, score);
        roleProbabilityListCache.remove(agent);
        agentProbabilityListCache.remove(role);
    }

    public DataScore<GameAgent> topAgent(Role role, Collection<GameAgent> candidates) {
        return getAgnetProbabilityList(role, candidates).get(0);
    }

    public DataScore<Role> topRole(GameAgent agent) {
        return getRoleProbabilityList(agent).get(0);
    }

    public DataScoreList<GameAgent> getAgnetProbabilityList(Role role) {
        DataScoreList<GameAgent> list = agentProbabilityListCache.get(role);
        if (list == null) {
            list = new DataScoreList<>();
            for (GameAgent ag : roleProbabilities.keySet()) {
                list.add(new DataScore<>(ag, roleProbabilities.get(ag).get(role)));
            }
            Collections.sort(list);
            agentProbabilityListCache.put(role, list);
        }
        return list;
    }

    public DataScoreList<GameAgent> getAgnetProbabilityList(Role role, Collection<GameAgent> subList) {
        return new DataScoreList<>(getAgnetProbabilityList(role).stream().filter(ds -> subList.contains(ds.data)).collect(Collectors.toList()));
    }

    public DataScoreList<Role> getRoleProbabilityList(GameAgent agent) {
        DataScoreList<Role> list = roleProbabilityListCache.get(agent);
        if (list == null) {
            list = new DataScoreList<>(roleProbabilities.get(agent).entrySet().stream().map(es -> new DataScore<>(es.getKey(), es.getValue())).sorted().collect(Collectors.toList()));
            roleProbabilityListCache.put(agent, list);
        }
        return list;
    }

}

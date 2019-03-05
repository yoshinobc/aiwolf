package jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.model;

import fasttext.FastText;
import fasttext.FastTextPrediction;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.CndlStrategy;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.framework.Game;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.framework.GameAgent;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.framework.GameEvent;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.framework.MetagameEventListener;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.util.ListMap;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.util.Utils;
import org.aiwolf.common.data.Role;
import org.aiwolf.common.data.Species;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.framework.GameEventListener;

/**
 * AIの行動を単語としてfastTextによるクラス分類を行うモデル
 */
public class ActTextModel2 implements GameEventListener, MetagameEventListener, RolePredictor {

    private CndlStrategy cndlStrategy;

    public ListMap<GameAgent, String> events;

    private Game game;
    private FastText model;
    private String name;

    public ActTextModel2(String name, CndlStrategy cndlStrategy) {
        this.cndlStrategy = cndlStrategy;
        this.name = name;
        cndlStrategy.addPersistentEventListener(this);
        cndlStrategy.addMetagameEventListener(this);
    }

    @Override
    public RoleProbabilityStruct getRoleProbabilityStruct(Game game) {
        RoleProbabilityStruct rps = new RoleProbabilityStruct();
        game.getAgentStream().forEach(ag -> rps.roleProbabilities.put(ag, getRoleProbability(ag)));
        return rps;
    }

    private Map<Role, Double> getRoleProbability(GameAgent agent) {
        List<FastTextPrediction> predictions = model.predictAll(events.getList(agent));
        Map<Role, Double> result = new HashMap<>();
        for (FastTextPrediction p : predictions) {
            try {
                Role role = Role.valueOf(p.label().replace("__label__", ""));
                if (getFixedProb(agent, role) != null) {
                    result.put(role, getFixedProb(agent, role));
                } else {
                    result.put(role, p.probability());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        RoleProbabilityStruct.normalizeL1(result);
        return result;
    }

    public void startGame(Game game) {
        if (model == null) {
            String filename = game.getVillageSize() == 15 ? name + "-at-15.bin" : name + "-at-5.bin";
            try {
                model = FastText.loadModel(ActTextModel2.class.getResourceAsStream(filename));
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }

        events = new ListMap<>();
        this.game = game;
        fixedRoleProbMap = new HashMap<>();
        switch (game.getSelf().role) {
            case SEER:
                setFixedProb(Role.SEER, 0.0);
                setFixedProb(game.getSelf(), Role.SEER, 1.0);
                break;
            case BODYGUARD:
                setFixedProb(Role.BODYGUARD, 0.0);
                setFixedProb(game.getSelf(), Role.BODYGUARD, 1.0);
                break;
            case MEDIUM:
                setFixedProb(Role.MEDIUM, 0.0);
                setFixedProb(game.getSelf(), Role.MEDIUM, 1.0);
                break;
            case POSSESSED:
                setFixedProb(Role.POSSESSED, 0.0);
                setFixedProb(game.getSelf(), Role.POSSESSED, 1.0);
                break;
            case WEREWOLF:
                setFixedProb(Role.WEREWOLF, 0.0);
                game.getAgentStream().filter(ag -> ag.role == Role.WEREWOLF).forEach(ag -> setFixedProb(ag, Role.WEREWOLF, 1.0));
                break;
            case VILLAGER:
                setFixedProb(game.getSelf(), Role.VILLAGER, 1.0);
        }
    }

    public void endGame(Game game) {
        try {
            model.close();
            model = null;
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private Double getFixedProb(GameAgent agent, Role role) {
        return fixedRoleProbMap.get(agent.getIndex() + "_" + role.name());
    }

    private void setFixedProb(GameAgent agent, Role role, Double p) {
        if (p == 1.0) {
            for (Role r : Utils.existingRole(game)) {
                fixedRoleProbMap.put(agent.getIndex() + "_" + r.name(), 0.0);
            }
        }
        fixedRoleProbMap.put(agent.getIndex() + "_" + role.name(), p);
    }

    private void setFixedProb(Role role, Double p) {
        for (int i = 0; i < 15; i++) {
            fixedRoleProbMap.put(i + "_" + role.name(), p);
        }
    }

    private Map<String, Double> fixedRoleProbMap = new HashMap<>();

    @Override
    public void handleEvent(Game g, GameEvent e) {
        switch (e.type) {
            case TALK:
                RoleProbabilityStruct rpsTalk = cndlStrategy.integratedModel.getRoleProbabilityStruct(g);
                e.talks.forEach(t -> {
                    if (t.isRepeat) return;
                    switch (t.getTopic()) {
                        case DIVINED:
                        case IDENTIFIED:
                        case OVER:
                        case AGREE:
                        case DISAGREE:
                        case GUARD:
                        case GUARDED:
                        case OPERATOR:
                        case DIVINATION:
                        case SKIP:
                            events.add(t.getTalker(), t.getTopic().name());
                            break;
                        case COMINGOUT:
                            events.add(t.getTalker(), "COMINGOUT_" + t.getRole());
                            break;
                        case VOTE:
                            GameAgent target = t.getTarget();
                            Role estRole = rpsTalk.topRole(target).data;
                            events.add(t.getTalker(), "DECLARE_VOTE_" + estRole);
                            break;
                        case ESTIMATE:
                            events.add(t.getTalker(), "ESTIMATE_" + t.getRole());
                            break;
                    }
                });
                break;
            case VOTE:
                RoleProbabilityStruct rpsVote = cndlStrategy.integratedModel.getRoleProbabilityStruct(g);
                e.votes.forEach(v -> {
                    GameAgent target = v.target;
                    Role estRole = rpsVote.topRole(target).data;
                    events.add(v.initiator, "VOTE_" + estRole);
                });
                break;
            case ATTACK:
                setFixedProb(e.target, Role.WEREWOLF, 0.0);
                break;
            case GUARD_SUCCESS:
                if (e.target != null) {
                    setFixedProb(e.target, Role.WEREWOLF, 0.0);
                }
                break;
            case DIVINE:
            case MEDIUM:
                if (e.species == Species.HUMAN) {
                    setFixedProb(e.target, Role.WEREWOLF, 0.0);
                } else {
                    setFixedProb(e.target, Role.WEREWOLF, 1.0);
                }
                break;
            default:
                break;
        }
    }

}

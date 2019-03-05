package jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl;

import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.evalmodel.HostilityEvalModel;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.evalmodel.PredictionEvalModel;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.evalmodel.TargetEvalModel;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.evalmodel.Village5TacEvalModel;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.evalmodel.VoteEvalModel;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.evalmodel.WinEvalModel;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.framework.Game;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.framework.rolebase.AbstractRoleBaseStrategy;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.model.ActFrequencyModel;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.evalmodel.VotePredictionEvalModel;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.model.ActTextModel2;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.model.AgentReliabilityModel;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.model.BelieveSeerModel;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.model.EnsembleRolePredictotrModel;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.model.EvilScoreModel;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.model.FindCndlModel;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.model.IntegratedModel;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.model.RolePredictor;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.model.TalkFrequencyModel;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.model.VoteModel;

public class CndlStrategy extends AbstractRoleBaseStrategy {

    //推測系
    public ActFrequencyModel actFrequencyModel;
    public TalkFrequencyModel talkFrequencyModel;
    public ActTextModel2 actTextModel;
    public EnsembleRolePredictotrModel ensembleRolePredictotrModel;
    public EnsembleRolePredictotrModel tfafModel;
    public IntegratedModel integratedModel;
    public RolePredictor bestPredictor;

    public FindCndlModel findCndlModel;
    public EvilScoreModel evilModel;
    public BelieveSeerModel believeSeerModel;
    public AgentReliabilityModel agentReliabilityModel;
    public PredictionEvalModel predictionEvalModel;

    //投票系
    public VoteModel voteModel;
    public VoteEvalModel voteEvalModel;
    public VotePredictionEvalModel votePredectionEvalModel;

    //その他
    public HostilityEvalModel hostilityEvalModel;
    public TargetEvalModel targetEvalModel;
    public WinEvalModel winEvalModel;
    public Village5TacEvalModel v5TacEvalModel;

    public CndlStrategy(boolean createObjectModel) {
        super(new RandomRoleSelector());

        //推測系
        actFrequencyModel = new ActFrequencyModel("old-yosen", this);
        ActFrequencyModel yosenAFModel = new ActFrequencyModel("yosen", this);
        ActFrequencyModel cedecAFModel = new ActFrequencyModel("cedec", this);
        ActFrequencyModel gatAFModel = new ActFrequencyModel("gat", this);

        talkFrequencyModel = new TalkFrequencyModel("old-yosen", this);
        TalkFrequencyModel yosenTFModel = new TalkFrequencyModel("yosen", this);
        TalkFrequencyModel cedecTFModel = new TalkFrequencyModel("cedec", this);
        TalkFrequencyModel gatTFModel = new TalkFrequencyModel("gat", this);

        actTextModel = new ActTextModel2("old-yosen", this);
        ActTextModel2 yosenATModel = new ActTextModel2("yosen", this);
        ActTextModel2 cedecATModel = new ActTextModel2("cedec", this);
        ActTextModel2 gatATModel = new ActTextModel2("gat", this);

        ensembleRolePredictotrModel = new EnsembleRolePredictotrModel(this, actFrequencyModel, yosenAFModel, cedecAFModel, gatAFModel, talkFrequencyModel, yosenTFModel, cedecTFModel, gatTFModel, actTextModel, yosenATModel, cedecATModel, gatATModel);
        tfafModel = new EnsembleRolePredictotrModel(this, actFrequencyModel, yosenAFModel, cedecAFModel, gatAFModel, talkFrequencyModel, yosenTFModel, cedecTFModel, gatTFModel);
        integratedModel = new IntegratedModel(this);

        //役職予測の評価
        predictionEvalModel = new PredictionEvalModel(this);
        predictionEvalModel.addPredictor("tf-af", tfafModel);
        predictionEvalModel.addPredictor("tf-af-at", ensembleRolePredictotrModel);
        predictionEvalModel.addPredictor("integrated", integratedModel);

        findCndlModel = new FindCndlModel(this);

        //投票系
        voteModel = new VoteModel(this);
        voteEvalModel = new VoteEvalModel(this);
        votePredectionEvalModel = new VotePredictionEvalModel(this);

        //その他
        v5TacEvalModel = new Village5TacEvalModel(this);
        hostilityEvalModel = new HostilityEvalModel(this);
        targetEvalModel = new TargetEvalModel(this);
        winEvalModel = new WinEvalModel(this);
    }

    @Override
    public void startGameHook(Game g) {
        bestPredictor = g.getVillageSize() == 15 ? integratedModel : ensembleRolePredictotrModel;
        agentReliabilityModel = new AgentReliabilityModel(this);
        believeSeerModel = new BelieveSeerModel(this, agentReliabilityModel);
        evilModel = new EvilScoreModel(this);

        super.startGameHook(g);
    }

}

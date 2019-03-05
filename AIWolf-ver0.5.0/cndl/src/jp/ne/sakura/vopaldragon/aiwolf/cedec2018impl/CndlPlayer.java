package jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl;

import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.framework.AbstractPlayer;
import org.aiwolf.common.data.Agent;
import org.aiwolf.common.net.GameInfo;
import org.aiwolf.common.net.GameSetting;

/**
 * cedec2018用Player
 */
public class CndlPlayer extends AbstractPlayer<CndlStrategy> {

    public static final boolean PRODUCTION_FLAG = true;

    @Override
    public String getName() {
        return "cndl";
    }

    @Override
    protected CndlStrategy createStrategy() {
        return new CndlStrategy(true);
    }

}

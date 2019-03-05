package jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.role;

import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.CndlStrategy;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.target.GuardBasic2;

public class V15BodyguardRole extends V15VillagerRole {

    public V15BodyguardRole(CndlStrategy strategy) {
        super(strategy);

        //守備戦術
        guardTactics.add(new GuardBasic2());

    }

}

package jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl;

import java.lang.reflect.Constructor;
import java.util.List;

import org.aiwolf.common.data.Role;

import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.framework.rolebase.AbstractRole;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.framework.rolebase.AbstractRoleBaseStrategy;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.framework.rolebase.RoleSelector;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.role.V15PossessedRole;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.role.V5PossessedRole;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.role.V15BodyguardRole;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.role.V15MediumRole;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.role.V15SeerRoll;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.role.V15VillagerRole;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.role.V5SeerRoll;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.role.V5VillagerRole;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.role.V15WerewolfRole;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.role.V5WerewolfRole;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.util.ListMap;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.util.Utils;

public class RandomRoleSelector implements RoleSelector {

    private ListMap<Role, Class<? extends AbstractRole>> village5RoleList = new ListMap<>();
    private ListMap<Role, Class<? extends AbstractRole>> village15RoleList = new ListMap<>();

    public RandomRoleSelector() {
        //5人村用Role
        village5RoleList.addAll(Role.WEREWOLF, V5WerewolfRole.class);
        village5RoleList.addAll(Role.POSSESSED, V5PossessedRole.class);
        village5RoleList.addAll(Role.SEER, V5SeerRoll.class);
        village5RoleList.addAll(Role.VILLAGER, V5VillagerRole.class);
        //15人村用Role
        village15RoleList.addAll(Role.WEREWOLF, V15WerewolfRole.class);
        village15RoleList.addAll(Role.POSSESSED, V15PossessedRole.class);
        village15RoleList.addAll(Role.MEDIUM, V15MediumRole.class);
        village15RoleList.addAll(Role.SEER, V15SeerRoll.class);
        village15RoleList.addAll(Role.BODYGUARD, V15BodyguardRole.class);
        village15RoleList.addAll(Role.VILLAGER, V15VillagerRole.class);
    }

    @Override
    public AbstractRole selectRole(AbstractRoleBaseStrategy strategy) {
        try {
            List<Class<? extends AbstractRole>> classList = strategy.getGame().getVillageSize() == 5
                ? village5RoleList.getList(strategy.getGame().getSelf().role)
                : village15RoleList.getList(strategy.getGame().getSelf().role);
            Class<? extends AbstractRole> selectecClass = Utils.getRandom(classList);
            Constructor<? extends AbstractRole> constructor = selectecClass.getConstructor(CndlStrategy.class);
            return constructor.newInstance(strategy);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }
}

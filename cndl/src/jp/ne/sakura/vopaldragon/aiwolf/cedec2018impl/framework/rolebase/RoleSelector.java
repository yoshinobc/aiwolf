package jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.framework.rolebase;

/**
 * その回のロールを決定する責務
 */
public interface RoleSelector {

    AbstractRole selectRole(AbstractRoleBaseStrategy strategy);

}

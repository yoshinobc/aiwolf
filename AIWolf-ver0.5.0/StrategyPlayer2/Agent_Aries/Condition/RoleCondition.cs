using Wepwawet.Lib;
using AIWolf.Lib;
using System;
using System.Collections.Generic;
using System.Text;

namespace Wepwawet.Condition
{
    /// <summary>
    /// 特定人物が特定役職かの条件を表現するクラス
    /// </summary>
    public class RoleCondition : ICondition
    {

        /// <summary>
        /// 条件のキャッシュ
        /// 同じ条件を大量に生成するため
        /// </summary>
        private static Dictionary<Role, Dictionary<Agent, RoleCondition>> ConditionCache
            = new Dictionary<Role, Dictionary<Agent, RoleCondition>>();

        public Agent Agent { get; private set; }
        public Role Role { get; private set; }

        /// <summary>
        /// インスタンスの取得
        /// </summary>
        /// <param name="agent">エージェント</param>
        /// <param name="role">役職（人狼・裏切り者・妖狐のみ対応）</param>
        /// <returns></returns>
        public static RoleCondition GetCondition(Agent agent, Role role)
        {
            if ( !ConditionCache.ContainsKey( role ) )
            {
                ConditionCache.Add( role, new Dictionary<Agent, RoleCondition>() );
            }
            Dictionary<Agent, RoleCondition> map = ConditionCache[role];

            if ( !map.ContainsKey( agent ) )
            {
                map.Add( agent, new RoleCondition( agent, role ) );
            }

            return map[agent];
        }

        public bool IsMatch( MonsterSidePattern pattern )
        {
            switch ( Role )
            {
                case Role.WEREWOLF:
                    return pattern.WerewolfAgent.Contains( Agent );
                    //return pattern.IsExist(Agent, Role.WEREWOLF);
                case Role.POSSESSED:
                    return pattern.PossessedAgent.Contains( Agent );
                    //return pattern.IsExist(Agent, Role.POSSESSED);
                case Role.FOX:
                    return pattern.FoxAgent.Contains( Agent );
                    //return pattern.IsExist(Agent, Role.FOX);
            }

            // 引数が不正だった場合など
            return false;

        }
        
        private RoleCondition(Agent agent, Role role)
        {
            Agent = agent;
            Role = role;
        }
        

        public override string ToString()
        {
            if ( Agent != null )
            {
                return Agent.AgentIdx + " is " + Role.ToString();
            }
            return "Invaild";
        }

    }
}

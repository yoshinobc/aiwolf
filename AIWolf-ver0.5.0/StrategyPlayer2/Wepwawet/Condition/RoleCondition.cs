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

        protected Agent Agent { get; set; }
        protected Role Role { get; set; }

        /// <summary>
        /// インスタンスの取得
        /// </summary>
        /// <param name="agent">エージェント</param>
        /// <param name="role">役職（人狼・裏切り者・妖狐のみ対応）</param>
        /// <returns></returns>
        public static RoleCondition GetCondition(Agent agent, Role role)
        {
            //TODO キャッシュ機能
            return new RoleCondition(agent, role);
        }

        public bool IsMatch( MonsterSidePattern pattern )
        {
            switch ( Role )
            {
                case Role.WEREWOLF:
                    return pattern.IsExist(Agent, Role.WEREWOLF);
                case Role.POSSESSED:
                    return pattern.IsExist(Agent, Role.POSSESSED);
                case Role.FOX:
                    return pattern.IsExist(Agent, Role.FOX);
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

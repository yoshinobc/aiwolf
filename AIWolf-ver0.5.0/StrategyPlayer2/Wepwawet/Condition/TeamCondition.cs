using Wepwawet.Lib;
using AIWolf.Lib;
using System;
using System.Collections.Generic;
using System.Text;

namespace Wepwawet.Condition
{
    /// <summary>
    /// 特定人物が特定チームかの条件を表現するクラス
    /// </summary>
    public class TeamCondition : ICondition
    {

        protected Agent Agent { get; set; }
        protected Team Team { get; set; }

        /// <summary>
        /// インスタンスの取得
        /// </summary>
        /// <param name="agent">エージェント</param>
        /// <param name="team">チーム</param>
        /// <returns></returns>
        public static TeamCondition GetCondition(Agent agent, Team team)
        {
            //TODO キャッシュ機能
            return new TeamCondition(agent, team);
        }

        public bool IsMatch( MonsterSidePattern pattern )
        {
            switch ( Team )
            {
                case Team.VILLAGER:
                    return !pattern.IsExist(Agent, Role.WEREWOLF) &&
                           !pattern.IsExist(Agent, Role.POSSESSED) &&
                           !pattern.IsExist(Agent, Role.FOX);
                case Team.WEREWOLF:
                    return pattern.IsExist(Agent, Role.WEREWOLF) || pattern.IsExist(Agent, Role.POSSESSED);
                case Team.OTHERS:
                    return pattern.IsExist(Agent, Role.FOX);
            }

            // 引数が不正だった場合など
            return false;

        }
        
        private TeamCondition(Agent agent, Team team)
        {
            Agent = agent;
            Team = team;
        }
        

        public override string ToString()
        {
            if ( Agent != null )
            {
                return Agent.AgentIdx + " is " + Team.ToString() + " Team";
            }
            return "Invaild";
        }

    }
}

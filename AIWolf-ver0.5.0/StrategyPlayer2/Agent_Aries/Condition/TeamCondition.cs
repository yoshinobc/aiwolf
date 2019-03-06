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
        /// <summary>
        /// 条件のキャッシュ
        /// 同じ条件を大量に生成するため
        /// </summary>
        private static Dictionary<Team, Dictionary<Agent, TeamCondition>> ConditionCache
            = new Dictionary<Team, Dictionary<Agent, TeamCondition>>();

        public Agent Agent { get; private set; }
        public Team Team { get; private set; }

        /// <summary>
        /// インスタンスの取得
        /// </summary>
        /// <param name="agent">エージェント</param>
        /// <param name="team">チーム</param>
        /// <returns></returns>
        public static TeamCondition GetCondition(Agent agent, Team team)
        {
            if ( !ConditionCache.ContainsKey(team) )
            {
                ConditionCache.Add( team, new Dictionary<Agent, TeamCondition>() );
            }
            Dictionary<Agent, TeamCondition> map = ConditionCache[team];
            
            if ( !map.ContainsKey( agent ) )
            {
                map.Add( agent, new TeamCondition( agent, team ) );
            }
            
            return map[agent];
        }

        public bool IsMatch( MonsterSidePattern pattern )
        {
            switch ( Team )
            {
                case Team.VILLAGER:
                    return !pattern.WerewolfAgent.Contains( Agent ) &&
                           !pattern.PossessedAgent.Contains( Agent ) &&
                           !pattern.FoxAgent.Contains( Agent );
                    //return !pattern.IsExist(Agent, Role.WEREWOLF) &&
                    //       !pattern.IsExist(Agent, Role.POSSESSED) &&
                    //       !pattern.IsExist(Agent, Role.FOX);
                case Team.WEREWOLF:
                    return pattern.WerewolfAgent.Contains( Agent ) ||
                           pattern.PossessedAgent.Contains( Agent );
                    //return pattern.IsExist(Agent, Role.WEREWOLF) || pattern.IsExist(Agent, Role.POSSESSED);
                case Team.OTHERS:
                    return pattern.FoxAgent.Contains( Agent );
                    //return pattern.IsExist(Agent, Role.FOX);
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

using System;
using System.Collections.Generic;
using System.Text;
using AIWolf.Lib;

namespace Wepwawet.Lib
{

    /// <summary>
    /// 人外陣営を表現するクラス
    /// </summary>
    public class MonsterSidePattern
    {

        public List<Agent> WerewolfAgent { get; set; } = new List<Agent>();
        public List<Agent> PossessedAgent { get; set; } = new List<Agent>();
        public List<Agent> FoxAgent { get; set; } = new List<Agent>();

        
        /// <summary>
        /// 指定エージェントを指定役職として含むか
        /// </summary>
        /// <param name="agent">エージェント</param>
        /// <param name="role">役職（人狼、裏切り者、妖狐のみ有効）</param>
        /// <returns></returns>
        public bool IsExist( Agent agent, Role role )
        {

            switch (role)
            {
                case Role.WEREWOLF:
                    return WerewolfAgent.Contains(agent);
                case Role.POSSESSED:
                    return PossessedAgent.Contains(agent);
                case Role.FOX:
                    return FoxAgent.Contains(agent);
            }

            // 役職の指定が不正な場合、含まないとみなす
            return false;

        }
        

        public string GetCode()
        {
            StringBuilder sb = new StringBuilder();

            if (WerewolfAgent.Count > 0)
            {
                sb.Append("wolf[");
                foreach (Agent wolf in WerewolfAgent)
                {
                    sb.Append(wolf.AgentIdx).Append(",");
                }
                sb.Remove(sb.Length - 1, 1);
                sb.Append("]");
            }

            if ( PossessedAgent.Count > 0 )
            {
                sb.Append(" possessed[");
                foreach (Agent pos in PossessedAgent)
                {
                    sb.Append(pos.AgentIdx).Append(",");
                }
                sb.Remove(sb.Length - 1, 1);
                sb.Append("]");
            }

            if (FoxAgent.Count > 0)
            {
                sb.Append(" fox[");
                foreach (Agent fox in FoxAgent)
                {
                    sb.Append(fox.AgentIdx).Append(",");
                }
                sb.Remove(sb.Length - 1, 1);
                sb.Append("]");
            }

            return sb.ToString();

        }

        
        override public string ToString()
        {
            StringBuilder sb = new StringBuilder();

            if (WerewolfAgent.Count > 0)
            {
                sb.Append("wolf[");
                foreach (Agent wolf in WerewolfAgent)
                {
                    sb.Append(wolf.AgentIdx).Append(",");
                }
                sb.Remove(sb.Length - 1, 1);
                sb.Append("]");
            }

            if (PossessedAgent.Count > 0)
            {
                sb.Append(" possessed[");
                foreach (Agent pos in PossessedAgent)
                {
                    sb.Append(pos.AgentIdx).Append(",");
                }
                sb.Remove(sb.Length - 1, 1);
                sb.Append("]");
            }

            if (FoxAgent.Count > 0)
            {
                sb.Append(" fox[");
                foreach (Agent fox in FoxAgent)
                {
                    sb.Append(fox.AgentIdx).Append(",");
                }
                sb.Remove(sb.Length - 1, 1);
                sb.Append("]");
            }

            return sb.ToString();

        }

    }
}

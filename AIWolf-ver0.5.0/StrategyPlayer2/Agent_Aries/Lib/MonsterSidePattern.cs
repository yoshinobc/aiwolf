using System;
using System.Collections.Generic;
using System.Text;
using AIWolf.Lib;
using System.Collections;

namespace Wepwawet.Lib
{   
    /// <summary>
    /// 人外陣営を表現するクラス
    /// エージェント32体まで対応
    /// </summary>
    public class MonsterSidePattern
    {
        /// <summary>
        /// 人狼エージェントのリスト
        /// </summary>
        public List<Agent> WerewolfAgent { get; private set; }

        /// <summary>
        /// 裏切り者エージェントのリスト
        /// </summary>
        public List<Agent> PossessedAgent { get; private set; }

        /// <summary>
        /// 妖狐エージェントのリスト
        /// </summary>
        public List<Agent> FoxAgent { get; private set; }

        public BitArray Code { private set; get; }

        /// <summary>
        /// 特定のエージェントが人狼か
        /// 最下位ビットがAgent[01]、最上位ビットがAgent[32]に対応
        /// ビットが1であれば条件を満たす
        /// </summary>
        public int IsWerewolfBits { get; private set; } = 0x0000;

        /// <summary>
        /// 条件「特定のエージェントが裏切り者であること」
        /// 最下位ビットがAgent[01]、最上位ビットがAgent[32]に対応
        /// ビットが1であれば条件としてチェックする
        /// </summary>
        public int IsPossessedBits { get; private set; } = 0x0000;

        /// <summary>
        /// 条件「特定のエージェントが人狼陣営であること」
        /// 最下位ビットがAgent[01]、最上位ビットがAgent[32]に対応
        /// ビットが1であれば条件としてチェックする
        /// </summary>
        public int IsWerewolfTeamBits { get; private set; } = 0x0000;
        


        public MonsterSidePattern( List<Agent> werewolfAgent, List<Agent> possessedAgent, List<Agent> foxAgent )
        {
            WerewolfAgent = werewolfAgent;
            PossessedAgent = possessedAgent;
            FoxAgent = foxAgent;
            
            // ビット演算用のビットを立てる
            //Code = new BitArray( AgentMax * 6 + 1 );
            foreach ( Agent agent in werewolfAgent )
            {
                //Code.Set( agent.AgentIdx, true );
                //Code.Set( AgentMax * 2 + agent.AgentIdx, true );
                IsWerewolfBits |= 1 << (agent.AgentIdx - 1);
                IsWerewolfTeamBits |= 1 << (agent.AgentIdx - 1);
            }
            foreach ( Agent agent in possessedAgent )
            {
                //Code.Set( AgentMax + agent.AgentIdx, true );
                //Code.Set( AgentMax * 2 + agent.AgentIdx, true );
                IsPossessedBits |= 1 << (agent.AgentIdx - 1);
                IsWerewolfTeamBits |= 1 << (agent.AgentIdx - 1);
            }
            //for ( int i = 1; i <= AgentMax * 3; i++ )
            //{
            //    Code.Set( AgentMax * 3 + i, !Code[i] );
            //}
        }
        
        /// <summary>
        /// 指定エージェントを指定役職として含むか
        /// </summary>
        /// <param name="agent">エージェント</param>
        /// <param name="role">役職（人狼、裏切り者、妖狐のみ有効）</param>
        /// <returns></returns>
        public bool IsExist( Agent agent, Role role )
        {

            switch(role)
            {
                case Role.WEREWOLF:
                    return (IsWerewolfBits & 1 << (agent.AgentIdx - 1)) != 0x00;
                case Role.POSSESSED:
                    return (IsPossessedBits & 1 << (agent.AgentIdx - 1)) != 0x00;
                case Role.FOX:
                    return FoxAgent.Contains(agent);
            }

            // 役職の指定が不正な場合、含まないとみなす
            return false;

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

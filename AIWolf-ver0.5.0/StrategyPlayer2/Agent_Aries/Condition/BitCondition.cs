using Wepwawet.Lib;
using AIWolf.Lib;
using System;
using System.Collections.Generic;
using System.Text;
using System.Collections;

namespace Wepwawet.Condition
{
    /// <summary>
    /// 条件をビット演算で判断するクラス
    /// 全てAnd条件になるが高速
    /// </summary>
    public class BitCondition : ICondition
    {

        /// <summary>
        /// 条件「特定のエージェントが人狼であること」
        /// 最下位ビットがAgent[01]、最上位ビットがAgent[32]に対応
        /// ビットが1であれば条件としてチェックする
        /// </summary>
        protected int isWerewolf = 0x0000;

        /// <summary>
        /// 条件「特定のエージェントが裏切り者であること」
        /// 最下位ビットがAgent[01]、最上位ビットがAgent[32]に対応
        /// ビットが1であれば条件としてチェックする
        /// </summary>
        protected int isPossessed = 0x0000;

        /// <summary>
        /// 条件「特定のエージェントが人狼陣営であること」
        /// 最下位ビットがAgent[01]、最上位ビットがAgent[32]に対応
        /// ビットが1であれば条件としてチェックする
        /// </summary>
        protected int isWerewolfTeam = 0x0000;

        /// <summary>
        /// 条件「特定のエージェントが人狼ではないこと」
        /// 最下位ビットがAgent[01]、最上位ビットがAgent[32]に対応
        /// ビットが1であれば条件としてチェックする
        /// </summary>
        protected int isNotWerewolf = 0x0000;

        /// <summary>
        /// 条件「特定のエージェントが裏切り者ではないこと」
        /// 最下位ビットがAgent[01]、最上位ビットがAgent[32]に対応
        /// ビットが1であれば条件としてチェックする
        /// </summary>
        protected int isNotPossessed = 0x0000;

        /// <summary>
        /// 条件「特定のエージェントが人狼陣営ではないこと」
        /// 最下位ビットがAgent[01]、最上位ビットがAgent[32]に対応
        /// ビットが1であれば条件としてチェックする
        /// </summary>
        protected int isNotWerewolfTeam = 0x0000;

        
        public bool IsMatch(MonsterSidePattern pattern)
        {
            // 「～である」系　条件を満たさない場合をチェック
            if((isWerewolf & pattern.IsWerewolfBits) != isWerewolf)
            {
                return false;
            }
            if((isPossessed & pattern.IsPossessedBits) != isPossessed)
            {
                return false;
            }
            if((isWerewolfTeam & pattern.IsWerewolfTeamBits) != isWerewolfTeam)
            {
                return false;
            }

            // 「～ではない」系　条件を満たさない場合をチェック
            if((isNotWerewolf & ~pattern.IsWerewolfBits) != isNotWerewolf)
            {
                return false;
            }
            if((isNotPossessed & ~pattern.IsPossessedBits) != isNotPossessed)
            {
                return false;
            }
            if((isNotWerewolfTeam & ~pattern.IsWerewolfTeamBits) != isNotWerewolfTeam)
            {
                return false;
            }

            // 満たさない条件が無ければ条件を満たしている
            return true;
        }


        /// <summary>
        /// 「特定のエージェントが人狼であること」を条件に加える
        /// </summary>
        /// <param name="agent"></param>
        public void AddWerewolf(Agent agent)
        {
            isWerewolf |= 1 << (agent.AgentIdx - 1);
        }


        /// <summary>
        /// 「特定のエージェントが裏切り者であること」を条件に加える
        /// </summary>
        /// <param name="agent"></param>
        public void AddPossessed(Agent agent)
        {
            isPossessed |= 1 << (agent.AgentIdx - 1);
        }


        /// <summary>
        /// 「特定のエージェントが人狼陣営であること」を条件に加える
        /// </summary>
        /// <param name="agent"></param>
        public void AddWerewolfTeam(Agent agent)
        {
            isWerewolfTeam |= 1 << (agent.AgentIdx - 1);
        }


        /// <summary>
        /// 「特定のエージェントが人狼ではないこと」を条件に加える
        /// </summary>
        /// <param name="agent"></param>
        public void AddNotWerewolf(Agent agent)
        {
            isNotWerewolf |= 1 << (agent.AgentIdx - 1);
        }


        /// <summary>
        /// 「特定のエージェントが裏切り者ではないこと」を条件に加える
        /// </summary>
        /// <param name="agent"></param>
        public void AddNotPossessed(Agent agent)
        {
            isNotPossessed |= 1 << (agent.AgentIdx - 1);
        }


        /// <summary>
        /// 「特定のエージェントが人狼陣営ではないこと」を条件に加える
        /// </summary>
        /// <param name="agent"></param>
        public void AddNotWerewolfTeam(Agent agent)
        {
            isNotWerewolfTeam |= 1 << (agent.AgentIdx - 1);
        }

    }
}

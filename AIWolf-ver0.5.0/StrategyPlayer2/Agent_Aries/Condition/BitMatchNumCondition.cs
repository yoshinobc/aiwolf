using Wepwawet.Lib;
using AIWolf.Lib;
using System;
using System.Collections.Generic;
using System.Text;
using System.Collections;

namespace Wepwawet.Condition
{
    /// <summary>
    /// ビット演算を使い、マッチ数による条件を表現するクラス
    /// 単純な条件しか使えないが高速
    /// 同じ条件を複数回追加しても１マッチ扱い
    /// </summary>
    public class BitMatchNumCondition : ICondition
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

        /// <summary>
        /// 最低マッチ数
        /// </summary>
        public int MinNum { get; set; }

        /// <summary>
        /// 最高マッチ数
        /// </summary>
        public int MaxNum { get; set; }


        public bool IsMatch(MonsterSidePattern pattern)
        {
            int matchNum = 0;

            // 「～である」系
            matchNum += BitCnt(isWerewolf & pattern.IsWerewolfBits);
            matchNum += BitCnt(isPossessed & pattern.IsPossessedBits);
            matchNum += BitCnt(isWerewolfTeam & pattern.IsWerewolfTeamBits);

            // 「～ではない」系
            matchNum += BitCnt(isNotWerewolf & ~pattern.IsWerewolfBits);
            matchNum += BitCnt(isNotPossessed & ~pattern.IsPossessedBits);
            matchNum += BitCnt(isNotWerewolfTeam & ~pattern.IsWerewolfTeamBits);

            // マッチ数が最低～最大なら条件を満たしている
            return matchNum >= MinNum && matchNum <= MaxNum;
        }


        /// <summary>
        /// 立っているビットの数を取得する
        /// </summary>
        /// <param name="val">変数</param>
        /// <returns>立っているビットの数</returns>
        static int BitCnt(int val)
        {
            val = val - ((val >> 1) & 0x55555555);
            val = (val & 0x33333333) + ((val >> 2) & 0x33333333);
            val = (val + (val >> 4)) & 0x0f0f0f0f;
            val += val >> 8;
            val += val >> 16;
            return val & 0x0000003f;
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

using AIWolf.Lib;
using System;
using System.Collections.Generic;
using System.Text;

namespace Wepwawet.ActionPlan
{
    public interface IActionPlanner
    {
        
        /// <summary>
        /// 行動方針の決定を実行する
        /// </summary>
        void Execute( ActionStrategyArgs args );

        /// <summary>
        /// 前回の実行結果による、追放投票の対象を取得する
        /// </summary>
        /// <returns>対象エージェント</returns>
        Agent GetVote();

        /// <summary>
        /// 前回の実行結果による、占いの対象を取得する
        /// </summary>
        /// <returns>対象エージェント</returns>
        Agent GetDevine();

        /// <summary>
        /// 前回の実行結果による、護衛の対象を取得する
        /// </summary>
        /// <returns>対象エージェント</returns>
        Agent GetGuard();

        /// <summary>
        /// 前回の実行結果による、襲撃投票の対象を取得する
        /// </summary>
        /// <returns>対象エージェント</returns>
        Agent GetAttack();
        
    }
}

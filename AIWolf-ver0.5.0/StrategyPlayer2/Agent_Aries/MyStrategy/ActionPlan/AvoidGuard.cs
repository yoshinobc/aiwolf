using AIWolf.Lib;
using System;
using System.Collections.Generic;
using System.Text;
using Wepwawet.Guess;
using Wepwawet.ActionPlan;
using Wepwawet.ActionPlan.Tree;
using Agent_Aries.Lib;
using Wepwawet.Lib;

namespace Agent_Aries.MyStrategy.ActionPlan
{
    /// <summary>
    /// 行動戦略「護衛避け」クラス
    /// 概要：ＧＪが出た直後、ＧＪ先を襲撃候補から外す
    /// </summary>
    class AvoidGuard : IActionPlannerNode
    {
        bool isSuccess = false;
        List<ActionRequest> requestList = new List<ActionRequest>();

        
        public bool IsSuccess()
        {
            return isSuccess;
        }


        public List<ActionRequest> GetRequest()
        {
            return requestList;
        }


        public void Update( ActionStrategyArgs args )
        {
            // 実行結果のクリア
            isSuccess = false;
            requestList.Clear();

            // 0,1日は実行失敗で抜ける
            if ( args.Agi.Day <= 1 )
            {
                return;
            }

            // ＧＪが発生していなければ実行失敗で抜ける
            if ( args.Agi.YesterdayInfo.AttackDeadAgent.Count != 0 )
            {
                return;
            }

            requestList.Add( new ActionRequest( args.Agi.YesterdayInfo.TryAttackAgent )
            {
                Attack = 0.2,
            } );
            

            // 実行成功にする
            isSuccess = true;
        }

    }
}

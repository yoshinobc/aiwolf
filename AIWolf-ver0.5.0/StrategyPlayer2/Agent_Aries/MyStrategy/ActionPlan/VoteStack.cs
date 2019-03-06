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
    /// 行動戦略「票合わせ」クラス
    /// 概要：他者の投票数が多いエージェントの投票スコアを上げる
    /// </summary>
    class VoteStack : IActionPlannerNode
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
            requestList.Clear();

            VoteAnalyzer saidVote = args.Agi.TodayInfo.SaidVote;
            foreach ( KeyValuePair<Agent, double> keyValue in saidVote.ReceiveVoteRate )
            {
                if ( keyValue.Value != 0 )
                {
                    requestList.Add( new ActionRequest( keyValue.Key )
                    {
                        Vote = 1.0 + keyValue.Value * 0.8,
                    } );
                }
            }
            
            // 実行成功にする
            isSuccess = true;
        }

    }
}

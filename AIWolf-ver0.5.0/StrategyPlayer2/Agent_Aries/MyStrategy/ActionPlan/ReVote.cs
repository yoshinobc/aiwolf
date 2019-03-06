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
    /// 行動戦略「再投票」クラス
    /// 概要：再投票時の戦略
    /// </summary>
    class Revote : IActionPlannerNode
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
            isSuccess = false;

            // 再投票時以外は抜ける
            if(args.Agi.TodayInfo.VoteList.Count <= 0)
            {
                return;
            }

            VoteAnalyzer firstVote = new VoteAnalyzer(args.Agi.TodayInfo.VoteList[0]);

            // 自分が追放されそうでなければ実行失敗で抜ける
            if(!firstVote.MaxVoteReceivedAgent.Contains(args.Agi.Me))
            {
                return;
            }

            // 自分が最大得票者に投票宣言しているか
            bool isVoteToMax = firstVote.MaxVoteReceivedAgent.Contains(firstVote.VoteMap[args.Agi.Me]);

            foreach(KeyValuePair<Agent, int> keyValue in firstVote.ReceiveVoteCount)
            {
                // 同票
                if(keyValue.Value == firstVote.MaxVoteReceiveCount && !args.Agi.GetAliveWerewolf().Contains(keyValue.Key))
                {
                    requestList.Add(new ActionRequest(keyValue.Key)
                    {
                        Vote = 3.0,
                    });
                }
                // １票差
                if(keyValue.Value == firstVote.MaxVoteReceiveCount - 1 && !args.Agi.GetAliveWerewolf().Contains(keyValue.Key))
                {
                    requestList.Add(new ActionRequest(keyValue.Key)
                    {
                        Vote = (firstVote.MaxVoteReceivedAgent.Count == 1) ? 3.0 : 2.25,
                    });
                }
            }

            // 実行成功にする
            isSuccess = true;
        }

    }
}

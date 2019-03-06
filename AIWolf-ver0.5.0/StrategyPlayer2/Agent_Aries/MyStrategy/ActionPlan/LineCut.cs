using AIWolf.Lib;
using System;
using System.Linq;
using System.Collections.Generic;
using System.Text;
using Wepwawet.Guess;
using Wepwawet.ActionPlan;
using Wepwawet.ActionPlan.Tree;
using Agent_Aries.Lib;
using Wepwawet.Lib;

namespace Agent_Aries.MyStrategy.ActionPlan
{
    class LineCut : IActionPlannerNode
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

            VoteAnalyzer saidVote = args.Agi.TodayInfo.SaidVote;

            // 一定ターン以下なら抜ける
            if(args.Agi.TodayInfo.TalkList.Count <= args.Agi.AliveAgentList.Count * 2)
            {
                return;
            }

            // 対戦回数が一定未満なら抜ける
            AgentStatistics statistics = (AgentStatistics)args.Items["AgentStatistics"];
            if(statistics.statistics[args.Agi.Me].gameCount < 40)
            {
                return;
            }

            // 自分が追放されそうでなければ実行失敗で抜ける
            if(!saidVote.MaxVoteReceivedAgent.Contains(args.Agi.Me))
            {
                return;
            }


            // 自分が最大得票者に投票宣言しているか
            bool isVoteToMax = saidVote.MaxVoteReceivedAgent.Contains( saidVote.VoteMap[args.Agi.Me] );

            bool canAvoid = false;
            foreach ( KeyValuePair<Agent, int> keyValue in saidVote.ReceiveVoteCount )
            {
                // ２票差以下ならまだ吊り逃れ可能と判断
                if ( keyValue.Value >= saidVote.MaxVoteReceiveCount - 2 && !args.Agi.GetAliveWerewolf().Contains(keyValue.Key))
                {
                    canAvoid = true;
                }
            }

            // 処刑可能なら実行失敗で抜ける
            if(canAvoid)
            {
                return;
            }

            // 処刑不可避 → 身内切り
            foreach(Agent agent in args.Agi.GetAliveWerewolf())
            {
                requestList.Add(new ActionRequest(agent)
                {
                    Vote = 5.0,
                });
            }

            // 実行成功にする
            isSuccess = true;
        }

    }
}

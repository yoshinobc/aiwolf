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
    class AvoidExecute : IActionPlannerNode
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

            if(args.Agi.MyRole == Role.WEREWOLF)
            {
                // 人狼が追放されそうでなければ実行失敗で抜ける
                if(saidVote.MaxVoteReceivedAgent.Intersect(args.Agi.GetAliveWerewolf()).Count() == 0)
                {
                    return;
                }
            }
            else
            {
                // 自分が追放されそうでなければ実行失敗で抜ける
                if ( !saidVote.MaxVoteReceivedAgent.Contains(args.Agi.Me) )
                {
                    return;
                }
            }
            

            // 自分が最大得票者に投票宣言しているか
            bool isVoteToMax = saidVote.MaxVoteReceivedAgent.Contains( saidVote.VoteMap[args.Agi.Me] );

            foreach ( KeyValuePair<Agent, int> keyValue in saidVote.ReceiveVoteCount )
            {
                // 同票
                if ( keyValue.Value == saidVote.MaxVoteReceiveCount && !args.Agi.GetAliveWerewolf().Contains(keyValue.Key) )
                {
                    requestList.Add( new ActionRequest( keyValue.Key )
                    {
                        Vote = 2.0,
                    } );
                }
                // １票差
                if(keyValue.Value == saidVote.MaxVoteReceiveCount - 1 && !args.Agi.GetAliveWerewolf().Contains(keyValue.Key))
                {
                    requestList.Add(new ActionRequest(keyValue.Key)
                    {
                        Vote = (saidVote.MaxVoteReceivedAgent.Count == 1) ? 2.0 : 1.5,
                    });
                }
                if(args.Agi.GameSetting.PlayerNum == 15)
                {
                    // ２票差
                    if(keyValue.Value == saidVote.MaxVoteReceiveCount - 2 && !args.Agi.GetAliveWerewolf().Contains(keyValue.Key))
                    {
                        requestList.Add(new ActionRequest(keyValue.Key)
                        {
                            Vote = (saidVote.MaxVoteReceivedAgent.Count == 1) ? 1.5 : 1.2,
                        });
                    }
                }
            }


            // 実行成功にする
            isSuccess = true;
        }

    }
}

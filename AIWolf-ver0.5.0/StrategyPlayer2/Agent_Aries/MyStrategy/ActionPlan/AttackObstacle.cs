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
    /// 行動戦略「意見喰い」クラス
    /// </summary>
    class AttackObstacle : IActionPlannerNode
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


        public void Update(ActionStrategyArgs args)
        {
            // 実行結果のクリア
            isSuccess = false;
            requestList.Clear();
            
            // 状況が付く前は実行失敗で抜ける
            if(args.Agi.AliveAgentList.Count == args.Agi.AgentList.Count)
            {
                return;
            }
            
            // 最新の投票を取得
            List<Vote> voteList = null;
            if(args.Agi.YesterdayInfo.VoteList.Count > 0)
            {
                voteList = args.Agi.YesterdayInfo.VoteList[0];
            }
            if(args.Agi.TodayInfo.VoteList.Count > 0)
            {
                voteList = args.Agi.TodayInfo.VoteList[0];
            }
            if(voteList == null)
            {
                return;
            }

            foreach(Vote vote in voteList)
            {
                if(args.Agi.GetAliveWerewolf().Contains(vote.Target))
                {
                    requestList.Add(new ActionRequest(vote.Agent)
                    {
                        Attack = 1.2,
                    });
                }
                
            }

            /*
             *
             *
		for( Agent agent : gameInfo.getAliveAgentList() ){
			if( args.agi.isWolf(agent.getAgentIdx())  ){
				continue;
			}
			double attackRate = 1.0;
			for( Agent target : gameInfo.getAliveAgentList() ){
				if( !agent.equals(target) && args.agi.isWolf(target.getAgentIdx()) ){
					double rate = args.agi.getSuspicionWerewolfRate(agent.getAgentIdx(), target.getAgentIdx());

					attackRate *= Math.pow(Math.max(rate, 0.1) + 0.5, 0.2);
				}
			}

			workReq = new Request(agent.getAgentIdx());
			workReq.attack = attackRate;
			Requests.add(workReq);
		}
             * 
             */

            // 実行成功にする
            isSuccess = true;
        }

    }
}

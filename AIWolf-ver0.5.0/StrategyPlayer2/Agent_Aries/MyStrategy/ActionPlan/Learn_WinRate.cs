using AIWolf.Lib;
using System;
using System.Collections.Generic;
using System.Linq;
using Wepwawet.Guess;
using Wepwawet.ActionPlan;
using Wepwawet.ActionPlan.Tree;

namespace Agent_Aries.MyStrategy.ActionPlan
{
    class Learn_WinRate : IActionPlannerNode
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

            AgentStatistics statistics = (AgentStatistics)args.Items["AgentStatistics"];

            // 10戦未満なら抜ける
            if(statistics.statistics[args.Agi.Me].gameCount < 10)
            {
                return;
            }

            Array roles = Enum.GetValues(typeof(Role));

            // 勝率計算
            Dictionary<Agent, double> winRate = new Dictionary<Agent, double>();
            foreach(Agent agent in args.Agi.AliveAgentList)
            {
                int winc = 0, losec = 0;
                Dictionary<Role, Dictionary<String, int>> eventCount = statistics.statistics[agent].eventCount;
                foreach(Role role in roles)
                {
                    if(eventCount[role].ContainsKey("Win"))
                    {
                        winc += eventCount[role]["Win"];
                    }
                    if(eventCount[role].ContainsKey("Lose"))
                    {
                        losec += eventCount[role]["Lose"];
                    }
                }
                winRate.Add(agent, winc / (double)(winc + losec));
            }
            if(winRate.ContainsKey(args.Agi.Me))
            {
                winRate.Remove(args.Agi.Me);
            }
            
            // 5人村初日用
            if(args.Agi.GameSetting.PlayerNum == 5 && args.Agi.MyRole == Role.SEER)
            {
                if(args.Agi.Day == 0)
                {
                    Agent target = winRate.OrderByDescending(a => a.Value).ElementAt(1).Key;
                    requestList.Add(new ActionRequest(target)
                    {
                        Devine = 10.0,
                    });
                    // 実行成功にして抜ける
                    isSuccess = true;
                    return;
                }
            }

            foreach(Agent agent in winRate.Keys)
            {
                double rate = winRate[agent];

                requestList.Add( new ActionRequest( agent )
                {
                    Vote = 1 + rate * 0.4,
                    Devine = 1 + rate * 1.0,
                    Attack = 1 + rate * 0.4,
                } );
            }
            
            // 実行成功にする
            isSuccess = true;
        }

    }
}

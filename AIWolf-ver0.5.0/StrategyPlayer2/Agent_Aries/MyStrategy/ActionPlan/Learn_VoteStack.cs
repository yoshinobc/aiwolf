using AIWolf.Lib;
using System;
using System.Collections.Generic;
using System.Text;
using Wepwawet.Guess;
using Wepwawet.ActionPlan;
using Wepwawet.ActionPlan.Tree;

namespace Agent_Aries.MyStrategy.ActionPlan
{
    class Learn_VoteStack : IActionPlannerNode
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

            AgentStatistics statistics = (AgentStatistics)args.Items["AgentStatistics"];

            Array roles = Enum.GetValues(typeof(Role));

            foreach(Agent agent in args.Agi.AliveAgentList)
            {
                int maxc = 0;
                int nmaxc = 0;

                Dictionary<Role, Dictionary<String, int>> eventCount = statistics.statistics[agent].eventCount;
                foreach(Role role in roles)
                {
                    if(eventCount[role].ContainsKey("VoteToMax"))
                    {
                        maxc += eventCount[role]["VoteToMax"];
                    }
                    if(eventCount[role].ContainsKey("VoteToNotMax"))
                    {
                        nmaxc += eventCount[role]["VoteToNotMax"];
                    }
                }

                if(maxc + nmaxc > 0)
                {
                    double rate = maxc / (double)(maxc + nmaxc);

                    requestList.Add( new ActionRequest( agent )
                    {
                        Vote = 1 + rate * 0.1,
                        Devine = 1 + rate * 0.3,
                        Attack = 1 + rate * 0.3,
                    } );
                }
            }
            
            // 実行成功にする
            isSuccess = true;
        }

    }
}

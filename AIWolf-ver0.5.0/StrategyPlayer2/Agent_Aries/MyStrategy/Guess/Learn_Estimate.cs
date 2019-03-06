using Agent_Aries.Lib;
using AIWolf.Lib;
using System;
using System.Collections.Generic;
using System.Text;
using Wepwawet.Condition;
using Wepwawet.Guess;
using Wepwawet.Guess.Tree;

namespace Agent_Aries.MyStrategy.Guess
{
    /// <summary>
    /// 推理戦略「推理発言学習」クラス
    /// </summary>
    class Learn_Estimate : IGuessNode
    {
        bool isSuccess = false;
        List<PartGuess> guessList = new List<PartGuess>();


        public List<PartGuess> GetGuess()
        {
            return guessList;
        }

        public bool IsSuccess()
        {
            return isSuccess;
        }

        public void Update( GuessStrategyArgs args )
        {

            // 実行結果のクリア
            isSuccess = false;
            guessList.Clear();

            // 初日は行わない
            if(args.Agi.Day < 1)
            {
                return;
            }
            
            AgentStatistics statistics = (AgentStatistics)args.Items["AgentStatistics"];

            List<Agent> estAgentList = new List<Agent>();
            foreach(ExtTalk talk in args.Agi.DayInfo[1].TalkList)
            {
                if(talk.Content.Operator == Operator.NOP && talk.Content.Topic == Topic.ESTIMATE)
                {
                    if(!estAgentList.Contains(talk.Agent))
                    {
                        estAgentList.Add(talk.Agent);
                    }
                }
            }

            foreach(Agent agent in estAgentList)
            {
                if(statistics.statistics[agent].roleCount[Role.VILLAGER] >= 5)
                {
                    int vilEveCnt = statistics.statistics[agent].eventCount[Role.VILLAGER].GetOrDefault("1d_Estimate", 0);
                    
                    if(statistics.statistics[agent].roleCount[Role.WEREWOLF] >= 5)
                    {
                        int roleEveCnt = statistics.statistics[agent].eventCount[Role.WEREWOLF].GetOrDefault("1d_Estimate", 0);

                        if(roleEveCnt == 0 && vilEveCnt >= 5)
                        {
                            guessList.Add( new PartGuess()
                            {
                                Condition = RoleCondition.GetCondition( agent, Role.WEREWOLF ),
                                Correlation = 0.7
                            } );
                        }
                    }

                    if(statistics.statistics[agent].roleCount[Role.POSSESSED] >= 3)
                    {
                        int roleEveCnt = statistics.statistics[agent].eventCount[Role.POSSESSED].GetOrDefault("1d_Estimate", 0);

                        if(roleEveCnt == 0 && vilEveCnt >= 5)
                        {
                            guessList.Add(new PartGuess()
                            {
                                Condition = RoleCondition.GetCondition(agent, Role.POSSESSED),
                                Correlation = 0.7
                            });
                        }
                    }
                }
            }

            // 実行成功にする
            isSuccess = true;
        }

    }
}

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
    /// 推理戦略「CO先学習」クラス
    /// </summary>
    class Learn_CO : IGuessNode
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

            // 1日目0発言では行わない
            if(args.Agi.Day == 1 && args.Agi.TodayInfo.TalkList.Count == 0)
            {
                return;
            }

            AgentStatistics statistics = (AgentStatistics)args.Items["AgentStatistics"];

            foreach(Agent agent in args.Agi.AgentList)
            {
                if(statistics.statistics[agent].roleCount[Role.WEREWOLF] >= 5)
                {
                    int roleCnt = statistics.statistics[agent].roleCount[Role.WEREWOLF];
                    int roleEveCnt = statistics.statistics[agent].COCount[Role.WEREWOLF][args.Agi.GetComingOutRole(agent)];

                    if(roleEveCnt == 0)
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
                    int roleCnt = statistics.statistics[agent].roleCount[Role.POSSESSED];
                    int roleEveCnt = statistics.statistics[agent].COCount[Role.POSSESSED][args.Agi.GetComingOutRole(agent)];

                    if(roleEveCnt == 0)
                    {
                        guessList.Add(new PartGuess()
                        {
                            Condition = RoleCondition.GetCondition(agent, Role.POSSESSED),
                            Correlation = 0.7
                        });
                    }
                }
            }
            
            // 実行成功にする
            isSuccess = true;
        }

    }
}

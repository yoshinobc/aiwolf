using AIWolf.Lib;
using System;
using System.Collections.Generic;
using System.Text;
using Wepwawet.Condition;
using Wepwawet.Guess;
using Wepwawet.Guess.Tree;

namespace Agent_Aries.MyStrategy.Guess
{
    class Learn_Judge : IGuessNode
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

            // 1日目2発言未満では行わない
            if(args.Agi.Day == 1 && args.Agi.TodayInfo.TalkList.Count < args.Agi.AliveAgentList.Count * 2)
            {
                return;
            }

            AgentStatistics statistics = (AgentStatistics)args.Items["AgentStatistics"];


            foreach(Wepwawet.Lib.Judge judge in args.Agi.SeerJudge)
            {
                if(judge.JudgeTalk.Day == 1)
                {
                    Agent agent = judge.Agent;

                    string eventCode1 = "";
                    string eventCode2 = "";
                    double posBaseEventRate = 0.0;  // この率より頻度が大きければ人外濃厚と見る
                    double posBigRate = 0.0;        // ↑で設定する人外度
                    switch(judge.Result)
                    {
                        case Species.HUMAN:
                            eventCode1 = "1d_DevineWhite";
                            eventCode2 = "1d_DevineBlack";
                            posBaseEventRate = 1.00;
                            posBigRate = 1.06;
                            break;
                        case Species.WEREWOLF:
                            eventCode1 = "1d_DevineBlack";
                            eventCode2 = "1d_DevineWhite";
                            posBaseEventRate = 0.66;
                            posBigRate = 1.3;
                            break;
                        default:
                            continue;
                    }

                    if(statistics.statistics[agent].roleCount[Role.POSSESSED] >= 3)
                    {
                        int roleCnt = statistics.statistics[agent].roleCount[Role.POSSESSED];
                        int roleEveCnt = statistics.statistics[agent].eventCount[Role.POSSESSED].GetOrDefault(eventCode1, 0);

                        if(roleEveCnt >= roleCnt * posBaseEventRate)
                        {
                            guessList.Add(new PartGuess()
                            {
                                Condition = RoleCondition.GetCondition(agent, Role.POSSESSED),
                                Correlation = posBigRate
                            });
                        }
                        if(roleEveCnt <= 0)
                        {
                            guessList.Add(new PartGuess()
                            {
                                Condition = RoleCondition.GetCondition(agent, Role.POSSESSED),
                                Correlation = 0.7
                            });
                        }
                    }
                    if(statistics.statistics[agent].roleCount[Role.WEREWOLF] >= 3)
                    {
                        int roleCnt = statistics.statistics[agent].roleCount[Role.WEREWOLF];
                        int roleEveCnt = statistics.statistics[agent].eventCount[Role.WEREWOLF].GetOrDefault(eventCode1, 0);
                        int roleEveCnt2 = statistics.statistics[agent].eventCount[Role.WEREWOLF].GetOrDefault(eventCode2, 0);

                        if(roleEveCnt > (roleEveCnt + roleEveCnt2) * 0.66)
                        {
                            guessList.Add(new PartGuess()
                            {
                                Condition = RoleCondition.GetCondition(agent, Role.WEREWOLF),
                                Correlation = 2.0
                            });
                        }
                        if(roleEveCnt2 >= 5 && roleEveCnt <= 0)
                        {
                            guessList.Add(new PartGuess()
                            {
                                Condition = RoleCondition.GetCondition(agent, Role.WEREWOLF),
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

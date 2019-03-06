using Agent_Aries.Lib;
using AIWolf.Lib;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using Wepwawet.Condition;
using Wepwawet.Guess;
using Wepwawet.Guess.Tree;

namespace Agent_Aries.MyStrategy.Guess
{
    /// <summary>
    /// 推理戦略「発言傾向学習」クラス
    /// </summary>
    class Learn_Talk : IGuessNode
    {
        bool isSuccess = false;
        List<PartGuess> guessList = new List<PartGuess>();

        double[] wolfScore;
        double[] posScore;

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

            // 初日１発言目は行わない
            if(args.Agi.Day < 1 && args.Agi.TodayInfo.TalkList.Count == 0)
            {
                wolfScore = Enumerable.Repeat(1.0, args.Agi.AgentList.Count + 1).ToArray();
                posScore = Enumerable.Repeat(1.0, args.Agi.AgentList.Count + 1).ToArray();
                return;
            }
            
            AgentStatistics statistics = (AgentStatistics)args.Items["AgentStatistics"];
            
            foreach(ExtTalk talk in args.Agi.TodayInfo.LatestTalkList)
            {
                int id = -1;
                if(talk.Content.Operator == Operator.NOP)
                {
                    switch(talk.Content.Topic)
                    {
                        case Topic.Over:
                            id = 1;
                            break;
                        case Topic.Skip:
                            id = 2;
                            break;
                        case Topic.VOTE:
                            id = 3;
                            break;
                        case Topic.ESTIMATE:
                            id = (talk.Content.Role.GetTeam() == Team.WEREWOLF) ? 4 : 5;
                            break;
                        case Topic.COMINGOUT:
                            id = 6;
                            break;
                        case Topic.DIVINED:
                        case Topic.IDENTIFIED:
                        case Topic.GUARDED:
                            id = 7;
                            break;
                        case Topic.AGREE:
                            id = 8;
                            break;
                        case Topic.DISAGREE:
                            id = 9;
                            break;
                        default:
                            break;
                    }
                }
                else if(talk.Content.Operator == Operator.REQUEST)
                {
                    id = 10;
                }

                if(id > 0)
                {
                    wolfScore[talk.Agent.AgentIdx] *=
                        PowBetween(
                            statistics.statistics[talk.Agent].talkCount.GetWolfRate(
                              talk.Day, talk.Turn, args.Agi.GetComingOutRole(talk.Agent), id
                            ),
                            0.7, 1.5
                        );
                    posScore[talk.Agent.AgentIdx] *=
                        PowBetween(
                            statistics.statistics[talk.Agent].talkCount.GetPosRate(
                              talk.Day, talk.Turn, args.Agi.GetComingOutRole(talk.Agent), id
                            ),
                            0.7, 1.5
                        );
                }
            }

            foreach(Agent agent in args.Agi.AgentList)
            {
                guessList.Add(new PartGuess()
                {
                    Condition = RoleCondition.GetCondition(agent, Role.WEREWOLF),
                    Correlation = PowBetween(wolfScore[agent.AgentIdx], 0.4, 3.0)
                });
                guessList.Add(new PartGuess()
                {
                    Condition = RoleCondition.GetCondition(agent, Role.POSSESSED),
                    Correlation = PowBetween(posScore[agent.AgentIdx], 0.4, 3.0)
                });
            }
            
            // 実行成功にする
            isSuccess = true;
        }

        /// <summary>
        /// 値を範囲内に丸める
        /// </summary>
        /// <param name="value">値</param>
        /// <param name="min">最小値</param>
        /// <param name="max">最大値</param>
        /// <returns>丸めた値</returns>
        private double PowBetween(double value, double min, double max)
        {
            value = Math.Pow(value, 0.8);

            if(value < min)
            {
                return min;
            }

            if(value > max)
            {
                return max;
            }

            return value;
        }

    }
}

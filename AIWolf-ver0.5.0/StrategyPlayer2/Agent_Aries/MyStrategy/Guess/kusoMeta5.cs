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
    /// 事前学習的なもの
    /// </summary>
    class KusoMeta5 : IGuessNode
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

            // 初日１発言目は行わない
            if(args.Agi.Day < 1 && args.Agi.TodayInfo.TalkList.Count == 0)
            {
                return;
            }

            List<Agent> seerList = args.Agi.GetComingOutAgent(Role.SEER);


            foreach(ExtTalk talk in args.Agi.DayInfo[1].TalkList)
            {
                if(talk.Content.Operator == Operator.NOP)
                {
                    switch(talk.Content.Topic)
                    {
                        case Topic.Over:
                            if(talk.Turn == 0)
                            {
                                // チーム TRKOkami Serval
                                guessList.Add(new PartGuess()
                                {
                                    Condition = RoleCondition.GetCondition(talk.Agent, Role.WEREWOLF),
                                    Correlation = 0.98
                                });
                            }
                            break;
                        case Topic.Skip:
                            if(talk.Turn == 0)
                            {
                                // チーム WordWolf WolfKing
                                guessList.Add(new PartGuess()
                                {
                                    Condition = TeamCondition.GetCondition(talk.Agent, Team.WEREWOLF),
                                    Correlation = 1.02
                                });
                            }
                            break;
                        case Topic.VOTE:
                            if(talk.Turn == 0)
                            {
                                // チーム Litt1eGirl
                                if(seerList.Contains(talk.Agent))
                                {
                                    guessList.Add(new PartGuess()
                                    {
                                        Condition = TeamCondition.GetCondition(talk.Agent, Team.WEREWOLF),
                                        Correlation = 0.95
                                    });
                                }
                            }
                            break;
                        case Topic.ESTIMATE:
                            if(talk.Turn == 0)
                            {
                                // チーム wasabi
                                if(talk.Content.Role.GetTeam() == Team.VILLAGER)
                                {
                                    guessList.Add(new PartGuess()
                                    {
                                        Condition = RoleCondition.GetCondition(talk.Agent, Role.WEREWOLF),
                                        Correlation = 1.2
                                    });
                                }
                            }
                            break;
                        case Topic.COMINGOUT:
                            // チーム hh
                            if(talk.Content.Role == Role.VILLAGER)
                            {
                                guessList.Add(new PartGuess()
                                {
                                    Condition = RoleCondition.GetCondition(talk.Agent, Role.WEREWOLF),
                                    Correlation = 1.2
                                });
                            }
                            break;
                        case Topic.DIVINED:
                        case Topic.IDENTIFIED:
                        case Topic.GUARDED:
                            if(talk.Turn == 0)
                            {
                                // チーム WordWolf
                                guessList.Add(new PartGuess()
                                {
                                    Condition = RoleCondition.GetCondition(talk.Agent, Role.WEREWOLF),
                                    Correlation = 1.2
                                });
                            }
                            break;
                        default:
                            break;
                    }
                }
                if(talk.Content.Operator == Operator.REQUEST)
                {
                    // チーム wasabi
                    if(talk.Content.Role.GetTeam() == Team.VILLAGER)
                    {
                        guessList.Add(new PartGuess()
                        {
                            Condition = RoleCondition.GetCondition(talk.Agent, Role.WEREWOLF),
                            Correlation = 0.95
                        });
                    }
                }

            }

            // 実行成功にする
            isSuccess = true;
        }
    }
}

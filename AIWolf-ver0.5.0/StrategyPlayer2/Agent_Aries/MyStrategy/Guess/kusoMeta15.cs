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
    class KusoMeta15 : IGuessNode
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
                            break;
                        case Topic.Skip:
                            if(talk.Turn == 0 && args.Agi.GetComingOutRole(talk.Agent) == Role.UNC)
                            {
                                // チーム いろいろ
                                guessList.Add(new PartGuess()
                                {
                                    Condition = RoleCondition.GetCondition(talk.Agent, Role.WEREWOLF),
                                    Correlation = 0.9
                                });
                            }
                            if(talk.Turn == 1)
                            {
                                // チーム いろいろ
                                guessList.Add(new PartGuess()
                                {
                                    Condition = RoleCondition.GetCondition(talk.Agent, Role.WEREWOLF),
                                    Correlation = 1.2
                                });
                            }
                            if(talk.Turn == 2)
                            {
                                // チーム いろいろ
                                guessList.Add(new PartGuess()
                                {
                                    Condition = TeamCondition.GetCondition(talk.Agent, Team.WEREWOLF),
                                    Correlation = 1.2
                                });
                            }
                            break;
                        case Topic.VOTE:
                            if(talk.Turn == 1)
                            {
                                // チーム いろいろ
                                if(args.Agi.GetComingOutRole(talk.Agent) == Role.UNC)
                                {
                                    guessList.Add(new PartGuess()
                                    {
                                        Condition = RoleCondition.GetCondition(talk.Agent, Role.WEREWOLF),
                                        Correlation = 0.98
                                    });
                                }
                            }
                            break;
                        case Topic.ESTIMATE:
                            if(talk.Turn == 0)
                            {
                            }
                            break;
                        case Topic.COMINGOUT:
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
                        case Topic.AGREE:
                            if(seerList.Contains(talk.Agent))
                            {
                                if(talk.Turn == 1 || talk.Turn == 2)
                                {
                                    // チーム TRKO Serval
                                    guessList.Add(new PartGuess()
                                    {
                                        Condition = TeamCondition.GetCondition(talk.Agent, Team.WEREWOLF),
                                        Correlation = 1.3
                                    });
                                }

                            }
                            else
                            {
                                if(talk.Turn == 0)
                                {
                                    // チーム いろいろ
                                    guessList.Add(new PartGuess()
                                    {
                                        Condition = TeamCondition.GetCondition(talk.Agent, Team.WEREWOLF),
                                        Correlation = 1.1
                                    });
                                }
                            }

                            break;
                        default:
                            break;
                    }
                }
                if(talk.Content.Operator == Operator.REQUEST)
                {
                }


            }

            if(args.Agi.Day >= 2)
            {
                foreach(ExtTalk talk in args.Agi.DayInfo[2].TalkList)
                {
                    if(talk.Content.Operator == Operator.NOP)
                    {
                        switch(talk.Content.Topic)
                        {
                            case Topic.Over:
                                break;
                            case Topic.Skip:
                                break;
                            case Topic.VOTE:
                                if(talk.Turn == 1)
                                {
                                    // チーム いろいろ
                                    Role coRole = args.Agi.GetComingOutRole(talk.Agent);
                                    if(coRole == Role.UNC)
                                    {
                                        guessList.Add(new PartGuess()
                                        {
                                            Condition = RoleCondition.GetCondition(talk.Agent, Role.WEREWOLF),
                                            Correlation = 1.1
                                        });
                                    }
                                }
                                break;
                            case Topic.ESTIMATE:
                                // チーム cndl
                                if(talk.Turn == 0 && talk.Content.Role.GetTeam() == Team.VILLAGER)
                                {
                                    guessList.Add(new PartGuess()
                                    {
                                        Condition = RoleCondition.GetCondition(talk.Agent, Role.POSSESSED),
                                        Correlation = 1.2
                                    });
                                }
                                break;
                            case Topic.COMINGOUT:
                                break;
                            case Topic.DIVINED:
                            case Topic.IDENTIFIED:
                            case Topic.GUARDED:
                                break;
                            default:
                                break;
                        }
                        if(talk.Turn == 0 && seerList.Contains(talk.Agent) && talk.Content.Topic != Topic.DIVINED)
                        {
                            guessList.Add(new PartGuess()
                            {
                                Condition = TeamCondition.GetCondition(talk.Agent, Team.WEREWOLF),
                                Correlation = 1.5
                            });
                        }
                    }
                    if(talk.Content.Operator == Operator.REQUEST)
                    {
                    }


                }

            }

            List<Agent> bodyguardList = args.Agi.GetComingOutAgent(Role.BODYGUARD);
            foreach(Agent agent in bodyguardList)
            {
                guessList.Add(new PartGuess()
                {
                    Condition = TeamCondition.GetCondition(agent, Team.WEREWOLF),
                    Correlation = 0.95
                });
            }

            List<Agent> villagerList = args.Agi.GetComingOutAgent(Role.VILLAGER);
            foreach(Agent agent in villagerList)
            {
                guessList.Add(new PartGuess()
                {
                    Condition = TeamCondition.GetCondition(agent, Team.WEREWOLF),
                    Correlation = 1.2
                });
            }
            

            // 実行成功にする
            isSuccess = true;
        }
    }
}

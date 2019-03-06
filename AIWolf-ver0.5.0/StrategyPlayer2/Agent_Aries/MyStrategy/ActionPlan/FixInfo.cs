using AIWolf.Lib;
using System;
using System.Collections.Generic;
using System.Text;
using Wepwawet.ActionPlan;
using Wepwawet.ActionPlan.Tree;

namespace Agent_Aries.MyStrategy.ActionPlan
{
    /// <summary>
    /// 行動戦略「確定情報」クラス
    /// 概要：確定情報によるスコアを付ける
    /// 目的：不可能な行動や意味の無い行動を排除する
    /// </summary>
    class FixInfo : IActionPlannerNode
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


            foreach ( Agent agent in args.Agi.AgentList )
            {
                if ( agent.Equals( args.Agi.Me ) )
                {
                    // 自分は全ての行動の対象にしない
                    requestList.Add( new ActionRequest( args.Agi.Me )
                    {
                        Vote = 0.0,
                        Devine = 0.001,
                        Guard = 0.0,
                        Attack = 0.0
                    } );
                }
                else if ( !args.Agi.TodayInfo.LatestAliveAgentList.Contains( agent ) )
                {
                    // 死亡済みエージェントは全ての行動の対象にしない
                    requestList.Add( new ActionRequest( agent )
                    {
                        Vote = 0.0,
                        Devine = 0.0,
                        Guard = 0.0,
                        Attack = 0.0
                    } );
                }
                else if ( !args.GuessResult.LikelyWolfPattern.ContainsKey(agent) ||
                          (!args.GuessResult.LikelyVillagerPattern.ContainsKey( agent ) &&
                           !args.GuessResult.LikelyPossessedPattern.ContainsKey( agent ) ) )
                {
                    // 確定白、確定黒は占いの対象にしない
                    requestList.Add( new ActionRequest( agent )
                    {
                        Devine = 0.001
                    } );
                }
            }

            // 仲間の人狼は襲撃の対象にしない
            foreach ( KeyValuePair<Agent, Role> val in args.Agi.RoleMap )
            {
                if ( val.Value == Role.WEREWOLF && !val.Key.Equals( args.Agi.Me ) )
                {
                    requestList.Add( new ActionRequest( val.Key )
                    {
                        Attack = 0.0
                    } );
                }
            }

            // 実行成功にする
            isSuccess = true;
        }
    }
}

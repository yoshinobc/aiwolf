using AIWolf.Lib;
using System;
using System.Collections.Generic;
using System.Text;
using Wepwawet.Guess;
using Wepwawet.ActionPlan;
using Wepwawet.ActionPlan.Tree;

namespace Agent_Aries.MyStrategy.ActionPlan
{
    /// <summary>
    /// 行動戦略「疑い」クラス
    /// 概要：疑い度によるスコアを付ける
    /// 目的：疑い度を行動に反映する
    /// </summary>
    class Suspicious : IActionPlannerNode
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

            foreach ( Agent agent in args.Agi.AliveAgentList )
            {
                // 自分は対象外
                if ( agent.Equals( args.Agi.Me ) )
                {
                    continue;
                }

                double likelyVilScore = 0.0;
                if ( args.GuessResult.LikelyVillagerPattern.ContainsKey( agent ) )
                {
                    likelyVilScore = args.GuessResult.LikelyVillagerPattern[agent].Score;
                }

                double likelyWolfScore = 0.0;
                if ( args.GuessResult.LikelyWolfPattern.ContainsKey( agent ) )
                {
                    likelyWolfScore = args.GuessResult.LikelyWolfPattern[agent].Score;
                }

                double likelyPosScore = 0.0;
                if ( args.GuessResult.LikelyPossessedPattern.ContainsKey( agent ) )
                {
                    likelyPosScore = args.GuessResult.LikelyPossessedPattern[agent].Score;
                }

                double suspiciousScore = Math.Max( likelyWolfScore, likelyPosScore / 2 );

                if ( likelyVilScore < 0.0001 )
                {
                    // 村陣営の内訳が存在しない or 非常に薄い
                    if(args.Agi.AliveAgentList.Count > 4)
                    {
                        suspiciousScore = 2.1;
                    }
                    else if(likelyWolfScore > likelyPosScore * 1.4)
                    {
                        suspiciousScore = 2.1;
                    }
                    else
                    {
                        suspiciousScore = 1.2;
                    }
                }
                else
                {
                    suspiciousScore = Math.Max( likelyWolfScore, likelyPosScore / 3 );
                    suspiciousScore = Math.Min( Math.Max( suspiciousScore / likelyVilScore, 0.001 ), 2.0 );
                }

                requestList.Add( new ActionRequest( agent )
                {
                    Vote = suspiciousScore,
                    Devine = Math.Pow( suspiciousScore, 0.4 ),
                    Guard = Math.Pow( 1 / suspiciousScore, 0.2 ),
                    Attack = Math.Pow( 1 / suspiciousScore, 0.3 ),
                } );
            }

            // 実行成功にする
            isSuccess = true;
        }

    }
}

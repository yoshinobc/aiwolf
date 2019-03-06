using AIWolf.Lib;
using System;
using System.Collections.Generic;
using System.Text;
using Wepwawet.Lib;
using Wepwawet.Guess;
using Wepwawet.ActionPlan;
using Wepwawet.ActionPlan.Tree;

namespace Agent_Aries.MyStrategy.ActionPlan
{
    /// <summary>
    /// 行動戦略「役職の重み」クラス
    /// 概要：役職によるスコアを付ける
    /// </summary>
    class RoleWeight5 : IActionPlannerNode
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


            // 役職CO者を取得
            List<Agent> seerList = args.Agi.GetComingOutAgent( Role.SEER );
            List<Agent> villagerList = args.Agi.GetComingOutAgent( Role.VILLAGER );

            // TODO 役職が襲撃されているかを取得
            // TODO 狼は0.3乗するのでAttackをその分大きくしている。別クラスにすべき？

            int day = args.Agi.Day;

            // 占い師
            foreach ( Agent agent in seerList )
            {
                double voteScore = Between( (-0.2 + day * 0.3), 0.001, 1.0 );
                double devineScore = 0.2;
                double guardScore;
                double attackScore = 15.0;
                
                switch ( seerList.Count )
                {
                    case 1:
                        guardScore = 10.0;
                        break;
                    case 2:
                        guardScore = 4.0;
                        break;
                    case 3:
                        guardScore = 2.0;
                        break;
                    default:
                        guardScore = 1.0;
                        break;
                }

                requestList.Add( new ActionRequest( agent )
                {
                    Vote = voteScore,
                    Devine = devineScore,
                    Guard = guardScore,
                    Attack = attackScore,
                } );
            }
            
            
            // 判定
            List<Agent> receiveWhiteList = new List<Agent>();
            List<Agent> receiveBlackList = new List<Agent>();
            foreach(Wepwawet.Lib.Judge judge in args.Agi.SeerJudge)
            {
                // 判定が有効ではない
                if(!judge.IsEnable())
                {
                    continue;
                }
                // 判定を出した者が村側のパターンが存在しない
                if(!args.GuessResult.LikelyVillagerPattern.ContainsKey(judge.Agent))
                {
                    continue;
                }
                ((judge.Result == Species.HUMAN) ? receiveWhiteList : receiveBlackList).Add(judge.Target);
            }
            foreach(Agent agent in args.Agi.AliveAgentList)
            {
                // 人間判定
                if(receiveWhiteList.Contains(agent))
                {
                    double voteScore = Between((0.7 + day * 0.2), 0.001, 1.0);
                    double devineScore = 1.0;
                    double guardScore = 1.2;
                    double attackScore = 3.0;

                    requestList.Add(new ActionRequest(agent)
                    {
                        Vote = voteScore,
                        Devine = devineScore,
                        Guard = guardScore,
                        Attack = attackScore,
                    });

                }
                // 人狼判定
                if(receiveBlackList.Contains(agent))
                {
                    double voteScore = Between((2.4 - day * 0.7), 1.0, 10.0);
                    double devineScore = 1.0;
                    double guardScore = Between((0.1 + day * 0.02), 0.001, 0.5);
                    double attackScore = 0.0005;

                    requestList.Add(new ActionRequest(agent)
                    {
                        Vote = voteScore,
                        Devine = devineScore,
                        Guard = guardScore,
                        Attack = attackScore,
                    });
                }
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
        private double Between( double value, double min, double max )
        {
            if ( value < min )
            {
                return min;
            }

            if ( value > max )
            {
                return max;
            }

            return value;
        }

    }
}

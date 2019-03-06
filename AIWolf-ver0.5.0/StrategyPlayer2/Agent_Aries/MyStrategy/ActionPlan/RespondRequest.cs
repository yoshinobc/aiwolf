using AIWolf.Lib;
using System;
using System.Collections.Generic;
using System.Text;
using Wepwawet.Guess;
using Wepwawet.ActionPlan;
using Wepwawet.ActionPlan.Tree;
using Agent_Aries.Lib;

namespace Agent_Aries.MyStrategy.ActionPlan
{
    /// <summary>
    /// 行動戦略「要求に応える」クラス
    /// 概要：他者のREQUESTに応じた動作をする
    /// </summary>
    class RespondRequest : IActionPlannerNode
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


            // 各エージェントの信用度を求める
            Dictionary<Agent, double> agentTrustScore = new Dictionary<Agent, double>();
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
                
                likelyVilScore = Math.Max( likelyVilScore, 0.0 );
                likelyWolfScore = Math.Max( likelyWolfScore, 0.0 );
                likelyPosScore = Math.Max( likelyPosScore, 0.0 );

                double suspiciousScore = Math.Max( likelyWolfScore, likelyPosScore / 2 );
                
                double trustScore = (likelyVilScore + 0.001) / (suspiciousScore + 0.001);

                // 一定以上信用できる人物のみ登録
                if ( trustScore > 2.50 )
                {
                    agentTrustScore.Add( agent, trustScore );
                }
            }

            // 一定以上信用できる人物を走査
            foreach ( KeyValuePair<Agent,double> keyValue in agentTrustScore )
            {
                // 投票要求先のエージェント
                Agent requestVoteAgent = null;

                foreach ( ExtTalk talk in args.Agi.TodayInfo.TalkList )
                {
                    // 発話エージェント一致
                    if ( talk.Agent.Equals( keyValue.Key ) )
                    {
                        Content content = talk.Content;
                        // REQUEST
                        if ( content.Operator == Operator.REQUEST )
                        {
                            Content refContent = content.ContentList[0];
                            // VOTEのREQUEST
                            if ( refContent.Topic == Topic.VOTE )
                            {
                                requestVoteAgent = refContent.Target;
                            }
                        }
                    }
                }

                // VOTEのREQUEST先が存在する
                if ( requestVoteAgent != null )
                {
                    requestList.Add( new ActionRequest( requestVoteAgent )
                    {
                        Vote = 1.2,
                    } );
                }
            }

            // 実行成功にする
            isSuccess = true;
        }

    }
}

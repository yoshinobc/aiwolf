using AIWolf.Lib;
using System;
using System.Collections.Generic;
using System.Text;
using Wepwawet.Guess;
using Wepwawet.ActionPlan;
using Wepwawet.ActionPlan.Tree;
using Agent_Aries.Lib;
using Wepwawet.Lib;
using Wepwawet.Guess.Tree;
using Wepwawet.SampleStrategy.Guess;
using Agent_Aries.MyStrategy.Guess;

namespace Agent_Aries.MyStrategy.ActionPlan
{
    class PossessedVote : IActionPlannerNode
    {
        bool isSuccess = false;
        List<ActionRequest> requestList = new List<ActionRequest>();

        ParallelTreeGuess guessRoot = null;
        
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

            // 初回実行時、推理戦略の設定
            if(guessRoot == null)
            {
                guessRoot = new ParallelTreeGuess();
                guessRoot.Child.Add(new FirstImpression());
                guessRoot.Child.Add(new Learn_Talk());
                guessRoot.Child.Add(new AllFake());
                guessRoot.Child.Add(new VoteLine());
                if(args.Agi.GameSetting.PlayerNum == 5)
                {
                    guessRoot.Child.Add(new COPattern());
                    guessRoot.Child.Add(new VoteTarget_5());
                    guessRoot.Child.Add(new KusoMeta5());
                }
                if(args.Agi.GameSetting.PlayerNum == 15)
                {
                    guessRoot.Child.Add(new VoteTarget_15());
                    guessRoot.Child.Add(new KusoMeta15());
                }
            }

            TreeGuessManager guessManager = new TreeGuessManager() { GuessStrategy = guessRoot };
            GuessStrategyArgs gArgs = new GuessStrategyArgs() { Agi = args.Agi, Items = args.Items };
            // 推理する視点
            Viewpoint vp = args.Agi.SelfPossessedViewTrustInfo;
            if(vp.MonsterSidePattern.Count <= 0)
            {
                vp = args.Agi.AllViewSystemInfo;
            }
            GuessResult posGuess = guessManager.Exec(gArgs, vp);


            foreach(Agent agent in args.Agi.AliveAgentList)
            {
                // 自分は対象外
                if(agent.Equals(args.Agi.Me))
                {
                    continue;
                }

                double likelyVilScore = 0.0;
                if(posGuess.LikelyVillagerPattern.ContainsKey(agent))
                {
                    likelyVilScore = posGuess.LikelyVillagerPattern[agent].Score;
                }

                double likelyWolfScore = 0.0;
                if(posGuess.LikelyWolfPattern.ContainsKey(agent))
                {
                    likelyWolfScore = posGuess.LikelyWolfPattern[agent].Score;
                }

                double likelyPosScore = 0.0;
                if(posGuess.LikelyPossessedPattern.ContainsKey(agent))
                {
                    likelyPosScore = posGuess.LikelyPossessedPattern[agent].Score;
                }

                double suspiciousScore = Math.Max(likelyWolfScore, likelyPosScore / 2);

                if(likelyVilScore < 0.0001)
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
                    suspiciousScore = Math.Max(likelyWolfScore, likelyPosScore / 2);
                    suspiciousScore = Math.Min(Math.Max(suspiciousScore / likelyVilScore, 0.001), 2.0);
                }

                requestList.Add(new ActionRequest(agent)
                {
                    Vote = 1 / suspiciousScore,
                    Devine = Math.Pow(suspiciousScore, 0.4),
                });
            }

            // 実行成功にする
            isSuccess = true;
        }

    }
}

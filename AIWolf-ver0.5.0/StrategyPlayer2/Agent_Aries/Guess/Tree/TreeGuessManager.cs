using AIWolf.Lib;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using Wepwawet.Condition;
using Wepwawet.Lib;

namespace Wepwawet.Guess.Tree
{
    class TreeGuessManager : IGuessManager
    {

        /// <summary>
        /// 推理戦略Rootノード
        /// </summary>
        public IGuessNode GuessStrategy { get; set; }


        public GuessResult Exec(GuessStrategyArgs args)
        {
            // 推理戦略の更新処理
            GuessStrategy.Update(args);

            // 戦略から部分内訳に対する推理リストを取得
            List<PartGuess> guessList = GuessStrategy.IsSuccess() ? GuessStrategy.GetGuess() : new List<PartGuess>();

            // 推理する視点
            Viewpoint vp = args.Agi.SelfViewTrustInfo;
            if(args.Agi.SelfViewTrustInfo.MonsterSidePattern.Count <= 0)
            {
                vp = args.Agi.SelfViewSystemInfo;
            }

            // 推理結果の集計を取得
            GuessResult result = GetResult(args, guessList, vp);

            return result;
        }

        public GuessResult Exec(GuessStrategyArgs args, Viewpoint vp)
        {
            // 推理戦略の更新処理
            GuessStrategy.Update(args);

            // 戦略から部分内訳に対する推理リストを取得
            List<PartGuess> guessList = GuessStrategy.IsSuccess() ? GuessStrategy.GetGuess() : new List<PartGuess>();
            
            // 推理結果の集計を取得
            GuessResult result = GetResult(args, guessList, vp);

            return result;
        }


        /// <summary>
        /// 推理結果の集計を取得
        /// </summary>
        /// <param name="args">推理戦略の引数</param>
        /// <param name="guessList">部分内訳への推理</param>
        private GuessResult GetResult(GuessStrategyArgs args, List<PartGuess> guessList, Viewpoint vp)
        {
            GuessResult result = new GuessResult();

            List<Agent> agentList = args.Agi.AgentList;

            // 複数エージェントに対する推理
            List<PartGuess> MultiAgentGuess = new List<PartGuess>(guessList.Count);

            // エージェント単体の推理を先に計算する（高速化）
            double[] singleWolfScore = new double[agentList.Count + 1];
            double[] singlePossessedScore = new double[agentList.Count + 1];
            foreach(Agent agent in agentList)
            {
                singleWolfScore[agent.AgentIdx] = 1.0;
                singlePossessedScore[agent.AgentIdx] = 1.0;
            }
            // 各推理が内訳条件を満たせばスコアを適用する
            foreach(PartGuess guess in guessList)
            {
                if(guess.Condition is RoleCondition)
                {
                    RoleCondition con = ((RoleCondition)guess.Condition);
                    if(con.Role == Role.WEREWOLF)
                    {
                        singleWolfScore[con.Agent.AgentIdx] *= guess.Correlation;
                    }
                    else if(con.Role == Role.POSSESSED)
                    {
                        singlePossessedScore[con.Agent.AgentIdx] *= guess.Correlation;
                    }
                }
                else if(guess.Condition is TeamCondition)
                {
                    TeamCondition con = ((TeamCondition)guess.Condition);
                    if(con.Team == Team.WEREWOLF)
                    {
                        singleWolfScore[con.Agent.AgentIdx] *= guess.Correlation;
                        singlePossessedScore[con.Agent.AgentIdx] *= guess.Correlation;
                    }
                    else
                    {
                        singleWolfScore[con.Agent.AgentIdx] /= guess.Correlation;
                        singlePossessedScore[con.Agent.AgentIdx] /= guess.Correlation;
                    }
                }
                else
                {
                    MultiAgentGuess.Add(guess);
                }
            }

            // 各集計用
            double allLikelyScore = double.MinValue;
            double[] likelyWolfScore = Enumerable.Repeat(double.MinValue, agentList.Count + 1).ToArray();
            double[] likelyPosScore = Enumerable.Repeat(double.MinValue, agentList.Count + 1).ToArray();
            double[] likelyFoxScore = Enumerable.Repeat(double.MinValue, agentList.Count + 1).ToArray();
            double[] likelyVilScore = Enumerable.Repeat(double.MinValue, agentList.Count + 1).ToArray();

            // 各内訳にスコアを付ける
            result.AllPattern = new List<AggregateGuess>(vp.MonsterSidePattern.Count);
            foreach(MonsterSidePattern pattern in vp.MonsterSidePattern.Values)
            {
                AggregateGuess gpattern = new AggregateGuess(pattern);
                result.AllPattern.Add(gpattern);

                // エージェント単体の推理を反映（高速化）
                foreach(Agent agent in gpattern.Pattern.WerewolfAgent)
                {
                    gpattern.Score *= singleWolfScore[agent.AgentIdx];
                }
                foreach(Agent agent in gpattern.Pattern.PossessedAgent)
                {
                    gpattern.Score *= singlePossessedScore[agent.AgentIdx];
                }

                // 各推理が内訳条件を満たせばスコアを適用する
                foreach(PartGuess guess in MultiAgentGuess)
                {
                    if(guess.Condition.IsMatch(pattern))
                    {
                        gpattern.Score *= guess.Correlation;
                    }
                }

                // 以下集計処理
                // 個人毎の最も村陣営・人狼・狂人・妖狐スコアの大きい内訳を集計する
                HashSet<Agent> remainAgent = new HashSet<Agent>();
                foreach(Agent agent in agentList)
                {
                    remainAgent.Add(agent);
                }

                foreach(Agent agent in gpattern.Pattern.WerewolfAgent)
                {
                    if(gpattern.Score > likelyWolfScore[agent.AgentIdx])
                    {
                        result.LikelyWolfPattern[agent] = gpattern;
                        likelyWolfScore[agent.AgentIdx] = gpattern.Score;
                    }
                    remainAgent.Remove(agent);
                }

                foreach(Agent agent in gpattern.Pattern.PossessedAgent)
                {
                    if(gpattern.Score > likelyPosScore[agent.AgentIdx])
                    {
                        result.LikelyPossessedPattern[agent] = gpattern;
                        likelyPosScore[agent.AgentIdx] = gpattern.Score;
                    }
                    remainAgent.Remove(agent);
                }

                foreach(Agent agent in gpattern.Pattern.FoxAgent)
                {
                    if(gpattern.Score > likelyFoxScore[agent.AgentIdx])
                    {
                        result.LikelyFoxPattern[agent] = gpattern;
                        likelyFoxScore[agent.AgentIdx] = gpattern.Score;
                    }
                    remainAgent.Remove(agent);
                }

                foreach(Agent agent in remainAgent)
                {
                    if(gpattern.Score > likelyVilScore[agent.AgentIdx])
                    {
                        result.LikelyVillagerPattern[agent] = gpattern;
                        likelyVilScore[agent.AgentIdx] = gpattern.Score;
                    }
                }

                // 最もスコアの大きい内訳を集計する
                if(gpattern.Score > allLikelyScore)
                {
                    result.LikelyPattern = gpattern;
                    allLikelyScore = gpattern.Score;
                }
            }

            return result;
        }


    }
}

using AIWolf.Lib;
using System;
using System.Collections.Generic;
using System.Text;
using Wepwawet.Guess;
using Wepwawet.Lib;

namespace Wepwawet.Player
{
    public class GuessResultExt : GuessResult
    {
        
        /// <summary>
        /// 保有戦略毎の推理一覧
        /// </summary>
        public Dictionary<HasGuessStrategy, List<PartGuess>> PartGuess { get; set; }
        

        /// <summary>
        /// コンストラクタ
        /// </summary>
        /// <param name="partGuess">保有戦略毎の推理結果</param>
        /// <param name="args">推理戦略の引数</param>
        public GuessResultExt( List<HasGuessStrategy> guessStrategy, GuessStrategyArgs args)
        {
            // 各推理戦略から推理結果を取得
            PartGuess = new Dictionary<HasGuessStrategy, List<PartGuess>>();
            foreach (HasGuessStrategy strategy in guessStrategy)
            {
                PartGuess.Add(strategy, strategy.Strategy?.GetGuess(args));
            }

            // 集計を行う
            Calc(args);
        }


        /// <summary>
        /// 集計を行う
        /// </summary>
        /// <param name="args">推理戦略の引数</param>
        private void Calc( GuessStrategyArgs args )
        {
            // 各内訳にスコアを付ける
            AllPattern = new List<AggregateGuess>();
            foreach (MonsterSidePattern pattern in args.Agi.SelfViewSystemInfo.MonsterSidePattern)
            {
                AggregateGuess gpattern = new AggregateGuess(pattern);
                AllPattern.Add(gpattern);

                // 各推理が内訳条件を満たせばスコアを適用する
                foreach (HasGuessStrategy strategy in PartGuess.Keys)
                {
                    foreach (PartGuess guess in PartGuess[strategy])
                    {
                        if (guess.Condition.IsMatch(pattern))
                        {
                            gpattern.Score *= guess.Correlation; Math.Pow(guess.Correlation, strategy.Weight);
                        }
                    }
                }
            }

            // 各集計を行う
            foreach ( AggregateGuess gpattern in AllPattern )
            {
                // 個人毎の最も村陣営・人狼・狂人・妖狐スコアの大きい内訳を集計する
                foreach ( Agent agent in args.Agi.LatestGameInfo.AgentList )
                {
                    if ( gpattern.Pattern.IsExist(agent, Role.WEREWOLF) )
                    {
                        if ( !LikelyWolfPattern.ContainsKey( agent ) || gpattern.Score > LikelyWolfPattern[agent].Score )
                        {
                            LikelyWolfPattern[agent] = gpattern;
                        }
                    }
                    else if ( gpattern.Pattern.IsExist( agent, Role.POSSESSED ) )
                    {
                        if ( !LikelyPossessedPattern.ContainsKey( agent ) || gpattern.Score > LikelyPossessedPattern[agent].Score )
                        {
                            LikelyPossessedPattern[agent] = gpattern;
                        }
                    }
                    else if ( gpattern.Pattern.IsExist( agent, Role.FOX ) )
                    {
                        if ( !LikelyFoxPattern.ContainsKey( agent ) || gpattern.Score > LikelyFoxPattern[agent].Score )
                        {
                            LikelyFoxPattern[agent] = gpattern;
                        }
                    }
                    else
                    {
                        if ( !LikelyVillagerPattern.ContainsKey( agent ) || gpattern.Score > LikelyVillagerPattern[agent].Score )
                        {
                            LikelyVillagerPattern[agent] = gpattern;
                        }
                    }
                }

                // 最もスコアの大きい内訳を集計する
                if ( LikelyPattern == null || gpattern.Score > LikelyPattern.Score )
                {
                    LikelyPattern = gpattern;
                }
            }
            

        }


    }
}

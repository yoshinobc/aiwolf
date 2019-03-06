using AIWolf.Lib;
using System;
using System.Collections.Generic;
using System.Text;
using Wepwawet.Guess;
using Wepwawet.Lib;

namespace Wepwawet.Guess
{
    public class GuessResult
    {

        /// <summary>
        /// 内訳に対する推理集計
        /// </summary>
        public List<AggregateGuess> AllPattern { get; set; }


        /// <summary>
        /// 最もスコアが高い内訳
        /// </summary>
        public AggregateGuess LikelyPattern { get; set; }



        /// <summary>
        /// エージェント毎の最も村陣営（人外に含まれない）スコアが高い内訳
        /// エージェントを含む内訳が存在しない場合、キー値自体が存在しない
        /// </summary>
        public Dictionary<Agent, AggregateGuess> LikelyVillagerPattern { get; set; } = new Dictionary<Agent, AggregateGuess>();


        /// <summary>
        /// エージェント毎の最も人狼スコアが高い内訳
        /// エージェントを含む内訳が存在しない場合、キー値自体が存在しない
        /// </summary>
        public Dictionary<Agent, AggregateGuess> LikelyWolfPattern { get; set; } = new Dictionary<Agent, AggregateGuess>();


        /// <summary>
        /// エージェント毎の最も裏切り者スコアが高い内訳
        /// エージェントを含む内訳が存在しない場合、キー値自体が存在しない
        /// </summary>
        public Dictionary<Agent, AggregateGuess> LikelyPossessedPattern { get; set; } = new Dictionary<Agent, AggregateGuess>();

        
        /// <summary>
        /// エージェント毎の最も妖狐スコアが高い内訳
        /// エージェントを含む内訳が存在しない場合、キー値自体が存在しない
        /// </summary>
        public Dictionary<Agent, AggregateGuess> LikelyFoxPattern { get; set; } = new Dictionary<Agent, AggregateGuess>();



    }
}

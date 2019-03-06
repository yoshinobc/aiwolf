using AIWolf.Lib;
using System;
using System.Collections.Generic;
using System.Text;
using Wepwawet.Condition;
using Wepwawet.Guess;
using Wepwawet.Guess.Tree;

namespace Wepwawet.SampleStrategy.Guess
{
    /// <summary>
    /// 推理戦略「第一印象」クラス
    /// 概要：全員にランダムなスコアを付ける
    /// </summary>
    public class FirstImpression : IGuessNode
    {
        private List<PartGuess> guessList = new List<PartGuess>();

        public void Update( GuessStrategyArgs args )
        {
            if ( guessList.Count == 0 )
            {
                Random r = new System.Random();
                guessList = new List<PartGuess>();
                foreach ( Agent agent in args.Agi.AgentList )
                {
                    guessList.Add( new PartGuess()
                    {
                        Condition = TeamCondition.GetCondition( agent, Team.WEREWOLF ),
                        Correlation = 0.95 + r.NextDouble() * 0.1
                    } );
                }
            }
        }

        public List<PartGuess> GetGuess()
        {
            return guessList;
        }

        public bool IsSuccess()
        {
            // 未生成(Execを実行せず呼ばれた)の場合のみ実行失敗
            return (guessList.Count != 0);
        }
    }
}

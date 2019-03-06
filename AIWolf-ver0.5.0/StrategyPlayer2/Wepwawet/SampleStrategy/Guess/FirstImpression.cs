using AIWolf.Lib;
using System;
using System.Collections.Generic;
using System.Text;
using Wepwawet.Condition;
using Wepwawet.Guess;

namespace Wepwawet.SampleStrategy.Guess
{
    /// <summary>
    /// 推理戦略「第一印象」クラス
    /// 概要：全員にランダムなスコアを付ける
    /// 目的：同一スコアの回避、意見の分散
    /// </summary>
    public class FirstImpression : IGuessStrategy
    {

        private List<PartGuess> guess = null;

        public List<PartGuess> GetGuess(GuessStrategyArgs args)
        {

            if (guess == null)
            {
                Random r = new System.Random();
                guess = new List<PartGuess>();
                foreach ( Agent agent in args.Agi.LatestGameInfo.AgentList )
                {
                    RoleCondition wolfCondition = RoleCondition.GetCondition(agent, Role.WEREWOLF);
                    RoleCondition posCondition = RoleCondition.GetCondition(agent, Role.POSSESSED);

                    guess.Add(new PartGuess(){
                        Condition = new OrCondition().AddCondition(wolfCondition).AddCondition(posCondition),
                        Correlation = 0.95 + r.NextDouble() * 0.1
                    });
                }
            }
            
            return guess;
            
        }

    }
}

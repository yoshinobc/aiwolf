using AIWolf.Lib;
using System;
using System.Collections.Generic;
using System.Text;
using Wepwawet.Condition;
using Wepwawet.Lib;
using Wepwawet.Guess;
using Wepwawet.Guess.Tree;

namespace Agent_Aries.MyStrategy.Guess
{
    /// <summary>
    /// 推理戦略「全偽」クラス
    /// 概要：CO役職が全て偽の内訳に低スコアを付ける
    /// </summary>
    public class AllFake : IGuessNode
    {
        bool isSuccess = false;
        List<PartGuess> guessList = new List<PartGuess>();
        

        public List<PartGuess> GetGuess()
        {
            return guessList;
        }

        public bool IsSuccess()
        {
            return isSuccess;
        }

        public void Update( GuessStrategyArgs args )
        {

            // 実行結果のクリア
            guessList.Clear();

            // 役職CO者を取得
            List<Agent> seerList = args.Agi.GetComingOutAgent( Role.SEER );
            List<Agent> mediumList = args.Agi.GetComingOutAgent( Role.MEDIUM );
            List<Agent> bodyguardList = args.Agi.GetComingOutAgent( Role.BODYGUARD );

            // 占い師が全偽のパターン
            if ( seerList.Count > 0 )
            {
                BitCondition con = new BitCondition();
                foreach ( Agent agent in seerList )
                {
                    con.AddWerewolfTeam( agent );
                }
                guessList.Add( new PartGuess()
                {
                    Condition = con,
                    Correlation = 0.2,
                } );
            }

            // ５人村－狂人が騙っていないパターン
            if(args.Agi.AgentList.Count == 5 && seerList.Count > 0)
            {
                BitCondition con = new BitCondition();
                foreach(Agent agent in seerList)
                {
                    con.AddNotPossessed(agent);
                }
                guessList.Add(new PartGuess()
                {
                    Condition = con,
                    Correlation = 0.3,
                });
            }

            // １５人村－狂人が騙っていないパターン
            if(args.Agi.AgentList.Count == 15 && seerList.Count + mediumList.Count >= 4)
            {
                BitCondition con = new BitCondition();
                foreach(Agent agent in seerList)
                {
                    con.AddNotPossessed(agent);
                }
                foreach(Agent agent in mediumList)
                {
                    con.AddNotPossessed(agent);
                }
                guessList.Add(new PartGuess()
                {
                    Condition = con,
                    Correlation = 0.3,
                });
            }

            // 霊媒師が全偽のパターン
            if ( mediumList.Count > 0 )
            {
                BitCondition con = new BitCondition();
                foreach ( Agent agent in mediumList )
                {
                    con.AddWerewolfTeam( agent );
                }
                guessList.Add( new PartGuess()
                {
                    Condition = con,
                    Correlation = 0.1,
                } );
            }

            // 実行成功にする
            isSuccess = true;

        }

    }
}

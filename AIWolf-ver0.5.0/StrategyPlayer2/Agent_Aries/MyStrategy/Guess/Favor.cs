using AIWolf.Lib;
using System;
using System.Collections.Generic;
using System.Text;
using Wepwawet.Condition;
using Wepwawet.Guess;
using Wepwawet.Guess.Tree;

namespace Agent_Aries.MyStrategy.Guess
{
    /// <summary>
    /// 推理戦略「仲間贔屓」クラス
    /// 概要：仲間狼の人外スコアを下げる
    /// </summary>
    class Favor : IGuessNode
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
            
            // 仲間狼の人外スコアを下げる
            foreach ( KeyValuePair<Agent,Role> keyValue in args.Agi.RoleMap )
            {
                if ( keyValue.Value == Role.WEREWOLF )
                {
                    guessList.Add( new PartGuess()
                    {
                        Condition = TeamCondition.GetCondition( keyValue.Key, Team.WEREWOLF ),
                        Correlation = 0.8
                    } );
                }
            }

            // 実行成功にする
            isSuccess = true;
        }

    }
}

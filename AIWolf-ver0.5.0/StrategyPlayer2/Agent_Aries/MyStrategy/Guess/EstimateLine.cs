using AIWolf.Lib;
using System;
using System.Collections.Generic;
using System.Text;
using Wepwawet.Condition;
using Wepwawet.Lib;
using Wepwawet.Guess;
using Wepwawet.Guess.Tree;
using Agent_Aries.Lib;

namespace Agent_Aries.MyStrategy.Guess
{
    /// <summary>
    /// 推理戦略「推理ライン」クラス
    /// </summary>
    public class EstimateLine : IGuessNode
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
            isSuccess = false;
            guessList.Clear();

            // 初日は実行失敗
            if(args.Agi.Day <= 1)
            {
                return;
            }

            DayInfo dayInfo = args.Agi.DayInfo[1];

            foreach(ExtTalk talk in dayInfo.TalkList)
            {
                if(talk.Content.Operator == Operator.NOP && talk.Content.Topic == Topic.ESTIMATE && talk.Content.Role == Role.WEREWOLF)
                {
                    BitCondition con = new BitCondition();
                    con.AddWerewolf(talk.Agent);
                    con.AddWerewolf(talk.Content.Target);

                    guessList.Add(new PartGuess()
                    {
                        Condition = con,
                        Correlation = 0.95,
                    });
                }
            }
            
            // 実行成功にする
            isSuccess = true;

        }
    }
}

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
    /// 推理戦略「投票ライン」クラス
    /// </summary>
    public class VoteLine : IGuessNode
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

            foreach ( DayInfo dayInfo in args.Agi.DayInfo.Values )
            {
                // その日の投票の取得
                VoteAnalyzer voteAnalyzer = null;
                if ( dayInfo.VoteList.Count > 0 )
                {
                    // 実際の投票(1回目の投票のみ)
                    voteAnalyzer = new VoteAnalyzer( dayInfo.VoteList[0] );
                }
                else if ( dayInfo.Equals( args.Agi.TodayInfo ) )
                {
                    // 投票宣言
                    voteAnalyzer = new VoteAnalyzer( dayInfo.LatestAliveAgentList, dayInfo.TalkList, Topic.VOTE );
                }

                if ( voteAnalyzer != null )
                {
                    foreach ( KeyValuePair<Agent,Agent> vote in voteAnalyzer.VoteMap )
                    {
                        if ( vote.Value != null )
                        {
                            BitCondition con = new BitCondition();
                            con.AddWerewolf( vote.Key );
                            con.AddWerewolf( vote.Value );
                            guessList.Add( new PartGuess()
                            {
                                Condition = con,
                                Correlation = 0.7,
                            } );

                            con = new BitCondition();
                            con.AddNotWerewolf(vote.Key);
                            con.AddNotWerewolf(vote.Value);
                            guessList.Add(new PartGuess()
                            {
                                Condition = con,
                                Correlation = 0.95,
                            });
                        }
                    }

                }
                
            }

            // 実行成功にする
            isSuccess = true;

        }
    }
}

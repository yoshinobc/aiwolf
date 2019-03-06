using AIWolf.Lib;
using System;
using System.Collections.Generic;
using System.Linq;
using Wepwawet.Condition;
using Wepwawet.Lib;
using Wepwawet.Guess;
using Wepwawet.Guess.Tree;

namespace Agent_Aries.MyStrategy.Guess
{
    /// <summary>
    /// 推理戦略「投票対象」クラス
    /// </summary>
    public class VoteTarget_15 : IGuessNode
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

            // 0日目は抜ける
            if(args.Agi.Day <= 0)
            {
                return;
            }
            
            // 1日目の投票の取得
            DayInfo dayInfo = args.Agi.DayInfo[1];
            VoteAnalyzer voteAnalyzer = null;
            if ( dayInfo.VoteList.Count > 0 )
            {
                // 実際の投票(1回目の投票のみ)
                voteAnalyzer = new VoteAnalyzer( dayInfo.VoteList[0] );
            }
            else
            {
                // 投票宣言
                voteAnalyzer = new VoteAnalyzer( dayInfo.LatestAliveAgentList, dayInfo.TalkList, Topic.VOTE );
            }

            if ( voteAnalyzer != null )
            {
                List<Agent> seerList = args.Agi.GetComingOutAgent(Role.SEER);
                List<Agent> mediumList = args.Agi.GetComingOutAgent(Role.MEDIUM);
                // 黒判定を抽出
                List<Agent> receiveBlack = new List<Agent>();
                foreach(Wepwawet.Lib.Judge judge in args.Agi.SeerJudge)
                {
                    if(judge.JudgeTalk.Day == 1 && judge.Result == Species.WEREWOLF && !seerList.Contains(judge.Target) && !mediumList.Contains(judge.Target))
                    {
                        receiveBlack.Add(judge.Target);
                    }
                }

                if(mediumList.Count >= 2 && receiveBlack.Count <= 0)
                {
                    foreach ( KeyValuePair<Agent,Agent> vote in voteAnalyzer.VoteMap )
                    {
                        if ( mediumList.Contains(vote.Value) )
                        {
                            guessList.Add( new PartGuess()
                            {
                                Condition = RoleCondition.GetCondition(vote.Key, Role.WEREWOLF),
                                Correlation = 0.7,
                            } );
                        }
                    }
                }
                
                if(receiveBlack.Count >= 1)
                {
                    foreach(KeyValuePair<Agent, Agent> vote in voteAnalyzer.VoteMap)
                    {
                        if(vote.Value != null && !receiveBlack.Contains(vote.Value) && !(receiveBlack.Count == 1 && receiveBlack.Contains(vote.Key)))
                        {
                            guessList.Add(new PartGuess()
                            {
                                Condition = RoleCondition.GetCondition(vote.Key, Role.WEREWOLF),
                                Correlation = 1.2,
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

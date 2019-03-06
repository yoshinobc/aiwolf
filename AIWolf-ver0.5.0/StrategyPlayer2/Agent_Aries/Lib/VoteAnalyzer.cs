using Agent_Aries.Lib;
using AIWolf.Lib;
using System;
using System.Collections.Generic;
using System.Text;

namespace Wepwawet.Lib
{
    public class VoteAnalyzer
    {

        /// <summary>全員の投票先 Key.投票元 Value.投票先（無宣言は投票先がNull）</summary>
        public Dictionary<Agent, Agent> VoteMap { get; private set; } = new Dictionary<Agent, Agent>();

        /// <summary>被投票数</summary>
        public Dictionary<Agent, int> ReceiveVoteCount { get; private set; } = new Dictionary<Agent, int>();

        /// <summary>被投票率（無宣言も総数に含む）</summary>
        public Dictionary<Agent, double> ReceiveVoteRate { get; private set; } = new Dictionary<Agent, double>();

        /// <summary>被投票数が最大のエージェント</summary>
        public List<Agent> MaxVoteReceivedAgent { get; private set; } = new List<Agent>();

        /// <summary>最大被投票数</summary>
        public int MaxVoteReceiveCount { get; private set; }


        /// <summary>
        /// コンストラクタ
        /// </summary>
        /// <param name="voteList"></param>
        public VoteAnalyzer( List<Vote> voteList )
        {
            foreach ( Vote vote in voteList )
            {
                VoteMap.Add( vote.Agent, vote.Target );
            }

            // 各計算を行う
            Calc();
        }


        /// <summary>
        /// コンストラクタ（発話リストから投票宣言先を取得）
        /// </summary>
        /// <param name="talkList">発話リスト</param>
        /// <param name="topic">宣言を読み取るTopic(VOTE、ATTACK以外は無効)</param>
        public VoteAnalyzer( List<Agent> agentList,  List<ExtTalk> talkList, Topic topic = Topic.VOTE )
        {
            // 投票宣言先の初期化
            foreach ( Agent agent in agentList )
            {
                VoteMap.Add( agent, null );
            }
            
            // 全発話を読む
            foreach ( ExtTalk talk in talkList )
            {
                Content content = talk.Content;

                // 投票宣言
                if ( content.Operator == Operator.NOP && content.Topic == topic )
                {
                    VoteMap[talk.Agent] = content.Target;
                }
            }

            // 各計算を行う
            Calc();
        }


        private void Calc()
        {
            // 初期化
            foreach ( Agent agent in VoteMap.Keys )
            {
                ReceiveVoteCount.Add( agent, 0 );
            }

            // 被投票数のカウント
            foreach ( Agent agent in VoteMap.Values )
            {
                // 存在しないエージェントに投票宣言することもあるので存在チェックする
                if ( agent != null && VoteMap.ContainsKey(agent) )
                {
                    ReceiveVoteCount[agent]++;
                    // 被投票数の最大を記憶
                    MaxVoteReceiveCount = Math.Max( MaxVoteReceiveCount, ReceiveVoteCount[agent] );
                }
            }
            
            // 被投票率のカウント
            foreach ( KeyValuePair<Agent, int> keyValue in ReceiveVoteCount )
            {
                ReceiveVoteRate.Add( keyValue.Key, keyValue.Value / (double)VoteMap.Count );
            }

            // 最大得票者の取得
            foreach ( KeyValuePair<Agent, int> keyValue in ReceiveVoteCount )
            {
                if ( keyValue.Value == MaxVoteReceiveCount )
                {
                    MaxVoteReceivedAgent.Add( keyValue.Key );
                }
            }
        }
        

    }
}

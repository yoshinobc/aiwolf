using Agent_Aries.Lib;
using AIWolf.Lib;
using System;
using System.Collections.Generic;
using System.Text;

namespace Wepwawet.Lib
{
    public class DayInfo
    {

        /// <summary>
        /// 日にち
        /// </summary>
        public int Day { get; private set; }


        #region 昼の情報

        /// <summary>
        /// 昼の生存エージェント
        /// </summary>
        public List<Agent> DayTimeAliveAgent { get; private set; }

        /// <summary>
        /// 通常会話リスト
        /// </summary>
        public List<ExtTalk> TalkList { get; private set; } = new List<ExtTalk>();

        /// <summary>
        /// 直前のターンに行われた通常会話リスト
        /// </summary>
        public List<ExtTalk> LatestTalkList { get; private set; } = new List<ExtTalk>();

        /// <summary>
        /// 発話で宣言した投票先
        /// </summary>
        public VoteAnalyzer SaidVote { get; private set; }

        #endregion


        #region 夕方の情報

        /// <summary>
        /// 追放投票リスト
        /// </summary>
        public List<List<Vote>> VoteList { get; private set; } = new List<List<Vote>>();

        /// <summary>
        /// この日追放したエージェント
        /// </summary>
        public Agent ExecuteAgent { get; private set; }

        #endregion
        

        #region 夜の情報

        /// <summary>
        /// 夜の生存エージェント(当日の昼はNull)
        /// </summary>
        public List<Agent> NightAliveAgent { get; private set; }
        
        /// <summary>
        /// 翌朝に死体で発見されたエージェント
        /// </summary>
        public List<Agent> AttackDeadAgent { get; private set; }
        
        /// <summary>
        /// 襲撃の対象になったエージェント
        /// </summary>
        public Agent TryAttackAgent { get; private set; }
        
        /// <summary>
        /// 護衛の対象になったエージェント
        /// </summary>
        public Agent GuardAgent { get; private set; }

        /// <summary>
        /// 襲撃投票リスト
        /// </summary>
        public List<List<Vote>> AttackVoteList { get; private set; } = new List<List<Vote>>();

        /// <summary>
        /// 囁き会話リスト
        /// </summary>
        public List<ExtTalk> WhisperList { get; private set; } = new List<ExtTalk>();

        #endregion


        /// <summary>
        /// 最新の生存エージェント（当日夜以降は夜の生存エージェント、それ以前は昼の生存エージェント）
        /// </summary>
        public List<Agent> LatestAliveAgentList
        {
            get
            {
                return NightAliveAgent ?? DayTimeAliveAgent;
            }
        }

        

        /// <summary>
        /// コンストラクタ(日が始まった時の情報を設定)
        /// </summary>
        /// <param name="gameInfo">ゲーム情報</param>
        public DayInfo( GameInfo gameInfo )
        {
            Day = gameInfo.Day;
            DayTimeAliveAgent = gameInfo.AliveAgentList;

            // Talk、Whisperを追加登録する
            ApendTalkList( gameInfo );

            // 0日目に限り、夜の生存エージェントも同時に設定する
            if ( Day == 0 )
            {
                NightAliveAgent = gameInfo.AliveAgentList;
            }
        }


        /// <summary>
        /// 情報を更新する
        /// </summary>
        /// <param name="gameInfo"></param>
        public void Update( GameInfo gameInfo )
        {

            // Talk、Whisperを追加登録する
            ApendTalkList( gameInfo );

            if ( NightAliveAgent == null )
            {
                // 追放再投票になった直後の処理
                // 第３回大会では再投票１回なので大丈夫だが、２回以上の場合は要改良
                if ( gameInfo.LatestExecutedAgent != null && gameInfo.LatestVoteList.Count > 0 )
                {
                    VoteList.Add( gameInfo.LatestVoteList );
                }

                // 襲撃再投票になった直後の処理
                // 第３回大会では再投票１回なので大丈夫だが、２回以上の場合は要改良
                if ( gameInfo.LatestAttackVoteList.Count > 0 )
                {
                    AttackVoteList.Add( gameInfo.LatestAttackVoteList );
                }

                // 夜になった直後の処理
                if ( gameInfo.LatestExecutedAgent != null )
                {
                    ExecuteAgent = gameInfo.LatestExecutedAgent;
                    NightAliveAgent = gameInfo.AliveAgentList;

                    VoteList.Add( gameInfo.VoteList );
                }
            }
            
        }


        /// <summary>
        /// 情報を更新する(翌日のデータから)
        /// </summary>
        public void UpdateFromTomorrow( GameInfo gameInfo )
        {
            AttackDeadAgent = gameInfo.LastDeadAgentList;
            
            GuardAgent = gameInfo.GuardedAgent;
            TryAttackAgent = gameInfo.AttackedAgent;

            if ( NightAliveAgent == null )
            {
                // 夜のデータが設定されていない場合(夜の行動が無い役職)
                ExecuteAgent = gameInfo.ExecutedAgent;

                NightAliveAgent = new List<Agent>( DayTimeAliveAgent );
                NightAliveAgent.Remove( ExecuteAgent );

                VoteList.Add( gameInfo.VoteList );
            }
            else
            {
                // 夜のデータが設定されている場合(夜の行動が有る役職)
                if ( gameInfo.AttackVoteList.Count > 0 )
                {
                    AttackVoteList.Add( gameInfo.AttackVoteList );
                }
            }
        }

        /// <summary>
        /// Talk、Whisperを追加登録する
        /// </summary>
        /// <param name="gameInfo"></param>
        private void ApendTalkList( GameInfo gameInfo )
        {
            LatestTalkList.Clear();

            for( int i = TalkList.Count; i < gameInfo.TalkList.Count; i++ )
            {
                ExtTalk newTalk = new ExtTalk(gameInfo.TalkList[i]);
                TalkList.Add(newTalk);
                LatestTalkList.Add(newTalk);
            }

            for ( int i = WhisperList.Count; i < gameInfo.WhisperList.Count; i++ )
            {
                WhisperList.Add( new ExtTalk( gameInfo.WhisperList[i] ) );
            }

            // 発言で宣言した投票先を取得
            SaidVote = new VoteAnalyzer( DayTimeAliveAgent, TalkList, Topic.VOTE );
        }
        
    }
}

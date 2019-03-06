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
        public int Day { get; set; }

        /// <summary>
        /// 生存エージェント(昼の開始時に設定)
        /// </summary>
        public List<Agent> AliveAgent { get; set; }

        /// <summary>
        /// 死体で発見されたエージェント(昼の開始時に設定)
        /// </summary>
        public List<Agent> AttackDeadAgent { get; set; }

        /// <summary>
        /// 占い結果(昼の開始時に設定)
        /// </summary>
        public Judge DivineResult { get; set; }
        
        /// <summary>
        /// 霊媒結果(昼の開始時に設定)
        /// </summary>
        public Judge MediumResult { get; set; }

        /// <summary>
        /// 襲撃の対象になったエージェント(昼の開始時に設定)
        /// </summary>
        public Agent TryAttackAgent { get; set; }
        
        /// <summary>
        /// 護衛の対象になったエージェント(昼の開始時に設定)
        /// </summary>
        public Agent GuardAgent { get; set; }
        
        /// <summary>
        /// 通常会話リスト
        /// </summary>
        public List<Talk> TalkList { get; set; }
        
        /// <summary>
        /// 通常会話リスト コンテンツ版
        /// </summary>
        public List<Content> TalkContentList { get; set; } = new List<Content>();
        
        /// <summary>
        /// 囁き会話リスト
        /// </summary>
        public List<Whisper> WhisperList { get; set; }
        
        /// <summary>
        /// 囁き会話リスト コンテンツ版
        /// </summary>
        public List<Content> WhisperContentList { get; set; } = new List<Content>();
        
        /// <summary>
        /// この日追放したエージェント(昼の投票が終わってから設定)
        /// </summary>
        public Agent ExecuteAgent { get; set; }
        
        /// <summary>
        /// 追放投票リスト(昼の投票が終わってから設定)
        /// </summary>
        public List<List<Vote>> VoteList { get; set; } = new List<List<Vote>>();
        
        /// <summary>
        /// 襲撃投票リスト(夜の襲撃が終わってから設定)
        /// </summary>
        public List<List<Vote>> AttackVoteList { get; set; } = new List<List<Vote>>();
        
    }
}

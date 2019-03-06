using AIWolf.Lib;
using System;
using System.Collections.Generic;
using System.Text;

namespace Wepwawet.Lib
{
    public class Judge
    {

        /// <summary>
        /// 判定を出したエージェント
        /// </summary>
        public Agent Agent { get; private set; }

        /// <summary>
        /// 判定を出されたエージェント
        /// </summary>
        public Agent Target { get; private set; }

        /// <summary>
        /// 判定結果
        /// </summary>
        public Species Result { get; private set; }

        /// <summary>
        /// 判定を出した発話
        /// </summary>
        public Talk JudgeTalk { get; private set; }
        
        /// <summary>
        /// 判定を取り消した発話
        /// </summary>
        public Talk CancelTalk { get; private set; }


        /// <summary>
        /// コンストラクタ
        /// </summary>
        /// <param name="judgeTalk">判定を出した発話</param>
        public Judge( Talk judgeTalk )
        {
            Agent = judgeTalk.Agent;
            JudgeTalk = judgeTalk;

            Content content = new Content( JudgeTalk.Text );
            Target = content.Target;
            Result = content.Result;
        }

        /// <summary>
        /// コンストラクタ
        /// </summary>
        /// <param name="agent">判定を出したエージェント</param>
        /// <param name="target">判定を出されたエージェント</param>
        /// <param name="result">判定結果</param>
        /// <param name="judgeTalk">判定を出した発話</param>
        public Judge( Agent agent, Agent target, Species result, Talk judgeTalk )
        {
            Agent = agent;
            Target = target;
            Result = result;
            JudgeTalk = judgeTalk;
        }
        

        public void Cancel( Talk cancelTalk )
        {
            CancelTalk = cancelTalk;
        }


        /// <summary>
        /// 最新データ時点で有効な判定か
        /// </summary>
        /// <returns></returns>
        public bool IsEnable()
        {
            return (CancelTalk == null);
        }


        public override string ToString()
        {
            return Agent.ToString() + " said " + Target.AgentIdx + " is " + Result.ToString();
        }

    }
}

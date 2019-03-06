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
        public Agent Agent { get; set; }

        /// <summary>
        /// 判定を出されたエージェント
        /// </summary>
        public Agent Target { get; set; }

        /// <summary>
        /// 判定結果
        /// </summary>
        public Species Result { get; set; }

        /// <summary>
        /// 判定を出した発話
        /// </summary>
        public Talk JudgeTalk { get; set; }


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

    }
}

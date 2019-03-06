using System;
using System.Collections.Generic;
using System.Text;
using Wepwawet.Lib;

namespace Wepwawet.Guess
{
    public class AggregateGuess
    {

        /// <summary>
        /// 人外陣営のパターン
        /// </summary>
        public MonsterSidePattern Pattern { get; private set; }

        /// <summary>
        /// 総合スコア
        /// </summary>
        public double Score { get; set; } = 1.0;


        /// <summary>
        /// コンストラクタ
        /// </summary>
        /// <param name="pattern">人外陣営のパターン</param>
        public AggregateGuess(MonsterSidePattern pattern)
        {
            Pattern = pattern;
        }

    }
}

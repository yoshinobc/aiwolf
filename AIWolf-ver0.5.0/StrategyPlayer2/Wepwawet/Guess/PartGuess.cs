using Wepwawet.Condition;
using System;
using System.Collections.Generic;
using System.Text;

namespace Wepwawet.Guess
{

    /// <summary>
    /// 部分内訳に対する推理を表現するクラス
    /// </summary>
    public class PartGuess
    {

        /// <summary>
        /// スコア係数を適用する条件
        /// </summary>
        public ICondition Condition { get; set; }

        /// <summary>
        /// スコア係数
        /// </summary>
        public double Correlation { get; set; } = 1.0;

    }
}

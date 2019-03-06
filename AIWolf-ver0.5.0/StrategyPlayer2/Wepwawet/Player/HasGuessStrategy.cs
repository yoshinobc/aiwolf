using System;
using System.Collections.Generic;
using System.Text;
using Wepwawet.Guess;

namespace Wepwawet.Player
{

    /// <summary>
    /// 保有する推理戦略を表すクラス
    /// </summary>
    public class HasGuessStrategy
    {

        /// <summary>
        /// 推理戦略
        /// </summary>
        public IGuessStrategy Strategy { get; set; }

        /// <summary>
        /// 戦略の重さ（スコア係数をWeight乗する）
        /// </summary>
        public double Weight { get; set; } = 1.0;

        /// <summary>
        /// 隠し戦略か（人狼の作為等、表に出さない戦略か）
        /// </summary>
        public bool Hidden { get; set; }
        
    }

}

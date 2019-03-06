using System;
using System.Collections.Generic;
using System.Text;

namespace Wepwawet.Guess
{

    /// <summary>
    /// 推理戦略クラスのインタフェース
    /// </summary>
    public interface IGuessStrategy
    {

        /// <summary>
        /// 推理を取得する
        /// </summary>
        /// <param name="args">引数</param>
        /// <returns>推理のリスト</returns>
        List<PartGuess> GetGuess(GuessStrategyArgs args);

    }
}

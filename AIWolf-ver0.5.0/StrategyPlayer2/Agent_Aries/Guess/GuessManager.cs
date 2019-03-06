using System;
using System.Collections.Generic;
using System.Text;

namespace Wepwawet.Guess
{
    public interface IGuessManager
    {

        /// <summary>
        /// 推理を実行する
        /// </summary>
        /// <param name="args">推理引数</param>
        /// <returns>推理結果</returns>
        GuessResult Exec( GuessStrategyArgs args );
        
    }
}

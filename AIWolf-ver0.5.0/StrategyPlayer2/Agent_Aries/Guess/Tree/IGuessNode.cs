using System;
using System.Collections.Generic;
using System.Text;

namespace Wepwawet.Guess.Tree
{
    public interface IGuessNode
    {

        /// <summary>
        /// 推理を更新する
        /// </summary>
        /// <param name="args">推理引数</param>
        void Update( GuessStrategyArgs args );

        /// <summary>
        /// 最後の更新が成功したかを取得する
        /// Update()未実行時の動作は不定
        /// </summary>
        /// <returns>実行が成功したか</returns>
        bool IsSuccess();

        /// <summary>
        /// 最後の更新結果の推理を取得する
        /// Update()未実行時、及びIsSuccess()==False時の動作は不定
        /// </summary>
        /// <returns>最後の更新結果の推理</returns>
        List<PartGuess> GetGuess();
        
    }
}

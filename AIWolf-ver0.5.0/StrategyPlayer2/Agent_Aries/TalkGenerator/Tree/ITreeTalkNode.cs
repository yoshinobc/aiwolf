using System;
using System.Collections.Generic;
using System.Text;

namespace Wepwawet.TalkGenerator.Tree
{
    public interface ITreeTalkNode
    {

        /// <summary>
        /// 発話生成を実行する
        /// </summary>
        /// <param name="args">発話引数</param>
        void Exec( TalkGeneratorArgs args );

        /// <summary>
        /// 実行が成功したかを取得する
        /// Update()未実行時の動作は不定
        /// </summary>
        /// <returns>実行が成功したか</returns>
        bool IsSuccess();

        /// <summary>
        /// 発話候補を取得する
        /// Update()未実行時、及びIsSuccess()==False時の動作は不定
        /// </summary>
        /// <returns>発話候補の内容とスコア</returns>
        Dictionary<string, double> GetTalk();

    }
}

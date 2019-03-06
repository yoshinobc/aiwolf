using System;
using System.Collections.Generic;
using System.Text;

namespace Wepwawet.ActionPlan.Tree
{
    public interface IActionPlannerNode
    {

        /// <summary>
        /// 実行が成功したかを取得する
        /// </summary>
        /// <returns>実行が成功したか</returns>
        bool IsSuccess();

        /// <summary>
        /// 行動要求を取得する
        /// Update()未実行時の動作は不定
        /// </summary>
        /// <returns>行動要求</returns>
        List<ActionRequest> GetRequest();

        /// <summary>
        /// 行動要求を更新する
        /// Update()未実行時、及びIsSuccess()==False時の動作は不定
        /// </summary>
        /// <param name="args">行動引数</param>
        void Update( ActionStrategyArgs args );

    }
}

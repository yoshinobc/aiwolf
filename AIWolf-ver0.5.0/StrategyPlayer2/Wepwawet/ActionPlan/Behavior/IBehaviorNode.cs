using System;
using System.Collections.Generic;
using System.Text;

namespace Wepwawet.ActionPlan.Tree
{
    public interface IBehaviorNode
    {

        /// <summary>
        /// 実行が成功したかを取得する
        /// </summary>
        /// <returns>実行が成功したか</returns>
        bool IsExecuteSuccess( ActionStrategyArgs args );
        
        /// <summary>
        /// 行動要求を取得する
        /// </summary>
        /// <returns>行動要求</returns>
        List<ActionRequest> GetRequest( ActionStrategyArgs args );

    }
}

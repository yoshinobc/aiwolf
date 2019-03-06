using AIWolf.Lib;
using System;
using System.Collections.Generic;
using System.Text;

namespace Wepwawet.ActionPlan
{
    public interface IActionPlanner
    {

        /// <summary>
        /// 行動方針の決定を実行する
        /// </summary>
        Plan Execute( ActionStrategyArgs args );
        
    }
}

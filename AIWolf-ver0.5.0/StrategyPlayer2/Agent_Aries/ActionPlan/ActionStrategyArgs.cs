using Wepwawet.Lib;
using Wepwawet.Guess;
using System.Collections.Generic;

namespace Wepwawet.ActionPlan
{
    /// <summary>
    /// 行動戦略の引数クラス
    /// </summary>
    public class ActionStrategyArgs
    {

        public AdvanceGameInfo Agi { get; set; }
        
        public GuessResult GuessResult { get; set; }
        
        public Dictionary<string, object> Items { get; set; } = new Dictionary<string, object>();

    }
}

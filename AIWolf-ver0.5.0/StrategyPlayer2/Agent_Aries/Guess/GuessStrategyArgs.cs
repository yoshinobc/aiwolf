using System.Collections.Generic;
using Wepwawet.Lib;

namespace Wepwawet.Guess
{
    /// <summary>
    /// 推理戦略の引数クラス
    /// </summary>
    public class GuessStrategyArgs
    {

        public AdvanceGameInfo Agi { get; set; }

        public Dictionary<string, object> Items { get; set; } = new Dictionary<string, object>();

    }
}

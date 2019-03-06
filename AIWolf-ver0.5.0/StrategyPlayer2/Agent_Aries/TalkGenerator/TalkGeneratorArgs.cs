using System;
using System.Collections.Generic;
using System.Text;
using Wepwawet.ActionPlan;
using Wepwawet.Guess;
using Wepwawet.Lib;

namespace Wepwawet.TalkGenerator
{
    public class TalkGeneratorArgs
    {

        /// <summary>
        /// ゲーム情報
        /// </summary>
        public AdvanceGameInfo Agi { get; set; }

        /// <summary>
        /// 推理結果
        /// </summary>
        public GuessResult GuessResult { get; set; }

        /// <summary>
        /// 行動方針
        /// </summary>
        public Plan ActionPlan { get; set; }

        /// <summary>
        /// その他のアイテム
        /// </summary>
        public Dictionary<string, Object> Items { get; set; } = new Dictionary<string, object>();


    }
}

using System;
using System.Collections.Generic;
using System.Text;

namespace Wepwawet.TalkGenerator
{
    public interface ITalkGenerator
    {

        /// <summary>
        /// 発話を生成する
        /// </summary>
        /// <returns>生成された発話</returns>
        string Generation( TalkGeneratorArgs args );

    }
}

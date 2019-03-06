using System;
using System.Collections.Generic;
using System.Text;

namespace Wepwawet.Lib
{
    public class ExtRandom
    {
        public static ExtRandom Instance { get; } = new ExtRandom();

        public Random Rnd { get; } = new Random();

        /// <summary>
        /// singletonコンストラクタ
        /// </summary>
        private ExtRandom()
        {
        }

    }
}

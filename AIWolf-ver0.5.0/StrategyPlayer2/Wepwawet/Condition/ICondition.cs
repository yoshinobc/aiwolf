using Wepwawet.Lib;
using System;
using System.Collections.Generic;
using System.Text;

namespace Wepwawet.Condition
{
    /// <summary>
    /// 条件クラスのインタフェース
    /// </summary>
    public interface ICondition
    {

        /// <summary>
        /// 条件を満たすかを取得する
        /// </summary>
        /// <param name="pattern">検査する人外陣営のパターン</param>
        /// <returns></returns>
        bool IsMatch( MonsterSidePattern pattern );

    }
}

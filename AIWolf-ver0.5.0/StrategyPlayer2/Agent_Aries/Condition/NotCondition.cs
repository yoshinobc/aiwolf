using System;
using System.Collections.Generic;
using System.Text;
using Wepwawet.Lib;

namespace Wepwawet.Condition
{
    /// <summary>
    /// NOT条件を表現するクラス
    /// </summary>
    public class NotCondition : ICondition
    {

        /// <summary>
        /// NOTする条件
        /// </summary>
        private ICondition condition;

        /// <summary>
        /// コンストラクタ
        /// </summary>
        /// <param name="condition">NOTする条件</param>
        public NotCondition(ICondition condition)
        {
            this.condition = condition;
        }

        public bool IsMatch(MonsterSidePattern pattern)
        {
            // 条件が無い場合は、条件を満たしていない扱いにする
            if (condition == null )
            {
                return false;
            }

            // 条件へのマッチをNOTして返す
            return !condition.IsMatch(pattern);
        }
        

        public override string ToString()
        {
            return "not(" + condition.ToString() + ")";
        }

    }
}

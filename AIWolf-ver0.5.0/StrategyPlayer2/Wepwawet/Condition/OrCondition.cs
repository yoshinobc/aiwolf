using System;
using System.Collections.Generic;
using System.Text;
using Wepwawet.Lib;

namespace Wepwawet.Condition
{
    /// <summary>
    /// OR条件を表現するクラス
    /// </summary>
    public class OrCondition : ICondition
    {

        /// <summary>
        /// 条件のリスト
        /// </summary>
        private List<ICondition> conditions = new List<ICondition>();

        public bool IsMatch(MonsterSidePattern pattern)
        {

            // 条件が１つも無い場合は、条件を満たした扱いにする
            if ( conditions.Count == 0 )
            {
                return true;
            }

            // 条件を１つずつチェックし、１つでも満せば　OR条件を満たす
            foreach (ICondition condition in conditions)
            {
                if ( condition.IsMatch(pattern) )
                {
                    return true;
                }
            }

            // 条件を満たすものが無ければOR条件を満たさない
            return false;
            
        }

        /// <summary>
        /// 条件を追加する(chainable)
        /// </summary>
        /// <param name="condition">追加する条件</param>
        /// <returns>自分自身のOR条件</returns>
        public OrCondition AddCondition(ICondition condition)
        {
            conditions.Add(condition);
            return this;
        }


        public override string ToString()
        {
            string ret = "";
            foreach ( ICondition condition in conditions )
            {
                ret += ((ret.Length == 0) ? "(" : " or ") + condition.ToString();
            }
            ret += ")";
            return ret;
        }

    }
}

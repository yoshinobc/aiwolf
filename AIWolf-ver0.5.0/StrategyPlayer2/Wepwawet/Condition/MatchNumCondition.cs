using System;
using System.Collections.Generic;
using System.Text;
using Wepwawet.Lib;

namespace Wepwawet.Condition
{
    /// <summary>
    /// マッチ数による条件を表現するクラス
    /// </summary>
    public class MatchNumCondition : ICondition
    {

        /// <summary>
        /// 条件のリスト
        /// </summary>
        private List<ICondition> conditions = new List<ICondition>();

        /// <summary>
        /// 最低マッチ数
        /// </summary>
        public int MinNum { get; set; }

        /// <summary>
        /// 最高マッチ数
        /// </summary>
        public int MaxNum { get; set; }
        

        public bool IsMatch(MonsterSidePattern pattern)
        {
            int matchNum = 0;
            
            // 条件を１つずつチェック
            foreach (ICondition condition in conditions)
            {
                if ( condition.IsMatch(pattern) )
                {
                    matchNum++;
                }
            }

            // マッチ数が最低～最高の範囲内かを返す
            return (matchNum >= MinNum && matchNum <= MaxNum);
        }

        /// <summary>
        /// 条件を追加する(chainable)
        /// </summary>
        /// <param name="condition">追加する条件</param>
        /// <returns>自分自身の条件</returns>
        public MatchNumCondition AddCondition(ICondition condition)
        {
            conditions.Add(condition);
            return this;
        }


        public override string ToString()
        {
            string ret = "Match(";
            foreach ( ICondition condition in conditions )
            {
                ret += condition.ToString() + ", ";
            }
            ret += MinNum + " to " + MaxNum + ")";
            return ret;
        }

    }
}

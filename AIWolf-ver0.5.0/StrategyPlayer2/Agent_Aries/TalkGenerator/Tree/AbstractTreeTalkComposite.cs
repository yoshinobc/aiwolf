using System;
using System.Collections.Generic;
using System.Text;

namespace Wepwawet.TalkGenerator.Tree
{
    public abstract class AbstractTreeTalkComposite : ITreeTalkNode
    {
        protected bool isSuccess = false;
        protected Dictionary<string, double> talkList = new Dictionary<string, double>();

        /// <summary>
        /// 実行条件（実行時に全て評価し、Falseが１つでもあれば実行失敗になる）
        /// </summary>
        public List<Predicate<TalkGeneratorArgs>> Condition { get; } = new List<Predicate<TalkGeneratorArgs>>();
        

        /// <summary>
        /// 発話生成を実行する
        /// </summary>
        /// <param name="args">発話引数</param>
        public void Exec( TalkGeneratorArgs args )
        {

            // 条件を１つでも満たさなければ実行失敗
            foreach ( Predicate<TalkGeneratorArgs> condition in Condition )
            {
                if ( !condition.Invoke( args ) )
                {
                    isSuccess = false;
                    return;
                }
            }

            // 発話生成を実行する（各クラス固有の内部動作）
            DoUpdate( args );

        }


        public bool IsSuccess()
        {
            return isSuccess;
        }


        public Dictionary<string, double> GetTalk()
        {
            if ( IsSuccess() )
            {
                return talkList;
            }

            return new Dictionary<string, double>();
        }


        /// <summary>
        /// 発話生成を実行する（各クラス固有の内部動作）
        /// </summary>
        /// <returns>発話候補の内容とスコア</returns>
        public abstract void DoUpdate( TalkGeneratorArgs args );

    }
}

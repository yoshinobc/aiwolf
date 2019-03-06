using AIWolf.Lib;
using System;
using System.Collections.Generic;
using System.Text;

namespace Wepwawet.TalkGenerator.Tree
{
    class TreeTalkGenerator : ITalkGenerator
    {

        /// <summary>
        /// 発話生成に使う戦略
        /// </summary>
        public ITreeTalkNode ActionStrategy { get; set; }


        public string Generation( TalkGeneratorArgs args )
        {

            // 戦略が設定されていない場合
            if( ActionStrategy == null )
            {
                return Content.OVER.Text;
            }

            ActionStrategy.Exec( args );

            if ( ActionStrategy.IsSuccess() )
            {
                Dictionary<string, double> ret = ActionStrategy.GetTalk();

                string maxScoreText = Content.OVER.Text;
                double maxScore = double.MinValue;

                foreach ( KeyValuePair<string, double> talk in ret )
                {
                    if ( talk.Value > maxScore )
                    {
                        maxScore = talk.Value;
                        maxScoreText = talk.Key;
                    }
                }

                return maxScoreText;
            }
            else
            {
                // 実行に失敗した場合
                return Content.OVER.Text;
            }
            
        }

    }
}

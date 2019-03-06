using System;
using System.Collections.Generic;
using System.Text;

namespace Wepwawet.TalkGenerator.Tree
{
    class TreeTalkParallel : AbstractTreeTalkComposite
    {

        public List<ITreeTalkNode> Child { get; set; } = new List<ITreeTalkNode>();
        
        public override void DoUpdate( TalkGeneratorArgs args )
        {
            isSuccess = false;
            talkList = new Dictionary<string, double>();

            // 子ノードを実行する
            foreach ( ITreeTalkNode node in Child )
            {
                node.Exec( args );

                // １つでも実行成功したら実行成功として続きのノードも続行
                if ( node.IsSuccess() )
                {
                    isSuccess = true;

                    // 結果のマージ
                    foreach ( KeyValuePair<string,double> talk in node.GetTalk() )
                    {
                        if ( talkList.ContainsKey(talk.Key) )
                        {
                            talkList.Add(talk.Key, talk.Value);
                        }
                        else
                        {
                            talkList[talk.Key] *= talk.Value;
                        }
                    }
                }
            }
        }

    }
}

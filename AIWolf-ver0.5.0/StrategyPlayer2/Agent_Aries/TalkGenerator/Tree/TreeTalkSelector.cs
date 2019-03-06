using System;
using System.Collections.Generic;
using System.Text;

namespace Wepwawet.TalkGenerator.Tree
{
    class TreeTalkSelector : AbstractTreeTalkComposite
    {

        public List<ITreeTalkNode> Child { get; set; } = new List<ITreeTalkNode>();

        public override void DoUpdate( TalkGeneratorArgs args )
        {
            isSuccess = false;

            // 子ノードを実行する
            foreach ( ITreeTalkNode node in Child )
            {
                node.Exec( args );

                // １つでも実行成功したら実行成功として終了
                if ( node.IsSuccess() )
                {
                    isSuccess = true;
                    talkList = node.GetTalk();
                    return;
                }
            }
        }

    }
}

using System;
using System.Collections.Generic;
using System.Text;
using AIWolf.Lib;
using Wepwawet.TalkGenerator.Tree;
using Wepwawet.TalkGenerator;

namespace Agent_Aries.MyStrategy.TalkGenerator
{
    class FakeSeerReport : ITreeTalkNode
    {
        private bool isSuccess = false;
        private Dictionary<string, double> talkList;

        private int reportCount = 0;

        public Dictionary<string, double> GetTalk()
        {
            return talkList;
        }


        public bool IsSuccess()
        {
            return isSuccess;
        }


        public void Exec( TalkGeneratorArgs args )
        {
            isSuccess = false;
            talkList = new Dictionary<string, double>();

            // 騙り判定を取得できなければ実行失敗にする
            if ( !args.Items.ContainsKey("FakeSeerJudge") )
            {
                return;
            }

            // 騙り判定の取得
            List<Judge> judgeList = (List<Judge>) args.Items["FakeSeerJudge"];

            // 全て報告済みなら実行失敗にする
            if ( judgeList.Count <= reportCount )
            {
                return;
            }

            Judge reportJudge = judgeList[reportCount];

            DivinedResultContentBuilder builder = new DivinedResultContentBuilder( reportJudge.Target, reportJudge.Result );
            talkList.Add( new Content( builder ).Text, 999.0 );

            // 報告件数のカウント（必ずこの発話を返す前提）
            reportCount++;

            isSuccess = true;
        }

    }
}

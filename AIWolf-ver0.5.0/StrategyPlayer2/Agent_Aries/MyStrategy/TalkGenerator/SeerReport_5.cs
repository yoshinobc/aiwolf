using System;
using System.Collections.Generic;
using System.Text;
using System.Linq;
using AIWolf.Lib;
using Wepwawet.TalkGenerator.Tree;
using Wepwawet.TalkGenerator;
using Wepwawet.Guess;

namespace Agent_Aries.MyStrategy.TalkGenerator
{
    class SeerReport_5 : ITreeTalkNode
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

            // 全て報告済みなら実行失敗にする
            if ( args.Agi.MySeerJudge.Count <= reportCount )
            {
                return;
            }
            
            List<Agent> seerList = args.Agi.GetComingOutAgent(Role.SEER);

            Judge reportJudge = args.Agi.MySeerJudge[reportCount];

            // 初回占い結果が人間かつ対抗でない場合、次点占い先候補に人狼判定を出す
            if(args.Agi.Day == 1 && reportJudge.Result == Species.HUMAN && !seerList.Contains(reportJudge.Target) && new System.Random().NextDouble() < 0.9)
            {
                reportJudge = new Judge(0, args.Agi.Me, args.ActionPlan.DevineTarget, Species.WEREWOLF);
            }

            DivinedResultContentBuilder builder = new DivinedResultContentBuilder( reportJudge.Target, reportJudge.Result );
            talkList.Add( new Content( builder ).Text, 999.0 );

            // 報告件数のカウント（必ずこの発話を返す前提）
            reportCount++;
            
            isSuccess = true;
        }

    }
}

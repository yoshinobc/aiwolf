using System;
using System.Collections.Generic;
using System.Text;
using AIWolf.Lib;
using Wepwawet.TalkGenerator.Tree;
using Wepwawet.TalkGenerator;

namespace Agent_Aries.MyStrategy.TalkGenerator
{
    class PointingAtVoteTarget : ITreeTalkNode
    {
        private bool isSuccess = false;
        private Dictionary<string, double> talkList;
        
        /// <summary>宣言した投票先</summary>
        private Agent voteTarget = null;
        
        /// <summary>最後に実行した日</summary>
        private int latestUpdateDay = -1;
        

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

            // 日が変わったら宣言していない状態に戻す
            if ( args.Agi.Day > latestUpdateDay )
            {
                voteTarget = null;
            }

            // ４人以下の場合は宣言しない
            if ( args.Agi.TodayInfo.DayTimeAliveAgent.Count <= 4 )
            {
                return;
            }

            Agent newTarget = args.ActionPlan.VoteTarget;
            if ( newTarget != null && !newTarget.Equals( voteTarget ) )
            {
                voteTarget = newTarget;

                latestUpdateDay = args.Agi.Day;
                isSuccess = true;

                VoteContentBuilder voteBuilder = new VoteContentBuilder( newTarget );
                RequestContentBuilder reqBuilder = new RequestContentBuilder( null, new Content( voteBuilder ) );
                talkList.Add( new Content( reqBuilder ).Text, 1.0 );
            }
        }

    }
}

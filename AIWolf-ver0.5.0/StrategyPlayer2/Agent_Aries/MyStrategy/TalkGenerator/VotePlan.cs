using System;
using System.Collections.Generic;
using System.Text;
using AIWolf.Lib;
using Wepwawet.TalkGenerator.Tree;
using Wepwawet.TalkGenerator;

namespace Agent_Aries.MyStrategy.TalkGenerator
{
    class VotePlan : ITreeTalkNode
    {
        /// <summary>投票宣言先と一致しても発言する</summary>
        public bool IsForce { get; set; } = false;

        private bool isSuccess = false;
        private Dictionary<string, double> talkList;
        
        
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

            // 宣言済みの投票先
            Agent voteTarget = null;
            if(args.Agi.TodayInfo.SaidVote.VoteMap.ContainsKey(args.Agi.Me))
            {
                voteTarget = args.Agi.TodayInfo.SaidVote.VoteMap[args.Agi.Me];
            }
            
            Agent newTarget = args.ActionPlan.VoteTarget;
            if ( newTarget != null && (IsForce || !newTarget.Equals(voteTarget)))
            {
                voteTarget = newTarget;
                
                isSuccess = true;

                VoteContentBuilder builder = new VoteContentBuilder( newTarget );
                talkList.Add( new Content(builder).Text, 1.0 );
            }
        }

    }
}

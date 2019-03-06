using System;
using System.Collections.Generic;
using System.Text;
using AIWolf.Lib;
using Wepwawet.TalkGenerator.Tree;
using Wepwawet.TalkGenerator;

namespace Agent_Aries.MyStrategy.TalkGenerator
{
    public class TalkComingOut : ITreeTalkNode
    {
        private bool isSuccess = false;
        private Dictionary<string, double> talkList;

        /// <summary>
        /// COする役職
        /// </summary>
        public Role Role { get; set; } = Role.UNC;
        

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
            // 役職が不正な場合
            if ( Role == Role.UNC )
            {
                isSuccess = false;
                return;
            }

            isSuccess = true;
            talkList = new Dictionary<string, double>();

            ComingoutContentBuilder builder = new ComingoutContentBuilder( args.Agi.Me, Role );
            talkList.Add( new Content( builder ).Text, 1.0 );
        }

    }
}

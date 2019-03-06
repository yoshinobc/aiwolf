using System;
using System.Collections.Generic;
using System.Text;
using AIWolf.Lib;
using Wepwawet.TalkGenerator.Tree;
using Wepwawet.TalkGenerator;

namespace Agent_Aries.MyStrategy.TalkGenerator
{
    class Over : ITreeTalkNode
    {
        
        public Dictionary<string, double> GetTalk()
        {
            return new Dictionary<string, double>()
            {
                { Content.OVER.Text, 1.0 }
            };
        }
        
        public bool IsSuccess()
        {
            return true;
        }

        public void Exec( TalkGeneratorArgs args )
        {
            // do nothing
        }

    }
}

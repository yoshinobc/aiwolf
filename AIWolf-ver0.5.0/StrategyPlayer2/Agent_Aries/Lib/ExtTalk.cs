using AIWolf.Lib;
using System;
using System.Collections.Generic;
using System.Text;

namespace Agent_Aries.Lib
{
    /// <summary>
    /// 発話クラス（拡張版）
    /// </summary>
    public class ExtTalk : Talk
    {

        public Content Content { get; private set; }
        
        public ExtTalk( int idx, int day, int turn, Agent agent, string text ) : base( idx, day, turn, agent, text )
        {
            Content = new Content( text );
        }

        public ExtTalk( Talk talk ) : base( talk.Idx, talk.Day, talk.Turn, talk.Agent, talk.Text )
        {
            Content = new Content( talk.Text );
        }

        public ExtTalk( Whisper talk ) : base( talk.Idx, talk.Day, talk.Turn, talk.Agent, talk.Text )
        {
            Content = new Content( talk.Text );
        }

    }
}

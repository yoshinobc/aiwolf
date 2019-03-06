using AIWolf.Lib;
using System;
using System.Collections.Generic;
using System.Text;

namespace Wepwawet.Lib
{
    /// <summary>
    /// カミングアウトを表すクラス
    /// </summary>
    public class ComingOut
    {

        /// <summary>
        /// エージェント
        /// </summary>
        public Agent Agent { get; private set; }

        /// <summary>
        /// 役職
        /// </summary>
        public Role Role { get; private set; }

        /// <summary>
        /// カミングアウトを行った発話
        /// </summary>
        public Talk ComingOutTalk{ get; private set; }

        /// <summary>
        /// カミングアウトを取り消した発話
        /// </summary>
        public Talk CancelTalk { get; private set; }


        public ComingOut(Agent agent, Role role, Talk comingOutTalk)
        {
            Agent = agent;
            Role = role;
            ComingOutTalk = comingOutTalk;
        }


        public void Cancel(Talk cancelTalk)
        {
            CancelTalk = cancelTalk;
        }


        public override string ToString()
        {
            if ( Agent != null )
            {
                return Agent.AgentIdx + " is " + Role.ToString();
            }
            return "Invaild";
        }


    }
}

using AIWolf.Lib;
using System;
using System.Collections.Generic;
using System.Text;

namespace Wepwawet.ActionPlan
{
    public class Plan
    {

        /// <summary>
        /// エージェント毎の集計結果
        /// </summary>
        public Dictionary<Agent, ActionRequest> AggregateRequest { get; set; } = new Dictionary<Agent, ActionRequest>();


        public Agent VoteTarget { get; set; }

        public Agent DevineTarget { get; set; }

        public Agent GuardTarget { get; set; }

        public Agent AttackTarget { get; set; }
        
    }
}

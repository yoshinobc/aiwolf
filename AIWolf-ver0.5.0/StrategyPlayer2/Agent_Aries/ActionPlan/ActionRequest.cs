using AIWolf.Lib;
using System;
using System.Collections.Generic;
using System.Text;

namespace Wepwawet.ActionPlan
{
    public class ActionRequest
    {

        /// <summary>対象のエージェント</summary>
        public Agent Agent { get; private set; }

        /// <summary>追放投票の要求度</summary>
        public double Vote { get; set; } = 1.0;

        /// <summary>占いの要求度</summary>
        public double Devine { get; set; } = 1.0;

        /// <summary>護衛の要求度</summary>
        public double Guard { get; set; } = 1.0;

        /// <summary>襲撃投票の要求度</summary>
        public double Attack { get; set; } = 1.0;

        /// <summary>
        /// コンストラクタ
        /// </summary>
        /// <param name="agent">対象のエージェント</param>
        public ActionRequest(Agent agent)
        {
            Agent = agent;
        }

        public override string ToString()
        {
            return Agent.ToString() + 
                   " Vote:" + Vote.ToString( "N3" ) +
                   " Devine:" + Devine.ToString( "N3" ) +
                   " Guard:" + Guard.ToString( "N3" ) +
                   " Attack:" + Attack.ToString( "N3" );
        }

    }
}

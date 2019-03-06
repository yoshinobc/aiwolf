using System;
using System.Collections.Generic;
using System.Text;
using AIWolf.Lib;

namespace Wepwawet.ActionPlan.Tree
{
    public class TreeActionPlanner : IActionPlanner
    {

        /// <summary>
        /// 行動方針の決定に使う戦略
        /// </summary>
        public IActionPlannerNode ActionStrategy { get; set; }
        

        public Plan Execute( ActionStrategyArgs args )
        {
            List<ActionRequest> requests = new List<ActionRequest>();

            // 戦略が設定されていて、実行に成功した場合
            if ( ActionStrategy != null )
            {
                ActionStrategy.Update( args );

                if ( ActionStrategy.IsSuccess() )
                {
                    // 方針決定戦略から結果を取得
                    requests = ActionStrategy.GetRequest();
                }
            }

            // 行動方針を取得して返す
            return GetPlan( args, requests );
        }
        

        /// <summary>
        /// 行動計画を取得する
        /// </summary>
        /// <param name="args"></param>
        /// <param name="requests"></param>
        protected Plan GetPlan( ActionStrategyArgs args, List<ActionRequest> requests )
        {

            // 全員分の集計を初期化
            Plan plan = new Plan();
            foreach ( Agent agent in args.Agi.AgentList )
            {
                plan.AggregateRequest.Add( agent, new ActionRequest( agent ) );
            }

            // 全計画を集計
            foreach( ActionRequest request in requests )
            {
                if(args.Agi.AgentList.Contains(request.Agent))
                {
                    ActionRequest agentRequest = plan.AggregateRequest[request.Agent];
                    agentRequest.Vote *= request.Vote;
                    agentRequest.Devine *= request.Devine;
                    agentRequest.Guard *= request.Guard;
                    agentRequest.Attack *= request.Attack;
                }
            }

            // 各行動の最大値を持つエージェントを取得
            ActionRequest VoteMax = null;
            ActionRequest DevineMax = null;
            ActionRequest GuardMax = null;
            ActionRequest AttackMax = null;
            foreach ( KeyValuePair<Agent, ActionRequest> val in plan.AggregateRequest )
            {
                if ( VoteMax == null || val.Value.Vote > VoteMax.Vote ) VoteMax = val.Value;
                if ( DevineMax == null || val.Value.Devine > DevineMax.Devine ) DevineMax = val.Value;
                if ( GuardMax == null || val.Value.Guard > GuardMax.Guard ) GuardMax = val.Value;
                if ( AttackMax == null || val.Value.Attack > AttackMax.Attack ) AttackMax = val.Value;
            }
            plan.VoteTarget = VoteMax?.Agent;
            plan.DevineTarget = DevineMax?.Agent;
            plan.GuardTarget = GuardMax?.Agent;
            plan.AttackTarget = AttackMax?.Agent;
            
            return plan;
        }

    }
}

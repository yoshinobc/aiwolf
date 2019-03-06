using System;
using System.Collections.Generic;
using System.Text;
using AIWolf.Lib;

namespace Wepwawet.ActionPlan.Tree
{
    public class BehaviorPlanner : IActionPlanner
    {

        /// <summary>
        /// 行動方針の決定に使う戦略
        /// </summary>
        public IBehaviorNode ActionStrategy { get; set; }


        private Dictionary<Agent, ActionRequest> aggregateRequest = new Dictionary<Agent, ActionRequest>();

        private Agent VoteTarget;
        private Agent DevineTarget;
        private Agent GuardTarget;
        private Agent AttackTarget;


        public void Execute( ActionStrategyArgs args )
        {

            // 戦略が設定されていない場合、実行に失敗した場合は何もしない
            if ( ActionStrategy == null || !ActionStrategy.IsExecuteSuccess( args ) )
            {
                return;
            }

            // 方針決定戦略から結果を取得
            List<ActionRequest> requests = ActionStrategy.GetRequest( args );

            // エージェント毎に行動要求を集計
            Calc( args, requests );

            // 各行動の対象になるエージェントを設定
            SetActionPlan();

        }

        public Agent GetAttack()
        {
            return AttackTarget;
        }

        public Agent GetDevine()
        {
            return DevineTarget;
        }

        public Agent GetGuard()
        {
            return GuardTarget;
        }

        public Agent GetVote()
        {
            return VoteTarget;
        }


        /// <summary>
        /// 行動要求をエージェント毎に集計する
        /// </summary>
        /// <param name="args"></param>
        /// <param name="requests"></param>
        protected void Calc( ActionStrategyArgs args, List<ActionRequest> requests )
        {
            aggregateRequest = new Dictionary<Agent, ActionRequest>();
            foreach ( Agent agent in args.Agi.LatestGameInfo.AgentList )
            {
                aggregateRequest.Add( agent, new ActionRequest( agent ) );
            }

            foreach ( ActionRequest request in requests )
            {
                ActionRequest agentRequest = aggregateRequest[request.Agent];
                agentRequest.Vote *= request.Vote;
                agentRequest.Devine *= request.Devine;
                agentRequest.Guard *= request.Guard;
                agentRequest.Attack *= request.Attack;
            }
        }

        /// <summary>
        /// 各行動の対象になるエージェントを設定
        /// </summary>
        protected void SetActionPlan()
        {
            ActionRequest VoteMax = null;
            ActionRequest DevineMax = null;
            ActionRequest GuardMax = null;
            ActionRequest AttackMax = null;

            foreach ( KeyValuePair<Agent, ActionRequest> val in aggregateRequest ){
                if ( VoteMax == null || val.Value.Vote > VoteMax.Vote ) VoteMax = val.Value;
                if ( DevineMax == null || val.Value.Devine > DevineMax.Devine ) DevineMax = val.Value;
                if ( GuardMax == null || val.Value.Guard > GuardMax.Guard ) GuardMax = val.Value;
                if ( AttackMax == null || val.Value.Attack > AttackMax.Attack ) AttackMax = val.Value;
            }

            VoteTarget = VoteMax?.Agent;
            DevineTarget = DevineMax?.Agent;
            GuardTarget = GuardMax?.Agent;
            AttackTarget = AttackMax?.Agent;
        }


    }
}

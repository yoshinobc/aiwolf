using AIWolf.Lib;
using System;
using System.Linq;
using System.Collections.Generic;
using System.Text;
using Wepwawet.Guess;
using Wepwawet.ActionPlan;
using Wepwawet.ActionPlan.Tree;
using Agent_Aries.Lib;
using Wepwawet.Lib;

namespace Agent_Aries.MyStrategy.ActionPlan
{
    /// <summary>
    /// 行動戦略「狂人を襲撃しない」クラス
    /// 概要：PP直前に狂人候補を襲撃しない
    /// </summary>
    class NoAttackPossessedPP : IActionPlannerNode
    {
        bool isSuccess = false;
        List<ActionRequest> requestList = new List<ActionRequest>();

        
        public bool IsSuccess()
        {
            return isSuccess;
        }


        public List<ActionRequest> GetRequest()
        {
            return requestList;
        }


        public void Update( ActionStrategyArgs args )
        {
            // 実行結果のクリア
            isSuccess = false;
            requestList.Clear();
            
            // PP直前でなければ実行失敗で抜ける
            if( args.Agi.GetAliveWerewolf().Count + 1 < args.Agi.AliveAgentList.Count / 2 ||
                args.Agi.AgentList.Count == args.Agi.AliveAgentList.Count)
            {
                return;
            }
            
            // 役職CO者を取得
            List<Agent> seerList = args.Agi.GetComingOutAgent(Role.SEER);
            List<Agent> mediumList = args.Agi.GetComingOutAgent(Role.MEDIUM);
            List<Agent> bodyguardList = args.Agi.GetComingOutAgent(Role.BODYGUARD);
            List<Agent> villagerList = args.Agi.GetComingOutAgent(Role.VILLAGER);
            
            // 全役職を見て、人間が２人以上いれば狂人候補にする
            List<Agent> possessedAgent = new List<Agent>();
            if(isExistPossessed(args.Agi, seerList))
            {
                possessedAgent.AddRange(seerList);
            }
            else if(isExistPossessed(args.Agi, mediumList))
            {
                possessedAgent.AddRange(mediumList);
            }
            else if(isExistPossessed(args.Agi, bodyguardList))
            {
                possessedAgent.AddRange(bodyguardList);
            }
            else
            {
                //役職にいない場合は村人、未COにいると考える
                possessedAgent.AddRange(args.Agi.AgentList);
                possessedAgent.RemoveAll(seerList.Contains);
                possessedAgent.RemoveAll(mediumList.Contains);
                possessedAgent.RemoveAll(bodyguardList.Contains);
            }

            // 狂人と思われるエージェントがいなければ実行失敗で抜ける
            if ( possessedAgent.Count <= 0 )
            {
                return;
            }

            // 狂人候補が１人でも死亡していれば実行失敗で抜ける
            if(isHumanDead(args.Agi, possessedAgent))
            {
                return;
            }
            
            // 狂人と思われるエージェントの襲撃スコアを下げる
            foreach ( Agent agent in possessedAgent )
            {
                requestList.Add( new ActionRequest( agent )
                {
                    Attack = 0.2,
                } );
            }
            
            // 実行成功にする
            isSuccess = true;
        }


        private bool isExistPossessed(AdvanceGameInfo agi, List<Agent> agentList)
        {
            int humanCnt = 0;

            foreach(Agent agent in agentList )
            {
                if( !isWolf(agi,agent) )
                {
                    humanCnt++;
                }
            }
            
            return humanCnt >= 2;
        }


        private bool isHumanDead(AdvanceGameInfo agi, List<Agent> agentList)
        {
            foreach(Agent agent in agentList)
            {
                if(!isWolf(agi, agent) && !agi.AliveAgentList.Contains(agent))
                {
                    return true;
                }
            }

            return false;
        }


        private bool isWolf( AdvanceGameInfo agi, Agent agent )
        {
            if ( !agi.RoleMap.ContainsKey(agent) )
            {
                return false;
            }

            return agi.RoleMap[agent] == Role.WEREWOLF;
        }

    }
}

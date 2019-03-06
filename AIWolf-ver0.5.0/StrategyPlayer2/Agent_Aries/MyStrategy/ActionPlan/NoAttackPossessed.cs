using AIWolf.Lib;
using System;
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
    /// 概要：狂人を特定できていれば襲撃スコアを下げる
    /// </summary>
    class NoAttackPossessed : IActionPlannerNode
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

            // 占霊判定を合わせたリストを取得
            List<Wepwawet.Lib.Judge> MargeJudge = new List<Wepwawet.Lib.Judge>();
            MargeJudge.AddRange( args.Agi.SeerJudge );
            MargeJudge.AddRange( args.Agi.MediumJudge );

            // 狂人候補
            HashSet<Agent> possessedAgent = new HashSet<Agent>();

            // 全判定を見て、間違っていれば狂人候補にする
            foreach ( Wepwawet.Lib.Judge judge in MargeJudge )
            {
                if ( judge.IsEnable() )
                {
                    if ( (judge.Result == Species.WEREWOLF) != (isWolf( args.Agi, judge.Target )) )
                    {
                        possessedAgent.Add( judge.Agent );
                    }
                }
            }

            // 狂人と思われるエージェントがいなければ実行失敗で抜ける
            if ( possessedAgent.Count <= 0 )
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

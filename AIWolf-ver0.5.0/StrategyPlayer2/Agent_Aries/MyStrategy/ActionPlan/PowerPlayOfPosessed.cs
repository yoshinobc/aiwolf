using AIWolf.Lib;
using System;
using System.Collections.Generic;
using System.Text;
using Wepwawet.Lib;
using Wepwawet.Guess;
using Wepwawet.ActionPlan;
using Wepwawet.ActionPlan.Tree;

namespace Agent_Aries.MyStrategy.ActionPlan
{
    /// <summary>
    /// 行動戦略「パワープレイ（狂人）」クラス
    /// 概要：パワープレイを行う
    /// </summary>
    class PowerPlayOfPosessed : IActionPlannerNode
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
            requestList.Clear();
            
            // 人外CO者を取得
            List<Agent> MonsterSideList = new List<Agent>();
            foreach ( ComingOut co in args.Agi.MonsterSideComingOut )
            {
                if ( co.IsEnable() && !MonsterSideList.Contains(co.Agent) )
                {
                    MonsterSideList.Add( co.Agent );
                }
            }

            // 人外CO者への投票スコアを下げる
            foreach ( Agent agent in MonsterSideList )
            {
                requestList.Add( new ActionRequest( agent )
                {
                    Vote = 0.9
                } );
            }
            
            // 実行成功にする
            isSuccess = true;
        }

    }
}

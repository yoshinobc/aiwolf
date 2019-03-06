using System;
using System.Collections.Generic;
using System.Text;
using Wepwawet.ActionPlan;
using Wepwawet.ActionPlan.Tree;

namespace Agent_Aries.MyStrategy.ActionPlan
{
    /// <summary>
    /// 重みを付けるためのクラス
    /// </summary>
    class Weight : IActionPlannerNode
    {
        private IActionPlannerNode node;
        double weight;

        bool isSuccess = false;
        List<ActionRequest> requestList = new List<ActionRequest>();



        public Weight(IActionPlannerNode node, double weight)
        {
            this.node = node;
            this.weight = weight;
        }


        public bool IsSuccess()
        {
            return isSuccess;
        }


        public List<ActionRequest> GetRequest()
        {
            return requestList;
        }


        public void Update(ActionStrategyArgs args)
        {
            node.Update(args);

            if(node.IsSuccess())
            {
                isSuccess = true;
                requestList = node.GetRequest();
                foreach(ActionRequest request in requestList)
                {
                    request.Vote = Math.Pow(request.Vote, weight);
                    request.Devine = Math.Pow(request.Devine, weight);
                    request.Guard = Math.Pow(request.Guard, weight);
                    request.Attack = Math.Pow(request.Attack, weight);
                }
            }
            else
            {
                isSuccess = false;
                requestList = new List<ActionRequest>();
            }

        }
    }
}

using System;
using System.Collections.Generic;
using System.Text;

namespace Wepwawet.ActionPlan.Tree
{
    public class ParallelTreeAction : IActionPlannerNode
    {

        public List<IActionPlannerNode> Child { get; set; } = new List<IActionPlannerNode>();
        

        public bool IsSuccess()
        {
            foreach ( IActionPlannerNode node in Child )
            {
                if ( node.IsSuccess() ) {
                    return true;
                }
            }
            return false;
        }


        public List<ActionRequest> GetRequest()
        {
            List<ActionRequest> request = new List<ActionRequest>();

            foreach ( IActionPlannerNode node in Child )
            {
                if ( node.IsSuccess() )
                {
                    request.AddRange( node.GetRequest() );
                }
            }
            return request;
        }
        

        public void Update( ActionStrategyArgs args )
        {
            foreach ( IActionPlannerNode node in Child )
            {
                node.Update( args );
            }
        }

    }
}

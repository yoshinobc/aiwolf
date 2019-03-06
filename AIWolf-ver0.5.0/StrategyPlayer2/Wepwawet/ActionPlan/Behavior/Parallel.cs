using System;
using System.Collections.Generic;
using System.Text;

namespace Wepwawet.ActionPlan.Tree
{
    public class Parallel : IBehaviorNode
    {

        public List<IBehaviorNode> Child { get; set; } = new List<IBehaviorNode>();

        List<ActionRequest> request = new List<ActionRequest>();


        public bool IsExecuteSuccess( ActionStrategyArgs args )
        {
            foreach ( IBehaviorNode node in Child )
            {
                if ( node.IsExecuteSuccess( args ) ) {
                    return true;
                }
            }

            return false;
        }

        public List<ActionRequest> GetRequest( ActionStrategyArgs args )
        {
            request = new List<ActionRequest>();

            foreach ( IBehaviorNode node in Child )
            {
                if ( node.IsExecuteSuccess( args ) )
                {
                    request.AddRange( node.GetRequest( args ) );
                }
            }

            return request;
        }

    }
}

using System;
using System.Collections.Generic;
using System.Text;

namespace Wepwawet.Guess.Tree
{
    public class ParallelTreeGuess : IGuessNode
    {

        public List<IGuessNode> Child { get; set; } = new List<IGuessNode>();

        List<PartGuess> guess = new List<PartGuess>();


        public void Update( GuessStrategyArgs args )
        {
            foreach ( IGuessNode node in Child )
            {
                node.Update( args );
            }
        }
        
        public bool IsSuccess()
        {
            foreach ( IGuessNode node in Child )
            {
                if ( node.IsSuccess() )
                {
                    return true;
                }
            }

            return false;
        }

        public List<PartGuess> GetGuess()
        {
            List<PartGuess> guessList = new List<PartGuess>();
            foreach ( IGuessNode node in Child )
            {
                if ( node.IsSuccess() )
                {
                    guessList.AddRange( node.GetGuess() );
                }
            }

            return guessList;
        }

    }
}

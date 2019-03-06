using System;
using System.Collections.Generic;
using System.Text;
using AIWolf.Lib;
using Wepwawet.TalkGenerator.Tree;
using Wepwawet.TalkGenerator;
using Wepwawet.Lib;

namespace Agent_Aries.MyStrategy.TalkGenerator
{
    class Probability : ITreeTalkNode
    {
        public ITreeTalkNode Child { get; }

        public double SuccessProbability = 1.0;

        protected bool isSuccess = false;
        protected Dictionary<string, double> talkList = new Dictionary<string, double>();


        public Probability(ITreeTalkNode child)
        {
            Child = child;
        }

        public Dictionary<string, double> GetTalk()
        {
            return talkList;
        }

        public bool IsSuccess()
        {
            return isSuccess;
        }

        public void Exec(TalkGeneratorArgs args)
        {
            double judgeValue = ExtRandom.Instance.Rnd.NextDouble();

            if(judgeValue < SuccessProbability)
            {
                Child.Exec(args);
                isSuccess = Child.IsSuccess();
                talkList = Child.GetTalk();
            }
            else
            {
                isSuccess = false;
                talkList = new Dictionary<string, double>();
            }
        }

    }
}

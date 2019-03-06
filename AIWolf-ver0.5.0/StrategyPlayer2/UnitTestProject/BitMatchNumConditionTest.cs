using System;
using Microsoft.VisualStudio.TestTools.UnitTesting;
using Agent_Aries;
using Wepwawet.Lib;
using AIWolf.Lib;
using Wepwawet.Condition;
using AIWolf.ReproServer;
using System.Collections.Generic;

namespace UnitTestProject
{
    [TestClass]
    public class BitMatchNumConditionTest
    {
        const int loop = 10000;

        // Å`Å`Ç≈Ç†ÇÈ

        MonsterSidePattern pattern = new MonsterSidePattern(
            new List<Agent>() { Agent.GetAgent(1), Agent.GetAgent(8), Agent.GetAgent(15), },
            new List<Agent>() { Agent.GetAgent(7), },
            new List<Agent>() {}
        );

        [TestMethod]
        public void IsWolf_True1()
        {
            BitMatchNumCondition con = new BitMatchNumCondition() { MinNum = 2, MaxNum = 2 };
            con.AddWerewolf(Agent.GetAgent(1));
            con.AddWerewolf(Agent.GetAgent(15));
            con.AddWerewolf(Agent.GetAgent(7));

            Assert.AreEqual(con.IsMatch(pattern), true);
        }

        [TestMethod]
        public void IsWolf_False1()
        {
            BitMatchNumCondition con = new BitMatchNumCondition() { MinNum = 1, MaxNum = 1 };
            con.AddWerewolf(Agent.GetAgent(1));
            con.AddWerewolf(Agent.GetAgent(15));
            con.AddWerewolf(Agent.GetAgent(7));

            Assert.AreEqual(con.IsMatch(pattern), false);
        }

        [TestMethod]
        public void IsWolf_False2()
        {
            BitMatchNumCondition con = new BitMatchNumCondition() { MinNum = 3, MaxNum = 3 };
            con.AddWerewolf(Agent.GetAgent(1));
            con.AddWerewolf(Agent.GetAgent(15));
            con.AddWerewolf(Agent.GetAgent(7));

            Assert.AreEqual(con.IsMatch(pattern), false);
        }


        /*
         * ë¨ìxån
         */

        [TestCategory("SpeedTest")]
        [TestMethod]
        public void SpeedCheck1()
        {
            BitMatchNumCondition con = new BitMatchNumCondition() { MinNum = 2, MaxNum = 2 };
            con.AddWerewolf(Agent.GetAgent(1));
            con.AddWerewolf(Agent.GetAgent(15));
            con.AddWerewolf(Agent.GetAgent(7));
            
            for(int i = 0; i < loop; i++)
            {
                bool a = con.IsMatch(pattern);
            }
        }
        

    }
}

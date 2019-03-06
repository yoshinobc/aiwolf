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
    public class BitConditionTest
    {
        const int loop = 100000;

        // Å`Å`Ç≈Ç†ÇÈ

        MonsterSidePattern pattern = new MonsterSidePattern(
            new List<Agent>() { Agent.GetAgent(1), Agent.GetAgent(8), Agent.GetAgent(15), },
            new List<Agent>() { Agent.GetAgent(7), },
            new List<Agent>() {}
        );

        [TestMethod]
        public void IsWolf_True1()
        {
            BitCondition con = new BitCondition();
            con.AddWerewolf(Agent.GetAgent(1));

            Assert.AreEqual(con.IsMatch(pattern), true);
        }

        [TestMethod]
        public void IsWolf_True2()
        {
            BitCondition con = new BitCondition();
            con.AddWerewolf(Agent.GetAgent(8));

            Assert.AreEqual(con.IsMatch(pattern), true);
        }

        [TestMethod]
        public void IsWolf_False()
        {
            BitCondition con = new BitCondition();
            con.AddWerewolf(Agent.GetAgent(3));

            Assert.AreEqual(con.IsMatch(pattern), false);
        }

        [TestMethod]
        public void IsPossessed_True()
        {
            BitCondition con = new BitCondition();
            con.AddPossessed(Agent.GetAgent(7));

            Assert.AreEqual(con.IsMatch(pattern), true);
        }

        [TestMethod]
        public void IsPossessed_False()
        {
            BitCondition con = new BitCondition();
            con.AddPossessed(Agent.GetAgent(8));

            Assert.AreEqual(con.IsMatch(pattern), false);
        }

        [TestMethod]
        public void IsWerewolfTeam_True()
        {
            BitCondition con = new BitCondition();
            con.AddWerewolfTeam(Agent.GetAgent(7));
            con.AddWerewolfTeam(Agent.GetAgent(8));

            Assert.AreEqual(con.IsMatch(pattern), true);
        }

        [TestMethod]
        public void IsWerewolfTeam_False()
        {
            BitCondition con = new BitCondition();
            con.AddWerewolfTeam(Agent.GetAgent(14));
            con.AddWerewolfTeam(Agent.GetAgent(15));

            Assert.AreEqual(con.IsMatch(pattern), false);
        }
        
        // Å`Å`Ç≈ÇÕÇ»Ç¢

        [TestMethod]
        public void IsNotWolf_True()
        {
            BitCondition con = new BitCondition();
            con.AddNotWerewolf(Agent.GetAgent(3));

            Assert.AreEqual(con.IsMatch(pattern), true);
        }

        [TestMethod]
        public void IsNotWolf_False()
        {
            BitCondition con = new BitCondition();
            con.AddNotWerewolf(Agent.GetAgent(8));

            Assert.AreEqual(con.IsMatch(pattern), false);
        }
        
        [TestMethod]
        public void IsNotPossessed_True()
        {
            BitCondition con = new BitCondition();
            con.AddNotPossessed(Agent.GetAgent(8));

            Assert.AreEqual(con.IsMatch(pattern), true);
        }

        [TestMethod]
        public void IsNotPossessed_False1()
        {
            BitCondition con = new BitCondition();
            con.AddNotPossessed(Agent.GetAgent(7));

            Assert.AreEqual(con.IsMatch(pattern), false);
        }

        [TestMethod]
        public void IsNotWerewolfTeam_True()
        {
            BitCondition con = new BitCondition();
            con.AddNotWerewolfTeam(Agent.GetAgent(2));
            con.AddNotWerewolfTeam(Agent.GetAgent(3));

            Assert.AreEqual(con.IsMatch(pattern), true);
        }

        [TestMethod]
        public void IsNotWerewolfTeam_False()
        {
            BitCondition con = new BitCondition();
            con.AddNotWerewolfTeam(Agent.GetAgent(7));
            con.AddNotWerewolfTeam(Agent.GetAgent(8));

            Assert.AreEqual(con.IsMatch(pattern), false);
        }
        

        /*
         * ë¨ìxån
         */

        [TestMethod]
        public void SpeedCheck1()
        {
            BitCondition con = new BitCondition();
            con.AddWerewolf(Agent.GetAgent(15));
            
            for(int i = 0; i < loop; i++)
            {
                bool a = con.IsMatch(pattern);
            }
        }
        

    }
}

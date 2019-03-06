using System;
using Microsoft.VisualStudio.TestTools.UnitTesting;
using Agent_Aries;
using Wepwawet.Lib;
using AIWolf.Lib;
using Wepwawet.Condition;
using AIWolf.ReproServer;
using System.Collections.Generic;
using Moq;

namespace UnitTestProject
{
    [TestClass]
    public class ViewpointTest
    {
        const int loop = 1;

        GameSetting gameSetting15 = GameSetting.GetDefaultGameSetting(15);
        Mock<GameInfo> gameInfoMock15 = new Mock<GameInfo>();
        GameInfo gameInfo15;


        [TestInitialize]
        public void TestInitialize()
        {
            // GameInfoは2018/07/08現在使われていないので初期化は必要ない
            gameInfo15 = gameInfoMock15.Object;
        }


        [TestMethod]
        public void 視点の初期化()
        {
            Viewpoint viewpoint = new Viewpoint( gameSetting15, gameInfo15);

            Assert.AreEqual( viewpoint.MonsterSidePattern.Count, 5460 );
        }

        
        [TestMethod]
        public void team条件_単一エージェント狼狂_1()
        {
            Viewpoint viewpoint = new Viewpoint( gameSetting15, gameInfo15);

            TeamCondition con = TeamCondition.GetCondition( Agent.GetAgent( 1 ) , Team.WEREWOLF);
            viewpoint.RemoveMatchPattern( con );

            Assert.AreEqual( viewpoint.MonsterSidePattern.Count, 4004 );
        }


        [TestMethod]
        public void bit条件_単一エージェント狼狂_1()
        {
            Viewpoint viewpoint = new Viewpoint(gameSetting15, gameInfo15);

            BitCondition con = new BitCondition();
            con.AddWerewolf( Agent.GetAgent(1) );
            viewpoint.RemoveMatchPattern( con );

            con = new BitCondition();
            con.AddPossessed( Agent.GetAgent( 1 ) );
            viewpoint.RemoveMatchPattern( con );

            Assert.AreEqual( viewpoint.MonsterSidePattern.Count, 4004 );
        }

        
        [TestMethod]
        public void bit条件_単一エージェント狼狂_2()
        {
            Viewpoint viewpoint = new Viewpoint(gameSetting15, gameInfo15);

            BitCondition con = new BitCondition();
            con.AddWerewolf( Agent.GetAgent( 1 ) );
            con.AddPossessed( Agent.GetAgent( 1 ) );
            viewpoint.RemoveMatchPattern( con );

            Assert.AreEqual( viewpoint.MonsterSidePattern.Count, 5460 );
        }

        
        [TestMethod]
        public void bit条件_複数エージェント狼()
        {
            for ( int i = 0; i < loop; i++ )
            {
                Viewpoint viewpoint = new Viewpoint(gameSetting15, gameInfo15);

                BitCondition con = new BitCondition();
                con.AddWerewolf( Agent.GetAgent( 1 ) );
                con.AddWerewolf( Agent.GetAgent( 2 ) );
                viewpoint.RemoveMatchPattern( con );

                Assert.AreEqual( viewpoint.MonsterSidePattern.Count, 5304 );
           }
        }


        [TestMethod]
        public void and条件_複数エージェント狼()
        {
            for ( int i=0; i < loop; i++ )
            {
                Viewpoint viewpoint = new Viewpoint(gameSetting15, gameInfo15);

                AndCondition con = new AndCondition();
                con.AddCondition( RoleCondition.GetCondition( Agent.GetAgent( 1 ), Role.WEREWOLF ) );
                con.AddCondition( RoleCondition.GetCondition( Agent.GetAgent( 2 ), Role.WEREWOLF ) );
                viewpoint.RemoveMatchPattern( con );

                Assert.AreEqual( viewpoint.MonsterSidePattern.Count, 5304 );
            }
        }


        [TestMethod]
        public void matchNum条件_複数エージェント狼()
        {
            for(int i = 0; i < loop; i++)
            {
                Viewpoint viewpoint = new Viewpoint(gameSetting15, gameInfo15);

                MatchNumCondition con = new MatchNumCondition() { MinNum = 1, MaxNum = 2 };
                con.AddCondition(RoleCondition.GetCondition(Agent.GetAgent(1), Role.WEREWOLF));
                con.AddCondition(RoleCondition.GetCondition(Agent.GetAgent(2), Role.WEREWOLF));
                con.AddCondition(RoleCondition.GetCondition(Agent.GetAgent(3), Role.WEREWOLF));
                viewpoint.RemoveMatchPattern(con);

                Assert.AreEqual(viewpoint.MonsterSidePattern.Count, 2652);
            }
        }


        [TestMethod]
        public void bitMatchNum条件_複数エージェント狼()
        {
            for(int i = 0; i < loop; i++)
            {
                Viewpoint viewpoint = new Viewpoint(gameSetting15, gameInfo15);

                BitMatchNumCondition con = new BitMatchNumCondition() { MinNum = 1, MaxNum = 2 };
                con.AddWerewolf(Agent.GetAgent(1));
                con.AddWerewolf(Agent.GetAgent(2));
                con.AddWerewolf(Agent.GetAgent(3));
                viewpoint.RemoveMatchPattern(con);

                Assert.AreEqual(viewpoint.MonsterSidePattern.Count, 2652);
            }
        }


    }
}

using Wepwawet.Condition;
using AIWolf.Lib;
using System;
using System.Collections.Generic;
using System.Text;

namespace Wepwawet.Lib
{

    /// <summary>
    /// 視点クラス
    /// </summary>
    public class Viewpoint
    {
        /// <summary>
        /// 人外パターン
        /// </summary>
        public List<MonsterSidePattern> MonsterSidePattern{ get; private set; } = new List<MonsterSidePattern>();

        /// <summary>
        /// この視点が包含する視点
        /// </summary>
        public List<Viewpoint> InclusionViewpoint { get; private set; } = new List<Viewpoint>();


        /// <summary>
        /// コンストラクタ
        /// </summary>
        /// <param name="gameSetting"></param>
        public Viewpoint(GameSetting gameSetting)
        {
            SetMonsterSidePattern(gameSetting);
        }

        /// <summary>
        /// コンストラクタ
        /// </summary>
        private Viewpoint()
        {
        }

        /// <summary>
        /// この視点が包含する視点を追加する
        /// </summary>
        /// <returns>追加した視点</returns>
        public Viewpoint AddInclusionViewpoint()
        {
            Viewpoint newViewPoint = new Viewpoint();

            // 人外パターンをコピー
            foreach( MonsterSidePattern pattern in MonsterSidePattern )
            {
                newViewPoint.MonsterSidePattern.Add(pattern);
            }

            // 包含する視点として追加
            InclusionViewpoint.Add(newViewPoint);

            return newViewPoint;
        }


        //TODO 他編成対応
        /// <summary>
        /// 人外パターンの設定
        /// </summary>
        /// <param name="gameSetting"></param>
        private void SetMonsterSidePattern(GameSetting gameSetting)
        {

            MonsterSidePattern = new List<MonsterSidePattern>();

            // ３狼１狂
            if (gameSetting.RoleNumMap[Role.WEREWOLF] == 3 && gameSetting.RoleNumMap[Role.POSSESSED] == 1)
            {
                for (int wolfAcnt = 1; wolfAcnt <= gameSetting.PlayerNum - 2; wolfAcnt++)
                {
                    for (int wolfBcnt = wolfAcnt + 1; wolfBcnt <= gameSetting.PlayerNum - 1; wolfBcnt++)
                    {
                        for (int wolfCcnt = wolfBcnt + 1; wolfCcnt <= gameSetting.PlayerNum; wolfCcnt++)
                        {
                            List<Agent> wolfAgent = new List<Agent>
                            {
                                Agent.GetAgent(wolfAcnt),
                                Agent.GetAgent(wolfBcnt),
                                Agent.GetAgent(wolfCcnt)
                            };

                            for (int possessedcnt = 1; possessedcnt <= gameSetting.PlayerNum; possessedcnt++)
                            {
                                if (possessedcnt != wolfAcnt && possessedcnt != wolfBcnt && possessedcnt != wolfCcnt)
                                {
                                    List<Agent> posAgent = new List<Agent>
                                    {
                                        Agent.GetAgent(possessedcnt)
                                    };
                                    MonsterSidePattern pattern = new MonsterSidePattern()
                                    {
                                        WerewolfAgent = wolfAgent,
                                        PossessedAgent = posAgent,
                                    };
                                    MonsterSidePattern.Add(pattern);
                                }
                            }
                        }
                    }
                }
            }

            // １狼１狂
            if (gameSetting.RoleNumMap[Role.WEREWOLF] == 1 && gameSetting.RoleNumMap[Role.POSSESSED] == 1)
            {
                for (int wolfAcnt = 1; wolfAcnt <= gameSetting.PlayerNum; wolfAcnt++)
                {
                    List<Agent> wolfAgent = new List<Agent>
                    {
                        Agent.GetAgent(wolfAcnt)
                    };
                    for (int possessedcnt = 1; possessedcnt <= gameSetting.PlayerNum; possessedcnt++)
                    {
                        if (possessedcnt != wolfAcnt)
                        {
                            List<Agent> posAgent = new List<Agent>
                            {
                                Agent.GetAgent(possessedcnt)
                            };
                            MonsterSidePattern pattern = new MonsterSidePattern()
                            {
                                WerewolfAgent = wolfAgent,
                                PossessedAgent = posAgent,
                            };
                            MonsterSidePattern.Add(pattern);
                        }
                    }
                }
            }



        }


        /// <summary>
        /// 指定したパターンを削除する
        /// </summary>
        /// <param name="pattern">削除するパターン</param>
        private void RemovePattern(MonsterSidePattern pattern)
        {
            // この視点から指定したパターンを削除する
            MonsterSidePattern.Remove(pattern);

            // 包含する視点から指定したパターンを削除する
            foreach ( Viewpoint child in InclusionViewpoint )
            {
                child.RemovePattern(pattern);
            }
            
        }


        /// <summary>
        /// マッチするパターンを削除する
        /// </summary>
        /// <param name="condition"></param>
        public void RemoveMatchPattern(ICondition condition)
        {
            for ( int i = MonsterSidePattern.Count - 1; i >= 0; i-- )
            {
                MonsterSidePattern pattern = MonsterSidePattern[i];
                if ( condition.IsMatch(pattern) )
                {
                    RemovePattern(pattern);
                }
            }
        }


        /// <summary>
        /// マッチしないパターンを削除する
        /// </summary>
        /// <param name="condition"></param>
        public void RemoveNotMatchPattern(ICondition condition)
        {
            for ( int i = MonsterSidePattern.Count - 1; i >= 0; i-- )
            {
                MonsterSidePattern pattern = MonsterSidePattern[i];
                if ( !condition.IsMatch(pattern) )
                {
                    RemovePattern(pattern);
                }
            }
        }


        /// <summary>
        /// 人狼の数が範囲外のパターンを削除する
        /// </summary>
        /// <param name="agentList">検査するエージェントのリスト</param>
        /// <param name="minNum">人狼の最小数</param>
        /// <param name="maxNum">人狼の最大数</param>
        public void RemovePatternFromWolfNum(List<Agent> agentList, int minNum, int maxNum)
        {
            for( int i = MonsterSidePattern.Count-1; i >= 0; i-- ){
                MonsterSidePattern pattern = MonsterSidePattern[i];
                int aliveWolfNum = 0;

                foreach ( Agent agent in agentList )
                {
                    if ( pattern.IsExist(agent, Role.WEREWOLF) )
                    {
                        aliveWolfNum++;
                    }
                }

                if ( aliveWolfNum < minNum || aliveWolfNum > maxNum )
                {
                    RemovePattern(pattern);
                }
            }
        }


    }
}

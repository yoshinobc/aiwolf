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
        /// <summary>人外パターン</summary>
        public Dictionary<int, MonsterSidePattern> MonsterSidePattern{ get; private set; } = new Dictionary<int, MonsterSidePattern>();

        /// <summary>この視点が包含する視点</summary>
        public List<Viewpoint> InclusionViewpoint { get; private set; } = new List<Viewpoint>();

        /// <summary>エージェント一覧</summary>
        public List<Agent> AgentList;


        /// <summary>キャッシュ情報が有効か</summary>
        private bool isCacheEnable = false;

        /// <summary>確定種族マップ（未確定はUNC）</summary>
        private Dictionary<Agent, Species> speciesMap = new Dictionary<Agent, Species>();

        /// <summary>確定陣営マップ（未確定はUNC）</summary>
        private Dictionary<Agent, Team> teamMap = new Dictionary<Agent, Team>();


        /// <summary>
        /// コンストラクタ
        /// </summary>
        public Viewpoint(GameSetting gameSetting, GameInfo gameInfo)
        {
            SetMonsterSidePattern(gameSetting);
            AgentList = gameInfo.AgentList;
        }


        /// <summary>
        /// コンストラクタ（他の視点から人外パターンをコピー）
        /// </summary>
        /// <param name="gameSetting">コピー元の視点</param>
        public Viewpoint(Viewpoint parent)
        {
            // 人外パターンとエージェントをコピー
            MonsterSidePattern = new Dictionary<int, MonsterSidePattern>(parent.MonsterSidePattern);
            AgentList = parent.AgentList;
        }
        

        /// <summary>
        /// この視点が包含する視点を追加する
        /// </summary>
        /// <param name="child">追加する視点</param>
        public void AddInclusionViewpoint(Viewpoint child)
        {
            InclusionViewpoint.Add(child);
        }


        /// <summary>
        /// この視点と包含する視点に別視点の内訳パターンをコピーする
        /// </summary>
        /// <param name="srcViewPoint">コピー元の視点</param>
        public void CopyMonsterSidePattern( Viewpoint srcViewPoint )
        {
            MonsterSidePattern = new Dictionary<int, MonsterSidePattern>( srcViewPoint.MonsterSidePattern );

            // 包含する視点に内訳をコピー
            foreach ( Viewpoint child in InclusionViewpoint )
            {
                child.CopyMonsterSidePattern( this );
            }
        }


        //TODO 他編成対応
        /// <summary>
        /// 人外パターンの設定
        /// </summary>
        /// <param name="gameSetting"></param>
        private void SetMonsterSidePattern(GameSetting gameSetting)
        {

            MonsterSidePattern = new Dictionary<int, MonsterSidePattern>(5460);
            int cnt = 0;

            List<Agent> foxAgent = new List<Agent>();
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
                                Agent.GetAgent( wolfAcnt ),
                                Agent.GetAgent( wolfBcnt ),
                                Agent.GetAgent( wolfCcnt )
                            };

                            for (int possessedcnt = 1; possessedcnt <= gameSetting.PlayerNum; possessedcnt++)
                            {
                                if (possessedcnt != wolfAcnt && possessedcnt != wolfBcnt && possessedcnt != wolfCcnt)
                                {
                                    List<Agent> posAgent = new List<Agent>
                                    {
                                        Agent.GetAgent(possessedcnt)
                                    };

                                    MonsterSidePattern pattern = new MonsterSidePattern( wolfAgent, posAgent, foxAgent );
                                    MonsterSidePattern.Add( cnt++, pattern );
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
                        Agent.GetAgent( wolfAcnt )
                    };
                    for (int possessedcnt = 1; possessedcnt <= gameSetting.PlayerNum; possessedcnt++)
                    {
                        if (possessedcnt != wolfAcnt)
                        {
                            List<Agent> posAgent = new List<Agent>
                            {
                                 Agent.GetAgent(possessedcnt)
                            };
                            MonsterSidePattern pattern = new MonsterSidePattern( wolfAgent, posAgent, foxAgent );
                            MonsterSidePattern.Add( cnt++, pattern );
                        }
                    }
                }
            }

        }


        /// <summary>
        /// 指定したパターンを削除する
        /// </summary>
        /// <param name="pattern">削除するパターン</param>
        private void RemovePattern( int key )
        {
            // この視点から指定したパターンを削除する
            MonsterSidePattern.Remove( key );

            // 包含する視点から指定したパターンを削除する
            foreach ( Viewpoint child in InclusionViewpoint )
            {
                child.RemovePattern( key );
            }

            isCacheEnable = false;
        }
        

        /// <summary>
        /// マッチするパターンを削除する
        /// </summary>
        /// <param name="condition"></param>
        public void RemoveMatchPattern( ICondition condition )
        {
            List<int> keys = new List<int>( MonsterSidePattern.Keys );

            foreach ( int key in keys )
            {
                MonsterSidePattern pattern = MonsterSidePattern[key];
                if ( condition.IsMatch(pattern) )
                {
                    RemovePattern( key );
                }
            }

            isCacheEnable = false;
        }


        /// <summary>
        /// マッチしないパターンを削除する
        /// </summary>
        /// <param name="condition"></param>
        public void RemoveNotMatchPattern(ICondition condition)
        {
            List<int> keys = new List<int>( MonsterSidePattern.Keys );

            foreach ( int key in keys )
            {
                MonsterSidePattern pattern = MonsterSidePattern[key];
                if ( !condition.IsMatch(pattern) )
                {
                    RemovePattern( key );
                }
            }

            isCacheEnable = false;
        }


        private void makeCache()
        {
            speciesMap.Clear();
            teamMap.Clear();
            
            int[] RoleWerewolfCnt = new int[AgentList.Count + 1];
            int[] TeamWerewolfCnt = new int[AgentList.Count + 1];
            
            foreach(MonsterSidePattern pattern in MonsterSidePattern.Values)
            {
                foreach(Agent agent in pattern.WerewolfAgent)
                {
                    RoleWerewolfCnt[agent.AgentIdx]++;
                    TeamWerewolfCnt[agent.AgentIdx]++;
                }

                foreach(Agent agent in pattern.PossessedAgent)
                {
                    TeamWerewolfCnt[agent.AgentIdx]++;
                }
            }

            foreach(Agent agent in AgentList)
            {
                if(RoleWerewolfCnt[agent.AgentIdx] == MonsterSidePattern.Count)
                {
                    speciesMap.Add(agent, Species.WEREWOLF);
                }
                else if(RoleWerewolfCnt[agent.AgentIdx] == 0)
                {
                    speciesMap.Add(agent, Species.HUMAN);
                }
                else
                {
                    speciesMap.Add(agent, Species.UNC);
                }

                if(TeamWerewolfCnt[agent.AgentIdx] == MonsterSidePattern.Count)
                {
                    teamMap.Add(agent, Team.WEREWOLF);
                }
                else if(TeamWerewolfCnt[agent.AgentIdx] == 0)
                {
                    teamMap.Add(agent, Team.VILLAGER);
                }
                else
                {
                    teamMap.Add(agent, Team.UNC);
                }
            }

            isCacheEnable = true;
        }

        public Dictionary<Agent, Species> GetSpeciesMap()
        {
            if(!isCacheEnable)
            {
                makeCache();
            }
            return speciesMap;
        }

        public Dictionary<Agent, Team> GetTeamMap()
        {
            if(!isCacheEnable)
            {
                makeCache();
            }
            return teamMap;
        }

    }
}

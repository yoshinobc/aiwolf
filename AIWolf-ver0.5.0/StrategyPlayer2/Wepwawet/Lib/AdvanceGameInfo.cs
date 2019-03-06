using Wepwawet.Condition;
using AIWolf.Lib;
using System;
using System.Collections.Generic;
using System.Text;

namespace Wepwawet.Lib
{
    public class AdvanceGameInfo
    {

        /// <summary>ゲーム設定</summary>
        public GameSetting GameSetting { get; protected set; }

        /// <summary>最新のゲーム情報</summary>
        public GameInfo LatestGameInfo { get; set; }

        /// <summary>各日の情報 Key=日</summary>
        public SortedDictionary<int, GameInfo> GameInfo { get; set; } = new SortedDictionary<int, GameInfo>();


        /// <summary>全視点のシステム情報のみでフィルタした視点情報</summary>
        public Viewpoint AllViewSystemInfo { get; protected set; }

        /// <summary>自分視点のシステム情報のみでフィルタした視点情報</summary>
        public Viewpoint SelfViewSystemInfo { get; protected set; }
        

        /// <summary>カミングアウトのリスト（自分に対する村陣営COのみ）</summary>
        public List<ComingOut> ComingOut { get; set; } = new List<ComingOut>();

        /// <summary>占い判定のリスト</summary>
        public List<Judge> SeerJudge { get; set; } = new List<Judge>();

        /// <summary>霊媒判定のリスト</summary>
        public List<Judge> MediumJudge { get; set; } = new List<Judge>();


        /// <summary>コンテンツ版Talkリスト Key.日</summary>
        public SortedDictionary<int, List<Content>> TalkContent { get; set; } = new SortedDictionary<int, List<Content>>();

        /// <summary>Talkの読み飛ばす数</summary>
        private int talkOffset;


        /// <summary>
        /// 最新のUpdate()で日が変わったか
        /// </summary>
        public bool IsDayUpdate { get; set; }



        /// <summary>
        /// コンストラクタ
        /// </summary>
        /// <param name="gameInfo"></param>
        /// <param name="gameSetting"></param>
        public AdvanceGameInfo(GameInfo gameInfo, GameSetting gameSetting)
        {
            // ゲーム設定を保管
            GameSetting = gameSetting;

            // 全視点情報・自分視点情報の初期化
            AllViewSystemInfo = new Viewpoint(GameSetting);
            SelfViewSystemInfo = AllViewSystemInfo.AddInclusionViewpoint();
            // 自分視点から自分が人外の内訳を削除する
            ICondition condition = TeamCondition.GetCondition(gameInfo.Agent, Team.VILLAGER);
            SelfViewSystemInfo.RemoveNotMatchPattern(condition);
        }


        public void Update(GameInfo gameInfo)
        {

            // 日付が変わっているか
            IsDayUpdate = false;
            if ( LatestGameInfo == null || gameInfo.Day > LatestGameInfo.Day )
            {
                IsDayUpdate = true;
            }
            
            if ( IsDayUpdate )
            {
                // 日付が変わったときの処理
                DayStart(gameInfo);
            }
            else
            {
                // 日の情報の更新
                UpdateDayInfo();

            }

            // 最新のゲーム情報を保管
            LatestGameInfo = gameInfo;
            GameInfo[gameInfo.Day] = gameInfo;
        }

        /// <summary>
        /// 日付変更時(昼の開始時)の処理
        /// </summary>
        private void DayStart(GameInfo gameInfo)
        {

            // 死体で発見されたエージェントが人狼のパターンを削除する
            foreach ( Agent agent in gameInfo.LastDeadAgentList)
            {
                ICondition condition = RoleCondition.GetCondition(agent, Role.WEREWOLF);
                AllViewSystemInfo.RemoveMatchPattern(condition);
            }

            //TODO 飽和が早い配役対応　(15人村→4日目から)(5人村→1狼のため必要無し)
            // 人狼生存人数がおかしいパターンを削除する
            int maxWolfNum = (gameInfo.AliveAgentList.Count - 1) / 2;
            maxWolfNum = Math.Min(maxWolfNum, GameSetting.RoleNumMap[Role.WEREWOLF]);
            if ( gameInfo.Day > GameSetting.RoleNumMap[Role.WEREWOLF] )
            {
                MatchNumCondition condition = new MatchNumCondition() { MinNum = 1, MaxNum = maxWolfNum, };
                foreach (Agent agent in gameInfo.AliveAgentList)
                {
                    condition.AddCondition(RoleCondition.GetCondition(agent, Role.WEREWOLF));
                }
                AllViewSystemInfo.RemoveNotMatchPattern(condition);
            }

            talkOffset = 0;

            // debug
            /*
            Console.Write(gameInfo.Day + "日目 : ");
            Console.Write(SelfViewSystemInfo.MonsterSidePattern.Count);
            Console.Write(" / ");
            Console.Write(AllViewSystemInfo.MonsterSidePattern.Count);
            Console.WriteLine("");
            */
        }

        public void UpdateDayInfo()
        {

            // 新規会話の処理
            if ( !TalkContent.ContainsKey( LatestGameInfo.Day ) )
            {
                TalkContent.Add( LatestGameInfo.Day, new List<Content>() );
            }
            List<Content> ContentList = TalkContent[LatestGameInfo.Day];
            for ( int i = talkOffset; i < LatestGameInfo.TalkList.Count; i++ )
            {
                Talk srcTalk = LatestGameInfo.TalkList[i];
                Content newContent = new Content(srcTalk.Text);

                // Content版を作成する
                ContentList.Add(newContent);

                // 単文か
                if ( newContent.Operator == Operator.NOP )
                {
                    switch ( newContent.Topic )
                    {
                        case Topic.COMINGOUT:
                            // カミングアウト
                            // 自分に対する村陣営COのみ対象
                            if ( srcTalk.Agent == newContent.Target && newContent.Role.GetTeam() == Team.VILLAGER)
                            {
                                // カミングアウトの更新
                                UpdateComingOut(newContent.Target, newContent.Role, srcTalk);
                            }
                            break;
                        case Topic.DIVINED:
                            // 占い結果

                            // カミングアウトの更新
                            UpdateComingOut( srcTalk.Agent, Role.SEER, srcTalk );

                            // 判定の追加
                            Judge newSeerJudge = new Judge( srcTalk );
                            SeerJudge.Add( newSeerJudge );

                            break;
                        case Topic.IDENTIFIED:
                            // 霊媒結果

                            // カミングアウトの更新
                            UpdateComingOut(srcTalk.Agent, Role.MEDIUM, srcTalk);

                            // 判定の追加
                            Judge newMediumJudge = new Judge( srcTalk );
                            MediumJudge.Add( newMediumJudge );

                            break;
                        case Topic.GUARDED:
                            // 護衛履歴
                            break;
                        default:
                            break;
                    }

                }

            }
            talkOffset = LatestGameInfo.TalkList.Count;

            // 新規囁きの処理
            /*            for (int i = todayInfo.WhisperContentList.Count; i < todayInfo.WhisperList.Count; i++)
                        {
                            Content newContent = new Content(todayInfo.WhisperList[i].Text);

                            // Content版を作成する
                            todayInfo.WhisperContentList.Add(newContent);
                        }*/

        }

/*
        /// <summary>
        /// 追放再投票時の更新処理
        /// </summary>
        public void UpdateReVote()
        {
            DayInfo todayInfo = DayInfo[LatestGameInfo.Day];
            todayInfo.VoteList.Add(LatestGameInfo.LatestVoteList);
        }

        /// <summary>
        /// 襲撃再投票時の更新処理
        /// </summary>
        public void UpdateReAttackVote()
        {
            DayInfo todayInfo = DayInfo[LatestGameInfo.Day];
            todayInfo.AttackVoteList.Add(LatestGameInfo.LatestAttackVoteList);
        }
*/

        /// <summary>
        /// CO役職の更新を行う
        /// </summary>
        /// <param name="agent">エージェント</param>
        /// <param name="role">役職</param>
        /// <param name="talk">更新が行われた発話</param>
        private void UpdateComingOut(Agent agent, Role role, Talk talk)
        {

            // CO中の役職を確認する
            foreach (ComingOut co in ComingOut)
            {
                if ( co.Agent == agent && co.CancelTalk == null )
                {
                    if ( co.Role != role )
                    {
                        // 違う役職をCOしていた場合、古いCOを無効にする
                        co.Cancel(talk);
                        break;
                    }
                    else
                    {
                        // 同じ役職をCOしていた場合、更新は必要ないため抜ける
                        return;
                    }
                }
            }

            // COリストに追加
            ComingOut newCO = new ComingOut( agent, role, talk );
            ComingOut.Add(newCO);

        }


        /// <summary>
        /// CO中の役職を返す
        /// </summary>
        /// <param name="agent">CO役職を調べるエージェント</param>
        /// <returns>CO中の役職(COなしはRole.UNC)</returns>
        public Role GetComingOutRole(Agent agent)
        {
            Role ret = Role.UNC;

            foreach (ComingOut co in ComingOut)
            {
                if( co.Agent == agent && co.CancelTalk == null )
                {
                    return co.Role;
                }
            }

            return ret;
        }


        /// <summary>
        /// 特定の役職をCOしているエージェントの一覧を取得する
        /// </summary>
        /// <param name="role">役職</param>
        /// <returns>エージェントの一覧</returns>
        public List<Agent> GetComingOutAgent(Role role)
        {
            List<Agent> ret = new List<Agent>();

            foreach ( Agent agent in LatestGameInfo.AgentList )
            {
                if ( GetComingOutRole(agent) == role )
                {
                    ret.Add(agent);
                }
            }

            return ret;
        }


    }
}

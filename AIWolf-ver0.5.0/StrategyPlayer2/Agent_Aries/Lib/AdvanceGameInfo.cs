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
        public GameSetting GameSetting { get; }
        
        /// <summary>各日の情報 Key=日</summary>
        public SortedDictionary<int, DayInfo> DayInfo { get; } = new SortedDictionary<int, DayInfo>();

        /// <summary>最新日の情報</summary>
        public DayInfo TodayInfo { get; private set; }

        /// <summary>前日の情報</summary>
        public DayInfo YesterdayInfo { get; private set; }


        /// <summary>全視点のシステム情報のみでフィルタした視点情報</summary>
        public Viewpoint AllViewSystemInfo { get; private set; }

        /// <summary>全視点のCO・判定情報を含めてフィルタした視点情報（フィルタは設定依存）</summary>
        public Viewpoint AllViewTrustInfo { get; private set; }

        /// <summary>自分視点のシステム情報のみでフィルタした視点情報</summary>
        public Viewpoint SelfViewSystemInfo { get; private set; }

        /// <summary>自分視点のCO・判定情報を含めてフィルタした視点情報（フィルタは設定依存）</summary>
        public Viewpoint SelfViewTrustInfo { get; private set; }

        /// <summary>自分狂人視点のCO・判定情報を含めてフィルタした視点情報（フィルタは設定依存）　自分が狂人でない場合はnull</summary>
        public Viewpoint SelfPossessedViewTrustInfo { get; private set; }


        /// <summary>カミングアウトのリスト（自分に対する村陣営COのみ）</summary>
        public List<ComingOut> ComingOut { get; } = new List<ComingOut>();

        /// <summary>カミングアウトのリスト（自分に対する人外COのみ）</summary>
        public List<ComingOut> MonsterSideComingOut { get; } = new List<ComingOut>();


        /// <summary>占い判定のリスト</summary>
        public List<Judge> SeerJudge { get; } = new List<Judge>();

        /// <summary>霊媒判定のリスト</summary>
        public List<Judge> MediumJudge { get; } = new List<Judge>();


        /// <summary>自分のエージェント</summary>
        public Agent Me
        {
            get
            {
                return latestGameInfo.Agent;
            }
        }

        /// <summary>自分の役職</summary>
        public Role MyRole
        {
            get
            {
                return latestGameInfo.Role;
            }
        }

        /// <summary>全エージェントのリスト</summary>
        public List<Agent> AgentList { get; }
        
        /// <summary>最新の生存エージェントのリスト</summary>
        public List<Agent> AliveAgentList {
            get
            {
                return TodayInfo.LatestAliveAgentList;
            }
        }

        /// <summary>最新の日</summary>
        public int Day
        {
            get
            {
                return latestGameInfo.Day;
            }
        }

        /// <summary>既知の役職マップ</summary>
        public Dictionary<Agent, Role> RoleMap
        {
            get
            {
                return latestGameInfo.RoleMap;
            }
        }

        /// <summary>
        /// 自分の占い判定（真占）
        /// </summary>
        public List<AIWolf.Lib.Judge> MySeerJudge { get; } = new List<AIWolf.Lib.Judge>();

        /// <summary>
        /// 自分の霊媒判定（真霊）
        /// </summary>
        public List<AIWolf.Lib.Judge> MyMediumJudge { get; } = new List<AIWolf.Lib.Judge>();

        /// <summary>
        /// 自分の護衛履歴（真狩） Key.実行日（夜時点の日付）　Value.護衛先
        /// </summary>
        public Dictionary<int, Agent> MyGuardHistory { get; } = new Dictionary<int, Agent>();


        /// <summary>最新のゲーム情報</summary>
        private GameInfo latestGameInfo;

        /// <summary>Talkの読み飛ばす数</summary>
        private int talkOffset;
        


        /// <summary>
        /// 最新のUpdate()で日が変わったか
        /// </summary>
        public bool IsDayUpdate { get; private set; }


        /// <summary>
        /// 村人陣営のＣＯや判定を信用して内訳を削除するか
        /// 初期値=False　ゲーム開始前に設定すること
        /// </summary>
        public bool IsRemoveVillagerFakeCO { get; set; } = false;


        /// <summary>
        /// 実際の役職の視点を有効にするか（狂人で使用）
        /// 初期値=False　ゲーム開始前に設定すること
        /// </summary>
        public bool IsEnableRealRoleViewPoint { get; set; } = false;


        /// <summary>
        /// コンストラクタ
        /// </summary>
        /// <param name="gameInfo"></param>
        /// <param name="gameSetting"></param>
        public AdvanceGameInfo(GameInfo gameInfo, GameSetting gameSetting)
        {
            // ゲーム設定を保管
            GameSetting = gameSetting;
            latestGameInfo = gameInfo;

            AgentList = gameInfo.AgentList;
            
            // 全視点情報・自分視点情報の初期化
            AllViewSystemInfo = new Viewpoint( GameSetting, gameInfo );
            AllViewTrustInfo = new Viewpoint(AllViewSystemInfo);
            SelfViewSystemInfo = new Viewpoint(AllViewSystemInfo);
            SelfViewTrustInfo = new Viewpoint(AllViewSystemInfo);
            AllViewSystemInfo.AddInclusionViewpoint(AllViewTrustInfo);
            AllViewSystemInfo.AddInclusionViewpoint(SelfViewSystemInfo);
            AllViewTrustInfo.AddInclusionViewpoint(SelfViewTrustInfo);
            SelfViewSystemInfo.AddInclusionViewpoint(SelfViewTrustInfo);
            if(MyRole == Role.POSSESSED)
            {
                SelfPossessedViewTrustInfo = new Viewpoint(AllViewTrustInfo);
                AllViewTrustInfo.AddInclusionViewpoint(SelfPossessedViewTrustInfo);
            }

            // 自分視点から自分が人外の内訳を削除する
            BitCondition condition = new BitCondition();
            condition.AddWerewolfTeam( gameInfo.Agent );
            SelfViewSystemInfo.RemoveMatchPattern( condition );

            // 自分狂人視点から自分が狂人でない内訳を削除する
            if(MyRole == Role.POSSESSED)
            {
                condition = new BitCondition();
                condition.AddNotPossessed(gameInfo.Agent);
                SelfPossessedViewTrustInfo.RemoveMatchPattern(condition);
            }

        }


        /// <summary>
        /// 日の情報を更新する
        /// </summary>
        /// <param name="gameInfo"></param>
        public void Update(GameInfo gameInfo)
        {

            // 最新のゲーム情報を保管
            latestGameInfo = gameInfo;

            // 日付が変わっているか
            IsDayUpdate = false;
            if ( !DayInfo.ContainsKey(gameInfo.Day) )
            {
                IsDayUpdate = true;
            }
            
            if ( IsDayUpdate )
            {
                // 日付が変わったときの処理
                DayStart( gameInfo );
            }
            else
            {
                // 日の情報の更新
                UpdateDayInfo( gameInfo );
            }

        }


        /// <summary>
        /// 日付変更時(昼の開始時)の処理
        /// </summary>
        private void DayStart(GameInfo gameInfo)
        {

            // 昨日の情報を更新
            YesterdayInfo = TodayInfo;
            if ( YesterdayInfo != null && YesterdayInfo.Day != 0 && YesterdayInfo.Day + 1 == gameInfo.Day )
            {
                TodayInfo.UpdateFromTomorrow( gameInfo );
            }

            // 日の情報を作成
            TodayInfo = new DayInfo( gameInfo );
            DayInfo.Add( gameInfo.Day, TodayInfo );

            // 死体で発見されたエージェントが人狼のパターンを削除する
            foreach ( Agent agent in gameInfo.LastDeadAgentList )
            {
                ICondition condition = RoleCondition.GetCondition( agent, Role.WEREWOLF );
                AllViewSystemInfo.RemoveMatchPattern( condition );
            }

            //TODO 飽和が早い配役対応　(15人村→4日目から)(5人村→1狼のため必要無し)
            // 人狼生存人数がおかしいパターンを削除する
            int maxWolfNum = (gameInfo.AliveAgentList.Count - 1) / 2;
            maxWolfNum = Math.Min(maxWolfNum, GameSetting.RoleNumMap[Role.WEREWOLF]);
            if ( gameInfo.Day > GameSetting.RoleNumMap[Role.WEREWOLF] )
            {
                BitMatchNumCondition condition = new BitMatchNumCondition() { MinNum = 1, MaxNum = maxWolfNum, };
                foreach (Agent agent in gameInfo.AliveAgentList)
                {
                    condition.AddWerewolf(agent);
                }
                AllViewSystemInfo.RemoveNotMatchPattern(condition);
            }

            // 自分の占い判定の追加
            if ( gameInfo.DivineResult != null )
            {
                MySeerJudge.Add( gameInfo.DivineResult );
            }

            // 自分の霊媒判定の追加
            if ( gameInfo.MediumResult != null )
            {
                MyMediumJudge.Add( gameInfo.MediumResult );
            }

            // 自分の護衛履歴の追加
            if ( gameInfo.GuardedAgent != null )
            {
                MyGuardHistory.Add( gameInfo.Day - 1, gameInfo.GuardedAgent );
            }

            // 護衛成功からのパターン削除
            if ( gameInfo.GuardedAgent != null && gameInfo.LastDeadAgentList.Count <= 0 )
            {
                BitCondition condition = new BitCondition();
                condition.AddWerewolf( gameInfo.GuardedAgent );

                AllViewSystemInfo.RemoveMatchPattern( condition );
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


        public void UpdateDayInfo( GameInfo gameInfo )
        {

            // 日の情報を更新
            TodayInfo.Update( gameInfo );

            // 新規会話の処理
            for ( int i = talkOffset; i < TodayInfo.TalkList.Count; i++ )
            {
                Talk talk = TodayInfo.TalkList[i];
                Content content = TodayInfo.TalkList[i].Content;
                
                // 単文か
                if ( content.Operator == Operator.NOP )
                {
                    switch ( content.Topic )
                    {
                        case Topic.COMINGOUT:
                            // カミングアウト
                            // 自分に対するCOのみ対象
                            if ( talk.Agent == content.Target )
                            {
                                if ( content.Role.GetTeam() == Team.VILLAGER )
                                {
                                    // 村陣営CO
                                    UpdateComingOut( content.Target, content.Role, talk );
                                }
                                else
                                {
                                    // 人外CO
                                    UpdateMonsterSideComingOut( content.Target, content.Role, talk );
                                }
                            }
                            break;
                        case Topic.DIVINED:
                            // 占い結果

                            // カミングアウトの更新
                            UpdateComingOut( talk.Agent, Role.SEER, talk );

                            // 判定の追加
                            Judge newSeerJudge = new Judge( talk );
                            SeerJudge.Add( newSeerJudge );

                            // 内訳の削除
                            if ( IsRemoveVillagerFakeCO )
                            {
                                RemovePatternFromJudge( newSeerJudge );
                            }

                            break;
                        case Topic.IDENTIFIED:
                            // 霊媒結果

                            // カミングアウトの更新
                            UpdateComingOut(talk.Agent, Role.MEDIUM, talk);

                            // 判定の追加
                            Judge newMediumJudge = new Judge( talk );
                            MediumJudge.Add( newMediumJudge );

                            // 内訳の削除
                            if ( IsRemoveVillagerFakeCO )
                            {
                                RemovePatternFromJudge( newMediumJudge );
                            }

                            break;
                        case Topic.GUARDED:
                            // 護衛履歴
                            break;
                        default:
                            break;
                    }

                }

            }
            talkOffset = TodayInfo.TalkList.Count;

            // 新規囁きの処理

        }
        

        /// <summary>
        /// CO役職の更新を行う
        /// </summary>
        /// <param name="agent">エージェント</param>
        /// <param name="role">役職</param>
        /// <param name="talk">更新が行われた発話</param>
        private void UpdateComingOut( Agent agent, Role role, Talk talk )
        {
            Boolean cancel = false;

            // CO中の役職を確認する
            foreach (ComingOut co in ComingOut)
            {
                if ( co.Agent == agent && co.IsEnable() )
                {
                    if ( co.Role != role )
                    {
                        // 違う役職をCOしていた場合、古いCOを無効にする
                        co.Cancel(talk);
                        cancel = true;
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
            ComingOut.Add( newCO );


            // 村騙りを考慮せず内訳を削除する
            if ( IsRemoveVillagerFakeCO )
            {
                if ( cancel )
                {
                    // 取り消し有り　→　内訳再編成
                    RemakeViewPointInfo();
                }
                else
                {
                    // 取り消し無し　→　内訳の削除
                    RemovePatternFromComingOut( role );
                }
            }

            return;
        }


        /// <summary>
        /// CO中の役職を返す
        /// </summary>
        /// <param name="agent">CO役職を調べるエージェント</param>
        /// <returns>CO中の役職(COなしはRole.UNC)</returns>
        public Role GetComingOutRole( Agent agent )
        {
            Role ret = Role.UNC;

            foreach (ComingOut co in ComingOut)
            {
                if( co.Agent == agent && co.IsEnable() )
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
        public List<Agent> GetComingOutAgent( Role role )
        {
            List<Agent> ret = new List<Agent>();

            foreach ( ComingOut co in ComingOut )
            {
                if ( co.Role == role && co.IsEnable() )
                {
                    ret.Add( co.Agent );
                }
            }

            return ret;
        }


        /// <summary>
        /// 生存している人狼のリストを取得する
        /// （自分が人狼のとき、及びゲーム終了後のみ結果を取得できる）
        /// </summary>
        /// <returns></returns>
        public List<Agent> GetAliveWerewolf()
        {
            List<Agent> ret = new List<Agent>();

            foreach(Agent agent in latestGameInfo.AliveAgentList)
            {
                if( RoleMap.ContainsKey(agent) && RoleMap[agent] == Role.WEREWOLF )
                {
                    ret.Add(agent);
                }
            }

            return ret;
        }


        /// <summary>
        /// COから村騙りが無い場合の内訳を絞り込む
        /// </summary>
        /// <param name="role"></param>
        private void RemovePatternFromComingOut( Role role )
        {
            if ( GameSetting.RoleNumMap.ContainsKey( role ) && GameSetting.RoleNumMap[role] == 1 )
            {
                List<Agent> coAgent = GetComingOutAgent( role );

                if ( coAgent.Count >= 2 )
                {
                    BitMatchNumCondition con = new BitMatchNumCondition()
                    {
                        MinNum = 0,
                        MaxNum = coAgent.Count - 2
                    };
                    foreach ( Agent agent in coAgent )
                    {
                        con.AddWerewolfTeam(agent);
                    }
                    AllViewTrustInfo.RemoveMatchPattern( con );
                }

            }
        }


        /// <summary>
        /// 占い判定の追加を行う
        /// </summary>
        /// <param name="agent">判定を出したエージェント</param>
        /// <param name="target">判定を出されたエージェント</param>
        /// <param name="result">判定結果</param>
        /// <param name="judgeTalk">判定を出した発話</param>
        private void AddSeerJudge( Agent agent, Agent target, Species result, Talk judgeTalk )
        {

            Boolean cancel = false;
            
            // 過去の判定を確認する
            foreach ( Judge judge in SeerJudge )
            {
                if ( judge.Agent == agent && judge.IsEnable() )
                {
                    if ( judge.Result != result )
                    {
                        // 同じ人物に違う判定を出していた場合、古い判定を無効にする
                        judge.Cancel( judgeTalk );
                        cancel = true;
                        break;
                    }
                    else
                    {
                        // 同じ人物に同じ判定を出していた場合、更新は必要ないため抜ける
                        return;
                    }
                }
            }
            
            // 判定の追加
            Judge newSeerJudge = new Judge( agent, target, result, judgeTalk );
            SeerJudge.Add( newSeerJudge );


            // 村騙りを考慮せず内訳を削除する
            if ( IsRemoveVillagerFakeCO )
            {
                if ( cancel )
                {
                    // 取り消し有り　→　内訳再編成
                    RemakeViewPointInfo();
                }
                else
                {
                    // 取り消し無し　→　内訳の削除
                    RemovePatternFromJudge( newSeerJudge );
                }
            }

        }


        /// <summary>
        /// 判定から村騙りが無い場合の内訳を絞り込む
        /// </summary>
        /// <param name="judge"></param>
        private void RemovePatternFromJudge( Judge judge )
        {
            BitCondition con = new BitCondition();
            con.AddNotWerewolfTeam( judge.Agent );
            if ( judge.Result == Species.HUMAN )
            {
                con.AddWerewolf( judge.Target );
            }
            else
            {
                con.AddNotWerewolf( judge.Target );
            }

            AllViewTrustInfo.RemoveMatchPattern( con );
        }


        /// <summary>
        /// 視点情報を作り直す(COの取り消し時などに使用)
        /// </summary>
        private void RemakeViewPointInfo()
        {

            // システム視点のみに戻す
            SelfViewTrustInfo.CopyMonsterSidePattern( SelfViewSystemInfo );

            // COからの判定絞込み
            RemovePatternFromComingOut( Role.SEER );
            RemovePatternFromComingOut( Role.MEDIUM );
            RemovePatternFromComingOut( Role.BODYGUARD );

            // 判定からの結果絞込み
            foreach ( Judge judge in SeerJudge )
            {
                if ( judge.IsEnable() )
                {
                    RemovePatternFromJudge( judge );
                }
            }
            foreach ( Judge judge in MediumJudge )
            {
                if ( judge.IsEnable() )
                {
                    RemovePatternFromJudge( judge );
                }
            }

        }


        /// <summary>
        /// 人外CO役職の更新を行う
        /// </summary>
        /// <param name="agent">エージェント</param>
        /// <param name="role">役職</param>
        /// <param name="talk">更新が行われた発話</param>
        private void UpdateMonsterSideComingOut( Agent agent, Role role, Talk talk )
        {
            // CO中の役職を確認する
            foreach ( ComingOut co in ComingOut )
            {
                if ( co.Agent == agent && co.IsEnable() )
                {
                    if ( co.Role != role )
                    {
                        // 違う役職をCOしていた場合、古いCOを無効にする
                        co.Cancel( talk );
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
            MonsterSideComingOut.Add( newCO );
            
            return;
        }


    }
}

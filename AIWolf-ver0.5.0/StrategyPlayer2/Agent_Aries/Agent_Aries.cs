using System;
using AIWolf.Lib;
using Wepwawet.Lib;
using Wepwawet.Player;
using Wepwawet.SampleStrategy.Guess;
using Agent_Aries.MyStrategy.Guess;
using Agent_Aries.MyStrategy.ActionPlan;
using Wepwawet.ActionPlan.Tree;
using Wepwawet.TalkGenerator.Tree;
using Agent_Aries.MyStrategy.TalkGenerator;
using Wepwawet.Guess.Tree;
using Wepwawet.TalkGenerator;
using System.Collections.Generic;
using Wepwawet.Guess;
using Wepwawet.ActionPlan;

namespace Agent_Aries
{
    public class Aries : WepwawetPlayer
    {

        /// <summary>偽の占い判定</summary>
        List<AIWolf.Lib.Judge> fakeSeerJudge = new List<AIWolf.Lib.Judge>();

        AgentStatistics statistics;
        

        public override string Name
        {
            get { return "Udon"; }
        }

        long maxUpdateTime;


        public override void Initialize(GameInfo gameInfo, GameSetting gameSetting)
        {
            try
            {
                base.Initialize(gameInfo, gameSetting);

                // 1戦目の初期化
                if(statistics == null)
                {
                    statistics = new AgentStatistics(gameInfo.AgentList);
                }

                // フラグ・蓄積情報等の初期化
                fakeSeerJudge.Clear();
                maxUpdateTime = 0;

                if(gameSetting.PlayerNum >= 15)
                {
                    // 15人村
                    // CO・判定からパターン削除を行う設定にする
                    Agi.IsRemoveVillagerFakeCO = true;
                }
                else
                {
                    // 5人村
                    // CO・判定からパターン削除を行わない設定にする
                    Agi.IsRemoveVillagerFakeCO = false;
                }


            }
            catch(Exception)
            {
                if(!isIgnoreError)
                {
                    throw;
                }
            }
        }

        public override void Update(GameInfo gameInfo)
        {
            try
            {
                // 時間計測を開始する
                System.Diagnostics.Stopwatch sw = System.Diagnostics.Stopwatch.StartNew();

                base.Update(gameInfo);

                // 時間計測を止める
                sw.Stop();
            
                if ( maxUpdateTime < sw.ElapsedMilliseconds )
                {
                    maxUpdateTime = sw.ElapsedMilliseconds;
                }

                //Console.WriteLine( gameInfo.Day + ":" + gameInfo.TalkList.Count + " " +
                //                   LatestGuess.LikelyPattern.Pattern.ToString() + " " +
                //                   LatestGuess.LikelyPattern.Score );
            }
            catch(Exception)
            {
                if(!isIgnoreError)
                {
                    throw;
                }
            }
        }

        public override void DayStart()
        {
            try
            {
                base.DayStart();

                if ( Agi.MyRole == Role.POSSESSED )
                {
                    AddFakeSeerJudge_Possessed();
                }
            }
            catch(Exception)
            {
                if(!isIgnoreError)
                {
                    throw;
                }
            }
        }
        
        public override void Finish()
        {
            try
            {
                statistics.update(Agi);
                //Console.WriteLine( maxUpdateTime );
            }
            catch(Exception)
            {
                if(!isIgnoreError)
                {
                    throw;
                }
            }
        }

        protected override void InitStrategy(GameInfo gameInfo, GameSetting gameSetting)
        {
            base.InitStrategy(gameInfo, gameSetting);
            
            // 推理
            GuessManager = MyStrategyBuilder.GetGuess(gameSetting, Agi.MyRole);
            
            // 行動
            ActionPlanner = MyStrategyBuilder.GetAction(gameSetting, Agi.MyRole);
            
            // 発言
            TalkGenerator = MyStrategyBuilder.GetTalk(gameSetting, Agi.MyRole);
        }

        public override string Talk()
        {
            try
            {
                TalkGeneratorArgs args = new TalkGeneratorArgs { Agi = Agi, GuessResult = LatestGuess, ActionPlan = LatestPlan };
                args.Items.Add( "FakeSeerJudge", fakeSeerJudge );
                args.Items.Add("AgentStatistics", statistics);

                return TalkGenerator.Generation( args );
            }
            catch(Exception)
            {
                if(!isIgnoreError)
                {
                    throw;
                }

                return Content.OVER.Text;
            }
        }


        /// <summary>
        /// 騙り占い判定の追加（狂人用）
        /// </summary>
        private void AddFakeSeerJudge_Possessed()
        {

            // 0日目は占い結果なし
            if ( Agi.Day == 0 ) { return; }

            // 占い結果の各項目（暫定設定）
            int day = Agi.Day;
            Agent me = Agi.Me;
            Agent target = LatestPlan.DevineTarget;
            Species result = Species.HUMAN;

            // 自分真視点で確定黒の人物を占った場合は人狼判定
            if ( !LatestGuess.LikelyVillagerPattern.ContainsKey( target ) &&
                 !LatestGuess.LikelyPossessedPattern.ContainsKey( target ) )
            {
                result = Species.WEREWOLF;
            }

            // 15人村初日→86戦目以降なら人狼判定
            if(Agi.AgentList.Count == 15 && Agi.AliveAgentList.Count == 15 && statistics.statistics[Agi.Me].gameCount >= 85)
            {
                result = Species.WEREWOLF;
            }

            // 15人村で生存者5人→自分狂視点の人間がいれば人狼判定
            if(Agi.AgentList.Count == 15 && Agi.AliveAgentList.Count == 5)
            {
                foreach(Agent agent in Agi.AliveAgentList)
                {
                    if(Agi.SelfPossessedViewTrustInfo.GetSpeciesMap()[agent] == Species.HUMAN &&
                       Agi.SelfViewTrustInfo.GetSpeciesMap()[agent] != Species.HUMAN)
                    {
                        target = agent;
                        result = Species.WEREWOLF;
                    }
                }
            }

            // 5人村初日→80%人狼判定
            if(Agi.AgentList.Count == 5 && Agi.AliveAgentList.Count == 5 && new Random().NextDouble() < 0.8)
            {
                if(Agi.SelfViewTrustInfo.GetSpeciesMap()[target] != Species.HUMAN)
                {
                    result = Species.WEREWOLF;
                }
            }

            // 3人→自分狼判定
            if(Agi.AliveAgentList.Count == 3)
            {
                target = Agi.Me;
                result = Species.WEREWOLF;
            }

            AIWolf.Lib.Judge judge = new AIWolf.Lib.Judge( day, me, target, result );

            fakeSeerJudge.Add( judge );
        }

        /// <summary>
        /// 推理を行う
        /// </summary>
        protected override void Guess()
        {
            GuessStrategyArgs args = new GuessStrategyArgs { Agi = Agi };
            args.Items.Add("AgentStatistics", statistics);
            LatestGuess = GuessManager.Exec(args);
        }

        /// <summary>
        /// 行動方針の決定
        /// </summary>
        protected override void PlanAction()
        {
            ActionStrategyArgs args = new ActionStrategyArgs { Agi = Agi, GuessResult = LatestGuess };
            args.Items.Add("AgentStatistics", statistics);
            LatestPlan = ActionPlanner.Execute(args);
        }

    }
}

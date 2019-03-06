using AIWolf.Lib;
using System;
using System.Collections.Generic;
using System.Text;
using Wepwawet.ActionPlan;
using Wepwawet.Guess;
using Wepwawet.TalkGenerator;
using Wepwawet.Lib;

namespace Wepwawet.Player
{
    public abstract class WepwawetPlayer : IPlayer
    {
        public virtual string Name
        {
            get { return "WepwawetPlayer"; }
        }

        /// <summary>ゲーム情報</summary>
        protected AdvanceGameInfo Agi { get; set; }


        /// <summary>推理するもの</summary>
        protected IGuessManager GuessManager { get; set; }

        /// <summary>行動方針を決定するもの</summary>
        protected IActionPlanner ActionPlanner { get; set; }

        /// <summary>発話を生成するもの</summary>
        protected ITalkGenerator TalkGenerator { get; set; }

        

        /// <summary>最後に行った推理の結果</summary>
        protected GuessResult LatestGuess { get; set; }

        /// <summary>最後に行った行動計画</summary>
        protected Plan LatestPlan { get; set; }


        /// <summary>エラーを無視して続行するか</summary>
        public bool isIgnoreError = false;


        public virtual void Initialize(GameInfo gameInfo, GameSetting gameSetting)
        {
            try
            {
                Agi = new AdvanceGameInfo(gameInfo, gameSetting);

                InitStrategy(gameInfo, gameSetting);
            }
            catch(Exception)
            {
                if(!isIgnoreError)
                {
                    throw;
                }
            }
        }

        public virtual void Update(GameInfo gameInfo)
        {
            try
            {
                Agi.Update(gameInfo);
            
                // 推理などを行うのはゲームが決着している場合以外
                if ( gameInfo.RoleMap.Count != gameInfo.AgentList.Count )
                {
                    // 推理を実行する
                    Guess();

                    // 行動方針を決定する
                    PlanAction();
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

        public virtual void DayStart()
        {
        }

        public virtual Agent Divine()
        {
            try
            {
                return LatestPlan.DevineTarget;
            }
            catch(Exception)
            {
                if(!isIgnoreError)
                {
                    throw;
                }

                return null;
            }
        }
        
        public virtual Agent Attack()
        {
            try
            {
                return LatestPlan.AttackTarget;
            }
            catch(Exception)
            {
                if(!isIgnoreError)
                {
                    throw;
                }

                return null;
            }
        }

        public virtual Agent Guard()
        {
            try
            {
                return LatestPlan.GuardTarget;
            }
            catch(Exception)
            {
                if(!isIgnoreError)
                {
                    throw;
                }

                return null;
            }
        }

        public virtual Agent Vote()
        {
            try
            {
                return LatestPlan.VoteTarget;
            }
            catch(Exception)
            {
                if(!isIgnoreError)
                {
                    throw;
                }

                return null;
            }
        }

        public virtual string Talk()
        {
            try
            {
                TalkGeneratorArgs args = new TalkGeneratorArgs { Agi = Agi };
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

        public virtual string Whisper()
        {
            return Content.OVER.Text;
        }

        public virtual void Finish()
        {
        }

        /// <summary>
        /// 戦略の初期化を行う
        /// </summary>
        protected virtual void InitStrategy(GameInfo gameInfo, GameSetting gameSetting)
        {
            //TODO エラーが出ない程度の仮初期化
        }

        /// <summary>
        /// 推理を行う
        /// </summary>
        protected virtual void Guess()
        {
            GuessStrategyArgs args = new GuessStrategyArgs { Agi = Agi };
            LatestGuess = GuessManager.Exec( args );
        }

        /// <summary>
        /// 行動方針の決定
        /// </summary>
        protected virtual void PlanAction()
        {
            ActionStrategyArgs args = new ActionStrategyArgs { Agi = Agi, GuessResult = LatestGuess };
            LatestPlan = ActionPlanner.Execute( args );
        }

    }
}

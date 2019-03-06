using AIWolf.Lib;
using System;
using System.Collections.Generic;
using System.Text;
using Wepwawet.ActionPlan;
using Wepwawet.Guess;
using Wepwawet.Lib;

namespace Wepwawet.Player
{
    public abstract class WepwawetPlayer : IPlayer
    {
        public virtual string Name
        {
            get { return "WepwawetPlayer"; }
        }

        protected AdvanceGameInfo agi;


        /// <summary>保有する推理戦略</summary>
        protected List<HasGuessStrategy> hasGuessStrategy = new List<HasGuessStrategy>();

        /// <summary>最後に行った推理の結果</summary>
        protected GuessResult LatestGuess { get; private set; }


        /// <summary>行動戦略</summary>
        protected IActionPlanner ActionPlanner;
        




        public virtual void Initialize(GameInfo gameInfo, GameSetting gameSetting)
        {
            agi = new AdvanceGameInfo(gameInfo, gameSetting);

            InitStrategy();
        }

        public virtual void Update(GameInfo gameInfo)
        {
            agi.Update(gameInfo);

            // 推理などは日が変わったタイミング以外で行う
            if ( agi.IsDayUpdate )
            {

            }
            else
            {

            }

            // こっちだけでいいかも
            // 推理などを行うのはゲームが決着している場合以外
            if ( gameInfo.RoleMap.Count != gameInfo.AgentList.Count )
            {
                // 推理を実行する
                Guess();

                // 行動方針を決定する
                PlanAction();

                // debug
                /*
                Console.WriteLine(gameInfo.Day + ":" + gameInfo.TalkList.Count + " " +
                                  LatestGuess.LikelyPattern.Pattern.GetCode() + " " +
                                  LatestGuess.LikelyPattern.Score);
                */
            }

        }

        public virtual void DayStart()
        {
        }

        public virtual Agent Divine()
        {
            return ActionPlanner.GetDevine();
        }
        
        public virtual Agent Attack()
        {
            return ActionPlanner.GetAttack();
        }
        public virtual Agent Guard()
        {
            return ActionPlanner.GetGuard();
        }

        public virtual Agent Vote()
        {
            return ActionPlanner.GetVote();
        }

        public virtual string Talk()
        {
            return Content.OVER.Text;
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
        protected virtual void InitStrategy()
        {
            //TODO エラーが出ない程度の仮初期化
        }

        /// <summary>
        /// 推理を行う
        /// </summary>
        protected virtual void Guess()
        {
            GuessStrategyArgs args = new GuessStrategyArgs { Agi = agi };
            LatestGuess = new GuessResultExt( hasGuessStrategy, args);
        }

        /// <summary>
        /// 行動方針の決定
        /// </summary>
        protected virtual void PlanAction()
        {
            ActionStrategyArgs args = new ActionStrategyArgs { Agi = agi, GuessResult = LatestGuess };
            ActionPlanner.Execute( args );
        }

    }
}

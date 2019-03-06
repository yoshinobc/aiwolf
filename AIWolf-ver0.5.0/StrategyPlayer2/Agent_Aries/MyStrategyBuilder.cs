using Agent_Aries.MyStrategy.ActionPlan;
using Agent_Aries.MyStrategy.Guess;
using Agent_Aries.MyStrategy.TalkGenerator;
using AIWolf.Lib;
using System;
using System.Collections.Generic;
using System.Text;
using Wepwawet.ActionPlan;
using Wepwawet.ActionPlan.Tree;
using Wepwawet.Guess;
using Wepwawet.Guess.Tree;
using Wepwawet.SampleStrategy.Guess;
using Wepwawet.TalkGenerator;
using Wepwawet.TalkGenerator.Tree;

namespace Agent_Aries
{
    class MyStrategyBuilder
    {

        public static IGuessManager GetGuess(GameSetting gameSetting, Role role)
        {

            ParallelTreeGuess guessRoot = new ParallelTreeGuess();
            TreeGuessManager guessManager = new TreeGuessManager() { GuessStrategy = guessRoot };

            guessRoot.Child.Add(new FirstImpression());
            guessRoot.Child.Add(new AllFake());
            guessRoot.Child.Add(new VoteLine());

            if(gameSetting.PlayerNum <= 5)
            {
                guessRoot.Child.Add(new COPattern());
            }

            if(role.GetTeam() == Team.VILLAGER)
            {
                guessRoot.Child.Add(new EstimateLine());
                guessRoot.Child.Add(new COAndJudge());
                guessRoot.Child.Add(new Learn_CO());
                guessRoot.Child.Add(new Learn_Judge());
                //guessRoot.Child.Add(new Learn_Estimate());
                guessRoot.Child.Add(new Learn_Talk());
                if(gameSetting.PlayerNum == 5)
                {
                    guessRoot.Child.Add(new VoteTarget_5());
                    guessRoot.Child.Add(new KusoMeta5());
                }
                if(gameSetting.PlayerNum == 15)
                {
                    guessRoot.Child.Add(new VoteTarget_15());
                    guessRoot.Child.Add(new KusoMeta15());
                }
            }

            if(role == Role.WEREWOLF)
            {
                guessRoot.Child.Add(new Favor());
            }

            return guessManager;
        }



        public static IActionPlanner GetAction(GameSetting gameSetting, Role role)
        {

            ParallelTreeAction actionRoot = new ParallelTreeAction();
            TreeActionPlanner actionPlanner = new TreeActionPlanner() { ActionStrategy = actionRoot };

            actionRoot.Child.Add(new FixInfo());
            actionRoot.Child.Add(new Learn_VoteStack());
            actionRoot.Child.Add(new Learn_WinRate());

            if(role.GetTeam() == Team.VILLAGER)
            {
                actionRoot.Child.Add(new Suspicious());
                actionRoot.Child.Add(new VoteStack());
                actionRoot.Child.Add(new RespondRequest());
                actionRoot.Child.Add(new AvoidExecute());
                actionRoot.Child.Add(new Revote());
                if(gameSetting.PlayerNum == 15)
                {
                    actionRoot.Child.Add(new RoleWeight());
                }
                else
                {
                    actionRoot.Child.Add(new RoleWeight5());
                }
            }

            if(role == Role.WEREWOLF)
            {
                actionRoot.Child.Add(new Weight(new Suspicious(), 0.5));
                actionRoot.Child.Add(new Weight(new AvoidExecute(), 2.0));
                actionRoot.Child.Add(new Weight(new VoteStack(), 1.5));
                actionRoot.Child.Add(new AvoidGuard());
                actionRoot.Child.Add(new NoAttackPossessed());
                actionRoot.Child.Add(new NoAttackPossessedPP());
                actionRoot.Child.Add(new AttackObstacle());
                actionRoot.Child.Add(new Revote());
                if(gameSetting.PlayerNum == 15)
                {
                    actionRoot.Child.Add(new LineCut());
                }
                if(gameSetting.PlayerNum == 15)
                {
                    actionRoot.Child.Add(new Weight(new RoleWeight(), 0.3));
                }
                else
                {
                    actionRoot.Child.Add(new Weight(new RoleWeight5(), 0.3));
                }
            }

            if(role == Role.POSSESSED)
            {
                actionRoot.Child.Add(new Weight(new Suspicious(), 0.5));
                actionRoot.Child.Add(new Weight(new RoleWeight(), 0.3));
                actionRoot.Child.Add(new Weight(new VoteStack(), 0.5));
                //actionRoot.Child.Add(new PowerPlayOfPosessed()); // 2018大会では使わない
                actionRoot.Child.Add(new PossessedVote());
            }

            return actionPlanner;
        }


        public static ITalkGenerator GetTalk(GameSetting gameSetting, Role role)
        {
            TreeTalkGenerator talkGenerator = new TreeTalkGenerator();

            TreeTalkSelector talkRoot = new TreeTalkSelector();


            // 村陣営（のふり）の動作
            TreeTalkSelector vilRoot = new TreeTalkSelector();

            // 占霊どちらかなら即CO
            if(role == Role.SEER || role == Role.MEDIUM)
            {
                TreeTalkSelector coSelector = new TreeTalkSelector();
                coSelector.Condition.Add((TalkGeneratorArgs args) => { return args.Agi.GetComingOutRole(args.Agi.Me) != role; });
                coSelector.Child.Add(new TalkComingOut() { Role = role });

                vilRoot.Child.Add(coSelector);
            }

            // 占霊なら結果報告
            if(role == Role.SEER)
            {
                if(gameSetting.PlayerNum >= 15)
                {
                    vilRoot.Child.Add(new SeerReport());
                }
                else
                {
                    vilRoot.Child.Add(new SeerReport_5());
                }
            }
            if(role == Role.MEDIUM)
            {
                vilRoot.Child.Add(new MediumReport());
            }

            // 狂人なら即占CO
            if(role == Role.POSSESSED)
            {
                TreeTalkSelector coSelector = new TreeTalkSelector();
                coSelector.Condition.Add((TalkGeneratorArgs args) => { return args.Agi.GetComingOutRole(args.Agi.Me) != Role.SEER; });
                coSelector.Child.Add(new TalkComingOut() { Role = Role.SEER });

                vilRoot.Child.Add(coSelector);
            }

            // 占騙りなら結果報告
            if(role == Role.POSSESSED)
            {
                vilRoot.Child.Add(new FakeSeerReport());
            }

            // 発言傾向の調整
            if(role == Role.BODYGUARD || role == Role.VILLAGER || role == Role.WEREWOLF)
            {
                TreeTalkSelector talkSelector = new TreeTalkSelector();
                talkSelector.Condition.Add(
                    (TalkGeneratorArgs args) =>
                    {
                        if(args.Agi.Day == 1 && args.Agi.TodayInfo.TalkList != null && args.Agi.TodayInfo.TalkList.Count == args.Agi.GameSetting.PlayerNum)
                        {
                            if(((AgentStatistics)args.Items["AgentStatistics"]).statistics[args.Agi.Me].gameCount > 40)
                            {
                                return true;
                            }
                        }
                        return false;
                    }
                );
                talkSelector.Child.Add(new Probability(new VotePlan() {IsForce = true}) { SuccessProbability = 0.9 });
                vilRoot.Child.Add(talkSelector);
            }

            // 発言傾向の調整
            if(gameSetting.PlayerNum == 5 && (role == Role.VILLAGER || role == Role.WEREWOLF))
            {
                TreeTalkSelector talkSelector = new TreeTalkSelector();
                talkSelector.Condition.Add(
                    (TalkGeneratorArgs args) =>
                    {
                        if(args.Agi.Day == 1 && args.Agi.TodayInfo.TalkList != null && args.Agi.TodayInfo.TalkList.Count == args.Agi.GameSetting.PlayerNum * 3)
                        {
                            if(((AgentStatistics)args.Items["AgentStatistics"]).statistics[args.Agi.Me].gameCount > 40)
                            {
                                return true;
                            }
                        }
                        return false;
                    }
                );
                talkSelector.Child.Add(new Probability(new Over()) { SuccessProbability = 0.9 });
                vilRoot.Child.Add(talkSelector);
            }

            // 発言傾向の調整
            if(gameSetting.PlayerNum == 15 && (role == Role.BODYGUARD || role == Role.VILLAGER || role == Role.WEREWOLF))
            {
                TreeTalkSelector talkSelector = new TreeTalkSelector();
                talkSelector.Condition.Add(
                    (TalkGeneratorArgs args) =>
                    {
                        if(args.Agi.Day == 1 && args.Agi.TodayInfo.TalkList != null && args.Agi.TodayInfo.TalkList.Count == args.Agi.GameSetting.PlayerNum * 2)
                        {
                            if(((AgentStatistics)args.Items["AgentStatistics"]).statistics[args.Agi.Me].gameCount > 40)
                            {
                                return true;
                            }
                        }
                        return false;
                    }
                );
                talkSelector.Child.Add(new Probability(new VotePlan() { IsForce = true }) { SuccessProbability = 0.9 });
                vilRoot.Child.Add(talkSelector);
            }


            // Talk条件
            Predicate<TalkGeneratorArgs> condition = (TalkGeneratorArgs args) =>
            {
                return args.Agi.Day == 1 && args.Agi.Day == 1;
            };
            vilRoot.Child.Add( new Probability(new VotePlan()) {SuccessProbability = 0.7});

            // 投票先の宣言
            vilRoot.Child.Add(new VotePlan());

            // 暫定
            talkGenerator.ActionStrategy = vilRoot;


            return talkGenerator;
        }
        

    }
}

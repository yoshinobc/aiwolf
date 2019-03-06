using AIWolf.Lib;
using System.Linq;
using System.Collections.Generic;
using Wepwawet.Condition;
using Wepwawet.Guess;
using Wepwawet.Guess.Tree;
using Wepwawet.Lib;

namespace Agent_Aries.MyStrategy.Guess
{
    /// <summary>
    /// 推理戦略「CO・判定」クラス
    /// </summary>
    class COAndJudge : IGuessNode
    {
        bool isSuccess = false;
        List<PartGuess> guessList = new List<PartGuess>();


        public List<PartGuess> GetGuess()
        {
            return guessList;
        }

        public bool IsSuccess()
        {
            return isSuccess;
        }

        public void Update( GuessStrategyArgs args )
        {

            // 実行結果のクリア
            isSuccess = false;
            guessList.Clear();

            // 初日は行わない
            if(args.Agi.Day < 1)
            {
                return;
            }

            // 1日目0発言では行わない
            if(args.Agi.Day == 1 && args.Agi.TodayInfo.TalkList.Count == 0)
            {
                return;
            }

            // 初手黒は狂人寄り
            if(args.Agi.GameSetting.PlayerNum == 15)
            {
                foreach(Wepwawet.Lib.Judge judge in args.Agi.SeerJudge.Where(judge => judge.JudgeTalk.Day == 1 && judge.Result == Species.WEREWOLF))
                { 
                    guessList.Add(new PartGuess()
                    {
                        Condition = RoleCondition.GetCondition(judge.Agent, Role.POSSESSED),
                        Correlation = 1.25
                    });
                }
            }

            // 身内切り・誤爆を薄く見る
            foreach(Wepwawet.Lib.Judge judge in args.Agi.SeerJudge)
            {
                BitCondition con = new BitCondition();
                con.AddWerewolf(judge.Agent);
                con.AddWerewolf(judge.Target);
                guessList.Add(new PartGuess()
                {
                    Condition = con,
                    Correlation = 0.6
                });

                con = new BitCondition();
                con.AddPossessed(judge.Agent);
                con.AddWerewolf(judge.Target);
                guessList.Add(new PartGuess()
                {
                    Condition = con,
                    Correlation = 0.8
                });
            }

            // ●打ち先が占霊CO、占COに●打ち
            List<Agent> seerList = args.Agi.GetComingOutAgent(Role.SEER);
            foreach(Wepwawet.Lib.Judge judge in args.Agi.SeerJudge)
            {
                if(judge.Result == Species.WEREWOLF)
                {
                    foreach(ComingOut co in args.Agi.ComingOut)
                    {
                        if(judge.Target.Equals(co.Agent) && (co.Role == Role.SEER || co.Role == Role.MEDIUM))
                        {
                            if(judge.JudgeTalk.Day * 10000 + judge.JudgeTalk.Turn < co.ComingOutTalk.Day * 10000 + co.ComingOutTalk.Turn)
                            {
                                guessList.Add(new PartGuess()
                                {
                                    Condition = RoleCondition.GetCondition(judge.Target, Role.WEREWOLF),
                                    Correlation = 1.5
                                });
                            }

                            if(judge.JudgeTalk.Day == 1 && seerList.Count == 2 && 
                               judge.JudgeTalk.Day * 10000 + judge.JudgeTalk.Turn > co.ComingOutTalk.Day * 10000 + co.ComingOutTalk.Turn)
                            {
                                guessList.Add(new PartGuess()
                                {
                                    Condition = TeamCondition.GetCondition(judge.Agent, Team.WEREWOLF),
                                    Correlation = 1.1
                                });
                            }
                        }
                    }
                }
            }


            // 噛まれた占いの結果を真で見る
            List<Agent> attackedSeer = new List<Agent>();
            foreach(DayInfo dayInfo in args.Agi.DayInfo.Values)
            {
                if(dayInfo.AttackDeadAgent != null && dayInfo.AttackDeadAgent.Count >= 1)
                {
                    if(seerList.Contains(dayInfo.AttackDeadAgent[0]))
                    {
                        attackedSeer.Add(dayInfo.AttackDeadAgent[0]);
                    }
                }
            }
            foreach(KeyValuePair<int, Agent> kv in args.Agi.MyGuardHistory)
            {
                if(args.Agi.DayInfo[kv.Key].AttackDeadAgent != null && args.Agi.DayInfo[kv.Key].AttackDeadAgent.Count <= 0)
                {
                    attackedSeer.Add(kv.Value);
                }
            }
            foreach(Wepwawet.Lib.Judge judge in args.Agi.SeerJudge)
            {
                if(attackedSeer.Contains(judge.Agent))
                {
                    guessList.Add(new PartGuess()
                    {
                        Condition = RoleCondition.GetCondition(judge.Target, Role.WEREWOLF),
                        Correlation = (judge.Result == Species.WEREWOLF) ? 2.0 : 0.5
                    });
                }
            }

            // 判定数が合わない人を偽で見る(暫定対応で多い場合のみ)
            foreach(Agent agent in seerList)
            {
                int count = args.Agi.SeerJudge.Where(judge => judge.Agent.Equals(agent)).Count();
                if(count > args.Agi.Day)
                {
                    guessList.Add(new PartGuess()
                    {
                        Condition = TeamCondition.GetCondition(agent, Team.WEREWOLF),
                        Correlation = 4.0
                    });

                }
            }
            List<Agent> mediumList = args.Agi.GetComingOutAgent(Role.MEDIUM);
            foreach(Agent agent in mediumList)
            {
                int count = args.Agi.MediumJudge.Where(judge => judge.Agent.Equals(agent)).Count();
                if(count > args.Agi.Day)
                {
                    guessList.Add(new PartGuess()
                    {
                        Condition = TeamCondition.GetCondition(agent, Team.WEREWOLF),
                        Correlation = 4.0
                    });

                }
            }


            // 実行成功にする
            isSuccess = true;

        }
    }
}

using AIWolf.Lib;
using System;
using System.Collections.Generic;
using System.Text;
using Wepwawet.Condition;
using Wepwawet.Lib;
using Wepwawet.Guess;
using Wepwawet.Guess.Tree;

namespace Agent_Aries.MyStrategy.Guess
{
    /// <summary>
    /// 推理戦略「COパターンからの絞込み」クラス
    /// 概要：役職COからのパターン絞込みを行う（パターンを削除せずスコアを付けたい場合に使用）
    /// 現状は５人村のみ対応
    /// </summary>
    public class COPattern : IGuessNode
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
            guessList.Clear();

            // 自分の判定は100%信じる
            foreach(AIWolf.Lib.Judge judge in args.Agi.MySeerJudge)
            {
                guessList.Add(new PartGuess()
                {
                    Condition = RoleCondition.GetCondition(judge.Target, Role.WEREWOLF),
                    Correlation = (judge.Result == Species.WEREWOLF) ? 10.0 : 0.0,
                });
            }
            
            // 役職CO者を取得
            List<Agent> seerList = args.Agi.GetComingOutAgent( Role.SEER );

            // 判定騙りをするエージェントを取得(推測)
            AgentStatistics statistics = (AgentStatistics)args.Items["AgentStatistics"];
            List<Agent> liarSeerList = new List<Agent>();
            foreach(Agent agent in seerList)
            {
                int roleEveCnt = statistics.statistics[agent].eventCount[Role.SEER].GetOrDefault("1d_DevineWhite", 0);
                int roleEveCnt2 = statistics.statistics[agent].eventCount[Role.SEER].GetOrDefault("1d_DevineBlack", 0);

                if(roleEveCnt + roleEveCnt2 >= 5 && roleEveCnt2 > roleEveCnt)
                {
                    liarSeerList.Add(agent);
                }
            }

            foreach(Wepwawet.Lib.Judge judge in args.Agi.SeerJudge)
            {
                double fakeBlaskJudgeRate = 0.1;
                if(liarSeerList.Contains(judge.Agent))
                {
                    fakeBlaskJudgeRate = 3.0 / 4.0 * 2.0 / 3.0;
                }

                // 村陣営が人間に人狼判定
                if(judge.Result == Species.WEREWOLF)
                {
                    BitCondition con = new BitCondition();
                    con.AddNotWerewolfTeam(judge.Agent);
                    con.AddNotWerewolf(judge.Target);
                    guessList.Add(new PartGuess()
                    {
                        Condition = con,
                        Correlation = fakeBlaskJudgeRate,
                    });
                }

                // 村陣営が人狼に人間判定
                if(judge.Result == Species.HUMAN)
                {
                    BitCondition con = new BitCondition();
                    con.AddNotWerewolfTeam(judge.Agent);
                    con.AddWerewolf(judge.Target);
                    guessList.Add(new PartGuess()
                    {
                        Condition = con,
                        Correlation = 0.1,
                    });
                }

            }

            // COから村騙りが無い場合の内訳を絞り込む
            List<Agent> coAgent = args.Agi.GetComingOutAgent(Role.SEER);
            if(coAgent.Count >= 2)
            {
                BitMatchNumCondition con = new BitMatchNumCondition()
                {
                    MinNum = 0,
                    MaxNum = coAgent.Count - 2
                };
                foreach(Agent agent in coAgent)
                {
                    con.AddWerewolfTeam(agent);
                }
                guessList.Add(new PartGuess()
                {
                    Condition = con,
                    Correlation = 0.1,
                });
            }

            // 実行成功にする
            isSuccess = true;

        }

    }
}

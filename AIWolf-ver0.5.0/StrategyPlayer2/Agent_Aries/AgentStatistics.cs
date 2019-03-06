using Agent_Aries.Lib;
using AIWolf.Lib;
using System;
using System.Collections.Generic;
using System.Text;
using Wepwawet.Condition;
using Wepwawet.Guess;
using Wepwawet.Lib;

namespace Agent_Aries
{

    public class AgentStatistics
    {

        public Dictionary<Agent, Statistics> statistics = new Dictionary<Agent, Statistics>();

        /// <summary>
        /// コンストラクタ
        /// </summary>
        /// <param name="agentList">エージェント一覧</param>
        public AgentStatistics(List<Agent> agentList)
        {
            // エージェント毎の統計情報を初期化
            foreach(Agent agent in agentList)
            {
                Statistics stat = new Statistics();
                stat.init(agent);
                statistics.Add(agent, stat);
            }
        }


        public void update(AdvanceGameInfo agi)
        {
            bool isWinWolf = agi.AliveAgentList.Exists(agent => agi.RoleMap[agent] == Role.WEREWOLF);

            // 勝敗
            foreach(KeyValuePair<Agent, Role> kv in agi.RoleMap)
            {
                statistics[kv.Key].gameCount++;
                statistics[kv.Key].roleCount[kv.Value] = statistics[kv.Key].roleCount[kv.Value] + 1;
                statistics[kv.Key].increaseCOCount(kv.Value, agi.GetComingOutRole(kv.Key));
                if((kv.Value.GetTeam() == Team.WEREWOLF) == isWinWolf)
                {
                    statistics[kv.Key].increaseEventCount(kv.Value, "Win");
                }
                else
                {
                    statistics[kv.Key].increaseEventCount(kv.Value, "Lose");
                }
            }

            // 投票
            foreach(DayInfo dayInfo in agi.DayInfo.Values)
            {
                if(dayInfo.VoteList.Count > 0)
                {
                    VoteAnalyzer va = dayInfo.SaidVote;
                    foreach(Vote vote in dayInfo.VoteList[0])
                    {
                        if(va.MaxVoteReceivedAgent.Contains(vote.Target))
                        {
                            statistics[vote.Agent].increaseEventCount(agi.RoleMap[vote.Agent], "VoteToMax");
                        }
                        else
                        {
                            statistics[vote.Agent].increaseEventCount(agi.RoleMap[vote.Agent], "VoteToNotMax");
                        }
                    }
                }
            }

            // 1d発言
            List<Agent> estAgentList = new List<Agent>();
            foreach(ExtTalk talk in agi.DayInfo[1].TalkList)
            {
                if(talk.Content.Operator == Operator.NOP && talk.Content.Topic == Topic.ESTIMATE)
                {
                    if(!estAgentList.Contains(talk.Agent))
                    {
                        statistics[talk.Agent].increaseEventCount(agi.RoleMap[talk.Agent], "1d_Estimate");
                        estAgentList.Add(talk.Agent);
                    }
                }
            }
            // 発言傾向
            foreach(DayInfo dayInfo in agi.DayInfo.Values)
            {
                foreach(ExtTalk talk in dayInfo.TalkList)
                {
                    int id = -1;
                    if(talk.Content.Operator == Operator.NOP)
                    {
                        switch(talk.Content.Topic)
                        {
                            case Topic.Over:
                                id = 1;
                                break;
                            case Topic.Skip:
                                id = 2;
                                break;
                            case Topic.VOTE:
                                id = 3;
                                break;
                            case Topic.ESTIMATE:
                                id = (talk.Content.Role.GetTeam() == Team.WEREWOLF) ? 4 : 5;
                                break;
                            case Topic.COMINGOUT:
                                id = 6;
                                break;
                            case Topic.DIVINED:
                            case Topic.IDENTIFIED:
                            case Topic.GUARDED:
                                id = 7;
                                break;
                            case Topic.AGREE:
                                id = 8;
                                break;
                            case Topic.DISAGREE:
                                id = 9;
                                break;
                            default:
                                break;
                        }
                    }
                    else if(talk.Content.Operator == Operator.REQUEST)
                    {
                        id = 10;
                    }

                    if(id > 0)
                    {
                        statistics[talk.Agent].talkCount.increaseCount(talk.Day, talk.Turn, agi.GetComingOutRole(talk.Agent), agi.RoleMap[talk.Agent], id);
                    }
                }
            }

            // 判定
            foreach(Wepwawet.Lib.Judge judge in agi.SeerJudge)
            {
                if(judge.JudgeTalk.Day == 1)
                {
                    if(judge.Result == Species.WEREWOLF)
                    {
                        statistics[judge.Agent].increaseEventCount(agi.RoleMap[judge.Agent], "1d_DevineBlack");
                    }
                    else
                    {
                        statistics[judge.Agent].increaseEventCount(agi.RoleMap[judge.Agent], "1d_DevineWhite");
                    }
                }

            }

        }

    }


    public class Statistics
    {

        /// <summary>記憶対象のエージェント</summary>
        public Agent agent;

        /// <summary>ゲーム回数</summary>
        public int gameCount = 0;

        /// <summary>役職になった回数</summary>
        public Dictionary<Role, int> roleCount = new Dictionary<Role, int>();

        /** 各役職で終了時に各村役をＣＯしていた回数 */
        public Dictionary<Role, Dictionary<Role, int>> COCount = new Dictionary<Role, Dictionary<Role, int>>();

        /** 役職毎のイベント回数 */
        public Dictionary<Role, Dictionary<String, int>> eventCount = new Dictionary<Role, Dictionary<String, int>>();

        /** 役職毎の発話回数 */
        public TalkCount talkCount = new TalkCount();

        /** 推理の有効度 */
        public Dictionary<String, Double> weightOfGuess = new Dictionary<String, Double>();


        /**
		 * 初期化
         */
        public void init(Agent agent)
        {

            this.agent = agent;

            foreach(Role role in Enum.GetValues(typeof(Role)))
            {
                roleCount.Add(role, 0);
                COCount.Add(role, new Dictionary<Role, int>());
                eventCount.Add(role, new Dictionary<String, int>());
                foreach(Role role2 in Enum.GetValues(typeof(Role)))
                {
                    COCount[role].Add(role2, 0);
                }
            }

        }


        /**
		 * ＣＯカウントの増加
		 * @param role 本当の役職
		 * @param fakeRole 騙りCOした役職(何もCOしない場合Role.UNC、未COと村COは区別する)
		 */
        public void increaseCOCount(Role role, Role fakeRole)
        {
            COCount[role][fakeRole] = COCount[role][fakeRole] + 1;
        }


        /**
		 * イベントカウントの増加
		 * @param role 本当の役職
		 * @param eventCode イベントのコード
		 */
        public void increaseEventCount(Role role, String eventCode)
        {
            if(eventCount[role].ContainsKey(eventCode))
            {
                eventCount[role][eventCode] = eventCount[role][eventCode] + 1;
            }
            else
            {
                eventCount[role].Add(eventCode, 1);
            }
        }


        public List<PartGuess> getGuessFromEvent(String eventCode, GameSetting gameSetting)
        {

            // 推理リスト
            List<PartGuess> guesses = new List<PartGuess>();

            double wolfRate = 1.0;
            double posRate = 1.0;

            // 村人をプレイ済の場合のみ計算を行う(村人の理由はなんとなく)
            if(roleCount.GetOrDefault(Role.VILLAGER, 0) > 0)
            {

                int allEventCount = 0;
                foreach(Role role in Enum.GetValues(typeof(Role)))
                {
                    allEventCount += eventCount[role].GetOrDefault(eventCode, 0);
                }

                // イベントが一定回数以上発生している場合のみ計算可能
                if(allEventCount >= 2)
                {

                    // 狼をプレイ済の場合のみ計算を行う
                    if(roleCount.GetOrDefault(Role.WEREWOLF, 0) > 0)
                    {
                        int wolfEventCount = eventCount[Role.WEREWOLF].GetOrDefault(eventCode, 0);

                        double eventWolfRate = (double)wolfEventCount / allEventCount;
                        double measurementCountWolfRate = (double)roleCount[Role.WEREWOLF] / gameCount;
                        //double theoreticalCountWolfRate = (double)gameSetting.GetRoleNum(Role.WEREWOLF) / gameSetting.PlayerNum;

                        wolfRate = eventWolfRate / measurementCountWolfRate;

                        if(wolfRate < 0.99 || wolfRate > 1.01)
                        {
                            double weight = Math.Min(gameCount * 0.02, 0.5);

                            RoleCondition wolfCondition = RoleCondition.GetCondition(agent, Role.WEREWOLF);
                            PartGuess guess = new PartGuess();
                            guess.Condition = wolfCondition;
                            guess.Correlation = Math.Pow(Math.Max(wolfRate, 0.3), weight);
                            guesses.Add(guess);
                        }
                    }

                    // 狂をプレイ済の場合のみ計算を行う
                    if(roleCount.GetOrDefault(Role.POSSESSED, 0) > 0)
                    {
                        int posEventCount = eventCount[Role.POSSESSED].GetOrDefault(eventCode, 0);

                        double eventPosRate = (double)posEventCount / allEventCount;
                        double measurementCountPosRate = (double)roleCount[Role.POSSESSED] / gameCount;
                        //double theoreticalCountPosRate = (double)gameSetting.GetRoleNum(Role.POSSESSED) / gameSetting.PlayerNum;

                        posRate = eventPosRate / measurementCountPosRate;

                        if(posRate < 0.99 || posRate > 1.01)
                        {
                            double weight = Math.Min(gameCount * 0.02, 0.5);

                            RoleCondition posCondition = RoleCondition.GetCondition(agent, Role.POSSESSED);
                            PartGuess guess = new PartGuess();
                            guess.Condition = posCondition;
                            guess.Correlation = Math.Pow(Math.Max(posRate, 0.3), weight);
                            guesses.Add(guess);
                        }
                    }

                }

            }

            return guesses;

        }

    }

    public class TalkCount
    {

        /** 
         * 役職毎の発話回数 [day][turn][coRole][realRole][talkKind]
         * realRole=Role.UNCは合計回数として利用される
         * talkKind=0は合計回数として利用される
         */
        public int[,,,,] count = new int[16, 20, 9, 9, 11];

        public TalkCount()
        {
        }

        public void increaseCount(int day, int turn, Role coRole, Role realRole, int talkKind)
        {
            count[day, turn, (int)coRole, (int)realRole, talkKind]++;
            count[day, turn, (int)coRole, (int)realRole, 0]++;
            count[day, turn, (int)coRole, 0, talkKind]++;
            count[day, turn, (int)coRole, 0, 0]++;
        }

        public int GetAllTalkCount(int day, int turn, Role coRole, Role role)
        {
            int ret = 0;
            for(int i=0;i<count.GetUpperBound(4);i++)
            {
                ret += count[day, turn, (int)coRole, (int)role, i];
            }
            return ret;
        }

        public List<PartGuess> GetRateGuess(int day, int turn, Role coRole, int talkKind, Agent agent)
        {
            int allRoleTalkCount = count[day, turn, (int)coRole, 0, 0];
            int wolfTalkCount = count[day, turn, (int)coRole, (int)Role.WEREWOLF, 0];
            int posTalkCount = count[day, turn, (int)coRole, (int)Role.POSSESSED, 0];
            int allRoleEventCount = count[day, turn, (int)coRole, 0, talkKind];
            int wolfEventCount = count[day, turn, (int)coRole, (int)Role.WEREWOLF, talkKind];
            int posEventCount = count[day, turn, (int)coRole, (int)Role.POSSESSED, talkKind];
            int vilRoleTalkCount = allRoleTalkCount - wolfTalkCount - posTalkCount;
            int vilRoleEventCount = allRoleEventCount - wolfEventCount - posEventCount;

            if(allRoleTalkCount >= 50)
            {
                double wolfRate = 1.0;
                double posRate = 1.0;
                if(vilRoleEventCount > 0)
                {
                    if(wolfEventCount > 0)
                    {
                        wolfRate = (wolfEventCount / (double)wolfTalkCount) / (vilRoleEventCount / (double)vilRoleTalkCount);
                    }
                    else
                    {
                        wolfRate = 0.5;
                    }
                    if(posEventCount > 0)
                    {
                        posRate = (posEventCount / (double)posTalkCount) / (vilRoleEventCount / (double)vilRoleTalkCount);
                    }
                    else
                    {
                        posRate = 0.5;
                    }
                }
                else
                {
                    wolfRate = 3.0;
                    posRate = 3.0;
                }
                return new List<PartGuess>()
                {
                    new PartGuess(){ Condition=RoleCondition.GetCondition(agent, Role.WEREWOLF), Correlation=wolfRate},
                    new PartGuess(){ Condition=RoleCondition.GetCondition(agent, Role.POSSESSED), Correlation=posRate},
                };

            }
            return new List<PartGuess>();
        }

        public double GetWolfRate(int day, int turn, Role coRole, int talkKind)
        {
            int allRoleTalkCount = count[day, turn, (int)coRole, 0, 0];
            int wolfTalkCount = count[day, turn, (int)coRole, (int)Role.WEREWOLF, 0];
            int posTalkCount = count[day, turn, (int)coRole, (int)Role.POSSESSED, 0];
            int allRoleEventCount = count[day, turn, (int)coRole, 0, talkKind];
            int wolfEventCount = count[day, turn, (int)coRole, (int)Role.WEREWOLF, talkKind];
            int posEventCount = count[day, turn, (int)coRole, (int)Role.POSSESSED, talkKind];
            int vilRoleTalkCount = allRoleTalkCount - wolfTalkCount - posTalkCount;
            int vilRoleEventCount = allRoleEventCount - wolfEventCount - posEventCount;

            double wolfRate = 1.0;
            if(wolfTalkCount >= 5 && vilRoleTalkCount >= 5)
            {
                if(vilRoleEventCount > 0)
                {
                    if(wolfEventCount > 0)
                    {
                        wolfRate = (wolfEventCount / (double)wolfTalkCount) / (vilRoleEventCount / (double)vilRoleTalkCount);
                    }
                    else
                    {
                        wolfRate = 0.5;
                    }
                }
                else
                {
                    wolfRate = 3.0;
                }
            }
            return wolfRate;
        }

        public double GetPosRate(int day, int turn, Role coRole, int talkKind)
        {
            int allRoleTalkCount = count[day, turn, (int)coRole, 0, 0];
            int wolfTalkCount = count[day, turn, (int)coRole, (int)Role.WEREWOLF, 0];
            int posTalkCount = count[day, turn, (int)coRole, (int)Role.POSSESSED, 0];
            int allRoleEventCount = count[day, turn, (int)coRole, 0, talkKind];
            int wolfEventCount = count[day, turn, (int)coRole, (int)Role.WEREWOLF, talkKind];
            int posEventCount = count[day, turn, (int)coRole, (int)Role.POSSESSED, talkKind];
            int vilRoleTalkCount = allRoleTalkCount - wolfTalkCount - posTalkCount;
            int vilRoleEventCount = allRoleEventCount - wolfEventCount - posEventCount;
            
            double posRate = 1.0;
            if(posTalkCount >= 3 && vilRoleTalkCount >= 3)
            {
                if(vilRoleEventCount > 0)
                {
                    if(posEventCount > 0)
                    {
                        posRate = (posEventCount / (double)posTalkCount) / (vilRoleEventCount / (double)vilRoleTalkCount);
                    }
                    else
                    {
                        posRate = 0.5;
                    }
                }
                else
                {
                    posRate = 3.0;
                }
            }
            return posRate;
        }


    }

    static class DictionaryExtensions
    {
        public static TValue GetOrDefault<TKey, TValue>(this Dictionary<TKey, TValue> dic, TKey key, TValue defaultValue)
        {
            return dic.ContainsKey(key) ? dic[key] : defaultValue;
        }
    }

    static class GameSettingExtensions
    {
        public static int GetRoleNum(this GameSetting gameSetting, Role role)
        {
            return gameSetting.RoleNumMap.ContainsKey(role) ? gameSetting.RoleNumMap[role] : 0;
        }
    }

}

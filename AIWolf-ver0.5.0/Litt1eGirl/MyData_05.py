# -*- coding: utf-8 -*-
import aiwolfpy
import aiwolfpy.templatetalkfactory as ttf
import aiwolfpy.templatewhisperfactory as twf
import numpy as np
import copy,random,re

import string
import PlayerData
import COData as co
import RequestData as req
import savelog as sv
import LSTM

class MyData():

    def __init__(self):
        self.__seerCODataMap_   = {}
        self.__villCODataMap_   = {}
        self.__wolfCODataMap_   = {}
        self.__possCODataMap_   = {}

        self.__playerDataMap_   = {}
        self.__talkNumMap_      = {}
        self.__talkAllNumMap_   = {}
        self.__voteMap_         = {}
        self.__resultMap_       = {}
        self.__voteNum_         = {}
        #AliveAgent
        self.__aliveAgentList_  = []
        self.__aliveAgentIndexList_ = []
        self.__agentList_  = []
        self.__agentIndexList_  = []
        self.__attackedAgentList_ =[]
        self.__executedAgentList_ =[]
        self.__voteTable_   = {}
        self.__today_       = 0
        self.__playerNum_   = 0

        self.__isCOCall_        = False
        self.__isDivineCall_    = False
        self.__isOneTimeCall_   = False

        #RequestData
        self.__requestData_  = []

        self.gameInfo           = None

        self.turn               = 0
        self.logTurn            = -1
        self.pred       = LSTM.Predictor_05()
        self.SaveLog        = sv.SaveLog_05()
        self.rank       = []

        self.wolfWinrate    = []
        self.villWinrate    = []
        self.calc_flag      = False

    def update(self,gameInfo,talk_history, whisper_history):
        self.gameInfo   =   gameInfo
        if (talk_history != None):

            for i in range(len(talk_history)):
                talk    = talk_history[i]
                agentIdx    = talk['agent']
                turn    = talk['turn']
                self.setTurn(turn)
                isBlack = False

                #CO
                if (talk['text'].split()[0]== 'COMINGOUT'):

                    #占い師CO
                    if (talk['text'].split()[2] == 'SEER'):
                        self.newSeerData(agentIdx, self.__today_, turn)
                        p_data = self.__playerDataMap_.get(agentIdx)
                        p_data.addCOData('SEER', self.__today_ , turn)
                        resultlist = []
                        self.__resultMap_.update({agentIdx:resultlist})
                    #村人CO
                    elif (talk['text'].split()[2] == 'VILLAGER'):
                        self.newVillData(agentIdx, self.getToday(), turn)
                    #人狼CO
                    elif (talk['text'].split()[2] == 'WEREWOLF'):
                        self.newWolfData(agentIdx, self.getToday(), turn)
                    #狂人CO
                    elif (talk['text'].split()[2] == 'POSSESSED'):
                        self.newPossData(agentIdx, self.getToday(), turn)

                    self.__isCall_ = True
                    self.__isOneTimeCall_ = True

                #DIVINED
                elif (talk['text'].split()[0]== 'DIVINED'):
                    resultList = []
                    if (agentIdx not in self.__resultMap_):
                        self.__resultMap_.update({agentIdx:resultList})
                    else:
                        resultList = self.__resultMap_.get(agentIdx)
                    if (talk['text'].split()[2]== 'WEREWOLF'):
                        isBlack = True
                    if (isBlack):   resultList.append(1)
                    else:   resultList.append(0)
                    target = talk['text'].split()[1]
                    targetIdx = self.AgentToIndex(target)
                    #発言者、対象、白黒、日付を保存
                    self.addSeerData(agentIdx, targetIdx, isBlack, self.__today_, turn)

                    self.__isOneTimeCall_   = True
                    self.__isDivineCall_        = True

                #REQUEST
                if (talk['text'].split("(")[0] == "REQUEST"):
                    content = talk['text'].split("(")[1]
                    content = content.split(")")
                    content = content[0].split()
                    content_a = ["VOTE","DIVINATION","GUARD","ATTACK"]
                    content_b = ["Over","Skip"]
                    first = content[0]
                    if(first in content_a):
                        target = self.AgentToIndex(content[1])
                        self.setRequest(agentIdx,first,None,target)
                    elif(first in content_b):
                        self.setRequest(agentIdx,first,None,None)

                    target = self.AgentToIndex(first)
                    if(target != None):
                        ct = content[1]
                        if(ct in content_a):
                            target2 = self.AgentToIndex(content[2])
                            self.setRequest(agentIdx,content,target,target2)
                        else:
                            self.setRequest(agentIdx,content,target,None)

                if (talk['text'].split()[0] != 'Skip' and talk['text'].split()[0] != 'Over'):
                    num = self.__talkNumMap_.get(agentIdx)
                    num = num + 1
                    self.__talkNumMap_.update({agentIdx:num})

                num = self.__talkAllNumMap_.get(agentIdx)
                num = num +1
                self.__talkAllNumMap_.update({agentIdx:num})

            #LOGの成形
            if(self.logTurn != self.getTurn()):
                self.SaveLog.saveTalk(gameInfo,talk_history)
                turndata = self.SaveLog.getTurntalk()
                if(len(turndata) > 0):
                    self.logTurn = self.getTurn()
                    self.Predictor(turndata)

    def dayStart(self,gameInfo):
        self.gameInfo   = gameInfo
        self.__today_   = gameInfo["day"]
        self.__isOneTimeCall_   = False
        self.__isCOCall_  = False
        self.__isDivineCall_   = False
        self.logTurn    = -1

        if "statusMap" in gameInfo.keys():
            statusMap = gameInfo["statusMap"]

        death_list  = []
        del self.__aliveAgentList_[:]
        del self.__aliveAgentIndexList_[:]
        for ids in statusMap.keys():
            if statusMap[ids] == "ALIVE":
                name = 'Agent[' + "{0:02d}".format(int(ids)) + ']'
                idx = int(ids)
                self.__aliveAgentList_.append(name)
                self.__aliveAgentIndexList_.append(idx)
                death_list.append(1)
            else:
                death_list.append(0)
        if(len(self.__aliveAgentIndexList_) == 0):
            print("error")
        self.pred.set_death_list(death_list)

        executedAgentIdx = -1
        if gameInfo["executedAgent"] != None:
            executedAgentIdx = gameInfo["executedAgent"]
            self.addExecutedAgentList(executedAgentIdx)

        attackedAgentIdx = -1
        if gameInfo["attackedAgent"] != None:
            attackedAgentIdx = gameInfo["attackedAgent"]
            self.addAttackedAgentList(attackedAgentIdx)

        self.setAlive(executedAgentIdx, attackedAgentIdx)

        dayVoteList = [-1] * self.__playerNum_

        self.__voteTable_.update({self.getToday()-1 : dayVoteList})
        for vote in gameInfo['voteList']:
            voteList = self.__voteTable_.get(self.getToday()-1)
            self.__voteTable_[self.getToday()-1][vote['agent']-1] = vote['target']

        for vote in gameInfo['voteList']:
            agentIdx    = vote['agent']
            voteIdx     = vote['target']
            talkedIdx = self.__voteMap_[agentIdx]

            if (voteIdx == talkedIdx):  votepattern = 0
            else:
                if (talkedIdx != -1):   votepattern = 1
                else:                   votepattern = 2

            p_data  = self.__playerDataMap_.get(agentIdx)
            seer    = self.getAliveSeerCOAgentList()
            if (self.__today_ > 1 and agentIdx in seer):
                p_data.addDayData_black(votepattern, self.__resultMap_.get(agentIdx))
            elif (self.__today_ > 0):
                p_data.addDayData(votepattern)

        for agent in self.__aliveAgentList_:
            idx = self.AgentToIndex(agent)
            self.__talkNumMap_[idx]     =   0
            self.__talkAllNumMap_[idx]  =   0
            self.__voteMap_[idx]        =   -1
            self.__voteNum_[idx]        =   0

        #LOGの成形
        if(self.getToday() > 1):
            self.SaveLog.saveVote(gameInfo)
            dayvote    = self.SaveLog.getDayvote()
            self.Predictor(dayvote)

    def gameStart(self,gameInfo, playerNum, agentNames):
        self.gameInfo       =   gameInfo
        self.__playerNum_   = playerNum
        self.__agentList_   = agentNames
        self.__aliveAgentList_  = agentNames
        self.__agentIdxlist_  = self.AgentToIndexList(agentNames)
        self.__aliveAgentIndexList_ = self.AgentToIndexList(agentNames)
        self.agentIdx   = gameInfo['agent']
        self.role       = gameInfo['roleMap'][str(self.agentIdx)]  #自分の役割

        for i in range(self.__playerNum_):
            pd = PlayerData.PlayerData(self.__agentIdxlist_[i],self.__agentList_[i])
            self.__playerDataMap_.update({self.__agentIdxlist_[i]:pd})
            self.__talkNumMap_.update({self.__agentIdxlist_[i]:0})
            self.__talkAllNumMap_.update({self.__agentIdxlist_[i]:0})
            self.__voteMap_.update({self.__agentIdxlist_[i]:-1})
            self.__voteNum_.update({self.__agentIdxlist_[i]:0})

        self.pred.initialize(self.__playerNum_)

    def finish(self):
        self.pred.finish()

    #エージェントをインデックスにする
    def AgentToIndex(self,agent):
        idx = str(agent)
        idx = idx.replace("Agent[0","")
        idx = idx.replace("Agent[", "")
        idx = idx.replace("]","")
        if(str.isdigit(idx)):
            re  = int(idx)
            return re
        return None

    #エージェントリストをインデックスリストにする
    def AgentToIndexList(self,agentlist):
        reList = []
        for i in enumerate(agentlist):
            idx = str(i[1])
            idx = idx.replace("Agent[0","")
            idx = idx.replace("Agent[", "")
            idx = idx.replace("]","")
            reList.append(int(idx))
        return reList

    #インデックスリストをエージェントリストにする
    def IndexToAgentList(self,indexlist):
        reList = []
        for i in enumerate(indexlist):
            if(i < 10):
                agent = "Agent[0" + str(talk['agent']) + "]"
            else:
                agent = "Agent[" + str(talk['agent']) + "]"
            reList.append(agentIdx)
        return reList

    #占いCO関連

    #新しく占いCOした人のデータ作成
    def newSeerData(self,agentIdx, day, turn):
        if (agentIdx not in self.__seerCODataMap_):
            data = co.SeerCOData(day, turn, True)
            self.__seerCODataMap_.update({agentIdx:data})

    #占い結果を出した占い師のデータ更新
    def addSeerData(self,agentIdx, targetIdx, isBlack, day, turn):
        #COせずに結果を言った人
        if (agentIdx not in self.__seerCODataMap_):
            data = co.SeerCOData(day, turn, False)
            self.__seerCODataMap_.update({agentIdx:data})
        #COして結果を言った人
        else:
            data = self.__seerCODataMap_[agentIdx]
        data.add(day, targetIdx, isBlack)

    def getSeerCOAgentList(self):
        reList = []
        for k,v in self.__seerCODataMap_.items():   reList.append(k)
        return reList

    def getAliveSeerCOAgentList(self):
        reList = []
        for k,v in self.__seerCODataMap_.items():
            if (v.isAlive()):   reList.append(k)
        return reList

    def getTodaySeerCOAgentList(self):
        reList = []
        for k,v in self.__seerCODataMap_.items():
            if (v.isAlive() and v.getDay() == self.getToday()): reList.append(k)
        return reList

    def getSeerResultsList(self,index):
        data  = self.getseerCODataMap()
        if (index in data):
            return data.get(index).getKnownList()
        else:
            return None

    def getSeerCODataMap(self):
        return self.__seerCODataMap_

    def setSeerCODataMap(self,seerCODataMap):
        self.__seerCODataMap_ = seerCODataMap

    #人狼CO関連

    #新しく人狼COした人のデータ作成
    def newWolfData(self, agentIdx, day, turn):

        if (agentIdx not in self.__wolfCODataMap_):
            data = co.COData(day, turn, True)
            self.__wolfCODataMap_.update({agentIdx:data})

            #既に占いCOしていたらスライドチェック
            if (agentIdx in self.__seerCODataMap_):
                data.setAfterSlide(True)
                self.__seerCODataMap_[agentIdx].setAlive(False)

    def getWolfCOAgentList(self):
        reList = []
        for k,v in self.__wolfCODataMap_.items():     reList.append(k)
        return reList

    def getAliveWolfCOAgentList(self):
        reList = []
        for k,v in self.__wolfCODataMap_.items():
            if (v.isAlive()):   reList.append(k)
        return reList

    def getTodayWolfCOAgentList(self):
        reList = []
        for k,v in self.__wolfCODataMap_.items():
            if (v.isAlive() and v.getDay() == self.getToday()): reList.append(k)
        return reList

    def getWolfCODataMap(self):
        return self.__wolfCODataMap_

    def setWolfCODataMap(self,wolfCODataMap):
        self.__wolfCODataMap_ = wolfCODataMap

    #狂人CO関連

    #新しく狂人COした人のデータ作成
    def newPossData(self, agentIdx, day, turn):

        if (agentIdx not in self.__possCODataMap_):
            data = co.COData(day, turn, True)
            self.__possCODataMap_.update({agentIdx:data})

            #既に占いCOしていたらスライドチェック
            if (agentIdx in self.__seerCODataMap_):
                data.setAfterSlide(True)
                self.__seerCODataMap_[agentIdx].setAlive(False)

    def getPossCOAgentList(self):
        reList = []
        for k,v in self.__possCODataMap_.items():     reList.append(k)
        return reList

    def getAlivePossCOAgentList(self):
        reList = []
        for k,v in self.__possCODataMap_.items():
            if (v.isAlive()):   reList.append(k)
        return reList

    def getTodayPossCOAgentList(self):
        reList = []
        for k,v in self.__possCODataMap_.items():
            if (v.isAlive() and v.getDay() == self.getToday()): reList.append(k)
        return reList

    def getPossCODataMap(self):
        return self.__possCODataMap_

    def setPossCODataMap(self,possCODataMap):
        self.__possCODataMap_ = possCODataMap


    #村人CO関連

    #新しく村人COした人のデータ作成
    def newVillData(self, agentIdx, day, turn):

        if (agentIdx not in self.__villCODataMap_):
            data = co.COData(day, turn, True)
            self.__villCODataMap_.update({agentIdx:data})

            #既に占いCOしていたらスライドチェック
            if (agentIdx in self.__seerCODataMap_):
                data.setAfterSlide(True)
                self.__seerCODataMap_[agentIdx].setAlive(False)

    def getVillCOAgentList(self):
        reList = []
        for k,v in self.__villCODataMap_.items():     reList.append(k)
        return reList

    def getAliveVillCOAgentList(self):
        reList = []
        for k,v in self.__villCODataMap_.items():
            if (v.isAlive()):   reList.append(k)
        return reList

    def getTodayVillCOAgentList(self):
        reList = []
        for k,v in self.__villCODataMap_.items():
            if (v.isAlive() and v.getDay() == self.getToday()): reList.append(k)
        return reList

    def getVillCODataMap(self):
        return self.__villCODataMap_

    def setVillCODataMap(self,villCODataMap):
        self.__villCODataMap_ = villCODataMap

    #リクエスト系統

    #リクエストsetter
    def setRequest(self,agent,content,target1,target2):
        data = req.RequestData(self.getToday(),self.getTurn(),agent,content,target1,target2)

        #全てのリクエスト
        self.__requestData_.append(data)

    #今までのリクエストリスト
    def getRequestData(self):
        return self.__requestData_

    #今日のリクエストリスト
    def getRequestToday(self):
        data    = self.__requestData_
        if len(data) > 0:
            return data[-1].getRequestToday(data,self.getToday())
        else:
            return []

    #今日のリクエスト(Content指定)
    def getRequestToday(self,content):
        data    = self.__requestData_
        if len(data) > 0:
            return data[-1].getRequestTodayContent(data,self.getToday(),content)
        else:
            return []

    #自分への今日のリクエスト
    def getRequestTodayMe(self):
        data    = self.__requestData_
        if len(data) > 0:
            return data[-1].getRequestTodayMe(data,self.getToday(),self.agentIdx)
        else:
            return []

    #全体への今日のリクエスト
    def getRequestTodayAll(self):
        data    = self.__requestData_
        if len(data) > 0:
            return data[-1].getRequestTodayAll(data,self.getToday())
        else:
            return []

    #処刑，襲撃，生死関連

    #今まで処刑されたプレイヤのリスト
    def getExecutedAgentList(self):
        return copy.deepcopy(self.__executedAgentList_)

    #前日処刑されたプレイヤ(daystartでget)
    def getExecutedAgent(self):
        return self.gameInfo['executedAgent']

    #当日処刑されたプレイヤ(Actionでget)
    def getLatestExecutedAgent(self):
        return self.gameInfo['latestExecutedAgent']

    #今まで襲撃されたプレイヤのリスト
    def getAttackedAgentList(self):
        return copy.deepcopy(self.__attackedAgentList_)

    #前日襲撃されたプレイヤ(daystartでget)
    def getAttackedAgent(self):
        if(len(self.gameInfo['lastDeadAgentList']) > 0):
            return self.gameInfo['lastDeadAgentList'][0]
        return 0

    #生存エージェントリスト
    def getAliveAgentList(self):
        return copy.deepcopy(self.__aliveAgentList_)

    #生存エージェントインデックスリスト
    def getAliveAgentIndexList(self):
        return copy.deepcopy(self.__aliveAgentIndexList_)

    #処刑されたエージェントをリストに追加
    def addExecutedAgentList(self,votedAgent):
        self.__executedAgentList_.append(votedAgent)

    #襲撃されたエージェントをリストに追加
    def addAttackedAgentList(self,attackedAgent):
        self.__attackedAgentList_.append(attackedAgent)

    #役職持ちの生死更新
    def setAlive(self,executedIdx,attackedIdx):
        for i in self.__seerCODataMap_.keys():
            if (i == executedIdx or i == attackedIdx):
                self.__seerCODataMap_[i].setAlive(False)

    #人狼予測

    #データを入れて予測結果を得る
    def Predictor(self,data):
        self.result = self.pred.predict(data)
        self.result[self.agentIdx] = float("-inf")

        self.rank = np.argsort(self.result)[::-1]


    '''
    人狼予測について
    人狼度 p > 0.5 のとき LSTM-Strategy
    人狼度 p < 0.5 のとき
        1～90ゲームのとき      PiggyBack-Strategy
        91～100ゲームのとき    Meta-Strategy

    実装予定メモ
    ※閾値としてのpは学習によって得たい．今は決め打ち．
    '''

    #最も人狼っぽいやつを返す
    def getMaxLikelyWolfAll(self):

        p = 0.4
        #LSTM-Strategy
        if(len(self.rank) > 0):
            for i in self.rank:
                if( i in self.getAliveAgentIndexList() and self.result[i] > p): return i

        s = 90
        #PiggyBack-Strategy
        if(self.gameTimes < s):
            candidate   = self.getPiggyBackAll()
            return candidate

        #Meta-Strategy
        else:
            for i in self.getStrongWolf():
                if (i != self.agentIdx and self.isAliveIndex(i)):   return i

        #例外処理
        if(len(self.getAliveAgentIndexList()) > 0):
            target = random.choice(self.getAliveAgentIndexList())
            return target
        else:
            return self.agentIdx

    #候補者の中から最も人狼っぽいやつを返す
    def getMaxLikelyWolf(self,targetList):

        p = 0.4
        #LSTM-Strategy
        if(len(self.rank) > 0):
            for i in self.rank:
                if( i in targetList and self.result[i] > p): return i

        s = 90
        #PiggyBack-Strategy
        if(self.gameTimes < s):
            candidate   = self.getPiggyBack(targetList)
            return candidate

        #Meta-Strategy
        else:
            for i in self.getStrongWolf():
                if (i in targetList and self.isAliveIndex(i)):   return i

        #例外処理
        if(len(self.getAliveAgentIndexList()) > 0):
            target = random.choice(self.getAliveAgentIndexList())
            return target
        else:
            return self.agentIdx

    def getMaxLikelyVillAll(self):
        if(len(self.rank) > 0):
            for i in reversed(self.rank):
                if(self.result[i] != float("-inf")):
                    return i

        candidate = self.getAliveAgentIndexList()
        candidate.remove(self.agentIdx)
        if(len(candidate) > 0):
            target = random.choice(candidate)
            return target

        return self.agentIdx

    def getMaxLikelyVill(self,targetList):
        if(len(self.rank) > 0):
            for i in reversed(self.rank):
                if(self.result[i] != float("-inf") and i in targetList):
                    return i

        if(len(targetList) > 0):
            target = random.choice(targetList)
            return target

        return self.getMaxLikelyVillAll()

    #他人の投票宣言に便乗する(全体)
    def getPiggyBackAll(self):
        maxVote = -1
        maxIdxList = []
        if(self.__voteNum_ != None ):
            for idx,votenum in self.__voteNum_.items():
                if(idx != self.agentIdx and self.isAlive(idx)):
                    if (maxVote < votenum):
                        maxVote = votenum
                        del maxIdxList[:]
                        maxIdxList.append(idx)
                    elif (maxVote == votenum):
                        maxIdxList.append(idx)

        if (len(maxIdxList) == 0):
            alive = self.getAliveAgentIndexList()
            perm = np.random.permutation(len(alive))
            return alive[perm[0]]
        elif (len(maxIdxList) == 1):
            return maxIdxList[0]
        else:
            perm = np.random.permutation(len(maxIdxList))
            return maxIdxList[perm[0]]

    #他人の投票宣言に便乗する(候補者から)
    def getPiggyBack(self,targetIdx):
        return self.getMaxLikelyExecuteAgent(targetIdx)

    #最もつられそうなエージェント
    def getMaxLikelyExecuteAgentAll(self):
        maxVote = -1
        maxIdxList = []
        if(self.__voteNum_ != None ):
            for idx,votenum in self.__voteNum_.items():
                if (maxVote < votenum):
                    maxVote = votenum
                    del maxIdxList[:]
                    maxIdxList.append(idx)
                elif (maxVote == votenum):
                    maxIdxList.append(idx)

        if (len(maxIdxList) == 0):
            perm = np.random.permutation(targetIdxList)
            return perm[0]
        elif (len(maxIdxList) == 1):
            return maxIdxList[0]
        else:
            perm = np.random.permutation(len(maxIdxList))
            return maxIdxList[perm[0]]

    def getMaxLikelyExecuteAgent(self,targetIdxList):
        maxVote = -1
        maxIdxList = []
        if(self.__voteNum_ != None ):
            for idx in targetIdxList:
                votenum = self.__voteNum_[idx]
                if (maxVote < votenum):
                    maxVote = votenum
                    del maxIdxList[:]
                    maxIdxList.append(idx)
                elif(maxVote == votenum):
                    maxIdxList.append(idx)

        if (len(maxIdxList) == 0):
            if(len(targetIdxList) > 0):
                perm = np.random.permutation(targetIdxList)
                return perm[0]
            else:
                return self.getMaxLikelyExecuteAgentAll()
        elif (len(maxIdxList) == 1):
            return maxIdxList[0]
        else:
            perm = np.random.permutation(len(maxIdxList))
            return maxIdxList[perm[0]]

    def getMaxLikelyExecuteAgentNum(self,targetIdxList):
        maxVote = -1
        maxIdxList = []
        if(self.__voteNum_ !=None ):
            for idx in range(self.__voteNum_.keySet()):
                votenum = self.__voteNum_.get(idx)
                if (maxVote < votenum):
                    maxVote = votenun
        return maxVote

    def getLikelyExecuteMap(self):
        votedmap = []
        for agentIdx in enumerate(self.getAliveAgentList()):
            votedmap.update(agentIdx, self.__voteMap_.get(agentIdx))
        return votedmap

    def getLatestVotedNumber(self):
        voteMap = {}

        for i in range(len(self.__voteTable_.get(self.__today_))):
            targetIdx = self.__voteTable_.get(self.__today_)[i]
            if (targetIdx in voteMap):
                voteMap.update(targetIdx,voteMap.get(targetIdx) + 1)
            else:
                voteMap.update(targetIdx, 1)
        return voteMap

    def isCOCall(self):
        answer = self.__isCOCall_
        self.__isCOCall_ = False
        return answer

    def isDivineCall(self):
        answer = self.__isDivineCall_
        self.__isDivineCall_ = False
        return answer

    def isOneTimeCall(self):
        answer = self.__isOneTimeCall_
        self.__isOneTimeCall_ = False
        return answer

    def isAlive(self,agent):
        if (agent in self.__aliveAgentList_):
            return True
        else:
            return False

    def isAliveIndex(self,agentIdx):
        if (agentIdx in self.getAliveAgentIndexList()):
            return True
        else:
            return False
    def getAgentIdxList(self):
        return self.__agentIdxlist_

    def getToday(self):
        return self.__today_

    def getPlayerDataMap(self):
        return self.__playerDataMap_

    def getResultMap(self):
        return self.__resultMap_

    def getTalkAllNumMap(self):
        return self.__talkAllNumMap_

    def getVillCODayMap(self):
        return self.__villCODayMap_

    def getWolfCODayMap(self):
        return self.__wolfCODayMap_

    def getPossCODayMap(self):
        return self.__possCODayMap_

    def getVoteMap(self):
        return self.__voteMap_

    def setTurn(self,turn):
        self.turn = turn

    def getTurn(self):
        return self.turn

    def setWinrate(self,wolfWinrate,villWinrate):
        self.wolfWinrate    = wolfWinrate
        self.villWinrate    = villWinrate

    def getWinrate(self):
        return self.wolfWinrate,self.villWinrate

    def CalcWinrank(self,gameTimes):
        self.gameTimes  = gameTimes
        self.calc_flag  = True

        wolf,vill   =   {},{}
        self.strongWolf,self.strongVill   = [],[]
        self.strongWolfrate,self.strongVillrate   = [],[]
        self.wolf_ave,self.vill_ave = 0,0

        #各エージェントの勝率を計算
        for k,v in self.wolfWinrate.items():
            if(v[0] != 0):  rate = float(v[1]) / float(v[0])
            else:           rate = 0
            wolf.update({k:rate})
        for k,v in self.villWinrate.items():
            if(v[0] != 0):  rate = float(v[1]) / float(v[0])
            else:           rate = 0
            vill.update({k:rate})

        #各エージェントを勝率順にソート
        for k, v in sorted(wolf.items(), key=lambda x: -x[1]):
            self.strongWolf.append(k)
            self.strongWolfrate.append(v)
            self.wolf_ave   += v
        for k, v in sorted(vill.items(), key=lambda x: -x[1]):
            self.strongVill.append(k)
            self.strongVillrate.append(v)
            self.vill_ave   += v

        #全エージェントの勝率平均
        self.wolf_ave = float(self.wolf_ave)/float(self.__playerNum_ )
        self.vill_ave = float(self.vill_ave)/float(self.__playerNum_ )

        self.strongVillMap,self.strongAgentMap  = {},{}
        self.strongAsVill,self.strongAgent    = [],[]

        for k,v in vill.items():
            #高いほど村人として強い
            if(self.vill_ave != 0):
                vill_rate   = float(v) / float(self.vill_ave)
            else:
                vill_rate   = 1
            #高いほど人狼として弱い
            if(wolf[k] != 0):
                wolf_rate   = float(self.wolf_ave) / float(wolf[k])
            else:
                wolf_rate   = 1
            rate    = vill_rate * wolf_rate

            #人狼としては弱いが，村人として強いエージェント
            self.strongVillMap.update({k:rate})

            #高いほど人狼として強い
            if(self.wolf_ave != 0):
                wolf_rate   = float(wolf[k]) / float(self.wolf_ave)
            else:
                wolf_rate   = 1
            rate    = vill_rate * wolf_rate
            #人狼としても，村人としても強いエージェント
            self.strongAgentMap.update({k:rate})

        for k, v in sorted(self.strongAgentMap.items(), key=lambda x: -x[1]):
            self.strongAgent.append(k)
        for k, v in sorted(self.strongVillMap.items(), key=lambda x: -x[1]):
            self.strongAsVill.append(k)

    def getStrongAsVill(self):
        return self.strongAsVill

    def getStrongVill(self):
        return self.strongVill

    def getStrongWolf(self):
        return self.strongWolf

    def getStrongAgent(self):
        return self.strongAgent

# -*- coding: utf-8 -*-
import aiwolfpy
import aiwolfpy.templatetalkfactory as ttf
import aiwolfpy.templatewhisperfactory as twf
import numpy as np
import copy,random,re

import string
import PlayerData
import COData as co
from RequestData import RequestData as req
import WhisperData as whisp
import savelog as sv
import LSTM

class MyData():

    def __init__(self):
        self.__seerCODataMap_   = {}
        self.__mediumCODataMap_ = {}
        self.__bgCODataMap_     = {}
        self.__villCODataMap_    = {}
        self.__wolfCODataMap_    = {}
        self.__possCODataMap_    = {}

        self.__MediumIdentifiedList_ = {}

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
        self.__isCall_          = False
        self.__isOneTimeCall_   = False

        #RequestData
        self.__requestData_  = []

        self.gameInfo           = None

        self.turn               = 1
        self.logTurn            = 0
        #self.pred       = LSTM.Predictor_15()
        self.SaveLog        = sv.SaveLog_15()
        self.rank       = []
        self.top3       = []
        self.result     = []

        self.werewolves = []

        self.wolfWinrate    = []
        self.villWinrate    = []
        self.calc_flag      = False

    def update(self,gameInfo,talk_history, whisper_history):
        self.gameInfo   =   gameInfo

        if (talk_history != None and len(talk_history) > 0):

            for i in range(len(talk_history)):
                talk    = talk_history[i]
                agentIdx    = talk['agent']
                turn    = talk['turn']
                self.setTurn(turn + 1)
                isBlack = False

                #CO
                if (talk['text'].split()[0]== 'COMINGOUT'):

                    #占い師CO
                    if (talk['text'].split()[2] == 'SEER'):
                        self.newSeerData(agentIdx, self.getToday(), turn)
                        p_data = self.__playerDataMap_.get(agentIdx)
                        p_data.addCOData('SEER', self.getToday() , turn)
                        resultlist = []
                        self.__resultMap_.update({agentIdx:resultlist})

                    #霊媒師CO
                    elif(talk['text'].split()[2] == 'MEDIUM'):
                        self.newMediumData(agentIdx, self.getToday(), turn)
                        p_data = self.__playerDataMap_.get(agentIdx)
                        p_data.addCOData('MEDIUM', self.getToday(), turn)

                    #狩人CO
                    elif (talk['text'].split()[2] == 'BODYGUARD'):
                        self.newBGData(agentIdx, self.getToday(), turn)
                        p_data = self.__playerDataMap_.get(agentIdx)
                        p_data.addCOData('BODYGUARD', self.getToday(), turn)

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
                    self.addSeerData(agentIdx, targetIdx, isBlack, self.getToday(), turn)
                    self.__isCall_ = True
                    self.__isOneTimeCall_ = True

                #IDENTIFIED
                elif (talk['text'].split()[0]== 'IDENTIFIED'):
                    if (talk['text'].split()[2] == 'WEREWOLF'): isBlack = True
                    target = talk['text'].split()[1]
                    targetIdx = self.AgentToIndex(target)
                    self.addMediumData(agentIdx, targetIdx, isBlack, self.getToday(), turn)
                    self.__isCall_ = True
                    self.__isOneTimeCall_ = True

                #GUARDED
                elif (talk['text'].split()[0]== 'GUARDED'):
                    target = talk['text'].split()[1]
                    targetIdx = self.AgentToIndex(target)
                    self.addBGData(agentIdx, targetIdx, self.getToday(), turn)
                    self.__isCall_ = True
                    self.__isOneTimeCall_ = True

                #p_data = self.__playerDataMap_.get(agentIdx)

                if (talk['text'].split()[0] == "VOTE"):
                    newTargetIdx = self.AgentToIndex(talk['text'].split()[1])
                    oldTargetIdx = self.__voteMap_.get(agentIdx)
                    if (oldTargetIdx != newTargetIdx):
                        if (oldTargetIdx != -1):
                            value = self.__voteNum_.get(oldTargetIdx)
                            value = value - 1
                            self.__voteNum_.update({oldTargetIdx:value})
                        value = self.__voteNum_.get(newTargetIdx)
                        if(value != None):
                            value = value + 1
                            self.__voteNum_.update({newTargetIdx:value})
                            self.__voteMap_.update({agentIdx:newTargetIdx})

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
                    #self.Predictor(turndata)

        if (whisper_history != None and self.role == "WEREWOLF"):
            self.Whisper.update(gameInfo, whisper_history)

    def dayStart(self,gameInfo):
        self.gameInfo   = gameInfo
        self.__today_   = gameInfo["day"]
        self.__isCall_  = False
        self.__isOneTimeCall_   = False
        self.logTurn    = -1

        #LOGの成形
        if(self.getToday() > 1):
            self.SaveLog.saveVote(gameInfo)
            dayvote    = self.SaveLog.getDayvote()
            #self.Predictor(dayvote)

        if "statusMap" in gameInfo.keys():
            statusMap = gameInfo["statusMap"]

        if self.role == "WEREWOLF":
            self.Whisper.isFinishWhisper()

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

        #self.pred.set_death_list(death_list)

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
            if (self.getToday() > 1 and agentIdx in seer):
                p_data.addDayData_black(votepattern, self.__resultMap_.get(agentIdx))
            elif (self.getToday() > 0):
                p_data.addDayData(votepattern)

        for idx in self.__agentIndexList_:
            self.__talkNumMap_[idx]     =   0
            self.__talkAllNumMap_[idx]  =   0
            self.__voteMap_[idx]        =   -1
            self.__voteNum_[idx]        =   0

    def gameStart(self,gameInfo, playerNum, agentNames):
        self.gameInfo       =   gameInfo
        self.__playerNum_   = playerNum
        self.__agentList_   = agentNames
        self.__aliveAgentList_  = agentNames
        self.__agentIndexList_  = self.AgentToIndexList(agentNames)
        self.__aliveAgentIndexList_ = self.AgentToIndexList(agentNames)
        self.agentIdx   = gameInfo['agent']
        self.role       = gameInfo['roleMap'][str(self.agentIdx)]  #自分の役割

        if self.role == 'WEREWOLF':
            for k,v in gameInfo['roleMap'].items():
                if self.agentIdx != int(k):
                    self.werewolves.append(int(k))

            self.Whisper    = whisp.WhisperData(self.werewolves)

        for i in range(self.__playerNum_):
            pd = PlayerData.PlayerData(self.__agentIndexList_[i],self.__agentList_[i])
            self.__playerDataMap_.update({self.__agentIndexList_[i]:pd})
            self.__talkNumMap_.update({self.__agentIndexList_[i]:0})
            self.__talkAllNumMap_.update({self.__agentIndexList_[i]:0})
            self.__voteMap_.update({self.__agentIndexList_[i]:-1})
            self.__voteNum_.update({self.__agentIndexList_[i]:0})

        #self.pred.initialize(self.__playerNum_)

    def finish(self):
        pass
        #self.pred.finish()

    #人狼のデータ関連
    def getWhisperData(self):
        return self.Whisper

    #インデックス，エージェント処理

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
            #既に霊媒COしていたらスライドチェック
            if (agentIdx in self.__mediumCODataMap_):
                data.setAfterSlide(True)
                self.__mediumCODataMap_[agentIdx].setAlive(False)
            #既に狩人COしていたらスライドチェック
            if (agentIdx in self.__bgCODataMap_):
                data.setAfterSlide(True)
                self.__bgCODataMap_[agentIdx].setAlive(False)

    #占い結果を出した占い師のデータ更新
    def addSeerData(self,agentIdx, targetIdx, isBlack, day, turn):
        #COせずに結果を言った人
        if (agentIdx not in self.__seerCODataMap_):
            data = co.SeerCOData(day, turn, False)
            self.__seerCODataMap_.update({agentIdx:data})
            if (agentIdx in self.__mediumCODataMap_):
                data.setAfterSlide(True)
                self.__mediumCODataMap_[agentIdx].setAlive(False)
            if (agentIdx in self.__bgCODataMap_):
                data.setAfterSlide(True)
                self.__bgCODataMap_[agentIdx].setAlive(False)
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
        data  = self.getSeerCODataMap()
        if (index in data):
            return data.get(index).getKnownList()
        else:
            return None

    def getSeerCODataMap(self):
        return self.__seerCODataMap_

    def setSeerCODataMap(self,seerCODataMap):
        self.__seerCODataMap_ = seerCODataMap

    #霊媒CO関連

    #新しく霊媒COした人のデータ作成
    def newMediumData(self,agentIdx, day, turn):
        if (agentIdx not in self.__mediumCODataMap_):
            data = co.MediumCOData(day, turn, True)
            self.__mediumCODataMap_.update({agentIdx:data})
            #既に占いCOしていたらスライドチェック
            if (agentIdx in self.__seerCODataMap_):
                data.setAfterSlide(True)
                self.__seerCODataMap_[agentIdx].setAlive(False)
            if (agentIdx in self.__bgCODataMap_):
                data.setAfterSlide(True)
                self.__bgCODataMap_[agentIdx].setAlive(False)

    #霊媒結果を出した霊媒師のデータ更新
    def addMediumData(self, agentIdx, targetIdx, isBlack, day, turn):
        #COせずに結果を言った人
        if (agentIdx not in self.__mediumCODataMap_):
            data = co.MediumCOData(day, turn, False)
            self.__mediumCODataMap_.update({agentIdx:data})
            if (agentIdx in self.__seerCODataMap_):
                data.setAfterSlide(True)
                self.__seerCODataMap_[agentIdx].setAlive(False)
            if (agentIdx in self.__bgCODataMap_):
                data.setAfterSlide(True)
                self.__bgCODataMap_[agentIdx].setAlive(False)
        #COして結果を言った人
        else:
            data = self.__mediumCODataMap_[agentIdx]
        data.add(day, targetIdx, isBlack)

    def getMediumCOAgentList(self):
        reList = []
        for k,v in self.__mediumCODataMap_.items(): reList.append(k)
        return reList

    def getAliveMediumCOAgentList(self):
        reList = []
        for k,v in self.__mediumCODataMap_.items():
            if (v.isAlive()):   reList.append(k)
        return reList

    def getTodayMediumCOAgentList(self):
        reList = []
        for k,v in self.__mediumCODataMap_.items():
            if (v.isAlive() and v.getDay() == self.getToday()): reList.append(k)
        return reList

    def getMediumResultsList(self,index):
        data  = self.getMediumCODataMap()
        if index in data:
            return data.get(index).getKnownList()
        else:
            return None

    def getMediumCODataMap(self):
        return self.__mediumCODataMap_

    def setMediumCODataMap(self,mediumCODataMap):
        self.__mediumCODataMap_ = mediumCODataMap

    # def setMediumIdentify(self):
        # self.__MediumIdentifiedList_.append({"target": self.getExecutedAgent(),"result":})
        # self.__MediumIdentifiedList_.append({"target": self.getAttackedAgent(),"result":})

    #狩人CO関連

    #新しく狩人COした人のデータ作成
    def newBGData(self,agentIdx, day, turn):
        if (agentIdx not in self.__bgCODataMap_):
            data = co.BGCOData(day, turn, True)
            self.__bgCODataMap_.update({agentIdx:data})
            #既に霊媒COしていたらスライドチェック
            if (agentIdx in self.__mediumCODataMap_):
                data.setAfterSlide(True)
                self.__mediumCODataMap_[agentIdx].setAlive(False)
            #既に占いCOしていたらスライドチェック
            if (agentIdx in self.__seerCODataMap_):
                data.setAfterSlide(True)
                self.__seerCODataMap_[agentIdx].setAlive(False)

    #護衛結果を出した狩人のデータ更新
    def addBGData(self, agentIdx, targetIdx, day, turn):
        if (agentIdx not in self.__bgCODataMap_):
            data = co.BGCOData(day, turn, False)
            self.__bgCODataMap_.update({agentIdx:data})
            if (agentIdx in self.__mediumCODataMap_):
                data.setAfterSlide(True)
                self.__mediumCODataMap_[agentIdx].setAlive(False)
            if (agentIdx in self.__seerCODataMap_):
                data.setAfterSlide(True)
                self.__seerCODataMap_[agentIdx].setAlive(False)
        else:
            data = self.__bgCODataMap_[agentIdx]
        data.add(targetIdx)

    def getBGCOAgentList(self):
        reList = []
        for k,v in self.__bgCODataMap_.items():     reList.append(k)
        return reList

    def getAliveBGCOAgentList(self):
        reList = []
        for k,v in self.__bgCODataMap_.items():
            if (v.isAlive()):   reList.append(k)
        return reList

    def getTodayBGCOAgentList(self):
        reList = []
        for k,v in self.__bgCODataMap_.items():
            if (v.isAlive() and v.getDay() == self.getToday()): reList.append(k)
        return reList

    def getBgCODataMap(self):
        return self.__bgCODataMap_

    def setBgCODataMap(self,bgCODataMap):
        self.__bgCODataMap_ = bgCODataMap

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
            #既に霊媒COしていたらスライドチェック
            if (agentIdx in self.__mediumCODataMap_):
                data.setAfterSlide(True)
                self.__mediumCODataMap_[agentIdx].setAlive(False)
            #既に狩人COしていたらスライドチェック
            if (agentIdx in self.__bgCODataMap_):
                data.setAfterSlide(True)
                self.__bgCODataMap_[agentIdx].setAlive(False)

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
            #既に霊媒COしていたらスライドチェック
            if (agentIdx in self.__mediumCODataMap_):
                data.setAfterSlide(True)
                self.__mediumCODataMap_[agentIdx].setAlive(False)
            #既に狩人COしていたらスライドチェック
            if (agentIdx in self.__bgCODataMap_):
                data.setAfterSlide(True)
                self.__bgCODataMap_[agentIdx].setAlive(False)

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
            #既に霊媒COしていたらスライドチェック
            if (agentIdx in self.__mediumCODataMap_):
                data.setAfterSlide(True)
                self.__mediumCODataMap_[agentIdx].setAlive(False)
            #既に狩人COしていたらスライドチェック
            if (agentIdx in self.__bgCODataMap_):
                data.setAfterSlide(True)
                self.__bgCODataMap_[agentIdx].setAlive(False)

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
        data = req(self.getToday(),self.getTurn(),agent,content,target1,target2)

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

        for i in self.__mediumCODataMap_.keys():
            if (i == executedIdx or i == attackedIdx):
                self.__mediumCODataMap_[i].setAlive(False)

        for i in self.__bgCODataMap_.keys():
            if (i == executedIdx or i == attackedIdx):
                self.__bgCODataMap_[i].setAlive(False)


    #人狼予測

    #データを入れて予測結果を得る
    def Predictor(self,data):
        self.result = self.pred.predict(data)[0]
        self.result[self.agentIdx-1] = float(-1)
        self.rank   = np.argsort(self.result)[::-1]
        for i in range(self.rank):
            self.rank[i] = self.rank[i] + 1

        self.top3  = np.argsort(self.result)[::-1][:3]
        for i in range(self.top3):
            self.top3[i] = self.top3[i] + 1
        print (self.top3)

        '''
        if(self.role == 'WEREWOLF'):
            for i in range (len(self.result)):
                if( (i+1) in self.werewolves):
                    self.result[i]  = 0.6 * self.result[i]
            self.rank = np.argsort(self.result)[::-1]
        '''

    #最も人狼っぽいやつを返す
    def getMaxLikelyWolfAll(self):

        '''
        #LSTM and Piggy-back Strategy
        if len(self.rank) > 0:
            #予測上位3名の中から最も吊られそうなエージェント
            target = self.getMaxLikelyExecuteAgent(self.top3)
            if target = None:
                return target
        '''

        s = 90
        #PiggyBack-Strategy
        if self.gameTimes < s or self.getToday() < 5:
            target   = self.getPiggyBackAll()
            return target

        #Meta-Strategy
        else:
            for i in self.getStrongWolf():
                if i != self.agentIdx and self.isAliveIndex(i):
                    return i

        #例外処理
        if len(self.getAliveAgentIndexList()) > 0:
            target = random.choice(self.getAliveAgentIndexList())
            return target
        else:
            return self.agentIdx

    #候補者の中から最も人狼っぽいやつを返す
    def getMaxLikelyWolf(self,targetList):

        if(len(targetList) == 0):
            print("target error")
            return self.getMaxLikelyWolfAll()

        '''
        #LSTM and Piggy-back Strategy
        if len(self.rank) > 0:

            #予測上位3名かつ候補者の中から最も吊られそうなエージェント
            cd = []
            i for self.top3:
                if i in targetList:
                    cd.append(i)
            if len(cd) > 0:
                target = self.getMaxLikelyExecuteAgent(cd)
                if target in cd:  return target
        '''

        s = 90
        #PiggyBack-Strategy
        if self.gameTimes < s  or self.getToday() < 5:
            candidate   = self.getPiggyBack(targetList)
            return candidate

        #Meta-Strategy
        else:
            for i in self.getStrongWolf():
                if self.isAliveIndex(i) and i in targetList:
                    return i

        #例外処理
        target = random.choice(targetList)
        if target != None:  return target
        else:   return self.agentIdx

    #LSTMの人狼度を返す
    def getLikelyWolfRate(self):
        return self.result

    #LSTMの人狼度を{エージェント：人狼度}で返す
    def getLikelyWolfRateMap(self):

        wolfmap = {}
        for i in range(len(self.result)):
            wolfmap.update({(i+1):self.result[i]})

        return wolfmap

    #最も村人っぽいやつを返す
    def getMaxLikelyVillAll(self):

        s = 90
        #90ゲーム以降かつ2日目以降は村人として強いが人狼として弱いエージェント
        if self.gameTimes > s  and self.getToday()> 1:
            for i in self.getStrongAsVill():
                if i != self.agentIdx and self.isAliveIndex(i):
                    target = i
                    return target

        #90ゲーム目までは便乗
        else:
            target = self.getMinLikelyExecuteAgentAll()
            if(target != None): return target

        if(len(self.getAliveAgentIndexList()) > 0):
            target = random.choice(self.getAliveAgentIndexList())
            return target
        else:
            return self.agentIdx

        '''
        #初日は便乗
        if(self.getToday() == 1):
            perm = np.random.permutation(self.getAliveAgentIndexList())
            return perm[0]
        #2日目以降LSTM
        else:
            if(len(self.rank) > 0):
                for i in self.rank:
                    if( (i+1) in self.getAliveAgentIndexList()):
                        return (i+1)
            perm = np.random.permutation(self.getAliveAgentIndexList())
            return perm[0]
        '''

    #候補者の中から最も村人っぽいやつを返す
    def getMaxLikelyVill(self,targetList):

        if len(targetList) == 0:
            return self.getMaxLikelyVillAll()

        s = 90
        #50ゲーム以降かつ2日目以降は村人として強いが人狼として弱いエージェント
        if(self.gameTimes > s  and self.getToday()> 1):
            for i in self.getStrongAsVill():
                if( i in targetList and self.isAliveIndex(i) ):
                    target = i
                    return target

        #50ゲーム目までは便乗
        else:
            target = self.getMinLikelyExecuteAgent(targetList)
            if(target != None): return target

        perm = random.choice(targetList)
        return perm

        '''
        #初日はランダム
        if(self.getToday() == 1):
            perm = np.random.permutation(targetList)
            return perm[0]
        #2日目以降LSTM
        else:
            if(len(self.rank) > 0):
                reverse = self.rank[::-1]
                for i in reverse:
                    if( (i+1) in self.getAliveAgentIndexList() and (i+1) in targetList):
                        return (i+1)
            perm = np.random.permutation(targetList)
            return perm[0]
        '''

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


    #最も処刑されそうなやつを返す
    def getMaxLikelyExecuteAgentAll(self):
        maxVote = -1
        maxIdxList = []
        if(self.__voteNum_ != None ):
            for idx,votenum in self.__voteNum_.items():
                if(self.isAliveIndex(idx)):
                    if (maxVote < votenum):
                        maxVote = votenum
                        del maxIdxList[:]
                        maxIdxList.append(idx)
                    elif (maxVote == votenum):
                        maxIdxList.append(idx)

        if (len(maxIdxList) == 0):
            if(len(self.getAliveAgentList()) > 0):
                perm = random.choice(self.getAliveAgentList())

                return self.AgentToIndex(perm)
            else:
                print(self.gameInfo['statusMap'])
                return self.agentIdx
        elif (len(maxIdxList) == 1):
            return maxIdxList[0]
        else:
            perm = random.choice(maxIdxList)
            return perm

    #候補者の中から最も処刑されそうなやつを返す
    def getMaxLikelyExecuteAgent(self,targetIdxList):
        maxVote = -1
        maxIdxList = []
        if(len(targetIdxList) == 0):    return None
        if(self.__voteNum_ != None ):
            for idx in targetIdxList:
                if(self.isAliveIndex(idx)):
                    votenum = self.__voteNum_[idx]
                    if (maxVote < votenum):
                        maxVote = votenum
                        del maxIdxList[:]
                        maxIdxList.append(idx)
                    elif(maxVote == votenum):
                        maxIdxList.append(idx)
        if (len(maxIdxList) == 0):
            perm = random.choice(targetIdxList)
            return perm
        elif (len(maxIdxList) == 1):
            return maxIdxList[0]
        else:
            perm = random.choice(maxIdxList)
            return perm

    #最も処刑されそうなやつの被投票宣言数を返す
    def getMaxLikelyExecuteAgentNum(self,targetIdxList):
        maxVote = -1
        maxIdxList = []
        if(self.__voteNum_ !=None ):
            for idx in range(self.__voteNum_.keySet()):
                votenum = self.__voteNum_.get(idx)
                if (maxVote < votenum):
                    maxVote = votenum
        return maxVote

    #最も処刑されなさそうなやつを返す
    def getMinLikelyExecuteAgentAll(self):
        minVote = 15
        minIdxList = []
        if(self.__voteNum_ != None ):
            for idx,votenum in self.__voteNum_.items():
                if (minVote > votenum):
                    minVote = votenum
                    del minIdxList[:]
                    minIdxList.append(idx)
                elif(minVote == votenum):
                    minIdxList.append(idx)

        if (len(minIdxList) == 0):
            if(len(self.getAliveAgentList()) > 0):
                perm = random.choice(self.getAliveAgentList())

                return self.AgentToIndex(perm)
            else:
                print(self.gameInfo['statusMap'])
                return self.agentIdx
        elif (len(minIdxList) == 1):
            return minIdxList[0]
        else:
            perm = random.choice(minIdxList)
            return perm

    #候補者の中から最も処刑されなさそうなやつを返す
    def getMinLikelyExecuteAgent(self,targetIdxList):
        minVote = 15
        minIdxList = []
        if(len(targetIdxList) == 0):
            return self.getMinLikelyExecuteAgentAll()
        if(self.__voteNum_ != None ):
            for idx in targetIdxList:
                votenum = self.__voteNum_[idx]
                if (minVote > votenum):
                    minVote = votenum
                    del minIdxList[:]
                    minIdxList.append(idx)
                elif(minVote == votenum):
                    minIdxList.append(idx)

        if (len(minIdxList) == 0):
            perm = random.choice(targetIdxList)
            return perm
        elif (len(minIdxList) == 1):
            return minIdxList[0]
        else:
            perm = random.choice(minIdxList)
            return perm

    #処刑(被投票宣言数)マップ
    def getLikelyExecuteMap(self):
        votedmap = []
        for agentIdx in enumerate(self.getAliveAgentList()):
            votedmap.update(agentIdx, self.__voteMap_.get(agentIdx))
        return votedmap


    def getAttackTargetAll(self):

        #村人として強い&人狼でないプレイヤ
        if(self.calc_flag):
            for i in self.getStrongVill():
                if( not i in self.werewolves and self.isAliveIndex(i) ):
                    return i

        return self.getMaxLikelyVillAll()


    def getAttackTarget(self,targetList):

        if(len(targetList) == 0):
            return self.getMaxLikelyVill(targetList)

        #村人として強い&人狼でないプレイヤ
        if(self.calc_flag):
            for i in self.getStrongVill():
                if( not i in self.werewolves and self.isAliveIndex(i) ):
                    return i

        return self.getMaxLikelyVillAll()


    def getLatestVotedNumber(self):
        voteMap = {}

        for i in range(len(self.__voteTable_.get(self.getToday()))):
            targetIdx = self.__voteTable_.get(self.getToday())[i]
            if (targetIdx in voteMap):
                voteMap.update(targetIdx,voteMap.get(targetIdx) + 1)
            else:
                voteMap.update(targetIdx, 1)
        return voteMap



    def isCall(self):
        return self.__isCall_

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
        if (agentIdx in self.__aliveAgentIndexList_):
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



    def getVoteMap(self):   return self.__voteMap_

    def setTurn(self,turn): self.turn = turn

    def getTurn(self):  return self.turn

    def setWinrate(self,wolfWinrate,villWinrate):
        self.wolfWinrate    = wolfWinrate
        self.villWinrate    = villWinrate

    def getWinrate(self):
        return self.wolfWinrate,self.villWinrate

    def CalcWinrank(self,gameTimes):
        self.gameTimes = gameTimes
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
            if(self.vill_ave != 0):   vill_rate   = float(v) / float(self.vill_ave)
            else:   vill_rate   = 1
            #高いほど人狼として弱い
            if(wolf[k] != 0):         wolf_rate   = float(self.wolf_ave) / float(wolf[k])
            else:   wolf_rate   = 1
            rate    = vill_rate * wolf_rate

            #人狼としては弱いが，村人として強いエージェント
            self.strongVillMap.update({k:rate})

            #高いほど人狼として強い
            if(self.wolf_ave != 0):         wolf_rate   = float(wolf[k]) / float(self.wolf_ave)
            else:   wolf_rate   = 1
            rate    = vill_rate * wolf_rate
            #人狼としても，村人としても強いエージェント
            self.strongAgentMap.update({k:rate})

        for k, v in sorted(self.strongAgentMap.items(), key=lambda x: -x[1]):
            self.strongAgent.append(k)
        for k, v in sorted(self.strongVillMap.items(), key=lambda x: -x[1]):
            self.strongAsVill.append(k)

    #信用したい(便乗したい)村人->人狼としては弱いので信じてもok
    def getStrongAsVill(self):
        return self.strongAsVill

    #一番強い村人
    def getStrongVill(self):
        return self.strongVill

    #一番強い人狼
    def getStrongWolf(self):
        return self.strongWolf

    #一番強いエージェント
    def getStrongAgent(self):
        return self.strongAgent

    def getVoteTable(self,day):
        return self.__voteTable_.get(day)

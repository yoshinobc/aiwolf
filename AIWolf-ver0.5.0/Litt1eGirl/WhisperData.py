# -*- coding: utf-8 -*-
import aiwolfpy
import aiwolfpy.templatetalkfactory as ttf
import aiwolfpy.templatewhisperfactory as twf
import numpy as np
import copy

import RequestData as req

class WhisperData(object):

    def __init__(self,werewolf):

        #人狼リスト
        self.__werewolfList_    = werewolf

        #WhisperでCO宣言
        self.__seerWillCOList_      = []
        self.__mediumWillCOList_    = []
        self.__villWillCOList_      = []
        self.__isCOMap_             = {}

        #Talkで実際にCO宣言
        self.__seerCOList_      = {}
        self.__mediumCOList_    = {}
        self.__villCOList_      = {}

        #REQUESTマップ
        self.__RequestMap_      = {}
        self.__voteRequestMap_  = {}
        self.__attackRequestMap_    = {}

        #発言マップ
        self.__voteMap_     = {}
        self.__attackMap_   = {}

        self.whisper_flag = True

    def update(self,gameInfo,whisper_history):



        if (whisper_history != None and len(whisper_history) > 0):

            if self.whisper_flag:
                self.isStartWhisper()

            for i in range(len(whisper_history)):

                whisper     = whisper_history[i]
                agentIdx    = whisper['agent']
                self.turn    = whisper['turn']

                #CO
                if (whisper['text'].split()[0]== 'COMINGOUT'):

                    #占い師CO
                    if (whisper['text'].split()[2] == 'SEER'):

                        #他役職CO済の場合，他役職のWillCOリストから除外
                        if agentIdx in self.__mediumWillCOList_:
                            self.__mediumWillCOList_.remove(agentIdx)
                        if agentIdx in self.__villWillCOList_:
                            self.__villWillCOList_.remove(agentIdx)

                        self.__isCOMap_[agentIdx]   = 'SEER'
                        self.__seerWillCOList_.append(agentIdx)

                    #霊媒師CO
                    elif(whisper['text'].split()[2] == 'MEDIUM'):

                        #他役職CO済の場合，他役職のWillCOリストから除外
                        if agentIdx in self.__seerWillCOList_:
                            self.__seerWillCOList_.remove(agentIdx)
                        if agentIdx in self.__villWillCOList_:
                            self.__villWillCOList_.remove(agentIdx)

                        self.__isCOMap_[agentIdx]   = 'MEDIUM'
                        self.__mediumWillCOList_.append(agentIdx)

                    #村人CO
                    elif(whisper['text'].split()[2] == 'VILLAGER'):

                        #他役職CO済の場合，他役職のWillCOリストから除外
                        if agentIdx in self.__seerWillCOList_:
                            self.__seerWillCOList_.remove(agentIdx)
                        if agentIdx in self.__mediumWillCOList_:
                            self.__mediumWillCOList_.remove(agentIdx)

                        self.__isCOMap_[agentIdx]   = 'VILLAGER'
                        self.__villWillCOList_.append(agentIdx)

                #VOTE
                if (whisper['text'].split()[0] == "VOTE"):
                    newTargetIdx = self.AgentToIndex(whisper['text'].split()[1])
                    self.__voteMap_.update({agentIdx:newTargetIdx})

                #ATTACK
                if (whisper['text'].split()[0] == "ATTACK"):
                    newTargetIdx = self.AgentToIndex(whisper['text'].split()[1])
                    self.__attackMap_.update({agentIdx:newTargetIdx})

                #REQUEST
                #現在考慮しているのは全体に対するREQUESTのみで，対象はVOTEとATTACKのみ
                if (whisper['text'].split("(")[0] == "REQUEST"):
                    content = whisper['text'].split("(")[1]
                    content = content.split(")")
                    content = content[0].split()

                    if(content[0] == "VOTE"):
                        target = self.AgentToIndex(content[1])
                        self.__voteRequestMap_.update({agentIdx:target})
                        self.__RequestMap_.update({agentIdx:target})

                    if(content[0] == "ATTACK"):
                        target = self.AgentToIndex(content[1])
                        self.__attackRequestMap_.update({agentIdx:target})
                        self.__RequestMap_.update({agentIdx:target})

    #Whisper終了
    def isFinishWhisper(self):
        self.whisper_flag = True

    #Whisper開始時，前Whisperを削除
    def isStartWhisper(self):

        #Votemapの初期化
        self.__voteMap_ = {}
        for idx in self.__werewolfList_:
            self.__voteMap_[idx]    =   -1

        #Attackmapの初期化
        self.__attackMap_ = {}
        for idx in self.__werewolfList_:
            self.__attackMap_[idx]  =   -1

        self.whisper_flag = False

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


    #CO系

    #whisperでのCOすべて：{エージェント：CO役職}の辞書
    def getWillCOMap(self):
        return copy.deepcopy(self.__isCOMap_)

    #whisperでの占いCOすべて：エージェントのリスト
    def getWillSeerCOList(self):
        return copy.deepcopy(self.__seerWillCOList_)

    #通常発言関連

    #whisperでのAttack宣言：{エージェント：襲撃ターゲット}の辞書
    def getRequestAttackMap(self):
        return copy.deepcopy(self.__attackMap_)

    #whisperでのVote宣言：{エージェント：投票ターゲット}の辞書
    def getRequestVoteMap(self):
        return copy.deepcopy(self.__voteMap_)

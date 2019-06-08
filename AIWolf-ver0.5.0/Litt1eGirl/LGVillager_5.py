# -*- coding: utf-8 -*-
import aiwolfpy
import aiwolfpy.templatetalkfactory as ttf
import aiwolfpy.templatewhisperfactory as twf

import numpy as np
import copy, random

import MyData_05 as md
import Baseplayers as bp

class Villager(bp.BaseVillager):

    def __init__(self, agent_name):
        super(Villager,self).__init__(agent_name)
        self.fakeList   = []
        self.grayList   = []
        self.whiteList  = []

        self.realSeer   = None
        self.possessed  = None
        self.werewolf   = None

        self.maybe_black    = []
        self.maybe_white    = []
        self.isCO       = False
        self.isWWCO     = False

        self.isFirst    = True
        self.isBelieve  = True

        self.divineMap = {}

    def getName(self):
        return self.agent_name

    def initialize(self, game_info, game_setting):
        super(Villager,self).initialize(game_info, game_setting)
        self.grayList = self.myData.getAliveAgentIndexList()
        self.grayList.remove(self.agentIdx)

    def update(self, game_info, talk_history, whisper_history, request):
        super(Villager,self).update(game_info, talk_history, whisper_history, request)

        self.seerList   = self.myData.getSeerCOAgentList()
        if self.agentIdx in self.seerList:
            self.seerList.remove(self.agentIdx)
        self.seerCONum = len(self.seerList)

        #COがあったとき
        if(self.myData.isCOCall()):
            self.realSeer = None

            #占い師が1人 or 2人
            if self.seerCONum == 1 or self.seerCONum == 2:
                self.grayList = copy.deepcopy(self.candidate)
                seer = self.seerList[0]
                if seer in self.grayList:
                    self.grayList.remove(seer)

            #占い師が3人(占い師と狂人と人狼)
            if self.seerCONum == 3:
                newlist = copy.deepcopy(self.candidate)
                self.removeAll(newlist,self.seerlist)
                self.grayList = newlist

            '''
            #占い師が1人(占い師)
            if self.seerCONum == 1:
                seer = self.seerList[0]
                if seer in self.grayList:
                    self.grayList.remove(seer)
                self.realSeer = seer

            #占い師が2人(占い師と狂人)
            if self.seerCONum == 2:
                seer = self.seerList[0]
                if seer in self.grayList:
                    self.grayList.remove(seer)

            #占い師が3人(占い師と狂人と人狼)
            if self.seerCONum == 3:
                newlist = []
                self.realSeer = None
                for seer in self.seerList:
                    if seer in self.seerList:
                        newlist.append(seer)
                self.grayList = newlist
            '''

        #DIVINEDがあったとき
        if(self.myData.isDivineCall()):

            seerMap   = self.myData.getSeerCODataMap()
            self.whiteList  = []
            self.maybe_black=[]
            self.maybe_white=[]

            self.fakeSeer = self.fakeSearch()

            if self.seerCONum > 1:

                if(self.seerCONum-len(self.fakeList) == 1):
                    for i in self.seerList:
                        if i not in self.fakeList and self.isBelieve:
                            self.realSeer = i

            #真占い確定時
            if(self.realSeer != None):
                seer    = self.seerList[0]
                v       = seerMap[seer]
                black   = v.getBlackList()
                white   = v.getWhiteList()
                if(len(black)>0):
                    self.werewolf = black[0]
                if(len(white)>0):
                    for i in white:
                        self.whiteList.append(i)
            '''
            else:
                seerMap   = self.myData.getSeerCODataMap()
                for k,v in seerMap.items():
                    if not k in self.fakeList:
                        black   = v.getBlackList()
                        white   = v.getWhiteList()
                        if(len(black) > 0):
                            self.maybe_black.append(black[0])
                        if(len(white) > 0):
                            for i in white:
                                self.maybe_white.append(i)
            '''

    def dayStart(self):
        super(Villager,self).dayStart()
        self.willvote   = None

        #死亡者結果処理(2日目以降)
        if(self.gameInfo['day'] > 1):
            execute     = self.myData.getExecutedAgent()
            attacked    = self.myData.getAttackedAgent()

            if(execute in self.whiteList):  self.whiteList.remove(execute)
            if(attacked in self.whiteList): self.whiteList.remove(attacked)
            if(execute in self.grayList):  self.grayList.remove(execute)
            if(attacked in self.grayList): self.grayList.remove(attacked)

            self.fakeList = self.fakeSearch()
            if self.seerCONum-len(self.fakeList) == 1:
                for i in self.seerList:
                    if not i in self.fakeList:  self.realSeer = i

    def talk(self):

        if(self.COTrigger()):
            if(self.isCO):
                return ttf.comingout(self.agentIdx,'SEER')
            else:
                return ttf.comingout(self.agentIdx,'VILLAGER')

        #2日目：狂人占い師確定でWWCO
        if(self.myData.getToday() > 1 and self.WWCOTrigger()):
            return ttf.comingout(self.agentIdx,'WEREWOLF')

        #VOTE宣言
        if(self.willvote != self.MyTarget()):
            self.willvote = self.MyTarget()
            return ttf.vote(self.willvote)

        #ESTIMATE宣言
        if(self.myData.getTurn() < 10):
            talk = self.Estimate(self.willvote)
            if(talk != None):   return talk

        return ttf.over()

    def vote(self):

        target = self.MyTarget()
        if(target != None):
            return target

        return self.getMaxLikelyWolfAll()

    def finish(self):
        return None

    def setmyData(self,mydata):
        self.myData = mydata

    #COTrigger
    def COTrigger(self):

        #SEERCOしていたら次ターンで撤回
        if(self.isCO):
            self.isCO = False
            return True

        #SEERCOするかどうかの判定
        if(self.myData.getToday() == 0 and self.myData.getTurn() < 2):
            rand_x = random.randrange(100)
            rand_y = random.randrange(5)
            if(rand_x < rand_y):
                self.isCO = True
                return True

        return False

    #偽者調査
    def fakeSearch(self):

        smap        = self.myData.getSeerCODataMap()
        seerlist    = self.myData.getSeerCOAgentList()
        fakeSeer    = []
        werewolves  = 1

        if(len(seerlist) > 0):
            #人狼数以上の黒出し
            for k,v in smap.items():
                black   = v.getBlackList()
                white   = v.getWhiteList()

                #自分に黒を出していたら偽者
                if self.agentIdx in black:
                    fakeSeer.append(k)

                #日にち以上の結果を出していたら偽物
                if len(black) + len(white) > self.myData.getToday() and k not in fakeSeer:
                    fakeSeer.append(k)

                if len(black) > 0 and k not in fakeSeer:

                    #黒を人狼数以上出していたら偽者
                    if len(black) > werewolves:
                        fakeSeer.append(k)

                    #黒出しが死んでいたら偽物
                    if not self.myData.isAliveIndex(black[0]):
                        fakeSeer.append(k)

        #真置き解除
        if self.realSeer != None and self.realSeer in fakeSeer:
            self.realSeer = None
            self.isBelieve = False

        return fakeSeer

    def MyTarget(self):

        candidate = copy.deepcopy(self.candidate)

        #1日目
        if(self.myData.getToday() == 1):

            if self.isFirst:
                self.isFirst = False
                target = random.choice(candidate)
                return target

            #真占い師確定時
            elif self.realSeer != None:

                #真占い師の黒出しに投票
                if self.werewolf != None:
                    target = self.werewolf
                    if self.myData.isAliveIndex(target):   return target

                #真占い師の白出しを除外して占い師以外からランダム投票
                if len(self.whiteList) > 0:
                    if self.whiteList[0] in self.grayList:
                        self.grayList.remove(self.whiteList[0])
                    if len(self.whiteList) > 0:
                        target = self.myData.getMaxLikelyWolf(self.grayList)
                        return target

            elif(self.seerCONum == 2):

                '''
                #黒出しに投票
                if(len(self.maybe_black) > 0):
                    target = self.myData.getMaxLikelyWolf(self.maybe_black)
                    return target
                '''

                #占い師以外に投票
                target = self.myData.getMaxLikelyWolf(self.grayList)
                return target

                '''
                #白出しも抜いてみる
                if(len(self.maybe_white)>0):
                    copy_list   = copy.deepcopy(self.grayList)
                    for i in self.maybe_white:
                        if i in copy_list:  copy_list.remove(i)
                    if(len(copy_list)>0):
                        target = self.myData.getMaxLikelyWolf(copy_list)
                        if(self.myData.isAliveIndex(target)):   return target

                if(len(self.grayList)>0):
                    target = self.myData.getMaxLikelyWolf(self.grayList)
                    if(self.myData.isAliveIndex(target)):   return target
                '''

            elif(self.seerCONum == 3):

                #偽確定がいればその中から選ぶ
                if(len(self.fakeList) > 0):
                    target  = self.myData.getMaxLikelyWolf(self.fakeList)
                    return target

                #占い師以外に黒を出している占い師は怪しい
                seerMap   = self.myData.getSeerCODataMap()
                maybe_dark= []
                for k,v in seerMap.items():
                    black   = v.getBlackList()
                    if len(black) > 0 and not black[0] in self.seerList:
                        maybe_dark.append(k)
                if len(maybe_dark) > 0:
                    target  = self.myData.getMaxLikelyWolf(maybe_dark)
                    return target

                #黒判定されている占い師がいれば怪しい
                for k,v in seerMap.items():
                    black   = v.getBlackList()
                    if len(black) > 0 and black[0] in self.seerList:
                        maybe_dark.append(black[0])
                if len(maybe_dark) > 0:
                    target  = self.myData.getMaxLikelyWolf(maybe_dark)
                    return target

                #占い師からランダム
                target  = self.myData.getMaxLikelyWolf(self.grayList)
                return target

        #2日目
        if(self.myData.getToday() == 2):

            #真占い師確定時
            if self.realSeer != None:

                #真占い師生存
                if self.myData.isAliveIndex(self.realSeer):
                    if self.realSeer in candidate:
                        candidate.remove(self.realSeer)
                    if len(candidate) > 0:
                        target  = self.myData.getMaxLikelyWolf(candidate)
                        return target

                if self.werewolf != None:
                    target = self.werewolf
                    return target

                if len(self.whiteList) > 0:
                    for i in self.whiteList:
                        if i in candidate: candidate.remove(i)
                    if len(candidate) > 0:
                        target = self.myData.getMaxLikelyWolf(candidate)
                        return target


                '''
                #占い師が一人で，真占い師死亡
                elif(self.seerCONum == 1):

                    if(self.werewolf != None and self.myData.isAliveIndex(self.werewolf)):
                        target = self.werewolf
                        return target
                    if(len(self.whiteList) > 0):
                        if(self.whiteList[0]  in self.candidate):
                            self.candidate.remove(i)
                    if(len(self.candidate) > 0):
                        target = self.myData.getMaxLikelyWolf(self.candidate)
                        return target
                '''

        if len(self.grayList) > 0:
            target  = self.myData.getMaxLikelyWolf(self.grayList)
            return target

        if len(self.candidate) > 0:
            target = self.myData.getMaxLikelyWolf(self.candidate)
            return target

        return self.myData.getMaxLikelyWolfAll()

    def Estimate(self,target):
        rand_x = random.randrange(10)
        rand_y = random.randrange(5)
        if(rand_x < rand_y):
            rand = random.randint(1,10)
            if(rand > 7):
                return ttf.estimate(target,'WEREWOLF')
            elif(rand == 8 and self.realSeer != None):
                return ttf.estimate(self.realSeer,'SEER')
            else:
                t = self.myData.getMaxLikelyVillAll()
                if(t != None): return ttf.estimate(t,'VILLAGER')

        return None

    def WWCOTrigger(self):

        if(self.isWWCO):
            return False

        if(self.possessed != None and self.myData.isAliveIndex(self.possessed) and len(self.myData.getAliveSeerCOAgentList()) == 1):
            rand_x  = random.randrange(5)
            self.isWWCO = True
            if(rand_x < 4): return True
            else:           return False
        return False

    def removeAll(self,targetList, delMemberList):
        for idx in delMemberList:
            if idx in targetList:
                targetList.remove(idx)

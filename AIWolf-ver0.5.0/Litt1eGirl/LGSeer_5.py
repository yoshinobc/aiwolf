# -*- coding: utf-8 -*-
import aiwolfpy
import aiwolfpy.templatetalkfactory as ttf
import aiwolfpy.templatewhisperfactory as twf

import math
import numpy as np
import copy, random

import MyData_05 as md
import Baseplayers as bp

class Seer(bp.BaseSeer):

    def __init__(self, agent_name):
        super(Seer,self).__init__(agent_name)
        self.willvote   = None
        self.possessed  = None
        self.werewolf   = None

        self.divineList = []
        self.whiteList  = []
        self.grayList   = []

        self.divined    = []
        self.result     = {}
        self.isCO       = False
        self.isPosCO    = False
        self.isDivined  = False
        self.isBlack      = False


    def initialize(self, game_info, game_setting):
        super(Seer,self).initialize(game_info, game_setting)
        self.divineList = self.myData.getAliveAgentIndexList()
        self.divineList.remove(self.agentIdx)
        self.grayList = self.myData.getAliveAgentIndexList()
        self.grayList.remove(self.agentIdx)

    def update(self, game_info, talk_history, whisper_history, request):
        super(Seer,self).update(game_info, talk_history, whisper_history, request)

        self.fakeseerList   = self.myData.getSeerCOAgentList()
        if self.agentIdx in self.fakeseerList:
            self.fakeseerList.remove(self.agentIdx)
        self.seerCONum = len(self.fakeseerList)

        #COがあったとき
        if(self.myData.isCOCall() and self.myData.getToday() == 1):

            #自分以外の占い師が1人(狂人)
            if self.seerCONum == 1:

                #占い師CO2人→1人で減った時
                if not self.isBlack:
                    self.werewolf = None
                    self.grayList = copy.deepcopy(self.default_grayList)

                fake = self.fakeseerList[0]
                if fake in self.grayList and fake != self.werewolf:
                    self.grayList.remove(fake)
                    self.possessed = fake


            #自分以外の占い師が2人(狂人と人狼)
            if self.seerCONum == 2:

                a   = self.fakeSeer[0]
                b   = self.fakeSeer[1]

                if self.werewolf == a: self.possessed = b
                if self.werewolf == b: self.possessed = a

                if a in self.whiteList:
                    self.possessed  = a
                    self.werewolf   = b
                if b in self.whiteList:
                    self.possessed  = b
                    self.werewolf   = a

                #両方未確定->グレー置き
                if self.werewolf == None:
                    self.grayList   = self.fakeSeer

    def dayStart(self):
        super(Seer,self).dayStart()
        self.willvote   = None
        self.isDivined = False
        divined  = self.gameInfo['divineResult']

        #占い結果処理
        if(divined != None):
            self.divined.append(divined)
            target  = divined['target']
            result  = divined['result']
            self.result.update({target:result})

            if target in self.divineList :  self.divineList.remove(target)

            if result == 'HUMAN':
                self.whiteList.append(target)
                if target in self.grayList: self.grayList.remove(target)
            else:
                self.werewolf   = target
                self.isBlack      = True

        #死亡者結果処理(2日目以降)
        if(self.gameInfo['day'] > 1):
            execute     = self.myData.getExecutedAgent()
            attacked    = self.myData.getAttackedAgent()
            candidate   = copy.deepcopy(self.candidate)

            if self.werewolf != None and self.werewolf in self.whiteList:
                self.werewolf = None

            if len(self.whiteList) > 0:
                for i in self.whiteList:
                    if i in candidate: candidate.remove(i)
                if len(candidate) == 1:
                    self.werewolf = candidate[0]

            if(execute in self.divineList):     self.divineList.remove(execute)
            if(attacked in self.divineList):    self.divineList.remove(attacked)
            if(execute in self.grayList):       self.grayList.remove(execute)
            if(attacked in self.grayList):      self.grayList.remove(attacked)

        self.default_grayList   = copy.deepcopy(self.grayList)


    def talk(self):

        #COTrigerがTrueならCO
        if self.COTrigger():
            self.isCO = True
            return ttf.comingout(self.agentIdx,'SEER')

        #2日目PPCO
        if self.myData.getToday() == 2 and self.PosCOTrigger():
            self.isPosCO = True
            return ttf.comingout(self.agentIdx,'POSSESSED')

        #COしたらdivined
        if(len(self.result) > 0 and not self.isPosCO):
            rand_x = random.randrange(10)
            threshhold = int(20 / math.pi * math.atan(self.myData.getTurn() - 1))
            rand_y = random.randrange(threshhold if threshhold > 1 else 1)
            if (self.isCO and rand_x <= rand_y):
                target = list(self.result.keys())[0]
                result = self.result[target]
                del self.result[target]
                return ttf.divined(target, result)

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

        return super(Seer,self).vote()

    def divine(self):

        #初日はランダム
        if(self.myData.getToday() == 0):
            target = random.choice(self.divineList)
            return target

        #2日目はif-then
        elif(self.myData.getToday() == 1):
            exe = self.gameInfo['latestExecutedAgent']
            if exe in self.divineList:
                self.divineList.remove(exe)

            #黒出ししていたらランダム
            if self.isBlack:
                target = random.choice(self.divineList)
                return target

            #黒出ししていないが人狼が特定できていればそいつ
            elif(self.werewolf != None):
                target = self.werewolf
                return target

            #グレーから
            elif(len(self.grayList) > 0):
                if exe in self.grayList:
                    self.grayList.remove(exe)
                if(len(self.grayList) > 0):
                    target = self.myData.getMaxLikelyWolf(self.grayList)
                    return target

            elif(len(self.divineList) > 0):
                target = self.myData.getMaxLikelyWolf(self.divineList)
                return target

        #例外対策
        return self.agentIdx

    def finish(self):
        return super(Seer,self).finish()

    def setmyData(self,mydata):
        self.myData = mydata

    def MyTarget(self):

        #自分の黒出しに投票
        if self.isBlack:
            target = self.werewolf
            return target

        if self.werewolf != None:
            target = self.werewolf
            return target

        #グレーリストから投票
        if len(self.grayList) > 0:
            target = self.myData.getMaxLikelyWolf(self.grayList)
            return target

        target = self.myData.getMaxLikelyWolf(self.candidate)
        return target

    def COTrigger(self):
        rand_x = random.randrange(10)
        threshhold = int(20 / math.pi * math.atan(self.myData.getTurn()))
        rand_y = random.randrange(threshhold if threshhold > 1 else 1)
        if (rand_x <= rand_y):
            return False
        elif(not self.isCO and self.myData.getTurn() > 10):
            self.isCO = True
            return True

        return False

    def Estimate(self,target):
        rand_x = random.randrange(10)
        rand_y = random.randrange(5)
        if(rand_x < rand_y):
            rand = random.randint(1,10)
            if(rand > 7):
                if(self.werewolf != None):  t = self.werewolf
                else:                       t = target
                return ttf.estimate(t,'WEREWOLF')
            elif(rand == 8 and self.possessed != None):
                return ttf.estimate(self.possessed,'POSSESSED')
            else:
                t = self.myData.getMaxLikelyVillAll()
                if(t != None): return ttf.estimate(t,'VILLAGER')

        return None

    #狂人CO，現状は確定CO
    def PosCOTrigger(self):

        if self.isPosCO:    return False

        #狂人生存が確定している
        if self.possessed != None and self.myData.isAliveIndex(self.possessed) :
            rand_x = random.randrange(10)
            if rand_x > 3 and self.myData.getTurn() < 3:
                return True

        return False

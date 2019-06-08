# -*- coding: utf-8 -*-
import aiwolfpy
import aiwolfpy.templatetalkfactory as ttf
import aiwolfpy.templatewhisperfactory as twf

import numpy as np
import copy, random

import MyData_05 as md
import Baseplayers as bp

class Werewolf(bp.BaseWerewolf):

    def __init__(self, agent_name):
        super(Werewolf,self).__init__(agent_name)
        self.realSeer   = None
        self.maybeSeer  = None
        self.possessed  = None
        self.fakeBlack  = None

        self.whiteList  = []
        self.targetList = []
        self.attackList = []
        self.villagerList   = []
        self.maybewhite = []    #客観的に白そう

        self.isCO   = False
        self.isWWCO = False

        self.isFirst = True

        self.otherCOMap = {}
        self.divineFlag = 0
        self.fakeSeerList = []
        self.divineMap = {}#{agentIdx:{targetIdx:'WEREWOLF'or'HUMAN'}}

        self.turn = 0

    def initialize(self, game_info, game_setting):
        super(Werewolf,self).initialize(game_info, game_setting)
        self.targetList = self.myData.getAliveAgentIndexList()
        self.targetList.remove(self.agentIdx)
        self.attackList = self.myData.getAliveAgentIndexList()
        self.attackList.remove(self.agentIdx)

    def update(self, game_info, talk_history, whisper_history, request):
        super(Werewolf,self).update(game_info, talk_history, whisper_history, request)

        self.seerList   = self.myData.getSeerCOAgentList()
        if self.agentIdx in self.seerList:
            self.seerList.remove(self.agentIdx)
        self.seerCONum = len(self.seerList)

        #COがあったとき
        if(self.myData.isCOCall()):
            self.realSeer = None

            '''
            #占い師が1人(占い師)
            if self.seerCONum == 1:
                seer = self.seerList[0]
                self.realSeer = seer
            '''

        #DIVINEDがあったとき
        if(self.myData.isDivineCall() and self.myData.getToday() < 2):

            seerMap   = self.myData.getSeerCODataMap()

            self.realSeer   = None
            self.possessed  = None
            self.maybeSeer  = None
            self.maybewhite = []

            for k,v in seerMap.items():
                black   = v.getBlackList()
                white   = v.getWhiteList()

                #黒が自分に出ていたら多分本物/自分以外なら偽物
                if len(black) > 0:
                    if self.agentIdx in black:
                        self.maybeSeer = k
                    else:
                        self.possessed = k
                        self.fakeBlack = black[0]

                #白が自分に出ていたら偽物
                if len(white) > 0:
                    if self.agentIdx in white:
                        self.possessed = k
                    else:
                        self.maybewhite.append(white[0])

            if self.seerCONum == 2:

                #狂人が確定していればもう一方が真占
                if self.possessed != None:
                    for i in self.seerList:
                        if i != self.possessed:    self.realSeer

                #真占い確定時
                if self.realSeer != None:
                    self.whiteList = []
                    seer    = self.seerList[0]
                    v       = seerMap[seer]
                    white   = v.getWhiteList()
                    if len(white) > 0:
                        for i in white: self.whiteList.append(i)


    def dayStart(self):
        super(Werewolf,self).dayStart()
        self.willvote   = None

        #死亡者結果処理(2日目以降)
        if(self.gameInfo['day'] > 1):
            execute     = self.myData.getExecutedAgent()
            attacked    = self.myData.getAttackedAgent()

            if(execute in self.whiteList):  self.whiteList.remove(execute)
            if(execute in self.targetList):  self.targetList.remove(execute)
            if(execute in self.attackList):  self.attackList.remove(execute)

            if(attacked in self.whiteList): self.whiteList.remove(attacked)
            if(attacked in self.targetList): self.targetList.remove(attacked)
            if(attacked in self.attackList): self.attackList.remove(attacked)


    def talk(self):


        if(self.COTrigger()):
            if(self.isCO):
                return ttf.comingout(self.agentIdx,'SEER')
            else:
                return ttf.comingout(self.agentIdx,'VILLAGER')

        #2日目：狂人確定＆生存または占い師2人生存でWWCO
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

        return self.getMaxLikelyExecuteAgentAll()

    def whisper(self):
        return ttf.over()

    def attack(self):
        executed    = self.gameInfo["latestExecutedAgent"]

        villagers   = copy.deepcopy(self.attackList)
        if executed in villagers: villagers.remove(executed)

        aliveseer   = self.myData.getAliveSeerCOAgentList()
        if executed in aliveseer: aliveseer.remove(executed)

        for i in villagers:
            if i in aliveseer:    villagers.remove(i)

        #占い師が2人残っている場合: 占い師以外を襲撃
        if len(aliveseer) == 2:

            if len(self.whiteList) > 0 and executed != self.whiteList[0]:
                return self.whiteList[0]

            if len(self.maybewhite) > 0 and executed != self.maybewhite[0]:
                return self.maybewhite[0]

            if len(villagers) > 0:
                target = self.myData.getMaxLikelyVill(villagers)
                return target

        #占い師が1人処刑された場合
        elif(len(aliveseer) == 1):

            #狂死亡・真生存で占を襲撃
            if(self.realSeer != None and self.realSeer != executed):
                target  = self.realSeer
                return target

            #真死亡・狂生存で村を襲撃
            if(self.possessed != None and self.possessed != executed):

                #真占いの白出しは襲撃しない(PPのため)
                if len(villagers) > 0 and len(self.whiteList) > 0:
                    if self.whiteList[0] in villagers:
                        villagers.remove(self.whiteList[0])
                if len(villagers) > 0:
                    target = self.myData.getMaxLikelyVill(self.villagers)
                    return target

            #不明ならばとりあえず占を噛んでおく
            else:
                if self.maybeSeer != None and self.maybeSeer != executed:
                    target  = self.maybeSeer
                    return target
                else:
                    target  = aliveseer[0]
                    return target

        target = target = self.myData.getMaxLikelyVill(self.targetList)
        return target

    def finish(self):
        super(Werewolf,self).finish()

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
            rand_x = random.randrange(10)
            rand_y = random.randrange(2)
            if(rand_x < rand_y):
                self.isCO = True
                return True

        return False

    def MyTarget(self):

        #1日目
        if(self.myData.getToday() == 1):

            if(self.isFirst):
                self.isFirst = False
                target = random.choice(self.candidate)
                return target

            #占い師が1人
            if(self.seerCONum == 1):

                #占い師と白出しは除外
                if(self.realSeer in self.targetList):
                    self.targetList.remove(self.realSeer)

                if(len(self.whiteList) > 0):
                    if(self.whiteList[0] in self.targetList):
                        self.targetList.remove(self.whiteList[0])

                #target  = self.myData.getMaxLikelyWolf(self.targetList)
                target = self.myData.getPiggyBack(self.targetList)
                return target

            #占い師が2人
            if(self.seerCONum == 2):
                #(偽黒)偽物による黒出しに投票
                if(self.fakeBlack != None):
                    target = self.fakeBlack
                    return target

                #占い師以外に投票
                if(len(self.maybewhite) == 2):
                    flag    = [False,False]
                    seer,vill   = [],[]
                    for i in self.maybewhite:
                        if i == self.agentIdx:
                            flag[0] = True
                        elif i in self.seerList:
                            flag[1] = True
                            seer.append(i)
                        else:
                            vill.append(i)

                        if i in self.targetList:
                            self.targetList.remove(i)

                    #村A白・村B白のとき->村から選ぶ
                    if(len(vill) == 2):
                        #target  = self.myData.getMaxLikelyWolf(vill)
                        target = self.myData.getPiggyBack(vill)

                    #自白のとき
                    elif(flag[0]):
                        #村A白or占A白のとき->村Bor村2人から選ぶ
                        for i in self.seerList:
                            if i in self.targetList:    self.targetList.remove(i)
                        #target  = self.myData.getMaxLikelyWolf(self.targetList)
                        target = self.myData.getPiggyBack(self.targetList)

                    #占白のとき
                    elif(flag[1]):
                        #村A白or占B白のとき->村Bor村2人から選ぶ
                        for i in self.seerList:
                            if i in self.targetList:    self.targetList.remove(i)
                        #target  = self.myData.getMaxLikelyWolf(self.targetList)
                        target = self.myData.getPiggyBack(self.targetList)

                    return target

        #2日目
        elif(self.myData.getToday() == 2):
            aliveseer   = self.myData.getAliveSeerCOAgentList()

            #PP確定
            if(self.isWWCO):
                target = None

                #狂人確定で狂人以外
                if(self.possessed != None and self.possessed in self.targetList):
                    self.targetList.remove(self.possessed)
                    if(len(self.targetList) > 0):
                        target  = self.myData.getMaxLikelyWolf(self.targetList)
                        if(target != None): return target

                #狂人COチェック
                self.CheckPossessedCO()
                if(self.possessed != None and self.possessed in self.targetList):
                    self.targetList.remove(self.possessed)
                    if(len(self.targetList) > 0):
                        target  = self.myData.getMaxLikelyWolf(self.targetList)
                        if(target != None): return target


                #占い師2人生存で真の方
                if(len(aliveseer) == 2):
                    if(self.realSeer != None):  target  = self.realSeer
                    elif(self.maybeSeer != None):   target  = self.maybeSeer
                    else:   target = self.myData.getMaxLikelyVill(aliveseer)

                if(target != None):
                    return target

            #形勢未確定
            else:

                #占い師1人生存(占狂不明)->占い師以外
                if(len(aliveseer) == 1):
                    if aliveseer[0] in self.targetList: self.targetList.remove(aliveseer[0])
                    if(len(self.targetList) > 0):
                        target  = self.myData.getMaxLikelyWolf(self.targetList)

                #占い師2人生存で真っぽい方(自分を殺そうとしている方)
                elif(len(aliveseer) == 2):
                    vmap    = self.myData.getVoteMap()
                    target,through = None,None
                    for i in aliveseer:
                        if(vmap[i] == self.agentIdx):   #自分に投票宣言
                            target = i
                        elif(vmap[i] != -1):    #自分以外に投票宣言
                            through = i
                    if(target != None): return target
                    elif(through != None):
                        if(aliveseer[0] == through):    return aliveseer[1]
                        else:   return aliveseer[1]

                    target = self.myData.getMaxLikelyVill(aliveseer)
                    return target

                #占い師全滅(村人に擬態を続ける)
                elif(len(aliveseer) == 0):
                    target = self.myData.getMaxLikelyVill(self.targetList)
                    return target

        target = self.myData.getMaxLikelyWolf(self.candidate)
        return target

    def Estimate(self,target):
        rand_x = random.randrange(10)
        rand_y = random.randrange(5)
        if(rand_x < rand_y):
            rand = random.randint(1,3)
            if(rand > 1):
                target = self.MyTarget()
                return ttf.estimate(target,'WEREWOLF')
            else:
                target = self.myData.getMaxLikelyVillAll()
                if(target != None): return ttf.estimate(target,'VILLAGER')

        return None

    def WWCOTrigger(self):

        if(self.isWWCO):
            return False

        if(self.possessed != None and self.myData.isAliveIndex(self.possessed)):
            rand_x  = random.randrange(5)
            self.isWWCO = True
            return True
            if(rand_x < 4): return True
            else:           return False

        if(len(self.myData.getAliveSeerCOAgentList()) == 2):
            rand_x  = random.randrange(5)
            self.isWWCO = True
            return True
            if(rand_x < 3): return True
            else:           return False

        return False

    def CheckPossessedCO(self):

        possList = self.myData.getAlivePossCOAgentList()

        #狂人が1人の場合
        if(len(possList) == 1):
            self.possessed = possList[0]

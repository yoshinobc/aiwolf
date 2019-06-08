# -*- coding: utf-8 -*-
import aiwolfpy
import aiwolfpy.templatetalkfactory as ttf
import aiwolfpy.templatewhisperfactory as twf

import math
import numpy as np
import copy, random

import MyData_05 as md
import Baseplayers as bp

class Possessed(bp.BasePossessed):

    def __init__(self, agent_name):
        super(Possessed,self).__init__(agent_name)
        self.willvote   = None
        self.realSeer   = None
        self.werewolf   = None
        self.maybewolf  = None

        self.divineList = []
        self.whiteList = []
        self.seerList   = []

        self.divined    = {}
        self.result     = {}
        self.myBlack    = None
        self.isCO       = False
        self.isStopCO   = False
        self.isDivined  = False
        self.isBlack    = False
        self.isPosCO    = False

    def getName(self):
        return self.agent_name

    def initialize(self, game_info, game_setting):
        super(Possessed,self).initialize(game_info, game_setting)
        self.grayList = self.myData.getAliveAgentIndexList()
        self.grayList.remove(self.agentIdx)
        self.divineList = self.myData.getAliveAgentIndexList()
        self.divineList.remove(self.agentIdx)

    def update(self, game_info, talk_history, whisper_history, request):
        super(Possessed,self).update(game_info, talk_history, whisper_history, request)

        self.seerList   = self.myData.getSeerCOAgentList()
        if self.agentIdx in self.seerList:
            self.seerList.remove(self.agentIdx)
        self.seerCONum = len(self.seerList)

        #COがあったとき
        if(self.myData.isCOCall()):
            self.realSeer = None

            #自分以外の占い師が1人(占い師)
            if self.seerCONum == 1:
                self.grayList = copy.deepcopy(self.default_grayList)

                self.isStopCO = False
                seer = self.seerList[0]
                if seer in self.grayList:
                    self.grayList.remove(seer)
                self.realSeer = seer

            #自分以外の占い師が2人以上
            if self.seerCONum == 2:
                self.isStopCO = True
                self.realSeer = None
                self.grayList = self.seerList

        #DIVINEDがあったとき
        if(self.myData.isDivineCall()):

            seerMap   = self.myData.getSeerCODataMap()

            if self.seerCONum == 1:
                seer    = self.seerList[0]
                v       = seerMap[seer]

                black   = v.getBlackList()
                white   = v.getWhiteList()
                if(len(black)>0):
                    self.werewolf = black[0]
                if(len(white)>0):
                    for i in white:
                        if not i in self.whiteList:
                            self.whiteList.append(i)

            if self.seerCONum == 2:

                for k,v in seerMap.items():

                    #自分への黒出し=人狼と仮定
                    black   = v.getBlackList()
                    if self.agentIdx in black:  self.werewolf = k
                    if not black in self.myData.getAliveAgentList():    self.werewolf = k
                    if len(black) > 0:  self.werewolf = k

                    #占い師以外への黒=人狼の偽黒出しと決め打ち

                    #人狼が確定していればもう片方を真占いと確定
                    for i in self.seerList:
                        if(self.werewolf != None and self.werewolf != i):
                            self.realSeer = i

                    #占い師への黒=真占い師の人狼への黒出しと決め打ち

                    #占い師への白=真占い師確定

                    #占い師以外への白=白と借り置く

            if self.werewolf in self.candidate:
                self.candidate.remove(self.werewolf)

        if(len(self.myData.getWolfCOAgentList()) > 0):
            for i in self.myData.getWolfCOAgentList():
                if( i not in self.whiteList and i != self.werewolf):
                    self.maybewolf = i

    def dayStart(self):
        super(Possessed,self).dayStart()

        self.willvote   = None

        #死亡者結果処理(2日目以降)
        if(self.gameInfo['day'] > 1):
            execute     = self.myData.getExecutedAgent()
            attacked    = self.myData.getAttackedAgent()

            if(execute in self.whiteList):  self.whiteList.remove(execute)
            if(attacked in self.whiteList): self.whiteList.remove(attacked)

        self.default_grayList   = copy.deepcopy(self.grayList)

        self.isDivined = False


    def talk(self):

        #COTrigerがTrueならCO
        if(self.COTrigger()):
            self.isCO = True
            return ttf.comingout(self.agentIdx,'SEER')

        #2日目PPCO
        if(self.myData.getToday() == 2 and self.PosCOTrigger()):
            self.isPosCO = True
            return ttf.comingout(self.agentIdx,'POSSESSED')

        #占い騙り時
        if(self.isCO and not self.isPosCO):
            rand_x = random.randrange(10)
            threshhold = int(20/math.pi*math.atan(self.myData.getTurn()-1))
            rand_y = random.randrange(threshhold if threshhold > 1 else 1 )
            if(rand_x <= rand_y and not self.isDivined):
                self.isDivined = True
                self.MyDivine()
                if(len(self.result) > 0):
                    target  = list(self.result.keys())[0]
                    result  = self.result[target]
                    del self.result[target]
                    return ttf.divined(target,result)

        #VOTE宣言
        target = self.MyTarget()
        if(self.willvote != target):
            self.willvote = target
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

        return super(Possessed,self).vote()

    def finish(self):
        return super(Possessed,self).finish()

    def setmyData(self,mydata):
        self.myData = mydata

    def MyTarget(self):

        #1日目
        if(self.myData.getToday()==1):

            '''
            #人狼確定時
            #潜伏時 or 占い師COしているが自分が白出ししたとき
            #50%前後の確率で人狼の投票宣言に便乗する
            ⇒
            self.werewolfがNoneのとき
            self.isStopCO or (self.isCO and not self.isBlack)のとき
            人狼プレイヤのVOTE，またはVOTEリクエストを得る
            '''

            if(self.seerCONum==1):

                #自分の黒出しに投票
                if self.isBlack:
                    target = self.myBlack
                    return target
                #占い師の白出しに投票
                if len(self.whiteList) > 0:
                    target = self.whiteList[0]
                    return target

                rand_x = random.randrange(2)

                #人狼の投票に便乗
                if self.werewolf != None and rand_x == 0:
                    votemap = self.myData.getVoteMap()
                    if self.werewolf in votemap:
                        target = votemap[self.werewolf]
                        if target > 0 and target != self.agentIdx:
                            return target

                '''
                #真占い師に投票
                if self.realSeer != None:
                    target = self.realSeer
                    return target
                '''

                if len(self.grayList) > 0:
                    target = self.myData.getMaxLikelyVill(self.grayList)
                    return target

                target = self.myData.getMaxLikelyVillAll()
                return target

            if(self.seerCONum==2):

                #人狼判明時
                if self.werewolf != None:

                    #人狼占い師の黒出しに投票
                    if self.werewolf in self.seerList:
                        seerMap   = self.myData.getSeerCODataMap()
                        v       = seerMap[self.werewolf]
                        black   = v.getBlackList()
                        white   = v.getWhiteList()
                        if len(black) > 0:
                            if black[0] != self.agentIdx:
                                return black[0]

                    #人狼の投票に便乗
                    votemap = self.myData.getVoteMap()
                    if self.werewolf in votemap:
                        target = votemap[self.werewolf]
                        if target > 0 and target != self.agentIdx:
                            return target


                #真占い師に投票
                if self.realSeer != None:
                    target = self.realSeer
                    return target

                #占い師以外に投票
                target = self.myData.getMaxLikelyVill(self.candidate)
                return target

        #2日目
        if(self.myData.getToday() == 2):

            #人狼が確定していれば除外
            if(self.werewolf in self.candidate):
                self.candidate.remove(self.werewolf)
                if(len(self.candidate) > 0):
                    return self.myData.getMaxLikelyVill(self.candidate)

            #maybe人狼が確定していれば除外
            elif(self.maybewolf in self.candidate):
                self.candidate.remove(self.maybewolf)
                if(len(self.candidate) > 0):
                    return self.myData.getMaxLikelyVill(self.candidate)

            #WolfCOがあれば除外
            elif(self.getWWCOPlayer() in self.candidate):
                self.candidate.remove(self.getWWCOPlayer())
                if(len(self.candidate) > 0):
                    return self.myData.getMaxLikelyVill(self.candidate)

            #占い師が1人
            if(self.seerCONum==1):

                #占い師が生きていれば投票
                if (self.myData.isAliveIndex(self.seerList[0])):
                    target = self.seerList[0]
                    return target

                #占い師が死んでいるとき
                else:
                    #確定白に投票
                    if(len(self.whiteList) > 0):
                        target = self.whiteList[0]
                        if(self.myData.isAliveIndex(target)):
                            return target

            #占い師が2人
            if(self.seerCONum==2):

                aliveseer = self.myData.getAliveSeerCOAgentList()
                if self.agentIdx in aliveseer:
                    aliveseer.remove(self.agentIdx)

                #占い師1名生存(=人狼＆村人)
                if(len(aliveseer) == 1):
                    if aliveseer[0] in self.candidate:
                        self.candidate.remove(aliveseer[0])
                    if(len(self.candidate) > 0):
                        return self.candidate[0]

                #占い師2名生存(=人狼＆占い師)
                else:
                    if(len(self.candidate) > 0):
                        return self.myData.getMaxLikelyVill(self.candidate)

        target = self.myData.getMaxLikelyVillAll()
        return target

    def MyDivine(self):

        #占い師が1人
        if(self.seerCONum == 1):
            rand_x = random.randint(1,10)
            rand_y = random.randrange(1,6)
            rand_z = random.randrange(1,3)

            if(self.realSeer in self.myData.getSeerCODataMap()):
                afterSayTarget = (self.myData.getSeerCODataMap()[self.realSeer]).getResult(self.myData.getToday())
            else:
                afterSayTarget = None

            black_target = None

            #真占いが先にDIVINEDしていたら
            if(afterSayTarget != None and list(afterSayTarget.keys())[0] in self.divineList):
                target = list(afterSayTarget.keys())[0]

                #黒出ししたとき
                if(afterSayTarget[target]):
                    black_target = target

                    #中確率で別のプレイヤに黒出し
                    if(rand_x < rand_y):
                        candidate = copy.deepcopy(self.divineList)
                        candidate.remove(target)
                        if(len(candidate) > 0):
                            new_target = self.getMaxLikelyVill(candidate)
                            self.divineList.remove(new_target)
                            self.divined.update({new_target: "WERWOLF"})
                            self.result.update({new_target: "WERWOLF"})
                            return

                    #低確率で黒出しされたプレイヤ(=人狼)に白出し
                    if(rand_x < rand_z):
                        self.divineList.remove(target)
                        self.divined.update({target: "HUMAN"})
                        self.result.update({target: "HUMAN"})
                        return

                #白出ししたとき
                else:
                    #低確率で便乗白出し
                    if(rand_x < rand_z):
                        self.divineList.remove(target)
                        self.divined.update({target: "HUMAN"})
                        self.result.update({target: "HUMAN"})
                        return
                    #低確率で便乗黒出し
                    if(rand_x < rand_z):
                        self.divineList.remove(target)
                        self.divined.update({target: "WEREWOLF"})
                        self.result.update({target: "WEREWOLF"})
                        return

                '''
                ※以下元々の仕様．

                #白出しには白便乗
                if(afterSayTarget[target] is False):
                    self.divined.update({target: "HUMAN"})
                    self.result.update({target: "HUMAN"})
                #黒出しには黒便乗
                else:
                    self.isBlack = True
                    self.myBlack = target
                    self.divined.update({target: "WEREWOLF"})
                    self.result.update({target: "WEREWOLF"})
                '''

            #低確率で占い師に白出し
            elif(rand_x < rand_z and self.seerList[0] in self.divineList):
                self.divineList.remove(self.seerList[0])
                self.divined.update({self.seerList[0]:"HUMAN"})
                self.result.update({self.seerList[0]:"HUMAN"})
                return

            #低確率で占い師以外に白出し
            elif(rand_x < rand_z):
                target = self.myData.getMaxLikelyWolf(self.divineList)
                self.divineList.remove(target)
                self.divined.update({target:"HUMAN"})
                self.result.update({target:"HUMAN"})
                return

            #高確率で占い師以外に黒出し
            else:
                candidate = copy.deepcopy(self.divineList)

                #真占いが黒出ししていたら，そこには黒を出さない
                if(black_target != None and black_target in candidate):
                    candidate.remove(black_target)

                target = self.myData.getMaxLikelyVill(candidate)
                self.isBlack = True
                self.myBlack = target
                self.divineList.remove(target)
                self.divined.update({target:"WEREWOLF"})
                self.result.update({target:"WEREWOLF"})
                return

        #占い師が2人
        elif(self.seerCONum == 2):
            rand_z = random.randrange(2)

            #中確率で占い師に白出し
            if(rand_z == 0):
                target = self.myData.getMaxLikelyWolf(self.seerList)
                if(target in self.divineList):
                    self.divineList.remove(target)
                    self.divined.update({target:"HUMAN"})
                    self.result.update({target:"HUMAN"})
                    return

            #中確率で占い師以外に黒出し
            else:
                t = []
                for i in self.divineList:
                    if i not in self.seerList:
                        t.append(i)
                if(len(t) > 0):
                    target = self.myData.getMaxLikelyVill(t)
                else:
                    target = self.myData.getMaxLikelyVill(self.divineList)
                self.isBlack = True
                self.myBlack = target
                self.divineList.remove(target)
                self.divined.update({target:"WEREWOLF"})
                self.result.update({target:"WEREWOLF"})
                return

    def COTrigger(self):

        #占いCO2人(人狼，占い師)の場合は潜伏
        if(self.isStopCO):  return False

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

            #潜伏時
            if(self.isStopCO):

                if(rand > 7):
                    return ttf.estimate(target,'WEREWOLF')
                elif(rand == 8 and self.werewolf != None):
                    if(self.werewolf in self.seerList):
                        return ttf.estimate(self.werewolf,'SEER')
                else:
                    t = self.myData.getMaxLikelyWolfAll()
                    if(t != None): return ttf.estimate(t,'VILLAGER')

            #占いCO時
            else:
                if(rand > 7):
                    if(self.myBlack != None):   t = self.myBlack
                    else:                       t = target
                    return ttf.estimate(t,'WEREWOLF')
                elif(rand == 8 and self.realSeer != None):
                    return ttf.estimate(self.realSeer,'POSSESSED')
                else:
                    t = self.myData.getMaxLikelyWolfAll()
                    if(t != None): return ttf.estimate(t,'VILLAGER')

        return None

    #狂人CO，現状は確定CO
    def PosCOTrigger(self):
        if(self.isPosCO):
            return False

        rand_x = random.randrange(10)
        if rand_x > 1 and self.myData.getTurn() < 3:
            return True
        return False

    #人狼COプレイヤの獲得
    def getWWCOPlayer(self):

        wwco    = self.myData.getWolfCOAgentList()

        if(len(wwco) == 1):
            return wwco[0]

        #WolfCOが2人，または0人のときはカウントしない
        else:
            return None

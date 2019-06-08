# -*- coding: utf-8 -*-
import aiwolfpy
import aiwolfpy.templatetalkfactory as ttf
import aiwolfpy.templatewhisperfactory as twf

import numpy as np
import copy,random

import MyData_15 as md
import Baseplayers as bp

class Villager(bp.BaseVillager):

    def __init__(self, agent_name):
        super(Villager,self).__init__(agent_name)
        self.isCall     = False

        #信仰占い師関連
        self.realSeer   = None
        self.isRealSeer = False
        self.myWhite    = []
        self.myBlack    = []

    def getName(self):
        super(Villager,self).getName()

    def update(self, game_info, talk_history, whisper_history, request):
        super(Villager,self).update(game_info, talk_history, whisper_history, request)

    def initialize(self, game_info, game_setting):
        super(Villager,self).initialize(game_info, game_setting)

    def dayStart(self):
        super(Villager,self).dayStart()

        #self.candidate = 自分以外の生存者リスト

        # reuquest は 1 日 1 回まで
        self.isRequest = True
        self.isRequestAll = True

    def talk(self):

        self.isFollow = False

        # 扇動受けるメソッド
        requests = self.myData.getRequestTodayMe()
        if requests != None:
            wolf = self.myData.getMaxLikelyWolfAll()
            x = random.randint(0,3)
            y = random.randint(0,5)
            if len(requests) > 0 and x > y:
                for request in requests:
                    if not request.getAgent() in self.flatten([self.fakeMediums, self.fakeSeers, self.myfakeSeers]) or request.getAgent() != wolf:
                        if request.getContent() in ['ESTIMATE']:
                            talk = self.Estimate(request.getTarget2())
                            if (talk != None):   return talk
                        if request.getContent() in ['VOTE']:
                            target = request.getTarget2()
                            self.willvote = target
                            self.isFollow = True
                            return ttf.vote(target)

        #VOTE宣言(明確な偽、自分への黒出し、最黒)
        target = self.MyTarget()
        if(self.willvote != target):
            self.willvote = target
            return ttf.vote(self.willvote)

        #ESTIMATE宣言
        if(self.myData.getTurn() < 10):
            talk = self.Estimate(self.willvote)
            if(talk != None):   return talk

        #REQUEST宣言
        if(self.isRequest or self.isRequestAll):
            fakeSeers   = self.alivefakeSeers + self.alivefakeMediums
            talk = self.Request(fakeSeers)
            if(talk != None):   return talk

        #BGCO
        if self.isBlacked and not self.isCall:
            self.isCall = True
            return ttf.comingout(self.agentIdx,'BODYGUARD')

        return ttf.over()

    def vote(self):

        target = self.MyTarget()
        if(target != None): return target

        target = self.getMaxLikelyWolfAll()
        if(target != None): return target

        return super(Villager,self).vote()

    def finish(self):
        super(Villager,self).finish()

    def setmyData(self,mydata):
        self.myData = mydata


    #扇動メソッド
    def Request(self,fakeSeers):

        x = random.randint(1,2)
        y = random.randint(1,10)

        #特定のプレイヤへのRequest
        if(self.isRequest):

            #黒出しされたとき，片占いに自分を占うように要求(p = 0.5)
            if x == 1 and self.isBlacked:
                self.isRequest = False
                if(self.realSeer != None):
                    return ttf.request('DIVINATION',self.realSeer,self.agentIdx)
                else:
                    for i in self.myData.getAliveSeerCOAgentList():
                        if len(fakeSeers) > 0 and i not in fakeSeers:
                            return ttf.request('DIVINATION',i,self.agentIdx)

            #真占い確定時，真占いに怪しそうな奴を占うよう要求(p = 0.2)
            elif y > 2 and self.realSeer != None:
                self.isRequest  = False
                cd = copy.deepcopy(self.candidate)
                removeAll(cd,self.myBlack)
                if len(cd) > 0:
                    target = self.myData.getMaxLikelyWolf(cd)
                    return ttf.request('DIVINATION',self.realSeer,target)

        if(self.isRequestAll):

            #偽確定時，5日目まで偽に投票するよう要求 p = 0.5
            #※5日まで->縄考慮のため
            if x == 0 and len(fakeSeers) > 0 and self.myData.getToday() < 6:
                for i in fakeSeers:
                    if self.myData.isAliveIndex(i):
                        self.isRequestAll = False
                        self.requestTarget = i
                        return ttf.request_all('VOTE',i)

            #怪しそうなやつに投票するよう要求 p = 0.2
            elif y == 0:
                self.isRequestAll = False
                if(len(self.candidate) > 0):
                    target = self.myData.getMaxLikelyWolf(self.candidate)
                    return ttf.request_all('DIVINATION',int(self.realSeer),int(target))

        return None

    def MyTarget(self):

        o_fake  = self.alivefakeSeers + self.alivefakeMediums

        #5日目まで偽確定を糾弾
        if(self.myData.getToday() < 6):
            if len(o_fake) > 0:
                target = self.myData.getMaxLikelyWolf(o_fake)
                return target
            if len(self.myalivefakeSeers) > 0:
                target = self.myData.getMaxLikelyWolf(self.myalivefakeSeers)
                return target

        #それ以外は候補者から選択
        if(len(self.candidate) > 0):
            target = self.myData.getMaxLikelyWolf(self.candidate)
            return target

        target  = self.myData.getMaxLikelyWolf(self.myData.getAliveAgentIndexList())
        return target

    def Estimate(self,target):
        rand_x = random.randrange(10)
        rand_y = random.randrange(5)

        if rand_x < rand_y:
            rand = random.randint(1,10)
            if(rand > 7):
                return ttf.estimate(target,'WEREWOLF')
            elif(rand == 8 and self.realSeer != None):
                return ttf.estimate(self.realSeer,'SEER')
            else:
                target = self.myData.getMaxLikelyVillAll()
                if(target != None): return ttf.estimate(target,'VILLAGER')

        return None


    #偽以外で占いCOが1人なら信仰
    def BeliefSeer(self):
        smap        = self.myData.getSeerCODataMap()
        seerlist    = self.myData.getAliveSeerCOAgentList()

        if(len(seerlist) > 1):
            self.removeAll(seerlist, self.fakeSeers)
            self.removeAll(seerlist, self.myfakeSeers)

            if(not self.isRealSeer):
                if(len(seerlist) == 1):
                    self.realSeer   = seerlist[0]
                    self.isRealSeer = True

            if(self.isRealSeer):
                if(len(seerlist) > 1):
                    self.isRealSeer = False
                    self.realSeer   = None
                    del self.myWhite[:]
                    del self.myBlack[:]

                elif(len(seerlist) == 1):
                    result = smap[seerlist[0]]
                    if(result != None):
                        self.myWhite    = result.getWhiteList()
                        self.myBlack    = result.getBlackList()
                        if self.realSeer in self.candidate:
                            self.candidate.remove(self.realSeer)
                        for i in self.myWhite:
                            if i in self.candidate:
                                self.candidate.remove(i)

    def removeAll(self,targetList, delMemberList):
        for idx in delMemberList:
            if idx in targetList:
                targetList.remove(idx)

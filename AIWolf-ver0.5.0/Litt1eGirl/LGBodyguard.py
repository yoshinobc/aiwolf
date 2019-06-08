# -*- coding: utf-8 -*-
import aiwolfpy
import aiwolfpy.templatetalkfactory as ttf
import aiwolfpy.templatewhisperfactory as twf

import numpy as np
import copy,random

import MyData_15 as md

import Baseplayers as bp

class Bodyguard(bp.BaseBodyguard):

    def __init__(self, agent_name):
        super(Bodyguard,self).__init__(agent_name)
        self.myguard    = None
        self.attacked   = None
        self.isGJ       = False
        self.isRealSeer = True
        self.isSeerguard    = False
        self.yesterdaySeer = []

        #信仰占い師関連
        self.realSeer     = None
        self.myWhite    = []
        self.myBlack    = []

        self.isCall     = False
        self.declare    = False         # 防衛対象を前日で明示したか

    def getName(self):
        return self.agent_name

    def update(self, game_info, talk_history, whisper_history, request):
        super(Bodyguard,self).update(game_info, talk_history, whisper_history, request)

    def initialize(self, game_info, game_setting):
        super(Bodyguard,self).initialize(game_info, game_setting)

    def dayStart(self):
        super(Bodyguard,self).dayStart()

        # reuquest は 1 日 1 回まで
        self.isRequest = True
        self.isRequestAll = True

        #護衛対象(占)が生存かつ襲撃失敗でGJ
        if(self.gameInfo['day'] > 1):
            self.attacked   = self.gameInfo['attackedAgent']
            indexlist       = self.myData.getAliveAgentIndexList()
            if (self.attacked == -1 and self.myguard in indexlist and self.isSeerguard):
                # self.mygurd は idx が格納されてる
                self.isGJ   = True
                self.realSeer = self.myguard

    def talk(self):
        self.isFollow = False

        # GJ 報告
        # CO した状況下で，GJ できていれば GJ 成功報告と guard 対象を伝える
        # 確率変動いる？
        if self.isGJ and self.isCall and self.declare and self.myguard != None:
            self.declare = False
            return ttf.guarded(self.myguard)

        # 扇動受けるメソッド
        requests = self.myData.getRequestTodayMe()
        if requests != None:
            wolf = self.myData.getMaxLikelyWolfAll()
            x = random.randint(0,3)
            y = random.randint(0,5)
            if len(requests) > 0 and x > y:
                for request in requests:
                    if not request.getAgent() in self.flatten([self.fakeMediums, self.fakeSeers, self.myfakeSeers]) or request.getAgent() != wolf:
                        if request.getContent() in ['VOTE']:
                            target = request.getTarget2()
                            self.isFollow = True
                            if self.willvote != target:
                                self.willvote = target
                                return ttf.vote(target)
                            else:
                                return ttf.over()

        #VOTE宣言(明確な偽、自分への黒出し、最黒)
        target = self.MyTarget()
        if(self.willvote != target):
            self.willvote = target
            return ttf.vote(self.willvote)


        #ESTIMATE宣言
        if(self.myData.getTurn() < 10):
            talk = self.Estimate(self.willvote)
            if(talk != None):   return talk

        #BGCO
        if(self.isBlacked and not self.isCall):
            self.isCall = True
            return ttf.comingout(self.agentIdx,'BODYGUARD')

        # 対抗 CO が出てきたとき限定で BGCO
        # probably 7/10
        x = random.randint(0,10)
        y = random.randint(0,5)
        if ( len(self.myData.getTodayBGCOAgentList()) > 0 and x > y  and not self.isCall):
            self.isCall = True
            return ttf.comingout(self.agentIdx, 'BODYGUARD')

        #REQUEST宣言
        if(self.isRequest or self.isRequestAll):
            fakeSeers   = self.alivefakeSeers + self.alivefakeMediums
            talk = self.Request(fakeSeers)
            if(talk != None):   return talk

        """
        def agree(talktype, day, id):
            return 'AGREE ' + talktype + ' day' + str(day) + ' ID:' + str(id)

        def disagree(talktype, day, id):
            return 'DISAGREE ' + talktype + ' day' + str(day) + ' ID:' + str(id)
        """

        # これから gurd する対象を伝える
        # 護衛対象明示 -> 真占い師確定時のみ
        x = random.randint(0,10)
        y = random.randint(0,5)
        if self.isCall and len(self.myData.getAliveSeerCOAgentList()) == 1 and self.isRealSeer and x > y:
            self.declare = True
            #return ttf.guard(self.guard()) ->guardは1日1度しか呼ばれないことを想定しています
            if self.myguard != None and self.myData.isAlive(self.myguard):
                return ttf.guard(self.myguard)


        return ttf.over()

    def vote(self):

        if self.isFollow and self.willvote != None:
            return self.willvote

        self.willvote = self.MyTarget()
        if self.willvote != None:
            return self.willvote

        return super(Bodyguard,self).vote()

    def finish(self):
        super(Bodyguard,self).finish()

    def guard(self):

        #GJリセッター
        reset = False

        exe= self.gameInfo["latestExecutedAgent"]
        if exe in self.candidate:   self.candidate.remove(exe)

        alive = self.myData.getAliveAgentIndexList()
        if exe in alive:    alive.remove(exe)

        voteTarget = self.MyTarget()

        #先日の護衛対象と投票対象が同じなら護衛解除
        if self.myguard == voteTarget:  self.myguard = None
        #真置き占い師と投票対象が同じなら真置き解除
        if self.realSeer == voteTarget:   self.realSeer  = None

        #if self.realSeer != self.myData.getMaxLikelyVillAll():    self.realSeer = None
        #if self.myguard != self.myData.getMaxLikelyVillAll():    self.myguard = None

        self.BeliefSeer()
        if self.isRealSeer and self.realSeer in alive:
            self.myguard = self.realSeer

        # 防衛対象がシロリストに入っている場合，GJ優先
        if(self.isGJ == True and self.myguard != voteTarget):

            #他の占い師が噛まれていればGJ解除
            if(self.attacked in self.yesterdaySeer):
                self.isGJ   = False
                reset       = True

            #生存していれば引き続き護衛
            if self.myguard in alive:   return self.myguard
            else:                       self.isGJ = False

        self.isSeerguard = False
        self.yesterdaySeer = self.myData.getAliveSeerCOAgentList()

        #占い師護衛優先
        if(len(self.myData.getAliveSeerCOAgentList()) > 0):
            guardList   = self.myData.getAliveSeerCOAgentList()

            #真置きしていればそいつ
            if(self.isRealSeer and self.realSeer in alive and not reset):
                self.myguard = self.realSeer
                self.isSeerguard = True
                return self.myguard

			#真占が出ていなければ最も村らしいものを選ぶ
            if(not self.isRealSeer and not reset):
                self.myguard    = self.myData.getMaxLikelyVill(guardList)
                self.isSeerguard = True
                if(self.myguard != exe):
                    return self.myguard

        #他占い噛みによるGJ解除後(reset == true)は占い師を守らない

        #霊媒護衛
        if(len(self.myData.getAliveMediumCOAgentList()) > 0):
            guardList   = self.myData.getAliveMediumCOAgentList()
            self.removeAll(guardList,self.alivefakeMediums)
            if(len(guardList) > 0):
                self.myguard    = self.myData.getMaxLikelyVill(guardList)
                if(self.myguard != exe):
                    return self.myguard

        #その他白出し
        if(len(self.myWhite) > 0):
            guardList = []
            for i in self.myWhite:
                if i in alive:  guardList.append(i)
            if(len(guardList) > 0):
                self.myguard    = self.myData.getMaxLikelyVill(guardList)
                return self.myguard

        #その他村人
        if(len(self.candidate) > 0):
            target  = self.myData.getMaxLikelyVill(self.candidate)
            self.myguard = target
            return self.myguard

        return super(Bodyguard,self).guard()

    #偽以外で占いCOが1人なら信仰
    def BeliefSeer(self):
        smap        = self.myData.getSeerCODataMap()
        seerlist    = self.myData.getSeerCOAgentList()

        if(len(seerlist) > 1):
            self.removeAll(seerlist, self.fakeSeers)
            self.removeAll(seerlist, self.myfakeSeers)

            if(not self.isRealSeer):
                if(len(seerlist) == 1):
                    self.realSeer     = seerlist[0]
                    self.isRealSeer = True

            if(self.isRealSeer):

                #占い真置き後、新たな占いが出たら真置き解除
                if(len(seerlist) > 1):
                    self.isRealSeer = False
                    self.realSeer     = None
                    del self.myWhite[:]
                    del self.myBlack[:]

                elif(len(seerlist) == 1):
                    result = smap[seerlist[0]]
                    self.realSeer = seerlist[0]
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

    def setmyData(self,mydata):
        self.myData = mydata

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

        if len(self.candidate) > 0:
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
                self.removeAll(cd,self.myBlack)
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

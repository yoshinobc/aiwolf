# -*- coding: utf-8 -*-
import aiwolfpy
import aiwolfpy.templatetalkfactory as ttf
import aiwolfpy.templatewhisperfactory as twf

import numpy as np
import copy,random

import MyData_15 as md

import Baseplayers as bp

class Medium(bp.BaseMedium):

    def __init__(self, agent_name):
        super(Medium,self).__init__(agent_name)
        self.willvote   = None
        self.isCall     = True
        self.yesterdayVote = "" # targetIdx を格納したい

        self.realSeer   = None

    def getName(self):
        return self.agent_name

    def update(self, game_info, talk_history, whisper_history, request):
        super(Medium,self).update(game_info, talk_history, whisper_history, request)

    def initialize(self, game_info, game_setting):
        super(Medium,self).initialize(game_info, game_setting)


    def dayStart(self):
        super(Medium,self).dayStart()
        '''
        BaseMediumから
        self.myBlack   :霊媒結果黒(人狼)だったプレイヤのリスト
        self.myWhite   :霊媒結果白(人間)だったプレイヤのリスト
        self.myResult  :霊媒結果のmap
        self.willSay   :まだいっていない霊媒結果のmap
        '''

        '''
        "reuquest は 1 日 1 回まで"
        ->  talkは毎ターン呼ばれます．1日1回の制限を付ける場合は，selfをつけてdayStartでresetしてください．
        '''
        self.isRequest = True
        self.isRequestAll = True

        self.willvote   = None
        self.isCall     = True


    def talk(self):
        self.isFollow   = False

        if not self.isCO:
            # COTrigerがTrueならCO
            if self.COTrigger():
                self.isCO = True
                #print("44444444444444")
                return ttf.comingout(self.agentIdx,self.role)

        #COしたら言っていない結果がなくなるまでdivined
        if(self.isCO and len(self.willSay) > 0):
            #print("&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&")
            #print(self.willSay)
            target = self.willSay[0]['target']
            result = self.willSay[0]['result']
            self.willSay.pop(0)
            return ttf.identified(int(target),result)

        # 扇動受けるメソッド
        requests = self.myData.getRequestTodayMe()
        if requests != None:
            wolf = self.myData.getMaxLikelyWolfAll()
            x = random.randint(0, 10)
            y = random.randint(0, 5)
            if len(requests) >0 and x > y:
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

        target = self.MyTarget()
        #ESTIMATE宣言
        if(self.myData.getTurn() < 10):
            talk = self.Estimate(target)
            if(talk != None):   return talk

        # VOTE宣言(明確な偽、自分への黒出し、最黒)
        if (self.willvote != target):
            self.willvote = target
            return ttf.vote(self.willvote)

        # 扇動するメソッド
        # probably 2/5
        '''
        確率もう少し低くてもオッケーです
        1日に0~1回やれば十分
        -> CLEA
        '''
        x = random.randint(0, 3)
        y = random.randint(0, 5)
        z = random.randint(0, 1)
        candidate = self.myData.getAliveSeerCOAgentList()
        if candidate != None:
            maxLikelySeer = False
            if len(self.fakeSeers) > 0:
                for index in self.myData.getAliveAgentIndexList():
                    if index in self.fakeSeers:
                        continue
                    elif index in candidate:
                        continue
                    else:
                        maxLikelySeer = index
            if x > y and z == 0 and self.isRequest and maxLikelySeer and target:
                return ttf.request('DIVINATION', int(maxLikelySeer), int(target))
            #elif x > y and z == 1 and self.isRequestAll and target:
            #    return ttf.request_all('VOTE', int(target))

        talk = self.Request(self.fakeSeers)
        if talk != None:    return talk

        return ttf.over()

    def vote(self):

        if(self.isFollow and self.willvote != None):
            self.yesterdayVote = self.willvote
            return self.willvote

        self.myfakeSearch()
        target = self.MyTarget()
        self.yesterdayVote = target
        if(target != None): return target

        return super(Medium,self).vote()

    #COするか否か
    def COTrigger(self):
        # 真偽両方の占い師の結果がおかしくなったタイミングで即座CO or 吊られそうなとき CO
        # それまでは村人 or 狩人擬態

        if self.isCO:
            self.isCall = False
            return False
        #self.isCall = False

        # 狂人による霊媒師語りに対する対抗 CO
        if len(self.myData.getAliveMediumCOAgentList()) > 0:
            return True

        # 吊られそうなときCO
        if self.myData.getTurn() > 10 and self.agentIdx == self.myData.getMaxLikelyExecuteAgentAll() :
            return True

        # 真偽両方の占い師の結果がおかしくなったタイミング
        # 占い師黒だしに対してシロ死体がでたらCOしましょう！
        seerList = self.myData.getAliveSeerCOAgentList()
        if len(self.med_fakeSeers) > 0:
            for i in range(len(self.med_fakeSeers)):
                seerResultList = self.myData.getSeerResultsList(self.med_fakeSeers[i])
                # seerResultList -> {targetIdx:isBlack}
                if seerResultList == None:
                    continue
                for agent in seerResultList:
                    if not agent in self.myData.getAliveAgentIndexList():
                        # 占われた対象が死んでいたら
                        # 占いがシロで，自分の結果がクロ
                        if agent in self.myBlack and not seerResultList[agent]:
                            return True
                        # 占いがクロで，自分の結果がシロ
                        elif agent in self.myWhite and seerResultList[agent]:
                            return True

        if len(seerList) > 1 or len(self.med_fakeSeers) > 0:
            # 2 日目以降は黒死体がでたら CO
            if (self.myData.getToday() >= 2):
                self.isCall = False
                if (len(self.myBlack) > 0):
                    return True
                else:
                    return False
        # 前日，自分が黒だと思って投票したプレイヤが白死体ならば CO
        if self.yesterdayVote == self.myData.getExecutedAgent() and self.willSay[0]['result'] == 'VILLAGER':
            self.isCall = False
            return True

        """
        #2日目黒で必ずCO
        if(self.myData.getToday() == 2):
            self.isCall     = False
            if(len(self.myBlack) > 0):
                self.isCO = True
                return True
            else:
                rand = random.randrange(2)
                if(rand < 1):
                    self.isCO = True
                    return True
                else:
                    return False
        """

        #4日目必ずCO
        if(self.myData.getToday() == 4):
            self.isCO = True
            return True

        return False

    def finish(self):
        super(Medium,self).finish()

    def setmyData(self,mydata):
        self.myData = mydata

    def removeAll(self,targetList, delMemberList):
        for idx in delMemberList:
            if idx in targetList:
                targetList.remove(idx)

    def MyTarget(self):

        #偽探し
        o_fake  = self.alivefakeSeers + self.alivefakeMediums
        myfake  = copy.deepcopy(self.myfakeSeers)

        #客観的偽
        if len(o_fake) > 0:
            target = self.myData.getMaxLikelyWolf(o_fake)
            return target

        #霊媒結果による矛盾偽
        myfake = self.med_alivefakeSeers + self.med_alivefakeMediums
        if len(myfake) > 0:
            target = self.Target(myfake)
            if target != None:  return target

        #主観的偽
        if len(self.myalivefakeSeers) > 0:
            target = self.myData.getMaxLikelyWolf(self.myalivefakeSeers)
            if target != None:  return target

        else:
            target = self.Target(self.candidate)
            if target != None:  return target

        target  = self.myData.getMaxLikelyWolfAll()
        return target

    def CheckSeer(self):

        seerlist = self.myData.getSeerCOAgentList()
        self.removeAll(seerlist,self.fakeSeers)
        self.removeAll(seerlist,self.med_fakeSeers)

        if len(seerlist) == 1:
            self.realSeer = seerlist[0]
        else:
            self.realSeer = None

    def Estimate(self,target):
        rand_x = random.randrange(10)
        rand_y = random.randrange(5)
        self.CheckSeer()

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

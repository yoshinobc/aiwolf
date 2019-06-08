# coding=utf-8
import aiwolfpy
import aiwolfpy.templatetalkfactory as ttf
import aiwolfpy.templatewhisperfactory as twf

import numpy as np
import copy,math,random

import MyData_15 as md

import Baseplayers as bp

class Seer(bp.BaseSeer):

    def __init__(self, agent_name):
        super(Seer,self).__init__(agent_name)

    def getName(self):
        super(Seer,self).getName()

    def update(self, game_info, talk_history, whisper_history, request):
        super(Seer,self).update(game_info, talk_history, whisper_history, request)

    def initialize(self, game_info, game_setting):
        super(Seer,self).initialize(game_info, game_setting)

    def dayStart(self):
        super(Seer,self).dayStart()
        self.willvote   = None

    def talk(self):

        #COTrigerがTrueならCO
        if(self.COTrigger()):
            self.isCO = True
            return ttf.comingout(self.agentIdx,'SEER')

        #COしたら言っていない結果がなくなるまでdivined
        if(self.isCO and len(self.willSay) > 0):
            target = self.willSay[0]['target']
            result = self.willSay[0]['result']
            self.willSay.pop(0)
            return ttf.divined(target,result)

        #VOTE宣言(明確な偽、自分への黒出し、最黒)
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
        if(target != None): return target

        return super(Seer,self).vote()

    def divine(self):
        exe = self.gameInfo['latestExecutedAgent']

        if (exe in self.divineList):
            self.divineList.remove(exe)

        #占い候補から最も狼らしい者
        if(len(self.divineList) > 0):
            target = self.myData.getMaxLikelyWolf(self.divineList)
            if(target != None): return target

        return super(Seer,self).divine()

    def finish(self):
        super(Seer,self).finish()

    def setmyData(self,mydata):
        self.myData = mydata

    '''
    以下，固有メソッド
    '''

    #投票や発言のターゲットを決める
    def MyTarget(self):

        if(len(self.aliveBlack) > 0):
            target = self.Target(self.aliveBlack)
            if(target != None):
                return target

        o_fake  = self.alivefakeSeers + self.alivefakeMediums

        #客観的偽
        if len(o_fake) > 0:
            target = self.myData.getMaxLikelyWolf(o_fake)
            return target

        myfake = self.seer_alivefakeSeers + self.seer_alivefakeMediums
        if len(myfake) > 0:
            target = self.myData.getMaxLikelyWolf(myfake)
            return target

        else:
            target = self.Target(self.candidate)
            if(target != None):
                return target

        target = self.myData.getMaxLikelyWolfAll()
        return target

    #ESTIMATE宣言のアルゴリズム
    def Estimate(self,target):
        rand_x = random.randrange(10)
        rand_y = random.randrange(5)
        if(rand_x < rand_y):
            rand = random.randint(1,3)
            if(rand > 1):
                return ttf.estimate(target,'WEREWOLF')
            else:
                target = self.myData.getMaxLikelyVillAll()
                if(target != None): return ttf.estimate(target,'VILLAGER')

        return None

    #COタイミング
    def COTrigger(self):

        if self.isCO:   return False

        #対抗が2人以上の場合はCO
        #(1人の場合は狂人→襲撃対象になってくれる可能性)
        seerlist    = self.myData.getSeerCOAgentList()
        if(len(seerlist) > 1):      return True

        #つられそうなときはCO
        exe     = self.myData.getMaxLikelyExecuteAgentAll()
        if(exe == self.agentIdx):     return True

        #黒が出ていたら2日目に必ずCO
        if len(self.myBlack) > 0 and self.myData.getToday() > 1:
            return True

        #1日目CO
        if self.myData.getToday() == 1:
            rand_x = random.randrange(10)
            threshhold = int(20 / math.pi * math.atan(self.myData.getTurn()))
            rand_y = random.randrange(threshhold if threshhold > 1 else 1)
            if rand_x >= rand_y:
                return False
            else:
                self.isCO = True
                return True

        #2日目以降高確率CO
        if self.myData.getToday() > 1:
            rand_x = random.randrange(10)
            threshhold = int(20 / math.pi * math.atan(self.myData.getTurn()))
            rand_y = random.randrange(threshhold if threshhold > 1 else 1)
            if rand_x <= rand_y:
                return False
            else:
                self.isCO = True
                return True

    #候補者から対象を絞り込む
    def Target(self,mylist):

        for i in mylist:
            if(not self.myData.isAliveIndex(i)):
                mylist.remove(i)

        if(len(mylist) == 1):
            target = mylist[0]
            return target

        if(len(mylist) > 1):
            target = self.myData.getMaxLikelyWolf(mylist)
            return target

        return None

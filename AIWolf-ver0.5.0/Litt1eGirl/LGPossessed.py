# coding=utf-8
import aiwolfpy
import aiwolfpy.templatetalkfactory as ttf
import aiwolfpy.templatewhisperfactory as twf

import numpy as np
import copy,math,random

import MyData_15 as md

import Baseplayers as bp

class Possessed(bp.BasePossessed):

    def __init__(self, agent_name):
        super(Possessed,self).__init__(agent_name)
        self.willvote   = None
        self.realSeer   = None

        #偽確定リスト
        self.fakeSeer   = []
        self.fakeMedium = []

        self.werewolves = []
        self.divineList = []
        self.myDivine   = []
        self.aliveList  = []
        self.divined    = {}
        self.doDivine = True
        self.isSeerCO = False

        self.hiding = False

        self.isBlackCO = False
        self.doPP = False


        # 真占の占結果 {agentIdx:isBlack} 辞書型のリスト
        self.realSeerDivinedList = []

    def getName(self):
        return self.agent_name

    def update(self, game_info, talk_history, whisper_history, request):
        super(Possessed,self).update(game_info, talk_history, whisper_history, request)

        if self.isOneTimeCall:
            self.checkseer()        #真占調査
            self.myfakeSearch()     #主観的偽者

    def initialize(self, game_info, game_setting):
        super(Possessed,self).initialize(game_info, game_setting)
        self.playerNum  = game_setting['playerNum']
        self.divineList = self.myData.AgentToIndexList(self.myData.getAliveAgentList())#占い候補リスト
        self.divineList.remove(self.agentIdx)
        self.blacktimes = 0
        self.aliveBlack = []
        self.aliveWhite = []

    def dayStart(self):
        super(Possessed,self).dayStart()

        self.willvote   = None

        self.isFollow = False

        self.doDivine = True
        attacked = self.myData.getAttackedAgent()
        if attacked in self.divineList: self.divineList.remove(attacked)

        #生存者の更新（自分以外）
        self.aliveList = self.myData.getAliveAgentIndexList()
        self.aliveList.remove(self.agentIdx)

        for i in self.aliveBlack:
            if(not i in self.myData.getAliveAgentIndexList()):   self.aliveBlack.remove(i)
        for i in self.aliveWhite:
            if(not i in self.myData.getAliveAgentIndexList()):   self.aliveWhite.remove(i)
            if(self.myData.getToday() > 4 and i in self.candidate):    self.candidate.remove(i)

    def talk(self):

        if self.isSeerCO and self.doDivine:
            if self.myData.getToday() > 1:
                execute = self.myData.getExecutedAgent()
                if execute in self.divineList:  self.divineList.remove(execute)
            target, result = self.PossessedDivine()
            self.myDivine.append(target)
            self.doDivine = False

        #2日目以降COの場合，Divineを規定回数するために
        if self.isSeerCO:
            if len(self.divined) < self.myData.getToday():
                target, result = self.PossessedDivine()
                self.myDivine.append(target)

        '''
        if self.myData.getToday() == 1 and self.doDivine:
            if self.myData.getTurn() == 1:
                execute = self.myData.getExecutedAgent()
                if (execute in self.divineList): self.divineList.remove(execute)
                target, result = self.PossessedDivine()
                self.myDivine.append(target)
                self.doDivine = False
        elif(self.myData.getToday() >=2 and self.doDivine):
            execute = self.myData.getExecutedAgent()
            if (execute in self.divineList): self.divineList.remove(execute)
            target, result = self.PossessedDivine()
            self.myDivine.append(target)
            self.doDivine = False
        '''

        #COTrigerがTrueならCO
        if(self.COTrigger() and not self.isSeerCO):
            self.isSeerCO = True
            return ttf.comingout(self.agentIdx,'SEER')

        #PPTrigerがTrueなら確率で人狼・狂人CO
        if (self.PPTrigger() and not self.isBlackCO):
            self.isBlackCO = True
            x = random.randint(1,2)
            if(x == 1):
                return ttf.comingout(self.agentIdx,'WEREWOLF')
            else:
                return ttf.comingout(self.agentIdx, 'POSSESSED')

        #PPTriggerがTrueなら生き残り全員に黒出す
        if (self.PPTrigger() and len(self.aliveList) > 0):
            self.doPP = True
            target = self.aliveList[0]
            self.aliveList.pop(0)
            return ttf.divined(target, 'WEREWOLF')

        #COしたら言っていない結果がなくなるまでdivined
        if(self.isSeerCO and len(self.myDivine) > 0):
            target  = self.myDivine[0]
            result  = self.divined[target]
            self.myDivine.pop(0)
            return ttf.divined(target,result)


        #VOTE宣言(明確な偽、自分への黒出し、最黒)
        target = self.MyTarget()
        if(self.willvote != target):
            self.willvote = target
            return ttf.vote(self.willvote)

        #狼陣営と予測された人からのリクエスト受付
        requests = self.myData.getRequestTodayMe()
        if requests != None:
            wolf = self.myData.getMaxLikelyWolfAll()
            x = random.randint(0, 10)
            y = random.randint(0,2)
            if len(requests) > 0 and x > y:
                for request in requests:
                    if (request.getAgent() in self.flatten([self.fakeMediums, self.fakeSeers, self.fakeMedium, self.fakeSeer,self.werewolves]) or request.getAgent() == wolf): #2重リストやとうまくいかないっぽいのでフラットにする
                        if request.getContent() in ['ESTIMATE']:
                            talk = self.Estimate(request.getTarget2())
                            if (talk != None):   return talk
                        if request.getContent() in ['VOTE']:
                            target = request.getTarget2()
                            if(target != self.agentIdx):
                                self.willvote = target
                                self.isFollow = True
                                return ttf.vote(target)

        #ESTIMATE宣言
        if(self.myData.getTurn() < 10):
            talk = self.Estimate(self.willvote)
            if(talk != None):   return talk

        '''
        #BGCO
        if(self.blacked and not self.isCall and not self.isSeerCO):
            self.isCall = True
            return ttf.comingout(self.agentIdx,'BODYGUARD')
        '''

        return ttf.over()

    def vote(self):
        aliveList = self.myData.getAliveAgentIndexList()

        #target = self.MyTarget()
        #if(target != None): return target

        # リクエスト受付
        if (self.isFollow and self.willvote != None):
            return self.willvote

        # 生き残り8人以上のときは黒に投票
        if (len(aliveList) > 7):
            target = self.MyTarget()
            if (target != None): return target

        # 終盤は最白に投票
        else:
            target = self.myData.getMaxLikelyVillAll()
            if (target != None): return target

        '''
        self.willvote = None

        #黒出し、客観的偽、対抗、最黒
        if(len(self.aliveBlack) > 0):
            if(self.Target(self.aliveBlack)):      return self.willvote
        if(len(fake_m) > 0 or len(fake_s) > 0):
            fake = fake_m + fake_s
            if(self.Target(fake)):      return self.willvote
        elif(self.realSeer != None):
            if(self.myData.isAliveIndex(self.realSeer)):
                return self.realSeer
        else:
            if(self.Target(self.candidate)):    return self.willvote
        '''
        return super(Possessed,self).vote()

    def PossessedDivine(self):

        #daystartで呼び出してるなら意味ない
        #占い対象決定
        if(self.myData.getToday() == 1):
            #1日目対抗COが1人いれば0.2の確率で対抗黒出し
            if(len(self.myData.getSeerCOAgentList()) == 2):
                rand = random.randint(1,10)
                if(rand < 3 and self.realSeer != None):
                    self.divineList.remove(self.realSeer)
                    self.divined.update({self.realSeer:'WEREWOLF'})
                    self.aliveBlack.append(self.realSeer)
                    return self.realSeer,'WEREWOLF'


        rand = random.randint(1,10)

        #4割最白に黒出し(3回まで)、2割最白に白だし，4割最黒に白出し
        if(len(self.divineList) > 0):
            if(rand > 6 and self.blacktimes < 3):
                target = self.myData.getMaxLikelyVill(self.divineList)
                self.divineList.remove(target)

                #襲撃されたプレイヤは白出し
                if(target in self.myData.getAttackedAgentList()):
                    self.divined.update({target:'HUMAN'})
                    #self.aliveWhite.append(target)
                    return target,'HUMAN'
                else:
                    self.divined.update({target:'WEREWOLF'})
                    self.aliveBlack.append(target)
                    self.blacktimes += 1
                    return target,'WEREWOLF'

            elif(rand > 4 and rand <=6):
                target = self.myData.getMaxLikelyVill(self.divineList)
                self.divineList.remove(target)
                self.divined.update({target: 'HUMAN'})
                self.aliveWhite.append(target)
                return target, 'HUMAN'


            else:
                target = self.myData.getMaxLikelyWolf(self.divineList)
                self.divineList.remove(target)
                self.divined.update({target:'HUMAN'})
                self.aliveWhite.append(target)
                return target,'HUMAN'


        self.divined.update({self.agentIdx:'HUMAN'})
        return self.agentIdx,'HUMAN'

    #COするか否か
    def COTrigger(self):
        # Trigger条件はここで調整可能。

        if(self.isSeerCO): return False

        #占いリストに人がいない場合はFalseを返す
        #if(len(self.myDivine) == 0):
        #    return False

        # 対抗が1人以上の場合はCO(占い1進行は許さぬ)
        if(self.myData.getToday() == 1 and self.myData.getTurn() >= 2):
            seerlist = self.myData.getSeerCOAgentList()
            if (len(seerlist) == 1):
                return True
            #占い2出ている場合は
            elif(len(seerlist) >=2) :
                rand = random.randint(1,10)
                if(rand <=8):
                    return True
                else:
                    self.hiding = True
                    return False

        # つられそうなときはCO
        exe = self.myData.getMaxLikelyExecuteAgentAll()
        if (exe == self.agentIdx):     return True

        if (self.hiding): return False

        # 黒が出ていたら2日目に必ずCO
        if len(self.aliveBlack) > 0 and self.myData.getToday() > 1:
            return True

        # 1日目CO
        if self.myData.getToday() == 1:
            rand_x = random.randrange(10)
            threshhold = int(20 / math.pi * math.atan(self.myData.getTurn()))
            rand_y = random.randrange(threshhold if threshhold > 1 else 1)
            if rand_x >= rand_y:
                return False
            else:
                return True

            # 2日目以降高確率CO
        if self.myData.getToday() > 1:
            rand_x = random.randrange(10)
            threshhold = int(20 / math.pi * math.atan(self.myData.getTurn()))
            rand_y = random.randrange(threshhold if threshhold > 1 else 1)
            if rand_x <= rand_y:
                return False
            else:
                return True

        # それ以外は占いCOしない
        return False

    #自分以外の占いＣＯがいれば真占とする
    def checkseer(self):
        seerlist = copy.deepcopy(self.myData.getSeerCOAgentList())
        if(self.isSeerCO):
            seerlist.remove(self.agentIdx)
        if(len(seerlist) == 1):
            self.realSeer = seerlist[0]

    #偽者調査
    def myfakeSearch(self):

        #偽占い師=狼探し
        seerlist = copy.deepcopy(self.myData.getSeerCOAgentList())

        if(self.agentIdx in seerlist):  seerlist.remove(self.agentIdx)
        if(self.realSeer in seerlist):  seerlist.remove(self.realSeer)

        if(len(seerlist) > 0):
            smap = self.myData.getSeerCODataMap()

            for i in seerlist:

                #真占確定時、自分と真占以外の占い師は狼
                if(self.realSeer != None):
                    if i not in self.fakeSeer:
                        self.fakeSeer.append(i)
                        self.werewolves.append(i)

                #真占い未確定時、自分に黒出しした占い師は狼
                #真占の虚偽発言は考慮しない
                else:
                    mymap   = smap[i]
                    black   = mymap.getBlackList()
                    if(self.agentIdx in black and i not in self.fakeSeer):
                        self.fakeSeer.append(i)
                        self.werewolves.append(i)

        #偽霊媒=狼探し
        mmap = self.myData.getMediumCODataMap()

        if(len(mmap) > 0 and self.realSeer != None):
            allmap  = self.myData.getSeerCODataMap()
            smap    = allmap[self.realSeer]
            black_s = smap.getBlackList()
            white_s = smap.getWhiteList()

            for k,v in mmap.items():
                black   = v.getBlackList()
                white   = v.getWhiteList()

                #真占の白出しに黒を出していたら偽者
                if(len(black) > 0):
                    for i in black:
                        if(i in white_s and i not in self.fakeMedium):
                            self.fakeMedium.append(k)
                            self.werewolves.append(k)

                #真占の黒出しに白を出していたら偽者
                if(len(white) > 0):
                    for i in white:
                        if(i in black_s and i not in self.fakeMedium):
                            self.fakeMedium.append(k)
                            self.werewolves.append(k)

    def finish(self):
        super(Possessed,self).finish()

    def setmyData(self,mydata):
        self.myData = mydata

    def MyTarget(self):


        #VOTE宣言(黒出し、客観的偽、対抗、最黒)
        if(len(self.aliveBlack) > 0):
            target = self.Target(self.aliveBlack)
            if target != None:
                return target

        o_fake  = self.alivefakeSeers + self.alivefakeMediums

        if len(o_fake) > 0:
            target = self.myData.getMaxLikelyVill(o_fake)
            if target != None:
                return target

        elif(self.realSeer != None and self.myData.isAliveIndex(self.realSeer)):
            target = self.realSeer
            return target
        else:
            target = self.Target(self.candidate)
            if target != None:
                return target

        target = self.myData.getMaxLikelyWolfAll()
        return target

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

    def Target(self,mylist):

        for i in mylist:
            if(not self.myData.isAliveIndex(i)):
                mylist.remove(i)

        if(len(mylist) > 0):
            target = self.myData.getMaxLikelyWolf(mylist)
            return target

        return None


    def PPTrigger(self):
        aliveList = self.myData.getAliveAgentIndexList()
        #狼陣営が確定で過半数になる
        if(len(aliveList) * 1.0 / (len(self.werewolves) + 1) <= 2):
            return True
        #生き残りが4人以下の場合
        elif(len(aliveList) <= 4):
            return True
        return False

    #2重のリストをフラットにする関数
    def flatten(self,nested_list):
        return [e for inner_list in nested_list for e in inner_list]

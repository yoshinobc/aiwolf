# -*- coding: utf-8 -*-
import aiwolfpy
import aiwolfpy.templatetalkfactory as ttf
import aiwolfpy.templatewhisperfactory as twf

import numpy as np
import copy,random

import MyData_15 as md

import Baseplayers as bp

class Werewolf(bp.BaseWerewolf,bp.BasePossessed):

    def __init__(self, agent_name):
        super(Werewolf,self).__init__(agent_name)

        self.seerlist   = []    #狼でも狂人でもない生存占い師リスト
        self.maybeSeer  = None  #狼に黒出しした占い師
        self.seer       = None  #真占い師
        self.mediumlist = []    #狼でも狂人でもない生存霊媒師リスト
        self.medium     = None  #真霊媒師
        self.bglist     = []    #狼でも狂人でもない生存狩人リスト
        self.possessed  = None  #狂人
        self.isGJ       = False

        #Whisper回数制限
        self.COend                = False
        self.attackend            = False
        self.voteend              = False
        self.estimateSeerend      = False
        self.estimateMediumend    = False
        self.estimatePossessedend = False

        #CO被りチェッカーフラグ
        self.SeerDeclareTrigger   = False
        self.COchecked            = False

        #占いで使うリストなど
        self.myDivine = []
        self.aliveList = []
        self.divined = {}
        self.myAliveblack  = []
        self.isSeerDeclare = False
        self.isSeerCO = False
        self.count = 0

        self.willattack = None

        self.isCall     = False


    def getName(self):
        return self.agent_name

    def update(self, game_info, talk_history, whisper_history, request):
        super(Werewolf,self).update(game_info, talk_history, whisper_history, request)

        if self.isOneTimeCall:
            self.checkPossessed()   #狂人調査
            self.checkReal()    #真占い調査


    def initialize(self, game_info, game_setting):
        super(Werewolf,self).initialize(game_info, game_setting)
        self.divineList = self.myData.AgentToIndexList(self.myData.getAliveAgentList())  # 占い候補リスト
        self.divineList.remove(self.agentIdx)
        self.blacktimes = 0

    def dayStart(self):
        super(Werewolf,self).dayStart()
        self.willattack = None
        # reuquest は 1 日 1 回まで
        self.isRequest = True
        self.isRequestAll = True
        self.attackend            = False
        self.voteend              = False
        self.estimateSeerend      = False
        self.estimateMediumend    = False
        self.estimatePossessedend = False

        self.count = 0
        if (self.myData.getToday() != 0):
            target, result = self.WerewolfDivine()
            self.myDivine.append(target)

        #占い候補から処刑者削除
        if (self.gameInfo['day'] > 1):
            execute = self.myData.getExecutedAgent()
            if (execute in self.divineList): self.divineList.remove(execute)
            if (execute in self.myAliveblack): self.myAliveblack.remove(execute)
            attacked = self.myData.getAttackedAgent()
            if (attacked in self.divineList): self.divineList.remove(attacked)
            if (attacked in self.myAliveblack): self.myAliveblack.remove(attacked)


        #GJcheck
        if(self.myData.getToday() > 1):
            result          = self.gameInfo['latestExecutedAgent']
            self.attacked   = self.gameInfo['latestAttackVoteList']
            self.isGJ = False

            #襲撃対象が生存でGJ
            if(result == -1 and self.myData.isAliveIndex(self.attacked)):
                self.isGJ = True



    def talk(self):

        self.isFollow = False

        # COTrigerがTrueならCO
        if (self.SeerCOTrigger() and not self.isSeerCO):
            self.isSeerCO = True
            return ttf.comingout(self.agentIdx, 'SEER')


        # COしたら言っていない結果がなくなるまでdivined
        if (self.isSeerCO and len(self.myDivine) > 0):
            target = self.myDivine[0]
            result = self.divined[target]
            self.myDivine.pop(0)
            if(target != self.agentIdx):
                return ttf.divined(target, result)

        # 扇動受けるメソッド
        requests = self.myData.getRequestTodayMe()
        if requests != None:
            x = random.randint(0,3)
            y = random.randint(0,5)
            if len(requests) > 0 and x > y:
                for request in requests:
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
            if(not self.isSeerCO): #占いCOしていない場合
                fakeSeers   = self.alivefakeSeers + self.alivefakeMediums
                talk = self.Request(fakeSeers)
                if(talk != None):   return talk

        return ttf.over()

    def vote(self):

        if (self.isFollow and self.willvote != None):
             return self.willvote

        aliveList = self.myData.getAliveAgentIndexList()

        # 生き残り8人以上のときは黒に投票
        if len(aliveList) > 7:
            target = self.MyTarget()
            if (target != None): return target

        # 終盤は最白に投票
        #生存7人以下 狼3匹で最白
        if len(self.aliveWerewolves) == 3 and len(aliveList) < 8:
            target = self.myData.getMaxLikelyVillAll()
            if (target != None): return target

        #生存5人以下 狼2匹以上で狼に合わせる
        if len(self.aliveWerewolves) > 1 and len(aliveList) < 3:
            votemap = self.myData.getVoteMap()
            for k,v in votemap:
                if k in self.aliveWerewolves:
                    if v not in self.aliveWerewolves and v != self.agentIdx and v != -1:
                        target = v
            return target

        #生存3人以下は便乗の方がよい
        if len(aliveList) < 3:
            target = self.myData.getMaxLikelyExecuteAgentAll()
            if (target != None): return target

        target = self.myData.getMinLikelyExecuteAgentAll()
        return target

    def whisper(self):

        #print(self.myData.getTurn())

        if(self.myData.getToday() == 0 and self.COend == False):
            self.COend = True
            rand=random.randint(1,100)
            if(rand < 6):
                self.SeerDeclareTrigger = True
                return twf.comingout(self.agentIdx,'SEER')
            else:
                self.SeerDeclareTrigger = False
                self.isSeerDeclare = False
                return twf.comingout(self.agentIdx,'VILLAGER')

        #CO被りチェッカー
        if(self.SeerDeclareTrigger == True):
        #if(self.SeerDeclareTrigger == True and self.COchecked == False):
        #    self.COchecked =True

            #CO宣言時，他に占いCOが居れば村にスライド
            if(len(self.myData.getWhisperData().getWillSeerCOList()) > 1):
                self.isSeerDeclare = False
                return twf.comingout(self.agentIdx,'VILLAGER')
            #他の狼も村スライドしたら，再び占い師CO
            elif(not self.isSeerDeclare):
                self.isSeerDeclare = True
                return twf.comingout(self.agentIdx,'SEER')


        #襲撃対象を決定・宣言
        if not self.attackend:

            self.attackend = True

            if(self.seer != None and self.seer != self.agentIdx):
                attacktarget = self.seer
                return twf.attack(attacktarget)

            attacktarget = self.Attacktarget(None)
            if self.willattack != attacktarget and attacktarget != None:
                self.willattack = attacktarget
                return twf.attack(attacktarget)
            else:
                attacktarget = self.myData.getMinLikelyExecuteAgent(self.aliveVillagers)
                if self.willattack != attacktarget and attacktarget != None:
                    self.willattack = attacktarget
                    return twf.attack(attacktarget)
                else:
                    attacktarget  = random.choice(self.aliveVillagers)
                    return twf.attack(attacktarget)

        if not self.voteend:
            self.voteend = True
            votetarget = self.MyTarget()
            if self.willvote != votetarget and votetarget != None:
                self.willvote = votetarget
                return twf.vote(votetarget)

        #ESTIMATE共有
        if self.seer != None and not self.estimateSeerend and self.seer != self.agentIdx:
            self.estimateSeerend = True
            return twf.estimate(self.seer,'SEER')
        if self.medium != None and not self.estimateMediumend and self.medium != self.agentIdx:
            self.estimateMediumend = True
            return twf.estimate(self.medium,'MEDIUM')
        if self.possessed != None and not self.estimatePossessedend:
            self.estimatePossessedend = True
            return twf.estimate(self.possessed,'POSSESSED')

        if(self.attackend == True and self.voteend == True):
            if(self.COend == True):
                return twf.over()

        return twf.over()

    def attack(self):
        exe = self.gameInfo['latestExecutedAgent']

        #Whisperで対象を宣言しており，処刑されていなければそいつ
        if self.willattack != None and self.willattack != exe:
            return self.willattack

        attacktarget = self.Attacktarget(exe)

        if exe in self.aliveVillagers:
            self.aliveVillagers.remove(exe)

        if attacktarget in self.aliveVillagers and attacktarget != None:
            return attacktarget

        return  self.myData.getAttackTarget(self.aliveVillagers)

    #扇動メソッド
    def Request(self,fakeSeers):

        x = random.randint(1,2)
        y = random.randint(1,10)

        #特定のプレイヤへのRequest
        if(self.isRequest):

            #黒出しされたとき，片占いに自分を占うように要求(p = 0.5)
            if x == 1 and self.isBlacked:
                self.isRequest = False
                for i in self.myData.getAliveSeerCOAgentList():
                    smap        = self.myData.getSeerCODataMap()
                    if not self.agentIdx in smap[i].getKnownList():
                        return ttf.request('DIVINATION',i,self.agentIdx)

            '''
            #占いに怪しそうな奴を占うよう要求(p = 0.2)
            elif y > 2 and len(self.seerlist) > 0:
                self.isRequest  = False
                cd = copy.deepcopy(self.candidate)
                removeAll(cd,self.myBlack)
                if len(cd) > 0:
                    target = self.myData.getMaxLikelyWolf(cd)
                    return ttf.request('DIVINATION',self.realSeer,target)
            '''

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
                    return ttf.request_all('DIVINATION',int(self.seer),int(target))

        return None

    #偽者調査
    def checkPossessed(self):

        #偽占い師=狂人探し
        smap        = self.myData.getSeerCODataMap()
        self.seerlist = []

        if len(smap) > 0:
            for k,v in smap.items():
                if not k in self.werewolves and k != self.agentIdx:
                    black   = v.getBlackList()
                    white   = v.getWhiteList()
                    isPossessed = False

                    if len(black) > 0:
                        for i in black:

                            #村に黒を出していたら偽者
                            if i in self.villagers:
                                self.possessed = k
                                isPossessed = True

                            #狼陣営に黒を出していたら本物かも
                            if i in self.werewolves:
                                self.maybeSeer = k

                    #狼に白を出していたら偽者
                    if len(white) > 0 :

                        for i in white:
                            if i in self.werewolves:
                                self.possessed = k
                                isPossessed = True

                    if self.maybeSeer == self.possessed:
                        self.maybeSeer = None

                    if not isPossessed and self.myData.isAliveIndex(k) and k not in self.seerlist:
                        self.seerlist.append(k)

        #偽霊媒探し　= 狂人
        mmap = self.myData.getMediumCODataMap()
        self.mediumlist = []

        if len(mmap) > 0:
            for k,v in mmap.items():
                if not k in self.werewolves:
                    black   = v.getBlackList()
                    white   = v.getWhiteList()
                    isPossessed = False

                    #村に黒を出していたら偽者
                    if len(black) > 0:
                        for i in black:
                            if i in self.werewolves:
                                self.possessed = k
                                isPossessed = True

                    #狼に白を出していたら偽者
                    if len(white) > 0:
                        for i in white:
                            if i in self.werewolves:
                                self.possessed = k
                                isPossessed = True

                    if not isPossessed and self.myData.isAliveIndex(k) and k not in self.mediumlist:
                        self.mediumlist.append(k)

    #狼と狂人以外の占いＣＯがいれば真占とする
    def checkReal(self):

        if len(self.seerlist) == 1:
            self.seer   = self.seerlist[0]
        if len(self.mediumlist) == 1:
            self.medium   = self.mediumlist[0]

        bg = copy.deepcopy(self.myData.getBGCOAgentList())
        for i in self.werewolves:
            if i in bg: bg.remove(i)
        if self.possessed in bg:
            bg.remove(self.possessed)

        self.bglist = bg

    def Attacktarget(self,exe):

        #初日
        if(self.myData.getToday() == 1):

            #占い師2人(狂人,占い師)のとき、占い師確定で占い噛み
            #(初日狂人のみCOの場合があるため)
            if(len(self.myData.getSeerCOAgentList()) > 1 and self.seer != None):
                attacktarget = self.seer
                if exe != attacktarget:
                    return attacktarget

            #霊媒師確定、占い師未確定で霊媒噛み
            if(self.medium != None and self.seer == None):
                attacktarget = self.medium
                if exe != attacktarget:
                    return attacktarget

        #GJが出ている場合
        if(self.isGJ):

            #占い師噛みでGJなら霊媒>狩人>村人噛み
            if(self.attacked in self.seerlist):
                #確定霊媒師噛み
                if(self.medium != None and self.myData.isAliveIndex(self.medium)):
                    attacktarget = self.medium
                    if exe != attacktarget:
                        return attacktarget

                #霊媒師噛み
                if(len(self.mediumlist) > 0):
                    attacktarget = self.myData.getAttackTarget(self.mediumlist)
                    return attacktarget

                #狩人噛み
                if(len(self.bglist) > 0):
                    attacktarget = self.myData.getAttackTarget(self.bglist)
                    return attacktarget

                #村人噛み
                if(len(self.aliveVillagers) > 0):
                    attacktarget = self.myData.getAttackTarget(self.aliveVillagers)
                    return attacktarget

            #霊媒師噛みでGJなら占い>狩人>村人噛み
            elif(self.attacked in self.mediumlist):
                #確定占い師最優先噛み
                if(self.seer != None and self.myData.isAliveIndex(self.seer)):
                    attacktarget = self.seer
                    return attacktarget

                #占い師噛み
                if(len(self.seerlist) > 0):
                    attacktarget = self.myData.getAttackTarget(self.seerlist)
                    return attacktarget

                #狩人噛み
                if(len(self.bglist) > 0):
                    attacktarget = self.myData.getAttackTarget(self.bglist)
                    return attacktarget

                #村人噛み
                if(len(self.aliveVillagers) > 0):
                    attacktarget = self.myData.getAttackTarget(self.aliveVillagers)
                    return attacktarget

        #GJが出ていない場合
        else:

            #確定占い師最優先噛み
            if(self.seer != None and self.myData.isAliveIndex(self.seer)):
                attacktarget = self.seer
                return attacktarget

            #maybe占い師最優先噛み
            if(self.maybeSeer != None and self.myData.isAliveIndex(self.maybeSeer)):
                attacktarget = self.maybeSeer
                return attacktarget

            #占い師噛み
            if(len(self.seerlist) > 0):
                attacktarget = self.myData.getAttackTarget(self.seerlist)
                return attacktarget

            #確定霊媒師噛み
            if(self.medium != None and self.myData.isAliveIndex(self.medium)):
                attacktarget = self.medium
                return attacktarget

            #霊媒師噛み
            if(len(self.mediumlist) > 0):
                attacktarget = self.myData.getAttackTarget(self.mediumlist)
                return attacktarget

            #狩人噛み
            if(len(self.bglist) > 0):
                attacktarget = self.myData.getAttackTarget(self.bglist)
                return attacktarget

            #村人噛み
            if(len(self.aliveVillagers) > 0):
                attacktarget = self.myData.getAttackTarget(self.aliveVillagers)
                return attacktarget

        attacktarget = self.myData.getAttackTargetAll()
        return attacktarget

    def MyTarget(self):

        if self.isSeerCO and len(self.myAliveblack) > 0:
            target = self.myData.getMaxLikelyWolf(self.myAliveblack)
            return target

        o_fake  = self.alivefakeSeers + self.alivefakeMediums

        if len(o_fake) > 0:
            target = self.myData.getMaxLikelyWolf(o_fake)
            return target

        if len(self.myalivefakeSeers) > 0:
            target = self.myData.getMaxLikelyWolf(self.myalivefakeSeers)
            return target

        if(len(self.candidate) > 0):
            target = self.myData.getMaxLikelyWolf(self.candidate)
            return target

        target  = self.myData.getMaxLikelyWolfAll()
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

    def finish(self):
        super(Werewolf,self).finish()

    def setmyData(self,mydata):
        self.myData = mydata


    def SeerCOTrigger(self):

        #WhisperでSeerDeclareがTrueなら占い騙り
        if(self.myData.getToday() == 1 and self.isSeerDeclare):
            if (self.myData.getTurn() == 0):
                rand = random.randint(1, 10)
                if (rand < 4):  return True

            # 初日2ターン目(確率0.3) 占いCOが0もしくは1のとき
            if (self.myData.getTurn() == 1 and not self.isSeerCO):
                rand = random.randint(1, 10)
                if (rand < 4):  return True

                # 初日3ターン目(確率1.0) 占いCOが0もしくは1のとき
            if (self.myData.getTurn() == 2 and not self.isSeerCO):
                rand = random.randint(1, 10)
                if (rand < 10):  return True

        return False

    def WerewolfDivine(self):

        rand = random.randint(1,10)
        self.count += 1

        #再帰5回目
        if self.count >=5:

            #前日襲撃されたプレイヤに白出し
            if rand > 5:
                target = self.myData.getAttackedAgent()
                if target in self.divineList:
                    self.divineList.remove(target)
                    self.divined.update({target:'HUMAN'})
                    return target,'HUMAN'

            #生存村の中で未占いがいない -> 狼に白出し
            #(本当は白出し回数チェックを入れたかったなぁ)
            else:
                for i in self.aliveWerewolves:
                    if i in self.divineList:
                        self.divineList.remove(i)
                        self.divined.update({i:'HUMAN'})
                        return i,'HUMAN'

            self.divined.update({self.agentIdx:'HUMAN'})
            return self.agentIdx,'HUMAN'

        matched_list = []
        for tag in self.divineList:
            for src in self.aliveVillagers:
                if tag == src:
                    matched_list.append(tag)

        #4割白に黒出し(3回まで)、3割白に白だし，3割黒に白出し
        if(len(self.divineList) > 0):
            if(rand > 6 and self.blacktimes < 3):
                if(len(matched_list) != 0): #まだ占いできれば占う
                    target = random.choice(matched_list)
                    self.divineList.remove(target)
                    #襲撃されたプレイヤは白出し
                    if(target in self.myData.getAttackedAgentList()):
                        self.divined.update({target:'HUMAN'})
                        return target,'HUMAN'
                    else:
                        self.divined.update({target:'WEREWOLF'})
                        self.myAliveblack.append(target)
                        self.blacktimes += 1
                        return target,'WEREWOLF'
                else:
                    return self.WerewolfDivine()

            elif(rand > 4 and rand <=6):
                if (len(matched_list) != 0):
                    target = random.choice(matched_list)
                    self.divineList.remove(target)
                    self.divined.update({target: 'HUMAN'})
                    return target, 'HUMAN'
                else:
                    return self.WerewolfDivine()
            else:
                if (len(matched_list) != 0):
                    target = random.choice(matched_list)
                    self.divineList.remove(target)
                    self.divined.update({target:'HUMAN'})
                    return target,'HUMAN'
                else:
                    return self.WerewolfDivine()


        self.divined.update({self.agentIdx:'HUMAN'})
        return self.agentIdx,'HUMAN'

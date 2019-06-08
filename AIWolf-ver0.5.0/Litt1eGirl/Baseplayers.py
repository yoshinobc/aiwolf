# -*- coding: utf-8 -*-
import aiwolfpy
import aiwolfpy.templatetalkfactory as ttf
import aiwolfpy.templatewhisperfactory as twf

import numpy as np
import copy,random

class BaseVillager(object):

    def __init__(self, agent_name):
        self.agent_name = agent_name
        self.role = 'VILLAGER'
        self.gameInfo   = None

        #偽確定リスト
        self.fakeSeers   = []    #偽占い師
        self.fakeMediums = []    #偽霊媒師
        self.alivefakeSeers   = []    #生存偽占い師
        self.alivefakeMediums = []    #生存偽霊媒師
        self.myfakeSeers    = []    #自分に黒出しした占い師
        self.myalivefakeSeers    = []    #自分に黒出しした生存占い師

    def getName(self):
        return self.agent_name

    def update(self, game_info, talk_history, whisper_history, request):
        self.gameInfo = game_info

        self.isOneTimeCall = self.myData.isOneTimeCall()

        if self.isOneTimeCall and self.is15Game:
            self.s_fakeSearch(self.agentIdx)
            self.o_fakeSearch()

    def initialize(self, game_info, game_setting):
        self.agentIdx   = game_info['agent']
        self.playerNum  = game_setting['playerNum']
        self.agentName  = 'Agent[' + "{0:02d}".format(self.agentIdx) + ']'
        self.gameInfo   = game_info

        if self.playerNum == 15:
            self.is15Game = True
        else:
            self.is15Game = False

        self.isBlacked  = False #被黒フラグ
        self.isOneTimeCall = False

    def dayStart(self):
        self.candidate = self.myData.getAliveAgentIndexList()
        self.candidate.remove(self.agentIdx)
        self.willvote = None

    def talk(self):
        return ttf.over()

    def vote(self):
        target      = self.myData.getMaxLikelyWolfAll()
        return target

    def finish(self):
        return None

    def setmyData(self,mydata):
        self.myData = mydata

    #客観的偽者調査
    def o_fakeSearch(self):

        #偽霊媒師探し
        mmap        = self.myData.getMediumCODataMap()
        mediumlist  = self.myData.getMediumCOAgentList()

        if self.agentIdx in mediumlist:
            mediumlist.remove(self.agentIdx)

        if(self.playerNum > 5): werewolves  = 3
        else:                   werewolves  = 1

        #偽霊媒師探し
        if (len(mediumlist) > 0):

            for k,v in mmap.items():

                if k not in self.fakeMediums:
                    black   = v.getBlackList()
                    white   = v.getWhiteList()

                    #黒を人狼数以上出していたら偽者
                    if(len(black) > werewolves):
                        self.fakeMediums.append(k)

                    #生存者に結果を出していたら偽者
                    result = v.getKnownList()
                    for i in result.keys():
                        if self.myData.isAliveIndex(i) and k not in self.fakeMediums:
                            self.fakeMediums.append(k)

        #偽占い師探し
        smap        = self.myData.getSeerCODataMap()
        seerlist    = self.myData.getSeerCOAgentList()

        if self.agentIdx in seerlist:
            seerlist.remove(self.agentIdx)

        if(len(seerlist) > 0):

            for k,v in smap.items():

                if k not in self.fakeSeers:

                    black   = v.getBlackList()
                    white   = v.getWhiteList()

                    #黒を人狼数以上出していたら偽者
                    if len(black) > werewolves:
                        self.fakeSeers.append(k)

                    #噛まれた奴に黒出ししてたら偽物
                    if len(black) > 0 :
                        for i in black:
                            if i in self.myData.getAttackedAgentList() and k not in self.fakeSeers:
                                self.fakeSeers.append(k)

            #2日目以降、霊媒師1人のときに、霊媒と異なる結果であれば偽者
            if(len(mediumlist) == 1 and self.myData.getToday() > 1):

                med_black = []
                med_white = []

                for k,v in mmap.items():
                    if k not in self.fakeMediums:
                        med_black   = v.getBlackList()
                        med_white   = v.getWhiteList()

                for k,v in smap.items():
                    if k not in self.fakeSeers:
                        black   = v.getBlackList()
                        white   = v.getWhiteList()

                        #占い師の白出しに霊媒師が黒を出していたら偽者
                        if len(white) > 0 and len(med_black) > 0:
                            for i in white:
                                if i in med_black:
                                    if not k in self.fakeSeers:
                                        self.fakeSeers.append(k)

                        #占い師の黒出しに霊媒師が白を出していたら偽者
                        if len(black) > 0 and len(med_white) > 0:
                            for i in black:
                                if i in med_white:
                                    if not k in self.fakeSeers:
                                        self.fakeSeers.append(k)

        if len(self.fakeSeers) > 0:
            self.alivefakeSeers = copy.deepcopy(self.fakeSeers)
            for i in self.fakeSeers:
                if not self.myData.isAliveIndex(i):
                    self.alivefakeSeers.remove(i)

        if len(self.fakeMediums) > 0:
            self.alivefakeMediums = copy.deepcopy(self.fakeMediums)
            for i in self.fakeMediums:
                if not self.myData.isAliveIndex(i):
                    self.alivefakeMediums.remove(i)

    #主観的偽者調査
    def s_fakeSearch(self,me):

        #偽占い師探し
        smap        = self.myData.getSeerCODataMap()
        seerlist    = self.myData.getSeerCOAgentList()
        fakeSeer    = []

        if(len(seerlist) > 0):
            #自分に黒を出していたら偽者
            for k,v in smap.items():
                black   = v.getBlackList()
                if(me in black):
                    self.isBlacked = True
                    fakeSeer.append(k)

        self.myfakeSeers = fakeSeer

        if len(self.myfakeSeers) > 0:
            self.myalivefakeSeers = copy.deepcopy(self.myfakeSeers)
            for i in self.myfakeSeers:
                if not self.myData.isAliveIndex(i):
                    self.myalivefakeSeers.remove(i)

    #ランダムで予想を言う
    def Estimate(self, target):
        rand_x = random.randrange(10)
        rand_y = random.randrange(5)
        if(rand_x < rand_y):
            rand = random.randint(1,3)
            if(rand > 1 and len(self.candidate) > 0):
                target = self.myData.getMaxLikelyWolf(self.candidate)
                if(target != None):
                    return ttf.estimate(target,'WEREWOLF')
            else:
                target = self.myData.getMaxLikelyVillAll()
                if(target != None):
                    return ttf.estimate(target,'VILLAGER')

        return None

    #talkやvoteのtargetを処理する
    def Target(self,mylist):

        for i in mylist:
            if(not self.myData.isAliveIndex(i)):
                mylist.remove(i)

        if(len(mylist) > 0):
            target = self.myData.getMaxLikelyWolf(mylist)
            return target

        return self.myData.getMaxLikelyWolfAll()

    #2重のリストをフラットにする関数
    def flatten(self,nested_list):
        return [e for inner_list in nested_list for e in inner_list]


class BaseBodyguard(BaseVillager):

    def __init__(self, agent_name):
        super(BaseBodyguard,self).__init__(agent_name)
        self.role = 'BODYGUARD'

    def getName(self):
        return self.agent_name

    def update(self, game_info, talk_history, whisper_history, request):
        return super(BaseBodyguard,self).update(game_info, talk_history, whisper_history, request)

    def initialize(self, game_info, game_setting):
        return super(BaseBodyguard,self).initialize(game_info, game_setting)

    def dayStart(self):
        return super(BaseBodyguard,self).dayStart()

    def talk(self):
        return super(BaseBodyguard,self).talk()

    def vote(self):
        return super(BaseBodyguard,self).vote()

    def guard(self):
        if(len(self.candidate) > 0):
            target      = self.myData.getMaxLikelyVill(self.candidate)
            return target
        return self.agentIdx

    def finish(self):
        return super(BaseBodyguard,self).finish()

class BaseMedium(BaseVillager):

    def __init__(self, agent_name):
        super(BaseMedium,self).__init__(agent_name)
        self.role = 'MEDIUM'

        #白黒リスト
        self.myBlack    = []    #霊媒結果黒(人狼)だったプレイヤのリスト
        self.myWhite    = []    #霊媒結果白(人間)だったプレイヤのリスト
        self.myResult   = []    #霊媒結果のmapのリスト
        self.willSay    = []    #まだいっていない霊媒結果のmapのリスト

        #偽確定リスト
        self.med_fakeSeers   = []    #自分の霊媒結果と異なる結果を出した占い師
        self.med_fakeMediums = []    #自分以外に霊媒師COしたプレイヤ
        self.med_alivefakeSeers   = []    #自分の霊媒結果と異なる結果を出した生存占い師
        self.med_alivefakeMediums = []    #自分以外に霊媒師COした生存プレイヤ

        self.isCall     = False
        self.isCO       = False

    def getName(self):
        return self.agent_name

    def update(self, game_info, talk_history, whisper_history, request):
        super(BaseMedium,self).update(game_info, talk_history, whisper_history, request)

        if self.isOneTimeCall and self.is15Game:
            self.myfakeSearch()

    def initialize(self, game_info, game_setting):
        super(BaseMedium,self).initialize(game_info, game_setting)

    def dayStart(self):
        super(BaseMedium,self).dayStart()

        if(self.gameInfo['day'] > 1):
            identified   = self.gameInfo['mediumResult']
            result      = identified['result']
            target      = identified['target']

            self.myResult.append(identified)
            self.willSay.append(identified)

            if(result == 'HUMAN'):
                self.myWhite.append(target)
            else:
                self.myBlack.append(target)

    def talk(self):
        return super(BaseMedium,self).talk()

    def vote(self):
        return super(BaseMedium,self).vote()

    def finish(self):
        return super(BaseMedium,self).finish()

    #COするか否か
    def COTrigger(self):
        if(self.myData.getToday() > 1):
            return True
        else:
            return False

    #偽者調査
    def myfakeSearch(self):
        #偽霊媒師探し
        mediumlist = self.myData.getMediumCOAgentList()

        if self.agentIdx in mediumlist:
            mediumlist.remove(self.agentIdx)

        if len(mediumlist) > 0:
            for i in mediumlist:
                if i not in self.med_fakeMediums:
                    self.med_fakeMediums.append(i)

        #偽占い師探し
        seer = self.myData.getSeerCODataMap()
        if(len(seer) > 0):
            for k,v in seer.items():
                black   = v.getBlackList()
                white   = v.getWhiteList()

                #自分の白出しに黒を出していたら偽者
                if(len(black) > 0):
                    for i in black:
                        if(i in self.myWhite and k not in self.med_fakeSeers):
                            self.med_fakeSeers.append(k)

                #自分の黒出しに白を出していたら偽者
                if(len(white) > 0):
                    for i in white:
                        if(i in self.myBlack and k not in self.med_fakeSeers):
                            self.med_fakeSeers.append(k)

        if len(self.med_fakeSeers) > 0:
            self.med_alivefakeSeers = copy.deepcopy(self.med_fakeSeers)
            for i in self.med_fakeSeers:
                if not self.myData.isAliveIndex(i):
                    self.med_alivefakeSeers.remove(i)

        if len(self.med_fakeMediums) > 0:
            self.med_alivefakeMediums = copy.deepcopy(self.med_fakeMediums)
            for i in self.med_fakeMediums:
                if not self.myData.isAliveIndex(i):
                    self.med_alivefakeMediums.remove(i)

class BaseSeer(BaseVillager):

    def __init__(self, agent_name):
        super(BaseSeer,self).__init__(agent_name)
        self.role = 'SEER'
        self.willvote   = None

        #占い結果リスト
        self.myResult   = []    #すべての占い結果
        self.willSay    = []    #まだ言っていない占い結果
        self.myBlack    = []    #自分の黒
        self.aliveBlack = []    #生存している黒
        self.myWhite    = []    #自分の白
        self.aliveWhite = []    #生存している白

        #偽確定リスト
        self.seer_fakeSeers   = []    #自分以外に占い師COしたプレイヤ
        self.seer_fakeMediums = []    #自分の占い結果と異なる結果を出した霊媒師
        self.seer_alivefakeSeers   = []    #自分以外に占い師COした生存プレイヤ
        self.seer_alivefakeMediums = []    #自分の占い結果と異なる結果を出した生存霊媒師
        self.isCall     = False
        self.isCO       = False

    def getName(self):
        return self.agent_name

    def update(self, game_info, talk_history, whisper_history, request):
        super(BaseSeer,self).update(game_info, talk_history, whisper_history, request)

        if self.isOneTimeCall and self.is15Game:
            self.myfakeSearch()

    def initialize(self, game_info, game_setting):
        super(BaseSeer,self).initialize(game_info, game_setting)
        self.divineList = self.myData.getAliveAgentIndexList()
        self.divineList.remove(self.agentIdx)

    def dayStart(self):
        super(BaseSeer,self).dayStart()
        self.isCall = False
        divined  = self.gameInfo['divineResult']

        #占い結果処理
        if(divined != None):
            self.myResult.append(divined)
            self.willSay.append(divined)
            target  = divined['target']
            result  = divined['result']

            if(result == 'HUMAN'):
                self.myWhite.append(target)
                self.aliveWhite.append(target)
                if(target in self.candidate):  self.candidate.remove(target)
            else:
                self.myBlack.append(target)
                self.aliveBlack.append(target)
            if(target in self.divineList):  self.divineList.remove(target)

        #死亡者結果処理(2日目以降)
        if(self.gameInfo['day'] > 1):
            execute     = self.myData.getExecutedAgent()
            attacked    = self.myData.getAttackedAgent()

            if(execute in self.divineList):     self.divineList.remove(execute)
            if(execute in self.aliveBlack):     self.aliveBlack.remove(execute)
            if(execute in self.aliveWhite):     self.aliveWhite.remove(execute)
            if(execute in self.candidate):      self.candidate.remove(execute)
            if(attacked in self.divineList):    self.divineList.remove(attacked)
            if(attacked in self.aliveBlack):    self.aliveBlack.remove(attacked)
            if(attacked in self.aliveWhite):    self.aliveWhite.remove(attacked)
            if(attacked in self.candidate):     self.candidate.remove(attacked)

    def talk(self):
        return super(BaseSeer,self).talk()

    def vote(self):
        if(len(self.candidate) > 0):
            return self.myData.getMaxLikelyWolf(self.candidate)

        #例外対策
        return super(BaseSeer,self).vote()

    def divine(self):

        #未占いから選択
        if(len(self.divineList) > 0):
            target = self.myData.getMaxLikelyWolf(self.divineList)
            return int(target)

        #例外対策
        return self.agentIdx

    def finish(self):
        super(BaseSeer,self).finish()

    #COするか否か
    def COTrigger(self):
        #初日1ターン目(確率0.9)
        if(self.myData.getToday() == 1 and self.myData.getTurn() == 0):
            rand = random.randint(1,10)
            if(rand < 10):  return True

        #黒を見つけたとき
        if(len(self.myBlack) > 0):  return True

        #他占い師COがあったとき
        seerlist    = self.myData.getSeerCOAgentList()
        if(len(seerlist) > 0):      return True

        #吊られそうなとき
        exe     = self.myData.getMaxLikelyExecuteAgentAll()
        if(exe == self.agentIdx):     return True

        #2日目は必ずCO
        if(self.myData.getToday == 2):  return True

        return False

    #偽者調査
    def myfakeSearch(self):

        #偽占い師探し
        seerlist = self.myData.getSeerCOAgentList()

        if len(seerlist) > 0:
            for i in seerlist:
                if i not in self.seer_fakeSeers and i != self.agentIdx:
                    self.seer_fakeSeers.append(i)

        #偽霊媒探し
        medium = self.myData.getMediumCODataMap()
        if(len(medium) > 0):
            for k,v in medium.items():
                black   = v.getBlackList()
                white   = v.getWhiteList()

                #自分の白出しに黒を出していたら偽者
                if(len(black) > 0):
                    for i in black:
                        if i in self.myWhite and i not in self.seer_fakeMediums:
                            self.seer_fakeMediums.append(k)

                #自分の黒出しに白を出していたら偽者
                if(len(white) > 0):
                    for i in white:
                        if i in self.myBlack and i not in self.seer_fakeMediums:
                            self.seer_fakeMediums.append(k)

        if len(self.seer_fakeSeers) > 0:
            self.seer_alivefakeSeers = copy.deepcopy(self.seer_fakeSeers)
            for i in self.seer_fakeSeers:
                if not self.myData.isAliveIndex(i):
                    self.seer_alivefakeSeers.remove(i)

        if len(self.seer_fakeMediums) > 0:
            self.seer_alivefakeMediums = copy.deepcopy(self.seer_fakeMediums)
            for i in self.seer_fakeMediums:
                if not self.myData.isAliveIndex(i):
                    self.seer_alivefakeMediums.remove(i)

    def setmyData(self,mydata):
        self.myData = mydata

class BaseWerewolf(BaseVillager):

    def __init__(self, agent_name):
        super(BaseWerewolf,self).__init__(agent_name)
        self.role = 'WEREWOLF'

    def getName(self):
        return self.agent_name

    def update(self, game_info, talk_history, whisper_history, request):
        super(BaseWerewolf,self).update(game_info, talk_history, whisper_history, request)

    def initialize(self, game_info, game_setting):
        super(BaseWerewolf,self).initialize(game_info, game_setting)
        self.werewolves = []      #仲間の人狼
        self.villagers  = []      #村人たち
        self.aliveWerewolves = []      #仲間の人狼
        self.aliveVillagers  = []      #村人たち

        #自分以外の人狼リストの作成
        for k,v in game_info['roleMap'].items():
            if(self.agentIdx != int(k)):
                self.werewolves.append(int(k))

        #村人リストの作成
        self.villagers = [target for target in range(1, self.playerNum+1)]
        for i in self.werewolves:
            if (i in self.villagers):   self.villagers.remove(i)
        self.villagers.remove(self.agentIdx)

    def dayStart(self):
        super(BaseWerewolf,self).dayStart()

        #生存リストの更新
        self.aliveVillagers, self.aliveWerewolves = [], []
        for i in self.myData.getAliveAgentIndexList():
            if(i in self.villagers):
                self.aliveVillagers.append(i)
            if(i in self.werewolves):
                self.aliveWerewolves.append(i)

    def talk(self):
        return ttf.over()

    def whisper(self):
        return twf.over()

    def vote(self):

        #村人から最も狼らしい者(身内切りはしない)
        if(len(self.aliveVillagers) > 0):
            return self.myData.getMaxLikelyWolf(self.aliveVillagers)

        #例外対策
        return super(BaseWerewolf,self).vote()

    def attack(self):

        #村人から最も村人らしい者(役職狙い撃ちはしない)
        if(len(self.aliveVillagers) > 0):
            return self.myData.getMaxLikelyVill(self.aliveVillagers)

        #例外対策
        return self.agentIdx

    def finish(self):
        super(BaseWerewolf,self).finish()

    def setmyData(self,mydata):
        self.myData = mydata

class BasePossessed(BaseVillager):

    def __init__(self, agent_name):
        super(BasePossessed,self).__init__(agent_name)
        self.Role = 'POSSESSED'

    def getName(self):
        return self.agent_name

    def update(self, game_info, talk_history, whisper_history, request):
        super(BasePossessed,self).update(game_info, talk_history, whisper_history, request)

    def initialize(self, game_info, game_setting):
        super(BasePossessed,self).initialize(game_info, game_setting)

    def dayStart(self):
        super(BasePossessed,self).dayStart()

    def talk(self):
        return super(BasePossessed,self).talk()

    def vote(self):
        return super(BasePossessed,self).vote()

    def finish(self):
        return super(BasePossessed,self).finish()

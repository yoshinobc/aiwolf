#!/usr/bin/env python
# -*- coding: utf-8 -*-
import aiwolfpy
import aiwolfpy.templatetalkfactory as ttf
import aiwolfpy.templatewhisperfactory as twf

import numpy as np

#各役職モジュールインポート
import LGVillager as lv
import LGSeer as ls
import LGMedium as lm
import LGBodyguard as lb
import LGPossessed as lp
import LGWerewolf as lw
import LGVillager_5 as bv
import LGSeer_5 as bs
import LGPossessed_5 as bp
import LGWerewolf_5 as bw

import MyData_15 as md
import MyData_05 as mdb

class Litt1eGirl(object):

    def __init__(self, agent_name):
        self.agent_name = agent_name
        self.gameInfo   = []

        self.wolfWinrate = {}
        self.villWinrate = {}
        for i in range(1,16):
            self.wolfWinrate.update({i:[0,0]})
            self.villWinrate.update({i:[0,0]})

        self.gameTimes  = 0
        pass

    def getName(self):
        return self.agent_name

    def initialize(self, game_info, game_setting):
        self.agentIdx   = game_info['agent']    #エージェントインデックス
        self.agentName  = 'Agent[' + "{0:02d}".format(self.agentIdx) + ']'  #エージェント名
        self.role       = game_info['roleMap'][str(self.agentIdx)]          #自分の役割
        self.roleMap    = game_info['roleMap']      #役割一覧（狼は全員の種別，他は自分のみ）
        self.playerNum  = game_setting['playerNum'] #プレイヤ人数
        #エージェント名のリスト(Agent[01]など).
        self.agentNames = ['Agent[' + "{0:02d}".format(target) + ']' for target in range(1, self.playerNum+1)]
        self.gameInfo   = game_info

        self.gameTimes  +=1
        self.isCall     = True

        #myDataは毎ゲームリセット
        if(len(self.agentNames) < 15):  self.myData     = mdb.MyData()
        else:                           self.myData     = md.MyData()

        self.myData.gameStart(game_info, self.playerNum, self.agentNames)

        if self.role == 'VILLAGER':
            if(len(self.agentNames) < 15):  self.agent = bv.Villager(self.agent_name)
            else:                           self.agent = lv.Villager(self.agent_name)
        elif self.role == 'SEER':
            if(len(self.agentNames) < 15):  self.agent = bs.Seer(self.agent_name)
            else:                           self.agent = ls.Seer(self.agent_name)
        elif self.role == 'MEDIUM':     self.agent = lm.Medium(self.agent_name)
        elif self.role == 'BODYGUARD':  self.agent = lb.Bodyguard(self.agent_name)
        elif self.role == 'POSSESSED':
            if(len(self.agentNames) < 15):  self.agent = bp.Possessed(self.agent_name)
            else:                           self.agent = lp.Possessed(self.agent_name)
        elif self.role == 'WEREWOLF':
            if(len(self.agentNames) < 15):  self.agent = bw.Werewolf(self.agent_name)
            else:                           self.agent = lw.Werewolf(self.agent_name)

        self.agent.setmyData(self.myData)
        self.agent.initialize(game_info, game_setting)

        #51ゲーム以降”強い”エージェントのチェック
        self.myData.setWinrate(self.wolfWinrate,self.villWinrate)
        self.myData.CalcWinrank(self.gameTimes)

    def update(self, game_info, talk_history, whisper_history, request):

        if(len(game_info) > 0): self.gameInfo = game_info
        self.myData.update(self.gameInfo, talk_history, whisper_history)
        self.agent.setmyData(self.myData)
        return self.agent.update(self.gameInfo, talk_history, whisper_history, request)

    def dayStart(self):
        self.myData.dayStart(self.gameInfo)
        self.agent.setmyData(self.myData)
        return self.agent.dayStart()

    def talk(self):
        return self.agent.talk()

    def whisper(self):
        return self.agent.whisper()

    def vote(self):
        return self.agent.vote()

    def attack(self):
        return self.agent.attack()

    def divine(self):
        return self.agent.divine()

    def guard(self):
        return self.agent.guard()

    def finish(self):

        if(self.isCall):
            statusMap   = self.gameInfo['statusMap']
            roleMap     = self.gameInfo['roleMap']
            wolf,vill  = [],[]
            winner      = False
            for k,v in statusMap.items():
                idx = int(k)
                if(v == 'ALIVE'):
                    if(roleMap[k] == 'WEREWOLF'):   winner = True
                if(roleMap[k] == 'WEREWOLF' or roleMap[k] == 'POSSESSED'):
                    wolf.append(idx)
                    self.wolfWinrate[idx][0]  += 1
                else:
                    vill.append(idx)
                    self.villWinrate[idx][0]  += 1
            if(winner):
                for i in wolf:
                    self.wolfWinrate[i][1]  += 1
            else:
                for i in vill:
                    self.villWinrate[i][1]  += 1
            self.isCall = False
        self.myData.finish()
        return self.agent.finish()

agent = Litt1eGirl('Litt1eGirl')

# run
if __name__ == '__main__':
    aiwolfpy.connect(agent)

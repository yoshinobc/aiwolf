#!/usr/bin/env python
from __future__ import print_function, division

# this is main script
# simple version

import aiwolfpy
import aiwolfpy.contentbuilder as cb

import aiwolfpy.cash
myname = 'bc_test'

class SampleAgent(object):

    def __init__(self, agent_name):
        # myname
        self.myname = agent_name
        print("init")
        self.count = 0
        print("count",self.count)
        self.predicter_5 = aiwolfpy.cash.Predictor_5()


    def getName(self):
        return self.myname

    def initialize(self, base_info, diff_data, game_setting):
        self.base_info = base_info
        # game_setting
        self.game_setting = game_setting
        print("initialize")
        # print(base_info)
        # print(diff_data)
        if self.game_setting["playerNum"] == 5:
            self.predicter_5.initialize(base_info,game_setting)

        self.divined_list = []
        self.comingout = ""
        self.myresult = ""
        self.not_reported = False
        self.vote_declare = 0

    def update(self, base_info, diff_data, request):
        self.base_info = base_info
        # print(base_info)
        # print(diff_data)

        #result
        if request == "DAILY_INITIALIZE":
            for i in range(diff_data.shape[0]):
                #identigy
                if diff_data["type"][i] == "identify":
                    self.not_reported = True
                    self.myresult = diff_data["text"][i]

                #divine
                if diff_data["type"][i] == "divine":
                    self.not_reported = True
                    self.myresult = diff_data["text"][i]

                #guard
                if diff_data["type"][i] == "guard":
                    self.myresult = diff_data["text"][i]

            #update
            if self.game_setting["playerNum"] == 5:
                self.predicter_5.update(diff_data)


    def dayStart(self):
        self.vote_declare = 0
        self.talk_turn = 0
        return None

    def talk(self):
        self.talk_turn += 1
        #comingout
        if self.base_info["myRole"] == "SEER" and self.comingout == "":
            self.comingout = "SEER"
            return cb.comingout(self.base_info["agentIdx"],self.comingout)
        elif self.base_info["myRole"] == "POSSESSED" and self.comingout == "":
            self.comingout = "SEER"
            return cb.comingout(self.base_info["agentIdx"],self.comingout)

        #report
        if self.base_info["myRole"] == "SEER" and self.not_reported:
            self.not_reported = False
            return self.myresult

        elif self.base_info["myRole"] == "POSSESSED" and self.not_reported:
            self.not_reported = False
            return self.myresult

        #declare vote
        if self.vote_declare != self.vote():
            self.vote_declare = self.vote()
            return cb.vote(self.vote_declare)

        #skip
        if self.talk_turn <= 10:
            return cb.skip()

        return cb.over()

    def whisper(self):
        return cb.over()

    def vote(self):
        for i in range(1,6):
            if self.base_info["statusMap"][str(i)] == "ALIVE" and i != self.base_info["agentIdx"]:
                idx = i
        return 0

    def attack(self):
        idx = -1
        for i in range(1,6):
            if self.base_info['statusMap'][str(i)] == "ALIVE" and i != self.base_info["agentIdx"] and self.predicter_5.x_2d[i-1,3] == 1:
                idx = i
                break
        if idx == -1:
            for i in range(1,6):
                if self.base_info["statusMap"][str(i)] == "ALIVE" and i != self.base_info["agentIdx"]:
                    idx = i
        return idx

    def divine(self):
        for i in range(1,6):
            if self.base_info["statusMap"][str(i)] == "ALIVE" and i not in self.divined_list:
                idx = i

        self.divined_list.append(idx)
        return idx

    def guard(self):
        return self.base_info['agentIdx']

    def finish(self):
        self.count+=1
        print("finish",self.count)
        return None



agent = SampleAgent(myname)



# run
if __name__ == '__main__':
    aiwolfpy.connect_parse(agent)

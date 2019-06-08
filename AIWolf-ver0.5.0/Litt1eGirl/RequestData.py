# -*- coding: utf-8 -*-
import aiwolfpy
import aiwolfpy.templatetalkfactory as ttf
import aiwolfpy.templatewhisperfactory as twf
import numpy as np


class RequestData(object):

    def __init__(self,day,turn,agent,content,target1,target2):
        self.day_   = day
        self.turn_  = turn
        self.agent_ = agent
        self.content_   = content
        self.target1_   = target1 # 全体 or 個人 (主語)
        self.target2_   = target2 # 動作に対する目的語

    def getRequestToday(self,data,day):
        newdata = []
        for i in data:
            if(i.getDay() == day):
                newdata.append(i)
        return newdata

    def getRequestTodayContent(self,data,day,content):
        newdata = []
        daydata = self.getRequestToday(data,day)
        for i in daydata:
            if(i.getContent() == content):
                newdata.append(i)
        return newdata

    def getRequestTodayMe(self,data,day,idx):
        newdata = []
        daydata = self.getRequestToday(data,day)
        for i in daydata:
            if(i.getTarget1() == idx):
                newdata.append(i)
        return newdata

    def getRequestTodayAll(self,data,day):
        newdata = []
        daydata = self.getRequestToday(data,day)
        for i in daydata:
            if(i.getTarget1() == None):
                newdata.append(i)
        return newdata

    def getDay(self):
        return self.day_

    def getTurn(self):
        return self.turn_

    def getAgent(self):
        return self.agent_

    def getContent(self):
        return self.day_

    def getTarget1(self):
        return self.target1_

    def getTarget2(self):
        return self.target2_

# -*- coding: utf-8 -*-
import aiwolfpy
import aiwolfpy.templatetalkfactory as ttf
import aiwolfpy.templatewhisperfactory as twf
import numpy as np



class PlayerData(object):

    def __init__(self,agentIndex,agentName):
        self.__role_ = None
        self.__agentIndex_ = agentIndex
        self.__agentName_ = agentName
        self.__coData_ = 0
        self.__coTiming_ = -1
        self.__talkNum_ = []
        self.__votePattern_ = []
        self.__resultBlack_ = []
        #sh_ = new SayHello();

    def addCOData(self,role, day, turn):
        timing = (10 * day) + turn
        if (self.__coTiming_ != -1):
            self.__coTiming_ = (self.__coTiming_ * 100) + timing
        else:
            self.__coTiming_ = timing;

    def addTalkData(self,talkNum):
        self.__talkNum_.append(talkNum)

    def addDayData(self,votePatern):
        self.__votePattern_.append(votePatern)

    def addDayData_black(self,votePatern, isBlack):
        if (len(self.__resultBlack_) != len(isBlack)):
            self.__resultBlack_ = isBlack
        self.addDayData(votePatern)

    def getAgentIndex(self):
        return self.__agentIndex_

    def getAgentIndex(self,gentIndex):
        self.__agentIndex_ = agentIndex

    def getAgentName(self,):
        return self.__agentName_

    def setAgentName(self,agentName):
        self.__agentName_ = agentName

    def getCoData(self):
        return self.__coData_

    def setCoData(self,coData):
        self.__coData_ = coData

    def getCoTiming(self):
        return self.__coTiming_

    def setCoTiming(self,coTiming):
        self.__coTiming_ = coTiming

    def getTalkNum(self):
        return self.__talkNum_

    def setTalkNum(self,talkNum):
        self.__talkNum_ = talkNum

    def getVotePattern(self):
        return self.__votePattern_

    def setVotePattern(self,votePattern):
        self.__votePattern_ = votePattern

    def getResultBlack(self):
        return self.__resultBlack_

    def setResultBlack(self,resultBlack):
        self.__resultBlack_ = resultBlack

    def getRole(self):
        return self.__.role_

    def setRole(self,role):
        self.__role_ = role

    def getCODay(self):
        timing = self.getCoTiming
        if (timing > 0):
            return timing / 10
        return -1

    '''
	public void checkSayHello(Content uttr) {
		sh_.getCheckWords(uttr);
	}

	public SayHello getSh() {
		return sh_;
	}

	public void setSh(SayHello sh) {
		sh_ = sh;
	}
    '''

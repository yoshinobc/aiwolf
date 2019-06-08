# -*- coding: utf-8 -*-
import aiwolfpy
import aiwolfpy.templatetalkfactory as ttf
import aiwolfpy.templatewhisperfactory as twf
import numpy as np


class COData(object):

    def __init__(self,day,turn,isCO):
        self.day_ = day
        self.turn_ = turn
        self.isCO_ = isCO
        self.isAfterSlide = False
        self.isAlive_ = True
        self.dayList_ = []
        self.blackList = []
        self.whiteList = []
        self.result_dict = {}
        self.day_isBlack_list = []
        self.result_target_list = []

    def getDay(self):
        return self.day_

    def getTurn(self):
        return self.turn_

    def getisCO(self):
        return self.isCO_

    def add(self,day,targetIdx, isBlack):
        self.dayList_.append(day)
        if (not targetIdx in self.result_dict.keys()):
            self.result_dict.update({targetIdx:isBlack})
            self.result_target_list.append(targetIdx)

            if (isBlack):
                self.blackList.append(targetIdx)
            else:
                self.whiteList.append(targetIdx)
            self.day_isBlack_list.append(isBlack)

    def getResultNum(self):
        return len(self.result_dict)

    def getBlackList(self):
        return self.blackList

    def getWhiteList(self):
        return self.whiteList

    def getDayList(self):
        return self.dayList_

    def getKnownList(self):
        return self.result_dict

    def isAlive(self):
        return self.isAlive_

    def setAlive(self,isAlive):
        self.isAlive_ = isAlive

    def isAfterSlide(self):
        return self.isAfterSlide_

    def setAfterSlide(self,isAfterSlide):
        self.isAfterSlide_ = isAfterSlide

    def getIsBlackList(self):
        return self.day_isBlack_list

    def getTargetResultList(self):
        return self.result_target_list

class BGCOData(COData):

    def __init__(self,day, turn,isCO):
        super(BGCOData,self).__init__(day,turn,isCO)
        self.guardList  = []

    def getAgent(self):
        gnow = self.getKnownList()
        if (len(gnow) > day):
           return gnow.get[day]
        return -1

    def isFullResult(self,day):
        if (len(self.guardList) != day - 1):
            return False
        return True

    def add(self,targetIdx):
        self.guardList.append(targetIdx)

    def getResultNum(self):
        return len(self.guardList)

    def getGuardList(self):
        return self.guardList

    def setGuardList(self,guardList):
        self.guardList = guardList

class MediumCOData(COData):

    def __init__(self,day,turn,isCO):
        super(MediumCOData,self).__init__(day,turn,isCO)

    def getAgent(self,day):
        if (len(self.getKnownList()) < day):
            return -1
        return self.getKnownList().get(day)

    def getResult(self,day):
        for i in range(len(self.getDayList())):
            if (self.getDayList()[i] == day ):
                result = {}
                targetlist  = self.getTargetResultList()
                blacklist   = self.getIsBlackList()
                if(len(targetlist) < i and len(blacklist) < i):
                    result.update({targetlist[i]:self.getIsBlackList()[i]})
                    return result
        return None

    def isFullResult(self,day):
        if (len(self.getIsBlackList()) != day):
            return False
        return True

class SeerCOData(COData):

    def __init__(self,day,turn,isCO):
        super(SeerCOData,self).__init__(day,turn,isCO)

    def getAgent(self):
        gnow = self.getKnownList()
        if (len(gnow) > day):
           return gnow.get[day]
        return -1

    def getResult(self,day):
        for i in range(0,len(self.getDayList())):
            if (self.getDayList()[i] == day ):
                result = {}
                targetlist  = self.getTargetResultList()
                blacklist   = self.getIsBlackList()
                if(len(targetlist) < i and len(blacklist) < i):
                    result.update({targetlist[i]:self.getIsBlackList()[i]})
                    return result
        return None

    def isFullResult(self,day):
        if (len(self.getIsBlackList()) != day):
            return False
        return True

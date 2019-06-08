# -*- coding: utf-8 -*-
import numpy as np
import string,copy,re

class SaveLog_05():

    def __init__(self):
        self.traintalk  = []    #全てのtalk
        self.daytalk    = []    #1日のTalk
        self.gametalk   = []    #全ての加工前talk
        self.turntalk   = []    #最新ターンのtalk
        self.trainvote  = []    #全てのvote
        self.dayvote    = []    #1日のTalk
        self.train      = []    #全てのtalk&vote
        self.player     = 5

    def Target(self,tg):
        tg = tg.replace("Agent[0","")
        tg = tg.replace("Agent[", "")
        tg = tg.replace("]","")
        if(tg != None):
            idx  = int(tg)
            return idx
        return 0

    #updateで呼び出す
    #会話履歴を保存する
    def saveTalk(self,gameInfo,talk_history):

        if (talk_history != None):
            del self.turntalk[:]

            for talk in talk_history:
                self.gametalk.append(talk)
                agent   = talk['agent']
                word    = re.split(r'[,() \n]',talk['text'])
                ct = word[0]
                data = None

                #AGREE,DISAGREE
                if(ct == 'AGREE' or ct == 'DISAGREE'):
                    day = talk['text'].split()[2].replace("day","")
                    ID  = talk['text'].split()[3].replace("ID:","")
                    for i in self.gametalk:
                        if(i['day'] == day and i['idx'] == ID):
                            target_1    = i['agent']
                            content     = i['text'].split()[0]
                            data = str(agent) + str(ct) + str(target_1) + str(content)
                            if len(i['text'].split() > 1):
                                data = data + i['text'].split()[1]
                            if len(i['text'].split() > 2):
                                data = data + i['text'].split()[2]

                #REQUEST
                if(ct == "REQUEST"):
                    content = talk['text'].split("(")[1]
                    content = content.split(")")
                    content = content[0].split()

                    topic = ''
                    for i in content:
                        i.replace(' ', '')
                        topic    = topic + ' ' + i
                    data    = ' ' + str(agent) + ' ' + str(ct) + topic

                else:
                    data    = ' ' + str(agent)
                    for i in word:
                        i.replace(' ', '')
                        data = data + ' ' + i

                if(data != None):
                    self.daytalk.append(data)
                    self.turntalk.append(data)

    #dayStartで呼び出す
    #投票情報や生死情報を保存する
    def saveVote(self,gameInfo):

        del self.dayvote[:]

        for vote in gameInfo['voteList']:
            agent   = vote['agent']
            target  = vote['target']

            c   = int(agent)*10 + int(target)
            self.dayvote.append(c)

        if (gameInfo["executedAgent"] != None):
            exe     = gameInfo["executedAgent"]

            c = "execute" + str(exe)
            self.dayvote.append(c)

        if gameInfo["attackedAgent"] != None:
            attack  = gameInfo["attackedAgent"]

            if(attack != -1):
                c   = "attack" + str(attack)
            else:
                c   = 0
            self.dayvote.append(c)

        self.traintalk.append(copy.deepcopy(self.daytalk))
        self.trainvote.append(copy.deepcopy(self.dayvote))
        daydata = self.daytalk + self.dayvote
        self.train.append(copy.deepcopy(daydata))
        del self.daytalk[:]

    #その日までのすべてのデータ
    def getAlldata(self):
        return self.train

    #指定した日のデータ
    def getDaydata(self,day):
        train = np.array(self.train[day-1], dtype=np.float32)
        return train

    def getTurntalk(self):
        turntalk = self.turntalk
        return turntalk

    #最新のtalk情報
    def getDaytalk(self):
        daytalk = np.array(self.daytalk, dtype=np.float32)
        return daytalk

    #最新のvote、死亡情報
    def getDayvote(self):
        #dayvote = np.array(self.dayvote, dtype=np.float32)
        return self.dayvote

class SaveLog_15():

    def __init__(self):
        self.traintalk  = []    #全てのtalk
        self.daytalk    = []    #1日のTalk
        self.gametalk   = []    #全ての加工前talk
        self.turntalk   = []    #最新ターンのtalk
        self.trainvote  = []    #全てのvote
        self.dayvote    = []    #1日のTalk
        self.train      = []    #全てのtalk&vote
        self.player     = 15

    def Target(self,tg):
        tg = tg.replace("Agent[0","")
        tg = tg.replace("Agent[", "")
        tg = tg.replace("]","")
        if(tg != None):
            idx  = int(tg)
            return idx
        return 0

    #updateで呼び出す
    #会話履歴を保存する
    def saveTalk(self,gameInfo,talk_history):

        if (talk_history != None):
            del self.turntalk[:]

            for talk in talk_history:
                self.gametalk.append(talk)
                agent   = talk['agent']
                word    = re.split(r'[,() \n]',talk['text'])
                ct = word[0]
                data = None

                '''
                #AGREE,DISAGREE
                if(ct == 'AGREE' or ct == 'DISAGREE'):
                    day = talk['text'].split()[2].replace("day","")
                    ID  = talk['text'].split()[3].replace("ID:","")
                    for i in self.gametalk:
                        if(i['day'] == day and i['idx'] == ID):
                            target_1    = i['agent']
                            content     = i['text'].split()[0]
                            data = str(agent) + str(ct) + str(target_1) + str(content)
                            if len(i['text'].split() > 1):
                                data = data + i['text'].split()[1]
                            if len(i['text'].split() > 2):
                                data = data + i['text'].split()[2]

                #REQUEST
                if(ct == "REQUEST"):
                    content = talk['text'].split("(")[1]
                    content = content.split(")")
                    content = content[0].split()

                    topic = ''
                    for i in content:
                        i.replace(' ', '')
                        topic    = topic + ' ' + i
                    data    = ' ' + str(agent) + ' ' + str(ct) + topic
                '''

                if(ct == "VOTE" or ct == "COMINGOUT" or ct == "ESTIMATE" or ct == "DIVINED" or ct == "INQUESTED"):
                    data    = ' ' + str(agent)
                    for i in word:
                        i.replace(' ', '')
                        data = data + ' ' + i

                if(data != None):
                    self.daytalk.append(data)
                    self.turntalk.append(data)

    #dayStartで呼び出す
    #投票情報や生死情報を保存する
    def saveVote(self,gameInfo):

        del self.dayvote[:]

        for vote in gameInfo['voteList']:
            agent   = vote['agent']
            target  = vote['target']

            c   = int(agent)*100 + int(target)
            self.dayvote.append(c)

        '''
        if (gameInfo["executedAgent"] != None):
            exe     = gameInfo["executedAgent"]

            c = "execute" + str(exe)
            self.dayvote.append(c)

        if gameInfo["attackedAgent"] != None:
            attack  = gameInfo["attackedAgent"]

            if(attack != -1):
                c   = "attack" + str(attack)
            else:
                c   = 0
            self.dayvote.append(c)
        '''

        self.traintalk.append(copy.deepcopy(self.daytalk))
        self.trainvote.append(copy.deepcopy(self.dayvote))
        daydata = self.daytalk + self.dayvote
        self.train.append(copy.deepcopy(daydata))
        del self.daytalk[:]

    #その日までのすべてのデータ
    def getAlldata(self):
        return self.train

    #指定した日のデータ
    def getDaydata(self,day):
        train = np.array(self.train[day-1], dtype=np.float32)
        return train

    def getTurntalk(self):
        turntalk = self.turntalk
        return turntalk

    #最新のtalk情報
    def getDaytalk(self):
        daytalk = np.array(self.daytalk, dtype=np.float32)
        return daytalk

    #最新のvote、死亡情報
    def getDayvote(self):
        #dayvote = np.array(self.dayvote, dtype=np.float32)
        return self.dayvote

class SaveLog_15_vector():

    def __init__(self):
        self.traintalk  = []    #全てのtalk
        self.daytalk    = []    #1日のTalk
        self.gametalk   = []    #全ての加工前talk

        self.turntalk   = []    #最新ターンのtalk

        self.trainvote  = []    #全てのvote
        self.dayvote    = []    #1日のTalk

        self.train      = []    #全てのtalk&vote

        self.player = 15

        #content type
        self.ctype	= {'VOTE':1,'GUARD':2,'GUARDED':3,'DIVINATION':4,
        'COMINGOUT':5,'ESTIMATE':6,'DIVINED':7,'IDENTIFIED':8,
        'AGREE':9,'DISAGREE':10,'REQUEST':11,'Over':12,'Skip':13,
        'vote':14,'execute':15,'attack':16}

        #role type
        self.rtype	= {'VILLAGER':1,'SEER':2,'MEDIUM':3,
        'BODYGUARD':4,'WEREWOLF':5,'POSSESSED':6,'HUMAN':7}

    def Normalize(self,tp,num):
    	if(tp == True):	#日付
    		maxday	= 13	#15人村最大日数
    		n_day	= 1 / maxday * num
    		return float(n_day)
    	else:			#ID
    		maxid	= 15 * 20	#15人村最大ターン数
    		n_id	= 1 / maxid * num
    		return float(n_id)

    def OneHot(self,ct,length):
    	tlist	= np.zeros(length)
    	if(ct != 0):
    		tlist[ct - 1] = 1
    	return tlist

    def Target(self,tg):
        tg = tg.replace("Agent[0","")
        tg = tg.replace("Agent[", "")
        tg = tg.replace("]","")
        if(tg != None):
            idx  = int(tg)
            return idx
        return 0

    #updateで呼び出す
    #会話履歴を保存する
    def saveTalk(self,gameInfo,talk_history):

        if (talk_history != None):
            del self.turntalk[:]

            for talk in talk_history:
                self.gametalk.append(talk)
                agent   = talk['agent']
                data    = []

                if(talk['text'].split()[0] in self.ctype):
                    ct = self.ctype[talk['text'].split()[0]]
                    data.append(self.Normalize(True,int(talk['day'])))  #day
                    data.append(self.Normalize(False,int(talk['idx']))) #ID
                else:
                    ct = 11

                #Skip or Over
                if(ct == 12 or ct == 13):
                    data.extend(self.OneHot(ct,len(self.ctype)))    #content(list)
                    data.extend(self.OneHot(agent,self.player))     #agent
                    data.extend(self.OneHot(0,self.player))			#target
                    data.extend(self.OneHot(0,len(self.rtype)))		#role
                    data.extend(self.OneHot(0,self.player))			#target2

                #VOTE or GUARD or GUARDED or DIVINATION
                elif(ct < 5):
                    target  = self.Target(talk['text'].split()[1])
                    data.extend(self.OneHot(ct,len(self.ctype)))    #content(list)
                    data.extend(self.OneHot(agent,self.player))     #agent
                    data.extend(self.OneHot(target,self.player))    #target
                    data.extend(self.OneHot(0,len(self.rtype)))		#role
                    data.extend(self.OneHot(0,self.player))			#target2

                #COMINGOUT or ESTIMATE or DIVINED or IDENTIFIED
                elif(ct > 4 and ct < 9):
                    target  = self.Target(talk['text'].split()[1])
                    role    = self.rtype[talk['text'].split()[2]]
                    data.extend(self.OneHot(ct,len(self.ctype)))     #content(list)
                    data.extend(self.OneHot(agent,self.player))      #agent
                    data.extend(self.OneHot(target,self.player))     #target
                    data.extend(self.OneHot(role,len(self.rtype)))   #role
                    data.extend(self.OneHot(0,self.player))          #target2

                #AGREE or DISAGREE
                elif(ct == 9 or ct == 10):
                    day = talk['text'].split()[2].replace("day","")
                    ID  = talk['text'].split()[3].replace("ID:","")
                    data.extend(self.OneHot(ct,len(self.ctype)))    #content
                    data.extend(self.OneHot(agent,self.player))          #agent
                    check = False
                    for i in self.gametalk:
                        if(i['day'] == day and i['idx'] == ID):
                            data.extend(self.OneHot(i['agent'],self.player))	#target
                            check = True
                    if(not check):
                        data.extend(self.OneHot(0,self.player))
                    data.extend(self.OneHot(0,len(self.rtype)))		#role
                    data.extend(self.OneHot(0,self.player))			#target2

                '''
                #REQUEST
                elif(ct == 11):
                    #Target1が指定されていない
                    if talk['text'].split(0) in self.ctype:
                        ar	= OneHot(self.ctype[talk['text'].split(0)],len(self.ctype))
                        ar[11-1]	= 1		#REQとctに1
                        target  = self.Target(talk['text'].split()[1])
                        data.extend(ar)						#content(list)
                        data.extend(OneHot(agent,self.player))	#agent
                        data.extend(OneHot(0,self.player))				#target
                        data.extend(OneHot(0,len(self.rtype)))			#role
                        data.extend(OneHot(Target(word[7]),self.player))	#target2

                    else:
                        ar	= OneHot(cttype[word[7]],len(cttype))
                        ar[11-1]	= 1		#REQとctに1
                        data.extend(ar)						#content(list)
                        data.extend(OneHot(int(word[4]),self.player))		#agent
                        data.extend(OneHot(Target(word[6]),self.player))	#target
                        data.extend(OneHot(0,len(rotype)))				#role
                        data.extend(OneHot(Target(word[8]),self.player))	#target2
                '''

                if(data != None):
                    if(len(data) == 70):
                        self.daytalk.append(data)
                        self.turntalk.append(data)

    #dayStartで呼び出す
    #投票情報や生死情報を保存する
    def saveVote(self,gameInfo,day):

        del self.dayvote[:]

        for vote in gameInfo['voteList']:
            data    = []
            agent   = vote['agent']
            target  = vote['target']
            ct      = self.ctype['vote']

            data.append(self.Normalize(True,day))        #day
            data.append(0)						    #ID
            data.extend(self.OneHot(ct,len(self.ctype))) #content
            data.extend(self.OneHot(agent,self.player))	#agent
            data.extend(self.OneHot(target,self.player))	#target
            data.extend(self.OneHot(0,len(self.rtype)))	#role
            data.extend(self.OneHot(0,self.player))		#target2

            self.dayvote.append(copy.deepcopy(data))

        if (gameInfo["executedAgent"] != None):
            data    = []
            exe     = gameInfo["executedAgent"]
            ct      = self.ctype['execute']
            data.append(self.Normalize(True,day))	    #day
            data.append(0)							#ID
            data.extend(self.OneHot(ct,len(self.ctype))) #content
            data.extend(self.OneHot(exe,self.player))    #agent
            data.extend(self.OneHot(0,self.player))		#target
            data.extend(self.OneHot(0,len(self.rtype)))	#role
            data.extend(self.OneHot(0,self.player))		#target2

            self.dayvote.append(copy.deepcopy(data))

        if gameInfo["attackedAgent"] != None:
            data    = []
            attack  = gameInfo["attackedAgent"]
            ct      = self.ctype['attack']
            data.append(self.Normalize(True,day))	        #day
            data.append(0)							#ID
            data.extend(self.OneHot(ct,len(self.ctype))) #content

            if(attack != -1):
                data.extend(self.OneHot(attack,self.player))	#agent
            else:
                data.extend(self.OneHot(0,self.player))

            data.extend(self.OneHot(0,self.player))		#target
            data.extend(self.OneHot(0,len(self.rtype)))	#role
            data.extend(self.OneHot(0,self.player))

            self.dayvote.append(copy.deepcopy(data))

        self.traintalk.append(copy.deepcopy(self.daytalk))
        self.trainvote.append(copy.deepcopy(self.dayvote))
        daydata = self.daytalk + self.dayvote
        self.train.append(copy.deepcopy(daydata))
        del self.daytalk[:]

    #その日までのすべてのデータ
    def getAlldata(self):
        return self.train

    #指定した日のデータ
    def getDaydata(self,day):
        train = np.array(self.train[day-1], dtype=np.float32)
        return train

    def getTurntalk(self):
        turntalk = np.array(self.turntalk, dtype=np.float32)
        return turntalk

    #最新のtalk情報
    def getDaytalk(self):
        daytalk = np.array(self.daytalk, dtype=np.float32)
        return daytalk

    #最新のvote、死亡情報
    def getDayvote(self):
        dayvote = np.array(self.dayvote, dtype=np.float32)
        return dayvote

# coding=utf-8
import argparse
from chainer import serializers
from chainer.dataset import convert
import chainer
import chainer.functions as F
import chainer.links as L
from chainer import Variable, training, Chain
from chainer.training import extensions
from chainer import reporter as reporter_module
from chainer import function
import numpy as np
from chainer.functions.evaluation import accuracy
from chainer.functions.loss import softmax_cross_entropy
from chainer import link
import copy
import os,shelve,math

class Predictor_05():

    def __init__(self):
        #繝｢繝・Ν隱ｭ縺ｿ霎ｼ縺ｿ
        base = os.path.dirname(os.path.abspath(__file__))
        name = os.path.normpath(os.path.join(base, './wolf.model'))
        self.model_name = name
        self.lstm = None

        #ID隱ｭ縺ｿ霎ｼ縺ｿ
        d = os.path.normpath(os.path.join(base, './ID_05'))
        dic = shelve.open(d)
        self.vocab = dic['dic']
        dic.close()

    def initialize(self,player):
        #繝代Λ繝｡繝ｼ繧ｿ險ｭ螳
        self.n_player = player
        self.n_input = 700
        self.embedding = 150
        self.n_units = 500
        self.model  = self.load_model(self.model_name, self.n_player)
        self.death_list = []

    def set_death_list(self,death_list):
        self.death_list = death_list

    def load_model(self, model_name, n_player):
        self.lstm    = LSTM_05(self.n_input, self.n_player, self.embedding, self.n_units)
        model   = L.Classifier(self.lstm)
        serializers.load_npz(model_name, model)
        return model

    def predict(self,data):
        newdata = []

        for i in data:
            if i in self.vocab:
                newdata.append(self.vocab[i])
            else:
                print(len(self.vocab)+1 , i)
                if(len(self.vocab) + 1 < self.n_input):
                    self.vocab[i] = len(self.vocab) + 1
                    newdata.append(self.vocab[i])
                else:
                    self.vocab[i] = len(self.vocab)
                    newdata.append(self.vocab[i])

        for i in newdata:
            x = chainer.Variable(np.asarray([int(i)]).astype(np.int32))
            y = self.model.predictor(x)

        y_copy  = copy.deepcopy(y)
        exp     = []
        softmax = []
        sum         = 0

        for i in range(self.n_player):
            if(self.death_list[i] == 0):
                y_copy.data[0][i+1] = float("-inf")
                exp.append(y_copy.data[0][i+1])
            else:
                exp.append(math.exp(y_copy.data[0][i+1]))
                sum += math.exp(y_copy.data[0][i+1])

        softmax.append(float("-inf"))
        for i in exp:
            if(i != float("-inf")):
                softmax.append(float(i/sum))
            else:
                softmax.append(i)

        return softmax

    def finish(self):
        self.lstm.reset_state()

class LSTM_05(chainer.Chain):
    def __init__(self, n_input, n_player, embedding, n_units, train = True, ignore_label=-1):
        super(LSTM_05, self).__init__(
            embed=L.EmbedID(n_input, embedding),
            l1=L.LSTM(embedding, n_units),
            l2=L.Linear(n_units, n_player+1),
        )
        self.player = n_player

    def __call__(self, x):
        h0 = self.embed(x)
        h1 = self.l1(h0)
        y = self.l2(F.relu(h1))
        return y

    def reset_state(self):
        self.l1.reset_state()

def new_function(self, y, t):
    sum = F.sum(F.softmax(y) * t)
    return -F.sum(F.log(F.softmax(y)) * t)

class Predictor_15():

    def __init__(self):
        #繝｢繝・Ν隱ｭ縺ｿ霎ｼ縺ｿ
        base = os.path.dirname(os.path.abspath(__file__))
        name = os.path.normpath(os.path.join(base, './wolf_15.model'))
        self.model_name = name

        #ID隱ｭ縺ｿ霎ｼ縺ｿ
        d = os.path.normpath(os.path.join(base, './ID_15'))
        dic = shelve.open(d)
        self.vocab = dic['dic']
        dic.close()

        self.n_player = 15
        self.n_input = 3000
        self.embedding = 300
        self.n_units = 1000
        self.lstm    = LSTM_15(self.n_input, self.n_player, self.embedding, self.n_units)


    def initialize(self,player):
        self.death_list = []
        self.model  = self.load_model(self.model_name, self.n_player)

    def set_death_list(self,death_list):
        self.death_list = death_list

    def load_model(self, model_name, n_player):
        model   = L.Classifier(self.lstm,lossfun = new_function)
        serializers.load_npz(model_name, model)
        return model

    def predict(self,data):
        newdata = []

        for i in data:
            if i in self.vocab:
                newdata.append(self.vocab[i])
            else:
                if(len(self.vocab) + 1 < self.n_input):
                    self.vocab[i] = len(self.vocab) + 1
                    newdata.append(self.vocab[i])
                else:
                    self.vocab[i] = len(self.vocab)
                    newdata.append(self.vocab[i])

        for i in newdata:
            x = chainer.Variable(np.asarray([int(i)]).astype(np.int32))
            y = self.model.predictor(x)

        y_copy  = copy.deepcopy(y)
        exp     = []
        softmax = []
        sum         = 0

        for i in range(self.n_player):
            if(self.death_list[i] == 0):
                y_copy.data[0][i] = float("-inf")
                exp.append(y_copy.data[0][i])
            else:
                exp.append(math.exp(y_copy.data[0][i]))
                sum += math.exp(y_copy.data[0][i])

        for i in exp:
            if(i != float("-inf")):
                softmax.append(float(i/sum))
            else:
                softmax.append(-1)

        return softmax

    def finish(self):
        self.lstm.reset_state()

class LSTM_15(chainer.Chain):
    def __init__(self, n_input, n_player, embedding, n_units, train = True, ignore_label=-1):
        super(LSTM_15, self).__init__(
            embed=L.EmbedID(n_input, embedding),
            l1=L.LSTM(embedding, n_units),
            l2=L.Linear(n_units, n_player),
        )
        self.player = n_player

    def __call__(self, x):
        h0 = self.embed(x)
        h1 = self.l1(h0)
        y = self.l2(F.relu(h1))
        return y

    def reset_state(self):
        self.l1.reset_state()

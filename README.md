# aiwolf
これはAIWolf用の強化学習エージェントです．
各エージェントのCO,Estimate,Vote,Talk,Talk回数，生存，日付，自分の役職を状態とし．それぞれのエージェントに対する村人から見たkill度をDDQNによって最適化をしています．
エージェントの実行には，./Autostarter.shを実行した後，python3 DQN.py -h localhost -p 10000を実行してください．
また，実行にはtensorflow,pandasが必要です．
このエージェントにはHarada　Kei氏のAIWolfPyが使われています．

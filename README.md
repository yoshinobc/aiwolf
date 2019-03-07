# aiwolf
これはAIWolf用の強化学習エージェントです.  
各エージェントのCO,Estimate,Vote,Talk,Talk回数，生存，日付，自分の役職を状態とし．それぞれのエージェントに対する村人から見たkill度をDDQNによって最適化をしています.  
エージェントの実行には,  
`bash Autostarter.sh`  
を実行した後,  
`python3 DQN.py -h localhost -p 10000`  
を実行してください.  
また，実行にはtensorflow,pandasが必要です.  
Autostarter.iniを変更することで任意のエージェントに対して学習を行うことができます．

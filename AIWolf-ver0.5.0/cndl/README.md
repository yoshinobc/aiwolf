# aiwolf-cndl@cedec2018

第4回人狼知能大会に提出したプログラムのソースコードです。

## エージェントのクラスパス

jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.CndlPlayer

## エージェント名

cndl

## ビルド

依存ライブラリはaiwolfライブラリ、fastText4jの利用しているguava-19.0、またFindCndlModelで利用している過去のcndlのエージェントです。FindCndlModelを使わない場合にはcndlのエージェントは不要です。

なお、fastTextを使用するためには、学習済みのモデル(bin）が必要になるので、別途準備が必要です。

## 実行

toolsのRunServer.javaからテスト実行することが可能です。

また、RunLocalは、サーバを立ち上げずに複数回のゲームを実行します。他のエージェントのjarを持ってくれば、
指定して戦わせることも可能です。

さらに、RunGameLogは、GameLogをシミュレートしてゲームを実行可能です。様々なGameLogの読込には、LogReaderの各種実装を利用します。


## カスタマイズする場合

### Tacticの変更

CndlTalkTacticかCndlTargetTacticを継承したクラスを作成し、各Roleの適切なtacticsにaddして下さい。

### Modelの変更

好きなモデルのクラスを作って、CndlStrategyの中でインスタンスを生成してください。CndlStrategyは各Tacticsに渡されるので、好きな場所からモデルを呼ぶことが可能です。

### Roleの変更

新しい役職の実装を作った場合、RandomRoleSelectorで指定して下さい。役職の実装を複数登録してランダムに選ばせたり、RandomRoleSelectorを修正することで状況に応じて切り替えることなども可能です。

### さらに根本的な変更

AbstractRoleBaseStrategyは、Roleに行動の決定を委譲するStrategyです。これを継承して独自のStrategyを作ることも可能です。

さらに、AbstractStrategyはもう一段階抽象度が高く、各行動の決定も実装する必要があります。これを継承することも可能です。

いずれにせよ、Playerクラスは、AbstractPlayerを継承して、適切なStrategyを生成するメソッドを実装する必要があります。

### リスナーについて

Strategyには3種類のリスナーを登録できます。ゲームの開始終了時に発火するMetagameListener、アクション（投票や襲撃）の前後に発火するGameActionListener、イベント（情報の更新）後に発火するGameEventListenerです。


モデルはこれらのアクションにフックして処理を書くことを推奨します。

はじめに

本リポジトリは Java 学習用 に作成した Minecraft プラグイン OreRush に関するものです。
ご利用によるトラブル等については一切責任を負いかねます。

注意事項
ゲーム開始時にプレイヤーをあらかじめ設定した座標へテレポートさせます。
この座標は開発環境のワールド固定値のため、他のワールドでは地中に埋まる可能性があります。
ご利用の際は config.yml 等で 安全なワープ先を事前設定 してください。

コンセプト

「30 秒の制限時間内に鉱石を掘り、スコアを競う」シンプルなミニゲーム

/orerush 実行で専用エリアにワープし、30 秒間の採掘ゲームがスタート

採掘した鉱石の種類に応じてスコアが加算

ゲーム概要

制限時間: 30 秒

スコア配点

銅: 10 点

金: 20 点

ダイヤモンド: 50 点

終了時に合計スコアを表示し、MySQL に記録

orerush list で直近 5 件のスコアを表示

デモ動画


※ クリックで YouTube を開きます。

動作環境

開発言語: Java Oracle OpenJDK 17.0.10

アプリケーション: Minecraft 1.21.8

サーバ: Spigot 1.21

コマンド
/orerush

採掘ゲームを開始します

プレイヤーに専用装備（鉄防具＋鉄のツルハシ）を付与し、スタート地点へテレポート

/orerush list

直近 5 件のスコアをデータベースから取得して表示

データベース構造（MySQL）
Field	Type	Null	Key	Default	Extra
id	int	NO	PRI	NULL	auto_increment
player_name	varchar(100)	YES		NULL	
score	int	YES		NULL	
difficulty	varchar(30)	YES		NULL	
registered_at	datetime	YES		NULL	
今後の改善予定

パッケージ名の修正（小文字に統一）

クラス構成の見直し（責務分離・保守性向上）

MyBatis の導入（現在は生 JDBC）

命名規則の統一（キャメルケース、マジックナンバーの定数化）

おわりに

学習のアウトプットとして作成したプラグインです。
感想やフィードバックをいただけると励みになります。

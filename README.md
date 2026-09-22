# Java & Spring Quest

JavaとSpringを日本語で学ぶ、全12問のブラウザーゲーム。Java 21 / Spring Boot 4.1.1 で実装しています。

## 起動

Java 21とMaven 3.6.3以上をインストールして、プロジェクトのフォルダーで実行します。

```sh
mvn spring-boot:run
```

[http://localhost:8080](http://localhost:8080) をブラウザーで開いてください。終了は Ctrl+C。
初回ビルドには依存ライブラリを取得するネット接続が必要です。

```sh
mvn verify
mvn package
java -jar target/spring-quest-1.0.0.jar
```

ポートを変える場合: `java -jar target/spring-quest-1.0.0.jar --server.port=8081`

## 遊び方

- 選択肢を選んで「答えをチェック」。不正解でも何度でも挑戦できます。
- 初回クリアで100 XP。3問ごとにレベルアップし、全問クリアで「開発の勇者」になります。
- ヒントと正解後の解説を読みながら、マップから自由に復習できます。
- クリア記録はブラウザーのlocalStorageに保存。アカウントやデータベースは不要です。
- 進捗リセットは確認ダイアログ付きです。スマートフォンは冒険マップを横スクロールできます。

学習範囲: 型、条件分岐、繰り返し、文字列比較、クラス、ジェネリクス、Spring Bootの起動、REST、コンストラクターDI、Service、JSON本文、パス変数。
コード穴埋めは選択式で、入力コードのコンパイル・実行機能はありません。

## ソースを読んで学ぶ

`src/main/java/com/example/quest` に実際のSpring実装があります。

- `QuestApplication`: アプリの起動
- `QuestController`: 問題取得と解答送信のREST API
- `QuestService`: 問題・正誤判定・解説。ここへ問題を追加できます。
- `src/main/resources/static`: HTML / CSS / JavaScript の画面

`GET /api/quests` は正答を含めずに問題を返します。
`POST /api/quests/{id}/answers` に `{"option":1}` のように0始まりの番号を送信すると、正誤・解説・XPが返ります。
不正な番号は400、存在しない問題は404です。

これはローカル学習用アプリです。XPの重複防止はブラウザー側で行い、サーバーはユーザーや進捗を管理しません。ブラウザーのデータ消去で記録は失われます。
標準では127.0.0.1のみで待ち受けます。フォントの外部読み込みができなくてもシステムフォントで利用できます。

## 検証とブランチ

`mvn verify` で実HTTPによる画面配信、問題取得、正誤判定、入力エラーと全12問の整合性を確認します。GitHub Actionsでも同じ検証を実行します。
開発は `codex/java-spring-quest`、統合先は `develop` です。

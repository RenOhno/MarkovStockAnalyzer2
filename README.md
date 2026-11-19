# MarkovStockAnalyzer2

[![CI](https://github.com/RenOhno/MarkovStockAnalyzer2/actions/workflows/ci.yml/badge.svg)](https://github.com/RenOhno/MarkovStockAnalyzer2/actions/workflows/ci.yml)

マルコフ連鎖を用いた株価の上昇・下落傾向分析を行う、ゼミ研究向けの検証ツール。
前身プロジェクト MarkovStockAnalyzer の作り直し版。

## 目的

マルコフ連鎖の理論（最尤推定・定常分布・収束）が、実際の株価データでどこまで成り立つかを検証する。
予測の良し悪しを感覚や当たり外れではなく、確率分布とベースライン比較によって定量的に評価する。

- 使用者：開発者本人、および指導教員・ゼミ生（ゼミ発表での説明・議論の場面を想定）
- 一般の個人投資家向けツールではない。銘柄の推奨や売買シグナルの提示は行わない

## 機能

1. 状態分類と遷移の表示：日次リターンを3状態（U / F / D）に分類し、遷移回数行列と遷移確率行列を表示
2. 状態分布と収束の可視化：h 営業日後の状態分布 α^(h) = α^(0) P̂^h と、定常分布への収束過程をグラフ表示
3. バックテスト：walk-forward 方式で Accuracy・Brier スコアなどを Majority・Persistence と比較
4. 結果の保存：分析条件と結果を保存し、後から再表示できる

## 数学仕様

- 日次リターン r_t = C_t / C_{t−1} − 1（C は調整後終値）
- 状態分類の閾値 θ = 0.005（±0.5%）
- 遷移確率は最尤推定 p̂_ij = n_ij / n_i、n_i = 0 の行は「未推定」（一様分布で埋めない）
- バックテストは walk-forward（拡大ウィンドウ）方式。最小学習数 30（遷移の本数）から開始し、各時点ではそれまでの遷移だけで推定した行列の、現在の状態の行を翌営業日の予測分布とする。現在の状態の行が未推定の場合は予測せずスキップする
- ベースライン：Majority（学習期間の状態分布をそのまま予測分布とする）、Persistence（現在の状態の一点分布）
- 評価指標：Accuracy / Precision / Recall / 混同行列 / Brier スコア / Log Loss

## 技術構成

| 区分 | 技術 |
|---|---|
| バックエンド | Java 21 / Spring Boot 4.0.2 / Maven、JPA + Flyway |
| 分析サービス | Python 3.12 / FastAPI、pandas・numpy・yfinance・exchange-calendars、依存管理は uv |
| データベース | MySQL 8.4 |
| フロントエンド | 静的 HTML / CSS / JavaScript（バックエンドが配信） |
| 実行環境 | Docker Compose（mysql / analysis / backend） |

## 起動方法

```bash
cp .env.example .env
# .env の3つのシークレット（MYSQL_PASSWORD / MYSQL_ROOT_PASSWORD / INTERNAL_API_TOKEN）を
# それぞれ独立したランダムな値に書き換える
docker compose up --build
```

起動後、http://localhost:8080 で画面が表示される。

## 開発ステータス

STEP 5（バックテストと比較表示）まで完了。分析結果には定常分布、h営業日後（1・3・5・10）の状態分布、現在の状態から定常分布へ収束していく過程のグラフが含まれる。バックテストでは walk-forward 方式でマルコフ予測を Majority・Persistence と比較し、Accuracy・Precision・Recall・Brier スコア・Log Loss と混同行列を表示する。結果はいずれも DB に保存され、保存済み一覧から再表示できる。バックエンド API は POST / GET /api/analyses と POST / GET /api/backtests。push のたびに GitHub Actions でバックエンドのビルド、分析サービスのテスト、Docker Compose のビルドと起動確認が自動実行される。機能実装は計画書に基づき段階的に進める。
計画書は開発者手元の `MarkovStockAnalyzer2_計画書.md` を参照。

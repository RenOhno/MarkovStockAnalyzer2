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

STEP 3（バックエンド連携と画面表示）まで完了。画面から銘柄・期間を指定して分析でき、結果（状態内訳・遷移回数・遷移確率）は DB に保存され、保存済み一覧から再表示できる。バックエンド API は POST / GET /api/analyses。push のたびに GitHub Actions でバックエンドのビルド、分析サービスのテスト、Docker Compose のビルドと起動確認が自動実行される。機能実装は計画書に基づき段階的に進める。
計画書は開発者手元の `MarkovStockAnalyzer2_計画書.md` を参照。

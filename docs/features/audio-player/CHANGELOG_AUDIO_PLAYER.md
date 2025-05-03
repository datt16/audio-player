# AudioPlayer 変更ログ

## [Unreleased]

### Added
- `CustomRenderersFactory`クラスの追加
  - ExoPlayerのレンダラーをカスタマイズする機能を追加
  - 特殊なオーディオ形式のサポートを追加

- `DownloadWorkerFactory`クラスの追加
  - WorkManagerのワーカー生成をカスタマイズ
  - ダウンロード処理の柔軟な設定を可能に

- `DownloadManagerBuilder`クラスの追加
  - ダウンロードマネージャーの構築を簡素化
  - 設定のカスタマイズ機能を追加

- `DataSourceType`列挙型の追加
  - データソースの種類を明確に定義
  - キャッシュ戦略の柔軟な設定を可能に

### Changed
- `ExoPlayerPlaybackManager`の改善
  - キャッシュ対応のDataSourceFactoryの使用を実装
  - 再生進捗の監視機能を最適化

- `DownloadController`の改善
  - ダウンロード管理の効率化
  - エラーハンドリングの強化

### Fixed
- オーディオ再生の安定性向上
- メモリリークの修正
- ダウンロード処理の安定性向上

## [2024-04-28] - 1.0.0

### Added
- 基本的なオーディオ再生機能の実装
- ダウンロード機能の実装
- 音声処理機能の実装

### Changed
- 初期リリース
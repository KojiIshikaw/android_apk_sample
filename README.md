# MyApplication

## ビルドとインストール手順

### デバッグビルド
```sh
# デバッグAPKのビルド
./gradlew assembleDebug

# ビルドされたAPKの場所
app/build/outputs/apk/debug/app-debug.apk
```

### リリースビルド
```sh
# リリースAPKのビルド
./gradlew assembleRelease

# ビルドされたAPKの場所
app/build/outputs/apk/release/app-release.apk
```

### 実機での動作確認

1. 実機の準備
   - 設定 → 端末情報 → ビルド番号を7回タップ（開発者オプションを有効化）
   - 設定 → 開発者オプション → USBデバッグをオン

2. 端末の接続確認
```sh
adb devices
```

3. APKのインストール
```sh
# デバッグAPKのインストール
adb install -r app/build/outputs/apk/debug/app-debug.apk

# リリースAPKのインストール
adb install -r app/build/outputs/apk/release/app-release.apk
```

4. アプリの起動
```sh
# アプリの起動
adb shell am start -n com.example.myapplication/.MainActivity
```

注意事項：
- リリースビルドを作成する場合は、`app/build.gradle`に署名設定（signingConfig）が必要です
- 実機にインストールする際は、端末の「不明なソース」のインストールを許可する必要がある場合があります


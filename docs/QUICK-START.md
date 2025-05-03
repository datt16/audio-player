# このアプリの起動の仕方

## アプリの起動の仕方
### 1. プロジェクトをクローンする

### 2. アプリをビルドし起動する
AndroidStudio で "Make Project" するか "Run 'app'" する。(後者は手順3も含める)
もしくは以下のコマンドを実行する

```shell
./gradlew app:installDebug
```


## メディアサーバーの起動の仕方(optional)
このアプリはホストマシン上でAPIサーバーを立ち上げて、そのサーバー上の音声ファイルを再生する機能を持っています。
必要に応じて以下の手順でAPIサーバーを立ち上げた上でアプリを動作させてください。
(エミュレータ上で通信するための `http://10.0.2.2:8888` は通信許可してますが、  
他のポートやアドレス使う場合はいい感じに編集してください。)

### 1. メディアサーバープロジェクトをクローンする
```shell
git clone git@github.com:datt16/media-hosting.git
```

### 2. アセットファイルを追加する
```shell
cd media-hosting
mkdir -p assets/m4a
cp {m4a_file_path} assets/m4a/
```

### 3. メディアサーバーコンテナをビルドする
```shell
podman build -t media-nginx .
```

### 4. メディアサーバーを起動する
```shell
podman run -d -p 8888:8888 -p 3000:3000 -v $(pwd)/assets:/assets:ro media-nginx
```


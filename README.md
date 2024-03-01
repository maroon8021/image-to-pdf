# 画像を PDF に変換しようとしたときに画像の方向が変になる現象をしらべた件

## 結論

EXIF 情報による向きの変更をしているものを PDF に変換しようとした際に、EXIF 情報が無視される。

## 雑感

### typescript でも kotlin でも再現した

言語やライブラリによって回転したりしなかったりするのか気になったので typescript と kotlin 両方で調べてみた。
どちらの言語でも発生したので、画像側の問題(もしくはライブラリが対応しているか)だということが判明

### ライブラリの問題

ライブラリによっては exif 情報を参照してくれることもあるらしいが、対応してないものもある。
ex: https://github.com/bpampuch/pdfmake/issues/2316

## 起動方法

frontend / proxy / backend-typescript か backend-kotlin のどちらかを起動する必要がある

### frontend

```sh
cd ./frontend
npm i
npm run start
```

### beckend-typescript

```sh
cd ./backend-typescript
npm i
npm run server
```

### proxy

```sh
# project root
npm i
npm run proxy
```

画像をバックエンドから取得する際に same-origin じゃないとちゃんと取得することができないので必要

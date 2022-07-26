# a2pr-j Plain Text to Printer

長らく a2ps が日本語を通してくれない問題が存在したので、これを解決すべく印刷用のプログラムを作成した。
![example](https://raw.githubusercontent.com/maildrop/a2pr-j/master/example.png)

なぜJavaで書いたか？

マルチプラットフォームで印刷させるのに都合がよかったから。

## How to build. Use Apache Maven

use maven 
```
mvn package
```

and copy target\a2pr-1.1-SNAPSHOT-jar-with-dependencies.jar in the same directory as a2pr.cmd

```
copy target\a2pr-1.1-SNAPSHOT-jar-with-dependencies.jar .
```

## command line options 

```
java -jar a2pr-1.1-SNAPSHOT-jar-with-dependencies.jar -h
usage: a2pr
 -e,--encoding <arg>   specify a file encoding
 -f,--file <arg>       target file
 -h,--help             display this help message
    --utf-8            specify a file encoding as utf-8
```

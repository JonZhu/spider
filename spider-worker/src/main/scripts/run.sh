#!/bin/bash

basedir=$(cd `dirname $0`/../; pwd)
echo $basedir
nohup java -cp $basedir:$basedir/lib/* -Dfile.encoding=UTF-8 com.zhujun.spider.worker.Startup 8601 >> $basedir/spider.log 2>&1 &

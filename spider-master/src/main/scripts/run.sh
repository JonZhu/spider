#!/bin/bash

basedir=$(cd `dirname $0`/../; pwd)
echo $basedir
cd $basedir
nohup java -cp $basedir:$basedir/lib/* -Dfile.encoding=UTF-8 -Dspring.profiles.active=prod com.zhujun.spider.master.Startup  > /dev/null 2>&1 &


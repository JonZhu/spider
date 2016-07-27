:: bat for run spider master
echo off

set base_dir=%~dp0..\
cd %base_dir%

java -cp .;./lib/*  com.zhujun.spider.master.Startup
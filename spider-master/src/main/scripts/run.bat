:: bat for run spider master
echo off

set base_dir=%~dp0../
cd %base_dir%

start java -cp %base_dir%;%base_dir%/lib/* -Dfile.encoding=UTF-8 com.zhujun.spider.master.Startup
:: bat for run spider worker
echo off

set base_dir=%~dp0..\
cd %base_dir%

java -cp .;./lib/* -Dfile.encoding=UTF-8 com.zhujun.spider.worker.Startup 127.0.0.1 8619
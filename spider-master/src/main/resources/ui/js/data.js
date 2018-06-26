$(function(){

    var urlPageOffset = []; // url分页offset
    var currentShowTaskId; // 当前显示的任务id
    var currentShowDatafileName; // 当前显示的数据文件
    var currentUrlPageNo = 0; // 当前url页号
    var urlPageSize = 100; // url数据分页大小
    var maxUrlOffset = 0; // 最大url offset位置

    // 获取url参数
    var urlParams = util.url.param(window.location.href);

    // 查询数据文件列表
    function queryDataFileList(taskId) {
        $.ajax({
            url: 'api/data/datafilelist',
            method: 'get',
            data: {taskId: taskId},
            dataType: 'json',
            success: function(result) {
                if (result.status == 0) {
                    showDataFileList(taskId, result.data);
                } else {
                    // 查询失败
                    alert(result.msg);
                }
            }
        });
    }

    // 显示数据列表
    function showDataFileList(taskId, fileList) {
        currentShowTaskId = taskId;
        $("#dataCount").text(fileList ? fileList.length : 0); // 设置文件数量
        var fileSizeSum = 0;
        var $tbody = $("#dataListTable tbody");
        $tbody.empty();
        if (fileList != null && fileList.length > 0) {
            fileList.forEach(function (dataFile, i) {
                var $tr = $('<tr></tr>');
                var $name = $("<a></a>").text(dataFile.name).click(function(){
                    queryUrlList(taskId, dataFile.name, 0);
                });
                $('<td></td>').append($name).appendTo($tr); // 名称
                $('<td></td>').text(util.byte.adapt(dataFile.size)).appendTo($tr); // 大小
                $('<td></td>').text(util.time.ms2Str(dataFile.createTime)).appendTo($tr); // 创建时间
                $('<td></td>').text(util.time.ms2Str(dataFile.modifyTime)).appendTo($tr); // 修改时间
                $tbody.append($tr);

                fileSizeSum += dataFile.size ? dataFile.size : 0;
            });
        }

        $("#dataSizeSum").text(util.byte.adapt(fileSizeSum)); // 设置文件大小合计
    }

    if (urlParams.taskId) {
        // 如果存在参数,增加任务数据文件
        queryDataFileList(urlParams.taskId);
    }

    // 查询datafile中的url
    function queryUrlList(taskId, dataFileName, offset, pageNo) {
        $.ajax({
            url: 'api/data/metadata',
            method: 'get',
            data: {
                taskId: taskId,
                dataFileName: dataFileName,
                offset: offset,
                count: urlPageSize
            },
            dataType: 'json',
            success: function(result) {
                if (result.status == 0) {
                    showUrlList(taskId, dataFileName, result.data, pageNo)
                } else {
                    alert(result.msg);
                }
            }
        });
    }

    // 显示url数据列表
    function showUrlList(taskId, dataFileName, urlList, pageNo) {
        currentShowDatafileName = dataFileName;
        var $tbody = $("#urlListTable tbody");
        $tbody.empty();
        $("#urlListModal").modal("show");
        if (urlList != null && urlList.length > 0) {
            pageNo = (pageNo == null || pageNo < 1 ? 1 : pageNo);
            setCurrentUrlPageNo(pageNo);
            if (pageNo == 1) {
                urlPageOffset = []; // 重置分页数据
            }
            urlPageOffset[pageNo] = urlList[0].offset;
            maxUrlOffset = urlList[urlList.length - 1].offset;

            urlList.forEach(function (data, i) {
                var $tr = $('<tr></tr>');
                var dataUrl = 'api/data/filedata?taskId='+taskId + "&offset="+data.offset+"&dataFileName="+dataFileName;
                var $url = $('<a target="_blank"></a>').text(data.url).attr('href', dataUrl);
                $('<td></td>').append($url).appendTo($tr); // url
                $('<td></td>').text(data.contentType).appendTo($tr); // Content-Type
                $('<td></td>').text(util.byte.adapt(data.size)).appendTo($tr); // 大小
                $('<td></td>').text(util.time.ms2Str(data.fetchTime)).appendTo($tr); // 抓取时间
                $('<td></td>').text(data.offset).appendTo($tr);// offset
                $tbody.append($tr);
            });
        }
    }

    // 设置当前url分页号
    function setCurrentUrlPageNo(pageNo) {
        currentUrlPageNo = pageNo;
        $("#urlPageNo").text(pageNo);
    }

    // 绑定url分页按钮，上一页
    $("#urlPrePageBtn").click(function(){
        if (currentUrlPageNo < 2) {
            alert("没有上页");
            return;
        }

        var newPage = currentUrlPageNo - 1;
        queryUrlList(currentShowTaskId, currentShowDatafileName, urlPageOffset[newPage], newPage);
    });

    // 绑定url分页按钮，下一页
    $("#urlNextPageBtn").click(function(){
        var newPage = currentUrlPageNo + 1;
        queryUrlList(currentShowTaskId, currentShowDatafileName, maxUrlOffset + 7, newPage);
    });

});
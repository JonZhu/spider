$(function(){

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
        var $tbody = $("#dataListTable tbody");
        $tbody.empty();
        if (fileList != null && fileList.length > 0) {
            fileList.forEach(function (dataFile, i) {
                var $tr = $('<tr></tr>');
                var $name = $("<a></a>").text(dataFile.name).click(function(){
                    queryUrlList(taskId, dataFile.name, 0, 50);
                });
                $('<td></td>').append($name).appendTo($tr); // 名称
                $('<td></td>').text(util.byte.adapt(dataFile.size)).appendTo($tr); // 大小
                $('<td></td>').text(util.time.ms2Str(dataFile.createTime)).appendTo($tr); // 创建时间
                $('<td></td>').text(util.time.ms2Str(dataFile.modifyTime)).appendTo($tr); // 修改时间
                $tbody.append($tr);
            });
        }
    }

    if (urlParams.taskId) {
        // 如果存在参数,增加任务数据文件
        queryDataFileList(urlParams.taskId);
    }

    // 查询datafile中的url
    function queryUrlList(taskId, dataFileName, offset, count) {
        $.ajax({
            url: 'api/data/metadata',
            method: 'get',
            data: {
                taskId: taskId,
                dataFileName: dataFileName,
                offset: offset,
                count: count
            },
            dataType: 'json',
            success: function(result) {
                if (result.status == 0) {
                    showUrlList(taskId, dataFileName, result.data)
                } else {
                    alert(result.msg);
                }
            }
        });
    }

    // 显示url数据列表
    function showUrlList(taskId, dataFileName, urlList) {
        var $tbody = $("#urlListTable tbody");
        $tbody.empty();
        $("#urlListModal").modal("show");
        if (urlList != null && urlList.length > 0) {
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

    // $("#urlListModal").modal("show");

});
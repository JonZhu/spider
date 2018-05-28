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
                    showDataFileList(result.data);
                } else {
                    // 查询失败
                    alert(result.msg);
                }
            }
        });
    }

    // 显示数据列表
    function showDataFileList(fileList) {
        var $tbody = $("#dataListTable tbody");
        $tbody.empty();
        if (fileList != null && fileList.length > 0) {
            fileList.forEach(function (dataFile, i) {
                var $tr = $('<tr></tr>');
                $('<td></td>').text(dataFile.name).appendTo($tr); // 名称
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

});
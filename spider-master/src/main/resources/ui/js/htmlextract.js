$(function(){

    $('#doExtractBt').click(function(){
        var url = $("#url").val();
        var extractConfig = $("#extractConfig").val();

        extractHtml(url, extractConfig);
    });

    function extractHtml(url, extractConfig) {
        $.ajax({
            url: 'api/html/extract/url',
            method: 'post',
            data: {
                url: url,
                extractConfig: extractConfig
            },
            dataType: 'json',
            success: function(result) {
                if (result.status == 0) {
                    showExtractResult(result.data);
                    alert("抽取成功")
                } else {
                    alert(result.msg);
                }
            }
        });
    }

    function showExtractResult(extractResult) {
        $("#extractResult").val(JSON.stringify(extractResult, null, 2));
    }

    // 查询抽取任务
    function queryExtractTask() {
        $.ajax({
            url: 'api/html/extract/task',
            method: 'get',
            dataType: 'json',
            success: function(result) {
                if (result.status == 0) {
                    showExtractTaskList(result.data);
                } else {
                    alert(result.msg);
                }
            }
        });
    }

    // 显示任务列表
    function showExtractTaskList(taskList) {
        var $tbody = $("#taskListTable tbody").empty();

        if (taskList) {
            $.each(taskList, function(i, item){
                var $tr = $("<tr></tr>");
                $("<td></td>").text(item.id).appendTo($tr);
                $("<td></td>").text(item.taskName).appendTo($tr);
                $("<td></td>").text(item.srcDataDir).appendTo($tr);
                $("<td></td>").text(item.mongoDbName).appendTo($tr);
                $("<td></td>").text(item.mongoCollectionName).appendTo($tr);
                $("<td></td>").text(item.successCount).appendTo($tr);
                $("<td></td>").text(item.failCount).appendTo($tr);
                $("<td></td>").text(util.time.ms2Str(item.createTime)).appendTo($tr);
                $("<td></td>").text(item.status).appendTo($tr);

                $delBtn = $('<button class="btn btn-default">删除</button>');
                $delBtn.click(function(){ // 绑定删除操作
                    deleteExtractTask(item);
                });
                $tr.append($delBtn);

                if (item.status === 1) {
                    // 暂停
                    $pauseBtn = $('<button class="btn btn-default">暂停</button>');
                    $pauseBtn.click(function(){ // 绑定暂停操作
                        pauseExtractTask(item);
                    });
                    $tr.append($pauseBtn);
                } else if (item.status === 4 || item.status == 0) {
                    // 恢复
                    $resumeBtn = $('<button class="btn btn-default">运行</button>');
                    $resumeBtn.click(function(){ // 绑定运行操作
                        runExtractTask(item);
                    });
                    $tr.append($resumeBtn);
                }

                $tbody.append($tr);
            });

        }
    }

    // 执行任务查询
    queryExtractTask();

    // 绑定创建任务事件
    $("#createTaskBtn").click(function(){
        var extractConfig = $("#extractConfig").val();
        var taskName = $("#taskNameInput").val();
        var srcDataDir = $("#taskDataDirInput").val();
        var mongoDbName = $("#taskMongoDbInput").val();
        var mongoCollectionName = $("#taskMongoCollectionInput").val();

        $.ajax({
            url: 'api/html/extract/task',
            method: 'post',
            data: {
                extractConfig: extractConfig,
                taskName: taskName,
                srcDataDir: srcDataDir,
                mongoDbName: mongoDbName,
                mongoCollectionName: mongoCollectionName
            },
            dataType: 'json',
            success: function(result) {
                if (result.status == 0) {
                    // 创建任务成功
                    queryExtractTask(); // 刷新任务
                    alert("创建任务成功");
                } else {
                    alert(result.msg);
                }
            }
        });
    });

    // 运行任务
    function runExtractTask(task) {
        changeTaskStatus(task.id, 1, function(result){
            if (result.status == 0) {
                queryExtractTask();
                alert("运行成功");
            } else {
                alert(result.msg);
            }
        });
    }

    // 暂停任务
    function pauseExtractTask(task) {
        changeTaskStatus(task.id, 4, function(result){
            if (result.status == 0) {
                queryExtractTask();
                alert("暂停成功");
            } else {
                alert(result.msg);
            }
        });
    }

    // 改变任务状态
    function changeTaskStatus(taskId, newStatus, successFun) {
         $.ajax({
             url: 'api/html/extract/task/status/'+ taskId +'/' + newStatus,
             method: 'put',
             dataType: 'json',
             success: successFun
         });
     }

    // 删除任务
     function deleteExtractTask(task) {
        $.ajax({
            url: 'api/html/extract/task/' + task.id,
            method: 'delete',
            dataType: 'json',
            success: function(result) {
                if (result.status == 0) {
                    queryExtractTask();
                    alert('删除任务成功');
                } else {
                    alert(result.msg);
                }
            }
        });
     }

})
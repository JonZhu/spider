$(function(){

    // 工具方法, type: success、info、warning、danger
    function showAlert(msg, type) {
        type = type ? type : "success";
        $("#alertMsg").attr("class", "alert alert-dismissible alert-" + type)
            .show().find(".content").text(msg);
    }

    // ------------------------------------------------------

    // 获取task list
    function getTaskList(pageNo) {
        $.ajax({
            url: "api/task/query",
            data: {pageSize: 200, pageNo: pageNo},
            dataType: "json",

            success: function(result) {
                console.info(result);
                if (result.status == 0) {
                    showTaskList(result.data);
                } else {
                    showAlert(result.msg, "warning");
                }
            }
        });
    }

    // 显示列表数据, 分页数据
    function showTaskList(page) {
        $tbody = $("#taskListTable tbody").empty();

        if (page.pageData) {
            $.each(page.pageData, function(i, item){
                $tr = $("#tplListRow").clone().removeAttr("id");
                var $taskId = $('<a></a>').text(item.id).attr('href', 'data.html?taskId=' + item.id);
                $(".task-id", $tr).append($taskId);
                $(".task-name", $tr).text(item.name);
                $(".task-author", $tr).text(item.author);
                $(".task-datadir", $tr).text(item.datadir);
                $(".task-createtime", $tr).text(util.time.ms2Str(item.createTime));
                $(".task-status", $tr).text(statusName(item.status));

                $delBtn = $('<button class="btn btn-default">删除</button>');
                $delBtn.click(deleteBtAction); // 绑定删除操作
                $(".op", $tr).append($delBtn);

                if (item.status === 1) {
                    // 暂停
                    $pauseBtn = $('<button class="btn btn-default">暂停</button>');
                    $pauseBtn.click(pauseBtAction); // 绑定暂停操作
                    $(".op", $tr).append($pauseBtn);
                } else if (item.status === 4) {
                    // 恢复
                    $resumeBtn = $('<button class="btn btn-default">恢复</button>');
                    $resumeBtn.click(resumeBtAction); // 绑定恢复操作
                    $(".op", $tr).append($resumeBtn);
                }

                $tbody.append($tr);
            });

        }

    }

    // 转换为status名称
    var statusNameObj = {0: "新建", 1: "运行中", 4: "暂停", 6: "完成"};
    function statusName(status) {
        var name = statusNameObj[status];
        return name ? name : "未知";
    }

    // 绑定删除操作动作
    var deleteTaskId; // 用于存储删除任务id
    function deleteBtAction() {
        deleteTaskId = $(this).closest("tr").find(".task-id").text();
        // alert(deleteTaskId);
        $("#deleteTaskConfirm").modal("show");
    }

    // 暂停
    function pauseBtAction() {
        var taskId = $(this).closest("tr").find(".task-id").text();
        $.ajax({
            url: "api/task/status/" + taskId + "/4",
            type: "put",
            dataType: "json",
            success: function(result) {
                if (result.status == 0) {
                    showAlert("暂停任务成功");
                    getTaskList(1);
                } else {
                    showAlert(result.msg, "warning");
                }
            }
        });
    }

    // 恢复
    function resumeBtAction() {
        var taskId = $(this).closest("tr").find(".task-id").text();
        $.ajax({
            url: "api/task/status/" + taskId + "/1",
            type: "put",
            dataType: "json",
            success: function(result) {
                if (result.status == 0) {
                    showAlert("恢复任务成功");
                    getTaskList(1);
                } else {
                    showAlert(result.msg, "warning");
                }
            }
        });
    }

    // 执行分页数据获取
    getTaskList(1);


    // ------------------------------------------------------

    // 创建任务
    $("#createTaskBtn").click(function(){

        var spiderDsl = $("#dslText").val();
        // alert(spiderDsl);
        $.ajax({
            url: "api/task",
            type: "post",
            contentType: "application/xml",
            data: spiderDsl,
            dataType: "json",
            success: function(result){
                if (result.status == 0) {
                    showAlert("创建成功");
                    getTaskList(1);
                    $('#createTaskModal').modal('hide');
                } else {
                    showAlert(result.msg, "warning");
                    $('#createTaskModal').modal('hide');
                }
            }
        });


    });


    // ------------------------------------------------------

    // 删除任务
    $("#confirmDeleteTaskBtn").click(function(){
        // alert(deleteTaskId);

        $.ajax({
            url: "api/task/" + deleteTaskId,
            type: "delete",
            dataType: "json",
            success: function(result) {
                if (result.status == 0) {
                    getTaskList(1); // 刷新列表
                    showAlert("删除任务成功");
                } else {
                    showAlert(result.msg, "warning");
                }
            }
        });
    });

});
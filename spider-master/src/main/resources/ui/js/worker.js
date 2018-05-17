$(function(){

    function queryWorkList() {
        $.ajax({
            url: "api/worker/query",
            dataType: "json",
            success: function(result) {
                if (result.status == 0) {
                    showTaskList(result.data);
                } else {
                    console.error(result.msg);
                }
            }
        });
    }

    function showTaskList(data) {
        var workList = data.workerList;
        $tbody = $("#workerListTable tbody").empty();

        if (workList) {
            // 合计数据
            var upBytesTotal = 0, upBytesPSTotal = 0, upMsgTotal = 0, upMsgPSTotal = 0,
                downBytesTotal = 0, downBytesPSTotal = 0, downMsgTotal = 0, downMsgPSTotal = 0;

            $.each(workList, function(i, item){
                $tr = $("<tr></tr>");
                $("<td></td>").text(item.id).appendTo($tr); //ID
                $("<td></td>").text(item.host).appendTo($tr); //主机
                $("<td></td>").text(item.port).appendTo($tr); //端口
                $("<td style='border-right: 2px solid #dff0d8'></td>").text(util.time.ms2Str(item.connectTime)).appendTo($tr); //连接时间
                $("<td></td>").text(adaptByte(item.upBytes)).appendTo($tr); //上行数据
                $("<td></td>").text(adaptByte(item.upBytesPS)).appendTo($tr); //每秒上行数据
                $("<td></td>").text(item.upMsg).appendTo($tr); //上行消息
                $("<td style='border-right: 2px solid #dff0d8'></td>").text(item.upMsgPS.toFixed(2)).appendTo($tr); //每秒上行消息
                $("<td></td>").text(adaptByte(item.downBytes)).appendTo($tr); //下行数据
                $("<td></td>").text(adaptByte(item.downBytesPS)).appendTo($tr); //每秒下行数据
                $("<td></td>").text(item.downMsg).appendTo($tr); //下行消息
                $("<td></td>").text(item.downMsgPS.toFixed(2)).appendTo($tr); //每秒下行消息

                $tbody.append($tr);

                upBytesTotal += item.upBytes;
                upBytesPSTotal += item.upBytesPS;
                upMsgTotal += item.upMsg;
                upMsgPSTotal += item.upMsgPS;
                downBytesTotal += item.downBytes;
                downBytesPSTotal += item.downBytesPS;
                downMsgTotal += item.downMsg;
                downMsgPSTotal += item.downMsgPS;
            });

            if (workList.length > 1) {
                // worker数量大于1个, 显示合计行
                $tr = $("<tr class='success'></tr>");
                $("<td colspan='4'>合计</td>").appendTo($tr); //合计
                $("<td></td>").text(adaptByte(upBytesTotal)).appendTo($tr); //上行数据
                $("<td></td>").text(adaptByte(upBytesPSTotal)).appendTo($tr); //每秒上行数据
                $("<td></td>").text(upMsgTotal).appendTo($tr); //上行消息
                $("<td></td>").text(upMsgPSTotal.toFixed(2)).appendTo($tr); //每秒上行消息
                $("<td></td>").text(adaptByte(downBytesTotal)).appendTo($tr); //下行数据
                $("<td></td>").text(adaptByte(downBytesPSTotal)).appendTo($tr); //每秒下行数据
                $("<td></td>").text(downMsgTotal).appendTo($tr); //下行消息
                $("<td></td>").text(downMsgPSTotal.toFixed(2)).appendTo($tr); //每秒下行消息

                $tbody.append($tr);
            }
        } else {
            // 当前无worker
            $tbody.append("<tr><td colspan='13'>当前无worker连接</td></tr>");
        }

        // 累计数据
        var accumTotal = data.accumTotal;
        if (accumTotal) {
            $tr = $("<tr class='success'></tr>");
            $("<td colspan='4'>累计</td>").appendTo($tr); //累计
            $("<td></td>").text(adaptByte(accumTotal.upBytes)).appendTo($tr); //上行数据
            $("<td></td>").text(adaptByte(accumTotal.upBytesPS)).appendTo($tr); //每秒上行数据
            $("<td></td>").text(accumTotal.upMsg).appendTo($tr); //上行消息
            $("<td></td>").text(accumTotal.upMsgPS.toFixed(2)).appendTo($tr); //每秒上行消息
            $("<td></td>").text(adaptByte(accumTotal.downBytes)).appendTo($tr); //下行数据
            $("<td></td>").text(adaptByte(accumTotal.downBytesPS)).appendTo($tr); //每秒下行数据
            $("<td></td>").text(accumTotal.downMsg).appendTo($tr); //下行消息
            $("<td></td>").text(accumTotal.downMsgPS.toFixed(2)).appendTo($tr); //每秒下行消息

            $tbody.append($tr);
        }

    }

    // 适配bytes单位
    var byteUnits = ["G", "M", "K", "B"];
    var byteUnitSize = [Math.pow(1024,3), Math.pow(1024,2) ,1024, 1];
    function adaptByte(byteTotal) {
        for (var i = 0; i < byteUnitSize.length; i++) {
            var temp = byteTotal / byteUnitSize[i];
            if (temp >= 1) {
                return temp.toFixed(2) + byteUnits[i];
            }
        }

        return "0";
    }

    queryWorkList();
    window.setInterval(queryWorkList, 5000);


    // 连接worker
    $("#connectWorkerBtn").click(function(){
        var host = $("#workerHostInput").val();
        var port = $("#workerPortInput").val();
        if (!host) {
            alert("worker host 不能为空");
            return;
        }
        if (!port) {
            alert("worker port 不能为空");
            return;
        }

        if (!new RegExp("^\\d+$").test(port)) {
            alert("worker port 只能为数字");
            return;
        }

        $.ajax({
            url: "api/worker/connectWorker",
            type: "post",
            data: {
                host: host,
                port: port
            },
            dataType: "json",
            success: function(result){

            }
        });

    });
});
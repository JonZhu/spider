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

})
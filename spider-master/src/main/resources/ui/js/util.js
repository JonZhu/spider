/**
 * 工具
 * @author zhujun
 */

(function(){
	
	if (window.util) { // 防止重复加载
		return;
	}
	
	window.util = {};
	
	// time工具
	window.util.time = (function(){
		function ms2Str(timeMs) {
			if (timeMs) {
				var time = new Date(timeMs);
				return time.getFullYear() + "-" + (time.getMonth() + 1) + "-" + time.getDate() + " "
						+ time.getHours() + ":" + time.getMinutes() + ":" + time.getSeconds();
			} else {
				return "";
			}
		}
		
		
		// time暴露的接口
		return {
			ms2Str: ms2Str
		};
	})();
	
	// url 工具
    window.util.url = (function(){
    	// 解析url
    	function parseParam(url) {
    		var paramObj = {};
			if (url) {
				var index = url.indexOf('?');
				if (index > -1) {
					url = url.substring(index + 1);
				}

				url.split('&').forEach(function (value) {
					var paramArray = value.split('=');
					paramObj[paramArray[0]] = paramArray.length > 1 ? paramArray[1] : "";
				});
			}
			return paramObj;
		}

    	return {
            param: parseParam
		}
	})();

    // byte工具
    window.util.byte = (function () {
		// 适配bytes单位
		var byteUnits = ["G", "M", "K", "B"];
		var byteUnitSize = [Math.pow(1024,3), Math.pow(1024,2) ,1024, 1];
		function adaptByte(byteTotal) {
			if (!byteTotal) {
				return null;
			}

			for (var i = 0; i < byteUnitSize.length; i++) {
				var temp = byteTotal / byteUnitSize[i];
				if (temp >= 1) {
					return temp.toFixed(2) + byteUnits[i];
				}
			}

			return null;
		}

		return {
            adapt: adaptByte
		}
    })();

})();
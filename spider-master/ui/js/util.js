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
	
	
})();
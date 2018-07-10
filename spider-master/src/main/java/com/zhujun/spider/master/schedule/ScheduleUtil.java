package com.zhujun.spider.master.schedule;

import com.zhujun.spider.master.data.db.IFetchUrlService;
import com.zhujun.spider.master.data.db.po.SpiderTaskPo;
import com.zhujun.spider.master.util.ThreadUtils;
import com.zhujun.spider.net.mina.SpiderNetMessage;

import java.nio.charset.Charset;

/**
 * Schedule工具
 * 
 * @author zhujun
 * @date 2016年6月7日
 *
 */
public class ScheduleUtil {

	public static String obj2str(Object obj) {
		return obj instanceof byte[] ? new String((byte[])obj, Charset.forName("UTF-8")) : String.valueOf(obj);
	}
	
	/**
	 * 等待worker push数据, 直到fetchurl中关于该action的数据已经抓取完
	 * @author zhujun
	 * 
	 * @param spider
	 * @param actionId
	 * @param fetchUrlService
	 * @return 返回null, 表示push队列无数据 且 fetchurl中无该action创建的无完成的url
	 * @throws Exception
	 */
	public static SpiderNetMessage waitPushData(SpiderTaskPo spider, String actionId, IFetchUrlService fetchUrlService) throws Exception {
		// 等待worker push数据, 直到fetchurl中关于该action的数据已经抓取完
		SpiderNetMessage item = null;
		while (true) {
			item = PushDataQueue.popPushData(spider.getId(), actionId);
			if (item == null) {
				ThreadUtils.sleep(5000);
				
				item = PushDataQueue.popPushData(spider.getId(), actionId); // 5秒后再次获取
				if (item == null) {
					// 队列中无数据, 查询数据库中该action是否还有url未处理完
					boolean existUnFetch = fetchUrlService.existUnFetchUrlInAction(spider, actionId);
					if (existUnFetch) {
						// 如果还有, 等待5秒 再尝试从 push data queue中获取
						ThreadUtils.sleep(5000);
						continue;
					}
				}
			}
			
			return item;
		}
	}
	
}

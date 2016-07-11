package com.zhujun.spider.master.schedule;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zhujun.spider.master.data.db.IFetchUrlService;
import com.zhujun.spider.master.data.db.po.FetchUrlPo;
import com.zhujun.spider.master.data.writer.SpiderDataWriter;
import com.zhujun.spider.master.di.DIContext;
import com.zhujun.spider.master.domain.Spider;
import com.zhujun.spider.master.domain.UrlSet;
import com.zhujun.spider.master.schedule.PushDataQueue.Item;
import com.zhujun.spider.master.util.ThreadUtils;

/**
 * Url集合 执行器
 * 
 * @author zhujun
 * @date 2016年6月5日
 *
 */
public class UrlSetExecutor extends ParentActionExecutor implements ActionExecutor {

	private final static Logger LOG = LoggerFactory.getLogger(UrlSetExecutor.class);
	
	private IFetchUrlService fetchUrlService = DIContext.getInstance(IFetchUrlService.class);
	
	@Override
	public void execute(IScheduleContext context) throws Exception {
		UrlSet urlSet = (UrlSet)context.getAction();
		Spider spider = context.getSpider();
		Map<String, Serializable> dataScope = context.getDataScope();
		
		LOG.debug("开始url入库");
		long startInsertUrlTime = System.currentTimeMillis();
		
		// 生成实际url
		List<String> urlList = new ArrayList<>();
		List<FetchUrlPo> urlPoList = new ArrayList<>();
		List<Integer> indexList = urlSet.getTempIndexList();
		if (indexList == null || indexList.isEmpty()) {
			// 无模板号
			urlList.add(urlSet.getUrltemplate());
			FetchUrlPo urlPo = new FetchUrlPo();
			urlPo.setUrl(urlSet.getUrltemplate());
			urlPo.setActionId(urlSet.getId());
		} else {
			// 填充模板值
			SequenceItem[] sequenceItems = new SequenceItem[indexList.size()];
			for (int i = 0; i < indexList.size(); i++) {
				int tempIndex = indexList.get(i);
				
				// 默认暂时都按enum处理
				String tempValue = urlSet.getTempValue(tempIndex);
				if (tempValue.startsWith("{") && tempValue.endsWith("}")) {
					// {}变量处理
					Object dataInScope = dataScope.get(tempValue.substring(1, tempValue.length() - 1));
					tempValue = ScheduleUtil.obj2str(dataInScope);
				}
				
				sequenceItems[i] = new EnumSequenceItem(tempValue.split(ScheduleConst.ENUM_VALUE_SEPARATOR));
			}
			Sequence sequence = new Sequence(sequenceItems);
			while (true) {
				Object[] sequenceValue = sequence.getNextValue(); // 根据temp生成值序列
				if (sequenceValue == null) {
					break;
				}
				
				// 设置值到Urltemplate
				String url = urlSet.getUrltemplate();
				for (int i = 0; i < indexList.size(); i++) {
					int tempIndex = indexList.get(i);
					url = url.replaceAll("\\{"+ tempIndex +"\\}", String.valueOf(sequenceValue[i]));
				}
				
				urlList.add(url);
				FetchUrlPo urlPo = new FetchUrlPo();
				urlPo.setUrl(url);
				urlPo.setActionId(urlSet.getId());
				urlPoList.add(urlPo);
				
				if (urlPoList.size() > 1000) {
					// 100条数据入库
					fetchUrlService.createFetchUrl(spider.getDataDir(), urlPoList);
					urlPoList = new ArrayList<>();
				}
			}
			
		}
		
		if (!urlList.isEmpty()) {
			fetchUrlService.createFetchUrl(spider.getDataDir(), urlPoList);
		}
		
		LOG.debug("结束url入库, time:{}", System.currentTimeMillis() - startInsertUrlTime);
		
		
		SpiderDataWriter writer = context.getDataWriter();
		
		// 等待worker push数据, 直到fetchurl中关于该action的数据已经抓取完
		while (true) {
			Item item = PushDataQueue.popPushData(spider.getId(), urlSet.getId());
			if (item == null) {
				ThreadUtils.sleep(5000);
				
				item = PushDataQueue.popPushData(spider.getId(), urlSet.getId()); // 5秒后再次获取
				if (item == null) {
					// 队列中无数据, 查询数据库中该action是否还有url未处理完
					boolean existUnFetch = fetchUrlService.existUnFetchUrlInAction(spider.getDataDir(), urlSet.getId());
					if (existUnFetch) {
						// 如果还有, 等待5秒 再尝试从 push data queue中获取
						ThreadUtils.sleep(5000);
						continue;
					} else {
						// 库中该action的url全部抓取完
						break;
					}
				}
			}
			
			
			// 从队列中获得数据
			if (item.success) {
				// 存储到文件
				writer.write(item.url, item.fetchTime, item.data);
				fetchUrlService.setFetchUrlStatus(spider.getDataDir(), item.urlId, FetchUrlPo.STATUS_SUCCESS, item.fetchTime);
				
				if (StringUtils.isNotBlank(urlSet.getId())) {
					dataScope.put(urlSet.getId(), item.data);
				}
				
				// 设置数据到data scope, 子结点使用
				dataScope.put(ScheduleConst.PRE_RESULT_DATA_KEY, item.data);
				dataScope.put(ScheduleConst.PRE_RESULT_URL_KEY, item.url);
				
				// 执行子级,例如paging
				super.execute(context);
			} else {
				// 抓取失败
				fetchUrlService.setFetchUrlStatus(spider.getDataDir(), item.urlId, FetchUrlPo.STATUS_ERROR, item.fetchTime);
			}
			
		}

	}


	/**
	 * 序列
	 * 
	 * <p>用于生成连续的按多个段序列号</p>
	 * 
	 * @author zhujun
	 * @date 2016年6月5日
	 *
	 */
	private static class Sequence {
		
		private SequenceItem[] sequenceItems;
		
		/**
		 * 序列值
		 */
		private Object[] value;
		
		/**
		 * 当前项
		 */
		private int currentItemIndex;
		
		public Sequence(SequenceItem[] items) {
			this.sequenceItems = items;
		}
		
		/**
		 * 获取下一个序列值, 返回null表示序列无值
		 * @return
		 */
		public Object[] getNextValue() {
			if (value == null) {
				// 初始化值
				value = new Object[sequenceItems.length];
				for (int i = 0; i < sequenceItems.length; i++) {
					SequenceItem sequenceItem = sequenceItems[i];
					
					value[i] = sequenceItem.getNext();
				}
				
				currentItemIndex = sequenceItems.length - 1;
			} else {
				
				boolean carryBitFlag = false; // 进位标识
				while (true) { // 该循环主要用于进位
					SequenceItem currentItem = sequenceItems[currentItemIndex];
					Object currentItemNext = currentItem.getNext();
					if (currentItemNext == null) {
						// 当前项达到最后, 应该进位
						if (currentItemIndex == 0) {
							// 已是最高位, 序列无值
							return null;
						} else {
							// 进位
							currentItemIndex--;
							carryBitFlag = true;
						}
					} else {
						// 当前位有值
						value[currentItemIndex] = currentItemNext;
						break;
					}
				}
				
				if (carryBitFlag) {
					// 有进位, 重置当前位之下的所有低位
					for (int i = currentItemIndex + 1; i < sequenceItems.length; i++) {
						SequenceItem item = sequenceItems[i];
						item.reset(); // 重置
						value[i] = item.getNext();
					}
					
					currentItemIndex = sequenceItems.length - 1; // 设置为最低位
					
				}
				
			}
			
			return value;
		}
		
		
	}
	
	/**
	 * 序列项
	 * 
	 * <p>用于获取</p>
	 * 
	 * @author zhujun
	 * @date 2016年6月5日
	 *
	 */
	private static interface SequenceItem {

		/**
		 * 获取项目中的下一个值
		 * @return 下一个值,返回null表示无下一值
		 */
		Object getNext();

		/**
		 * 重置后,获取下一值时为第一个值
		 */
		void reset();
		
	}
	
	private static class EnumSequenceItem implements SequenceItem {

		private Object[] enums;
		private int index = -1;
		
		public EnumSequenceItem(Object[] enums) {
			this.enums = enums;
		}
		
		@Override
		public Object getNext() {
			if (index < enums.length - 1) {
				index++;
				return enums[index];
			}
			
			return null;
		}

		@Override
		public void reset() {
			index = -1;
		}
		
	}
	
}
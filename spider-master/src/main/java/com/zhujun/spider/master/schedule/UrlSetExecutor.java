package com.zhujun.spider.master.schedule;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import com.zhujun.spider.master.contentfetcher.ContentFetcher;
import com.zhujun.spider.master.contentfetcher.JavaUrlContentFetcher;
import com.zhujun.spider.master.domain.DslAction;
import com.zhujun.spider.master.domain.Spider;
import com.zhujun.spider.master.domain.UrlSet;

/**
 * Url集合 执行器
 * 
 * @author zhujun
 * @date 2016年6月5日
 *
 */
public class UrlSetExecutor extends ParentActionExecutor implements ActionExecutor {

	@Override
	public void execute(Spider spider, DslAction action, Map<String, Object> dataScope) {
		UrlSet urlSet = (UrlSet)action;
		
		ContentFetcher contentFetcher = JavaUrlContentFetcher.getInstance();
		
		// 生成实际url
		List<String> urlList = new ArrayList<>();
		List<Integer> indexList = urlSet.getTempIndexList();
		if (indexList == null || indexList.isEmpty()) {
			// 无模板号
			urlList.add(urlSet.getUrltemplate());
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
					tempValue = dataInScope instanceof byte[] ? new String((byte[])dataInScope) : String.valueOf(dataInScope);
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
			}
			
		}
		
		// 内容抓取
		for (String url : urlList) {
			byte[] content = contentFetcher.fetch(url);
			//TODO 存储到文件
			
			if (StringUtils.isNotBlank(urlSet.getName())) {
				dataScope.put(urlSet.getName(), content);
			}
			
			// 执行子级,例如paging
			super.execute(spider, urlSet, dataScope);
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
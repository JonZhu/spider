package com.zhujun.spider.master.schedule;

import java.io.Serializable;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.zhujun.spider.master.domain.DataTransition;
import org.springframework.stereotype.Component;

/**
 * 数据转换
 * 
 * @author zhujun
 * @date 2016年6月5日
 *
 */
@Component
public class DataTransitionExecutor implements ActionExecutor {

	@Override
	public void execute(IScheduleContext context) {
		DataTransition dataTransition = (DataTransition)context.getAction();
		Map<String, Serializable> dataScope = context.getDataScope();
		Object inputData = dataScope.get(dataTransition.getInput());
		if (inputData == null) {
			throw new RuntimeException("DataTransition["+ dataTransition.getId() +"] input数据为空");
		}
		
		Document htmlDoc = Jsoup.parse(ScheduleUtil.obj2str(inputData));
		Elements selectElements = htmlDoc.select(dataTransition.getSelect());
		if (selectElements.isEmpty()) {
			dataScope.put(dataTransition.getId(), "");
			return;
		}
		
		StringBuilder valueBuilder = new StringBuilder();
		Pattern valueReg = null;
		if (dataTransition.getRegex() != null) {
			// 编译值正则表达式
			valueReg = Pattern.compile(dataTransition.getRegex());
		}
		for (Element element : selectElements) {
			String attrValue = element.attr(dataTransition.getAttr());
			if (StringUtils.isBlank(attrValue)) {
				continue;
			}
			if (valueReg == null) {
				valueBuilder.append(attrValue).append(ScheduleConst.ENUM_VALUE_SEPARATOR);
			} else {
				// 使用正则查找,并取group(1)
				Matcher valueMatcher = valueReg.matcher(attrValue);
				if (valueMatcher.find() && valueMatcher.groupCount() > 0) {
					valueBuilder.append(valueMatcher.group(1)).append(ScheduleConst.ENUM_VALUE_SEPARATOR);
				}
			}
		}
		
		if (valueBuilder.length() > 0) {
			// 删除最后的分隔符
			valueBuilder.deleteCharAt(valueBuilder.length() - 1);
		}
		
		// 存储结果
		dataScope.put(dataTransition.getId(), valueBuilder.toString());

	}

}

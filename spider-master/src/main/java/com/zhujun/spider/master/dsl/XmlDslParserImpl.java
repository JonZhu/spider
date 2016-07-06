package com.zhujun.spider.master.dsl;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zhujun.spider.master.domain.DataTransition;
import com.zhujun.spider.master.domain.DataWrite;
import com.zhujun.spider.master.domain.DslAction;
import com.zhujun.spider.master.domain.Spider;
import com.zhujun.spider.master.domain.Url;
import com.zhujun.spider.master.domain.internal.DataTransitionImpl;
import com.zhujun.spider.master.domain.internal.DataWriteImpl;
import com.zhujun.spider.master.domain.internal.PagingImpl;
import com.zhujun.spider.master.domain.internal.UrlImpl;
import com.zhujun.spider.master.domain.internal.UrlSetImpl;
import com.zhujun.spider.master.domain.internal.XmlSpider;

/**
 * Xml DSL解析实现
 * @author zhujun
 * @date 2016年6月3日
 *
 */
public class XmlDslParserImpl implements DslParser {

	private final static Logger LOG = LoggerFactory.getLogger(XmlDslParserImpl.class);
	
	@Override
	public Spider parse(InputStream inputStream) {
		LOG.debug("解析XML DSL");
		
		try {
			Document doc = new SAXReader().read(inputStream);
			Element rootEle = doc.getRootElement();
			
			XmlSpider xmlSpider = new XmlSpider();
			xmlSpider.setSpiderDslDoc(doc);
			String dataDir = rootEle.attributeValue("datadir");
			if (StringUtils.isBlank(dataDir)) {
				throw new RuntimeException("spider的datadir属性不能为空");
			}
			xmlSpider.setDataDir(dataDir);
			xmlSpider.setName(rootEle.attributeValue("name"));
			xmlSpider.setAuthor(rootEle.attributeValue("author"));
			
			List<Element> eleList = rootEle.elements();
			if (eleList != null && !eleList.isEmpty()) {
				List<DslAction> actionList = new ArrayList<>();
				String eleName;
				for (Element element : eleList) {
					eleName = element.getName();
					if ("url".equals(eleName)) {
						actionList.add(parseUrl(element));
					} else if ("datatran".equals(eleName)) {
						actionList.add(parseDataTransition(element));
					} else if ("urlset".equals(eleName)) {
						actionList.add(parseUrlSet(element));
					} else {
						LOG.warn("spider暂不支持 {} 节点", eleName);
					}
				}
				
				xmlSpider.setChildren(actionList);
			}
			
			return xmlSpider;
		} catch (Exception e) {
			throw new RuntimeException("解析Dsl失败", e);
		}
		
	}

	private DataWrite parseDataWrite(Element element) {
		String filename = element.attributeValue("filename");
		if (StringUtils.isBlank(filename)) {
			throw new RuntimeException("datawrite的filename属性不能为空");
		}
		
		DataWriteImpl dataWrite = new DataWriteImpl();
		dataWrite.setFilename(filename);
		dataWrite.setType(element.attributeValue("type"));
		
		return dataWrite;
	}

	private DslAction parseUrlSet(Element element) {
		String urltemplate = element.attributeValue("urltemplate");
		if (StringUtils.isBlank(urltemplate)) {
			throw new RuntimeException("urlset的urltemplate属性不能为空");
		}
		
		UrlSetImpl urlSet = new UrlSetImpl();
		urlSet.setUrltemplate(urltemplate);
		urlSet.setId(getActionId(element));
		
		List<Integer> indexList = new ArrayList<>();
		Map<Integer, String> typeMap = new HashMap<>();
		Map<Integer, String> valueMap = new HashMap<>();
		
		Pattern templateReg = Pattern.compile("\\{(\\d)\\}"); // \{(\d)\}
		Matcher templateMacher = templateReg.matcher(urltemplate);
		while (templateMacher.find()) {
			int index = Integer.valueOf(templateMacher.group(1));
			if (typeMap.containsKey(index)) {
				// 重复index
				continue;
			}
			
			indexList.add(index);
			String tempTypeAttr = "temp" + index + "type";
			String tempValueEle = "temp" + index + "value";
			
			String tempType = element.attributeValue(tempTypeAttr);
			if (StringUtils.isBlank(tempType)) {
				throw new RuntimeException("urlset的"+ tempTypeAttr +"属性不能为空");
			}
			
			String tempValue = element.elementText(tempValueEle);
			if (StringUtils.isBlank(tempValue)) {
				throw new RuntimeException("urlset的"+ tempValueEle +"结点不能为空");
			}
			
			typeMap.put(index, tempType);
			valueMap.put(index, tempValue);
			
		}
		
		if (!indexList.isEmpty()) {
			Collections.sort(indexList); // 序号排序
		}
		urlSet.setTempIndexList(indexList);
		urlSet.setTypeMap(typeMap);
		urlSet.setValueMap(valueMap);
		
		Element pagingEle = element.element("paging");
		if (pagingEle != null) {
			List<DslAction> childrenList = new ArrayList<>();
			childrenList.add(parsePaging(pagingEle));
			urlSet.setChildren(childrenList);
		}
		
		return urlSet;
	}

	private DslAction parsePaging(Element pagingEle) {
		String select = pagingEle.attributeValue("select");
		if (StringUtils.isBlank(select)) {
			throw new RuntimeException("paging的select属性不能为空");
		}
		
		String urlattr = pagingEle.attributeValue("urlattr");
		if (StringUtils.isBlank(urlattr)) {
			throw new RuntimeException("paging的urlattr属性不能为空");
		}
		
		PagingImpl paging = new PagingImpl();
		paging.setSelect(select);
		paging.setUrlAttr(urlattr);
		paging.setId(getActionId(pagingEle));
		
		return paging;
	}

	private DataTransition parseDataTransition(Element element) {
		String input = element.attributeValue("input");
		if (StringUtils.isBlank(input)) {
			throw new RuntimeException("datatran的input属性不能为空");
		}
		
		String select = element.attributeValue("select");
		if (StringUtils.isBlank(select)) {
			throw new RuntimeException("datatran的select属性不能为空");
		}
		
		String attr = element.attributeValue("attr");
		if (StringUtils.isBlank(attr)) {
			throw new RuntimeException("datatran的attr属性不能为空");
		}
		
		String regex = element.attributeValue("regex");
		
		DataTransitionImpl dataTransition = new DataTransitionImpl();
		dataTransition.setInput(input);
		dataTransition.setSelect(select);
		dataTransition.setAttr(attr);
		dataTransition.setRegex(regex);
		dataTransition.setId(getActionId(element));
		
		return dataTransition;
	}

	private Url parseUrl(Element element) {
		String href = element.attributeValue("href");
		if (StringUtils.isBlank(href)) {
			throw new RuntimeException("url的href属性不能为空");
		}
		
		UrlImpl url = new UrlImpl();
		url.setHref(href);
		url.setId(getActionId(element));
		
		return url;
	}

	private String getActionId(Element element) {
		return element.attributeValue("id");
	}

}

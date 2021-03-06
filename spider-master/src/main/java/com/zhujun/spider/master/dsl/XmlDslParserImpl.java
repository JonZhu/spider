package com.zhujun.spider.master.dsl;

import com.zhujun.spider.master.domain.*;
import com.zhujun.spider.master.domain.internal.*;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.Node;
import org.dom4j.io.SAXReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
		LOG.debug("parse XML DSL");
		
		try {
			Document doc = new SAXReader().read(inputStream);
			Element rootEle = doc.getRootElement();
			
			XmlSpider xmlSpider = new XmlSpider();
			xmlSpider.setSpiderDslDoc(doc);
			xmlSpider.setId(rootEle.attributeValue("id"));
			String dataDir = rootEle.attributeValue("datadir");
			if (StringUtils.isBlank(dataDir)) {
				throw new RuntimeException("spider的datadir属性不能为空");
			}
			xmlSpider.setDataDir(dataDir);
			xmlSpider.setName(rootEle.attributeValue("name"));
			xmlSpider.setAuthor(rootEle.attributeValue("author"));
			
			String datawriter = rootEle.attributeValue("datawriter");
			if (StringUtils.isNotBlank(datawriter)) {
				xmlSpider.setDataWriterType(datawriter);
			}
			
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
					} else if ("clone".equals(eleName)) {
						actionList.add(parseClone(element));
					} else {
						LOG.warn("spider not support {} element", eleName);
					}
				}
				
				xmlSpider.setChildren(actionList);
			}
			
			return xmlSpider;
		} catch (Exception e) {
			throw new RuntimeException("解析Dsl失败", e);
		}
		
	}

	private DslAction parseClone(Element element) {
		CloneImpl clone = new CloneImpl();
		clone.setId(getActionId(element));
		if (StringUtils.isBlank(clone.getId())) {
			throw new RuntimeException("clone的id属性不能为空");
		}
		
		String css = element.attributeValue("css");
		String js = element.attributeValue("js");
		String image = element.attributeValue("image");
		clone.setAllowCss(BooleanUtils.toBoolean(css));
		clone.setAllowJs(BooleanUtils.toBoolean(js));
		clone.setAllowImage(BooleanUtils.toBoolean(image));

		// seeds
		List<Node> seedEleList = element.selectNodes("seeds/seed");
		if (seedEleList == null || seedEleList.isEmpty()) {
			throw new RuntimeException("clone的seeds seed节点不能为空");
		}
		List<String> seedUrlList = new ArrayList<>();
		for (Node seedEle : seedEleList) {
			seedUrlList.add(seedEle.getText());
		}
		clone.setSeeds(seedUrlList.toArray(new String[]{}));
		
		// hosts
		List<Node> hostEleList = element.selectNodes("hosts/host");
		if (hostEleList == null || hostEleList.isEmpty()) {
			throw new RuntimeException("clone的hosts host节点不能为空");
		}
		List<String> hostList = new ArrayList<>();
		for (Node hostEle : hostEleList) {
			hostList.add(hostEle.getText());
		}
		clone.setHosts(hostList.toArray(new String[]{}));

		// urlPatterns
        List<Node> urlPatternNodeList = element.selectNodes("urlpatterns/pattern");
        if (urlPatternNodeList != null && !urlPatternNodeList.isEmpty()) {
            List<String> patternList = new ArrayList<>();
            for (Node patternNode : urlPatternNodeList) {
                String pattern = patternNode.getText();
                if (StringUtils.isNotBlank(pattern)) {
                    patternList.add(pattern.trim());
                }
            }
            if (!patternList.isEmpty()) {
                clone.setUrlPatterns(compilePatterns(patternList));
            }
        }
		
		return clone;
	}

    /**
     * 编译url正则表达式
     * @param patterns
     * @return
     */
    private Pattern[] compilePatterns(List<String> patterns) {
        if (patterns == null || patterns.isEmpty()) {
            return null;
        }

        Pattern[] compiledPatterns = new Pattern[patterns.size()];
        for (int i = 0; i < patterns.size(); i++) {
            compiledPatterns[i] = Pattern.compile(patterns.get(i));
        }

        return compiledPatterns;
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
		if (StringUtils.isBlank(urlSet.getId())) {
			throw new RuntimeException("urlset的id属性不能为空");
		}
		
		List<Integer> indexList = new ArrayList<>();
		Map<Integer, String> typeMap = new HashMap<>();
		Map<Integer, String> valueMap = new HashMap<>();
		
		Pattern templateReg = Pattern.compile("\\{(\\d)\\}"); // \{(\d)\}
		Matcher templateMacher = templateReg.matcher(urltemplate);
		while (templateMacher.find()) {
			int index = Integer.parseInt(templateMacher.group(1));
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

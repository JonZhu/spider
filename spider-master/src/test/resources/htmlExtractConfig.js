// https://baike.baidu.com/item/%E9%95%BF%E6%B1%9F%E4%B8%89%E8%A7%92%E6%B4%B2%E5%9F%8E%E5%B8%82%E7%BE%A4/5973620

// 基本信息
basicInfo = {
  dataType: 'object', //object|array|number|string|date
  selector: '.basic-info',
  properties: [{
    // 静态属性
    name: 'html-title',
	dataType: 'string',
	selector: '/html title'
  },{
	name: null,
	nameSelector: '.basicInfo-item.name', // 动态属性
	dataType: 'string',
	selector: '$name:next-one(.basicInfo-item.value)' // 以名称为起点搜索 $name:next-one(.basicInfo-item.value)
  }]
}

// 行政区域
region = {
    dataType: 'array',
    selector: 'table tr',
    startSkip: 1, // 开始跳过的项目数量
    endSkip: 0, //结束跳过的项目数量
    itemData: { // 数据项
        dataType: 'object',
        properties: [{
            name: 'province',
            dataType: 'string',
            selector: 'td:nth-child(1)'
        },{
          name: 'shi',
          dataType: 'string',
          selector: 'td:nth-child(2)'
      },{
         name: 'qu',
         dataType: 'string',
         selector: 'td:nth-child(3)'
     }]
    }
}

// test
test = {
    dataType: 'object',
    properties: [{
        dataType: 'string',
        name: 'city-region-title',
        selector: '/body > div.body-wrapper > div.content-wrapper > div > div.main-content > div:nth-child(22) > h2'
    }, {
        dataType: 'string',
        name: 'city-region-content',
        selector: '/body > div.body-wrapper > div.content-wrapper > div > div.main-content > div:nth-child(23)'
    }]
}

// 根对象,必需定义在最下面
root = {
  dataType: 'object',
  selector: 'html body',
  properties: [{
    name: 'basicInfo',
	ref: basicInfo // 配置引用
  },{
    name: 'title',
	dataType: 'string',
	selector: '.lemmaWgt-lemmaTitle-title h1'
  },{
    name: "region",
    ref: region
  },{
       name: "test",
       ref: test
   }]
}
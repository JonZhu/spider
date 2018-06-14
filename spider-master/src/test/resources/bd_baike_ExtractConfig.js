// https://baike.baidu.com/item/%E6%9D%8E%E5%B9%BC%E6%96%8C/12503

// 基本信息
basicInfo = {
  dataType: 'object', //object|array|number|string|date
  selector: '.basic-info',
  properties: [{
	name: null,
	nameSelector: '.basicInfo-item.name', // 动态属性
	dataType: 'string',
	selector: '$name:next-one(.basicInfo-item.value)' // 以名称为起点搜索 $name:next-one(.basicInfo-item.value)
  }]
}

// 关系
relations = {
    name: 'relations',
    dataType: 'array',
    selector: '.relations #slider_relations ul li',
    itemData: {
        dataType: 'object',
        properties: [{
            name: 'name',
            dataType: 'string',
            selector: 'div.name em'
        },{
            name: 'relation',
            dataType: 'string',
            selector: 'div.name'
        },{
            name: 'image',
            dataType: 'string',
            selector: 'img@src'
        }]
    }
}

// 根对象,必需定义在最下面
root = {
  dataType: 'object',
  selector: 'html body',
  properties: [{
    name: 'htmlTitle', // html标题
    dataType: 'string',
    selector: '/html title'
  },{
       name: 'baikeTitle', // 百科标题
   	dataType: 'string',
   	selector: '.lemmaWgt-lemmaTitle-title h1'
  },{
    name: 'basicInfo',
	ref: basicInfo // 配置引用
  },{
      name: 'tag', // 标签
      dataType: 'array',
      selector: '#open-tag-item .taglist',
      itemData: {
          dataType: 'string'
      }
  },
  relations // 关系
  ]
}
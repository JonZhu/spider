// https://baike.baidu.com/item/%E9%95%BF%E6%B1%9F%E4%B8%89%E8%A7%92%E6%B4%B2%E5%9F%8E%E5%B8%82%E7%BE%A4/5973620

basicInfo = {
  dataType: 'object', //object|array|number|string|date
  selector: '',
  properties: [{
    // 静态属性
    name: 'title',
	dataType: 'string',
	selector: '/html title h1'
  },{
	name: null,
	nameSelector: '.basicInfo-item.name', // 动态属性
	dataType: 'string',
	selector: '$name/../' // 以名称为起点搜索
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
	selector: '.lemmaWgt-lemmaTitle-title'
  }]
}
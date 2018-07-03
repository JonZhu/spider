// 行政区划
// http://xzqh.mca.gov.cn/fuzzySearch?xzid=121721&jb=1

root = {
    dataType: 'array',
    selector: '.info_table tr',
    startSkip: 1,
    itemData: {
        dataType: 'object',
        properties: [{
            name: 'name', // 名称
            dataType: 'string',
            selector: 'td:nth-child(1)'
        },{
          name: 'parentName', // 上级名称
          dataType: 'string',
          selector: 'td:nth-child(1) input[name=hidzxs]@value'
      },{
          name: 'siteCode', // 网站代码，用于查询
          dataType: 'string',
          selector: 'td:nth-child(1) input[name=xzcode]@value'
      },{
            name: 'station', // 驻地
            dataType: 'string',
            selector: 'td:nth-child(2)'
        },{
             name: 'population', // 人口
             dataType: 'string',
             selector: 'td:nth-child(3)'
         },{
           name: 'area', // 面积
           dataType: 'string',
           selector: 'td:nth-child(4)'
       },{
           name: 'code', // 行政区划代码
           dataType: 'string',
           selector: 'td:nth-child(5)'
       },{
           name: 'phoneCode', // 电话区号
           dataType: 'string',
           selector: 'td:nth-child(6)'
       },{
           name: 'postcode', // 电话区号
           dataType: 'string',
           selector: 'td:nth-child(7)'
       }]
    }
}
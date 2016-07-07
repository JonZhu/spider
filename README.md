# spider
数据抓取工具

## 2016-06-07 plan
<ol>
	<li>paging executor实现 (do)</li>
	<li>异常逻辑处理, 如fetch url失败</li>
	<li>master和worker分离，实现分布式</li>
</ol>

## 2016-06-10 plan
<ol>
	<li>spider任务调度状态持久化</li>
</ol>

## 数据储存文件结构

描述系统中数据怎样储存，数据结构。本系统数据分为两部分：
<ol>
	<li>master数据，包括 spider任务列表</li>
	<li>spider数据，包括 spider任务运行时数据，url列表，抓取内容</li>
</ol>

下面分别介绍这两种数据

### Master数据

Master数据使用sqlite db文件储存, 文件路径为：master运行目录/db/master.db。<br/>
master.db中表结构如下：

spider_task表，用于储存master系统中spider任务列表。
<table>
	<tr>
		<td>字段</td>
		<td>类型</td>
		<td>空</td>
		<td>主键</td>
		<td>外键</td>
		<td>说明</td>
	</tr>
	<tr>
		<td>id</td>
		<td>vc(100)</td>
		<td>N</td>
		<td>Y</td>
		<td>N</td>
		<td>主键</td>
	</tr>
	<tr>
		<td>name</td>
		<td>vc(200)</td>
		<td>N</td>
		<td>N</td>
		<td>N</td>
		<td>名称</td>
	</tr>
	<tr>
		<td>author</td>
		<td>vc(100)</td>
		<td>Y</td>
		<td>N</td>
		<td>N</td>
		<td>作者</td>
	</tr>
	<tr>
		<td>datadir</td>
		<td>vc(200)</td>
		<td>N</td>
		<td>N</td>
		<td>N</td>
		<td>spider任务数据目录</td>
	</tr>
	<tr>
		<td>createtime</td>
		<td>datetime</td>
		<td>N</td>
		<td>N</td>
		<td>N</td>
		<td>创建时间</td>
	</tr>
</table>


### Spider任务数据目录
数据目录为spider的datadir属性指向的目录。目录中文件如下：

<table>
	<tr>
		<td>路径</td>
		<td>用途</td>
	</tr>
	<tr>
		<td>data-时间</td>
		<td>抓取内容数据</td>
	</tr>
	<tr>
		<td>spiderdsl.xml</td>
		<td>定义该spider任务的dsl原始内容</td>
	</tr>
	<tr>
		<td>spider.db</td>
		<td>该spider任务运行数据，为sqlite文件</td>
	</tr>
	<tr>
		<td>datascope.bin</td>
		<td>任务运行时,数据域,为任务继续执行做支持</td>
	</tr>
</table>


spider.db中表结构如下：

fetchurl表，用于记录抓取的url任务及状态。
<table>
	<tr>
		<td>字段</td>
		<td>类型</td>
		<td>空</td>
		<td>主键</td>
		<td>外键</td>
		<td>说明</td>
	</tr>
	<tr>
		<td>id</td>
		<td>bigint</td>
		<td>N</td>
		<td>Y</td>
		<td>N</td>
		<td>主键 autoincrement</td>
	</tr>
	<tr>
		<td>url</td>
		<td>vc(500)</td>
		<td>N</td>
		<td>N</td>
		<td>N</td>
		<td>要抓取的url地址</td>
	</tr>
	<tr>
		<td>status</td>
		<td>int</td>
		<td>N</td>
		<td>N</td>
		<td>N</td>
		<td>状态: 0 初始态, 2 已下发, 3 抓取成功, 4 抓取失败</td>
	</tr>
	<tr>
		<td>inserttime</td>
		<td>datetime</td>
		<td>N</td>
		<td>N</td>
		<td>N</td>
		<td>数据插入时间</td>
	</tr>
	<tr>
		<td>modifytime</td>
		<td>datetime</td>
		<td>N</td>
		<td>N</td>
		<td>N</td>
		<td>最后更新时间</td>
	</tr>
	<tr>
		<td>actionid</td>
		<td>varchar(100)</td>
		<td>Y</td>
		<td>N</td>
		<td>N</td>
		<td>spider中哪个action生成的url</td>
	</tr>
</table>


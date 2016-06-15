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

数据目录为spider的datadir属性指向的目录

<table>
	<tr>
		<td>路径</td>
		<td>用途</td>
	</tr>
	<tr>
		<td>/data-时间</td>
		<td>抓取内容数据</td>
	</tr>
	<tr>
		<td>/fetchqueue</td>
		<td>抓取url队列</td>
	</tr>
	<tr>
		<td>/fetchqueue.index</td>
		<td>抓取url队列有效数据位置偏移</td>
	</tr>
</table>
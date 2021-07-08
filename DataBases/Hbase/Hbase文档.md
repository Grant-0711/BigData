# 1 Hbase简介

## 1.1 hbase是什么?

​		hbase是分布式.存储海量数据的NOsql数据库

## 1.2 hbase的应用场景?

​		hbase一般用于存储实时处理的结果数据

## 1.3 hbase数据模型

​		Table: hbase的数据以表的形式存储

​		列簇: hbase的表结构,hbase的数据是以字节数组的形式存储

​		rowkey: 每条数据的唯一索引,类似mysql的主键
​			hbase的数据有序的,以rowkey字典序排序

​		列限定符: 相当于字段

​		Version: 版本,在创建表的时候指定的,version代表的是列簇下的列限定符最多可以存放多少分历史数据

​		timestamp: 每条数据写入时间,后续通过时间戳判断数据是否为最新数据

​		region: 表的分段,每个region保存在不同的服务器上

​		store: region中会根据列簇进行划分,一个列簇对应一个store

​		NameSpace:命名空间,类似mysql的库

​		Column: 列簇+列限定符代表一个列

​		Row: row代表逻辑结构的一行

​		Cell: rowkey+列簇+列限定符+时间戳可以确定唯一的一个cell

## 1.4 hbase基本架构

### Master

职责:
				1.监听regionserver的状态,通过zookeeper监听
				2.负责region的故障转换[regionserver宕机之后,会将宕机的regionserver中的region在其他或者的regionserver中创建出来,与保存在HDFS的数据关联上]
				3.负责regionserver中region的负载均衡
				4.负责表的创建.删除.修改

### RegionServer

职责:
				1.regionserver保管region,负责client对数据的增删改查
				2.负责region split.storeFile compact

### Region: 表的分段

### Store: 一个列簇对应一个store

### memstore

是一块内存区域,后续client会将数据写入memstore中,等到memstore达到一定的阈值之后会flush落盘【数据在memstore落盘之前会进行排序】

### storeFile

memstore flush生成,每flush一次生成一个storeFile文件,storeFile最终以HFile文件格式保存在HDFS

### HLog

预写日志。如果没有HLog,数据直接写入memstore,此时如果regionserver宕机,memstore的数据会丢失,所以保证memstore的数据不丢失,client写入数据到memstore之前,是先通过Hlog将数据以日志的形式保存在HDFS的

一个regionserver对应一个Hlog
memstore flush之后,Hlog会清理掉memstore flush的数据

### zookeeper

master监听regionserver状态.元数据的位置信息等依赖于zookeeper

# 2 hbase常用shell指令

## 2.1 namespace相关

​		1.创建namespace: create_namespace '命名空间名称'
​		2.查看所有的命名空间： list_namespace
​		3.查看命名空间下所有表: list_namespace_tables '命名空间名称'
​		4.删除命名空间[删除命名的时候,命名空间下不能有表,如果有表不能删除命名空间]: drop_namespace '命名空间名称'

## 2.2 表相关

​		1.创建表[版本默认为1]: create '表名','列簇名1','列簇2',..
​		2.创建表,指定版本号:  create '表名',{NAME=>'列簇名',VERSIONS=>版本数},...
​		3.修改表: alter '表',{NAME=>'列簇名',VERSIONS=>版本数},..
​		4.删除表: 
​			1.禁用表: disable '表名'
​			2.删除表: drop '表名'
​		5.启动表: enable '表名'
​		6.查看所有表: list 

## 2.3 数据相关

### 2.3.1 插入/修改数据

put '命名空间名称:表名',rowkey,'列簇名:列限定符名称',值

### 2.3.2 查询数据

​			根据rowkey查询单条数据:
​				1.查询整行数据: get '命名空间名称:表名',rowkey
​				2.查询某个列的数据: get '命名空间名称:表名',rowkey,'列簇名:列限定符名称'
​				3.查看某个列簇的数据: get '命名空间名称:表名',rowkey,'列簇名'
​				4.查看某个时间戳的数据: get '命名空间名称:表名',rowkey,{COLUMN=>'列簇名:列限定符名称',TIMESTAMP=>时间戳}
​				5.查看多个版本的数据: get '命名空间名称:表名',rowkey,{COLUMN=>'列簇名:列限定符名称',VERSIONS=>版本数}
​			扫描表
​				1.查询整表数据: scan '命名空间名称:表名'
​				2.查询查询某个列的数据: scan '命名空间名称:表名',{COLUMNS=>'列簇名:列限定符名称'}
​				3.查看某个列簇的数据： scan '命名空间名称:表名',{COLUMNS=>'列簇名'}
​				4.查看多个版本的数据：scan '命名空间名称:表名',{COLUMNS=>'列簇名',VERSIONS=>版本数}
​				5.查看指定rowkey范围段的数据: scan '命名空间名称:表名',{STARTKEY=>起始rowkey,STOPKEY=>结束rowkey} [查询结果不包含stopkey]

### 2.3.3 删除数据

​			1.delete： 只能删除单个cell
​				delete '命名空间名称:表名',rowkey,'列簇名:列限定符名称'
​			2.deleteall： 可以删除单个cell也可以删除整行数据
​				1.删除单个cell: deleteall '命名空间名称:表名',rowkey,'列簇名:列限定符名称'
​				2.删除整行: deleteall '命名空间名称:表名',rowkey
​		4.统计表的行数: count '命名空间名称:表名'
​		5.清空表数据: truncate '命名空间名称:表名'

# 3 hbase原理

## 3.1 元数据表[hbase:meta]

​		元数据表hbase:meta只有一个region
​		元数据表的rowkey=表名,region的起始rowkey,时间戳.region名称
​		元数据表的列中保存region名称,startKey,stopKey,region所在的regionserver信息..
​		后续通过rowkey查询/插入/修改数据的时候,可以通过数据rowkey与region的startkey.stopkey对比就可以知道数据在哪个region中,region在哪个regionserver中

## 3.2 数据写入流程

​		1.client向zookeeper发起获取元数据表所在regionserver的请求
​		2.zookeeper向client返回元数据表所在regionserver信息
​		3.client向元数据表所在的regionserver发起获取元数据的请求
​		4.regionserver返回元数据信息给client,client会缓存元数据,后续需要元数据的时候可以直接从缓存中获取
​		5.client通过元数据得知数据应该写入哪个region,region处于哪个regionserver,向region所在的regionserver发起数据写入请求
​		6.首先通过Hlog将数据以日志的形式写入HDFS
​		7.再讲数据写入region所在store的memstore中
​		8.regionserver向client返回信息告知写入完成

## 3.3 数据读取流程

​		1.client向zookeeper发起获取元数据表所在regionserver的请求
​		2.zookeeper向client返回元数据表所在regionserver信息
​		3.client向元数据表所在的regionserver发起获取元数据的请求
​		4.regionserver返回元数据信息给client,client会缓存元数据,后续需要元数据的时候可以直接从缓存中获取
​		5.client通过元数据得知数据应该写入哪个region,region处于哪个regionserver,向region所在的regionserver发起数据读取请求
​		6.首先从读缓存中查询数据
​		7.再从memstore.storeFile中查询数据
​			如何快速从storeFile中查询到数据?
​				1.通过布隆过滤器判断数据大概处于哪些storeFile文件中
​					布隆过滤器特性: 如果判断存在则不一定存在，如果判断不存在则一定不存在
​				2.对布隆过滤器筛选的storeFile再次进行查询,通过HFile文件格式中的数据索引查询数据是否真实存在于storeFile文件中,如果存在则通过索引得到数据
​		8.将查询结果合并之后返回给client

## 3.4 memstore flush的触发条件

​		实际工作中,创建表的时候列簇的个数一般为1个,最多不超过两个【原因: 避免在flush的时候产生大量的小文件】
​		hbase在flush的时候是flush整个region而不是当个memstore
​		1.region中某个memstore的数据量达到128M的时候,region会flush
​		2.region中所有的memstore的总数据量达到512M的时候,region会flush
​		3.regionserver中所有的region的所有的memstore的总数据量达到 java_heap * 40% * 95%,此时会触发flush,在flush之前会regionserver中所有region按照memstore占用的空间大小进行排序,优先flush占用空间多的region
​		4.如果处于写高峰期的时候,此时第三个触发条件可以适当延迟,等到regionserver中所有的region的所有的memstore的总数据量达到java_heap * 40%,此时会阻塞client写入,优先flush。
​			在flush之前会regionserver中所有region按照memstore占用的空间大小进行排序,优先flush占用空间多的region,每次flush完一个region就会判断当前regionserver中所有的region的所有的memstore的总数据量<=java_heap * 40% * 95%,如果满足条件则会停止flush允许client继续写入
​		5.region距离上一次flush如果达到1小时,此时触发region flush
​		6.regionserver的预写日志的文件数达到32的时候,会触发region flush
​		7.手动flush： flush '表名'

## 3.5 storeFile Compact

​		原因: memstore每flush一次都会生成一个storeFile文件,随着flush次数越来越多,storeFile越来越多,次数随着文件数的变多,查询效率下降,所以需要合并storeFile文件
​		minor compact：
​			触发条件: 小文件达到3个,会触发minor compact
​			合并过程: minor compact在合并过程中不会删除无效版本以及标记删除的数据
​			合并结果: 将相邻的小文件合并成大文件,最终一个store下可能有多个大文件
​		major compact：
​			触发条件: 7天一次
​			合并过程: 在合并过程中会删除无效版本以及标记删除的数据
​			合并结果: 将store中所有的文件合并成一个文件

## 3.6 region Split

​		原因: region随着写入的数据量越来越多,服务器客户端请求负载压力会越来越大,所以需要切分region，将region由master分配到不同的服务器上减轻负载压力
​		region split的触发条件:
​			0.9版本之前: region中某个store的总数据量达到10G会切分region
​			0.9-2.0版本: N==0 || N>10 ? 10G: Min(10G,2*128*N^3)
​				N代表region所属表在当前regionserver中的region个数
​			2.0版本: N==1 ? 2*128M : 10G

# 4 hbase优化

## 4.1 预分区

​		原因: 默认在创建表的时候,表只有一个region,那么如果读写并发请求比较多,此时region所在的regionserver负载压力会比较大,所以需要在创建表的时候多创建几个region,分散负载压力
​		如何预分区:
​			1.shell命名操作:
​				1.create '表名','列簇',...,SPLITS=>['rowkey1','rowkey2',..]
​					此时创建的表region个数 = splits中rowkey的个数+1
​				2.create '表名','列簇',...,SPLITS_FILE=>'文件路径'
​					此时创建的表region个数 = SPLITS_FILE文件中rowkey的个数+1
​				3.create '表名','列簇',...,{NUMREGIONS=>region个数,SPLITALGO=>'HexStringSplit'}
​			2.api操作:
​					byte[][] splitKeys = {"rowkey1".getBytes(),"rowkey2".getBytes(),..}
​				1.admin.createTable(tableDesc,splitKeys)
​					byte[] startKey = "rowkey1".getBytes()
​					byte[] stopKey = "rowkey2".getBytes()
​				2.admin.createTable(tableDesc,startKey,stopKey,region个数)

## 4.2 rowkey设计

​		rowkey设计的原则:
​			1.长度原则: rowkey不能太长,一般为16字节以下
​				太长会占用过多的存储空间.如果客户端缓存空间不足的时候缓存的元数据条数会比较少
​			2.唯一性原则: 两条数据rowkey不能一样
​			3.hash原则: 将数据均衡分布在不同的region中
​		rowkey如果设计不合理可能导致数据集中分布在某一个region上,导致该region负载压力会比较大[热点问题]
​		热点问题解决方案:
​			1.给rowkey加盐[加随机数]
​			2.将rowkey反转
​			3.使用rowkey的hashcode

## 4.3 内存优化

工作中一般给hbase分配内存为16-48G

## 4.4 基础优化

1.允许HDFS文件追加数据
			原因:hbase数据写入HDFS的时候是以追加的形式写入,默认是开启的

2.调整client的缓存大小
			原因: 如果client的缓存比较小,元数据比较大,此时缓存中只能缓存一部分元数据,后续client需要元数据的时候可能缓存中没有需要重新通过zookeeper查找元数据位置,然后再向regionserver发起请求获取元数据,这样的话效率比较低。所以如果将client缓存调大,能够存放更多的元数据,后续客户端可以直接从缓存中获取元数据,提高效率

3.调整hbase的RPC监听数
			原因: hbase有一个线程池,线程池中线程用于处理client的请求,如果并发量比较大,此时线程不够用,会出现请求排队,效率比较低

4.优化hbase写入效率
			原因: 在写入数据到HDFS的时候可以将数据压缩写入,减少写入的时间,提高效率

5.调整client请求超时时间
			原因: 如果网络延迟比较大,此时client请求可能会超时,需要重新发起请求,效率比较低

6.调整store的大小[10G]
			原因: 后续如果使用MR读取hbase的数据的时候,maptask的数量 = store的数量,一个maptask处理一个store的数据,如果store的数据量比较大,此时单个maptask花费的时间比较长

7.调整scan.next的扫描数据条数
			原因: scan.next扫描的数据如果比较多会占用大量的内存空间

8.调整datanode最大文件打开数
			原因: 读取/写入数据的时候都会打开datanode的文件,datanode同一时间能够打开的文件数默认是4096个,如果并发量比较大,此时可能需要同一时间打开大量的文件,最大文件打开数如果不够,此时会出现请求排队,影响效率

9.region split.memstore flush.storeFile compact参数调整
			一般需要调整hbase.regionserver.global.memstore.size.lower.limit,从默认的0.95调整到0.75左右

# 5 phoenix

## 5.1 shell操作

### 5.1.1 创建表

​			1.hbase表不存在
​				1.单个字段作为主键
​					create table 表名(
​						字段名 类型 primary key,
​						....
​					)
​					phonenix中主键对应hbase表的rowkey
​				2.组合主键
​					create table 表名(
​						字段名 类型,
​						....,
​						CONSTRAINT 主键名 PRIMARY KEY(字段名,..)
​					)
​				在phoenix中创建表的时候如果hbase的表不存在会同步创建hbase的表

### 5.1.2  hbase表存在

[在phoenix中创建表与hbase表建立映射关系]
1.建表映射
					create table 表名(
							字段名 类型 primary key,
							"列簇"."列限定符名称" 类型,
							....)
2.建视图映射
					create view 表名(
							字段名 类型 primary key,
							"列簇"."列限定符名称" 类型,
							....)
视图与表的区别:
1.建表的时候如果hbase表不存在,会自动创建。表可以进行数据增删改查,删除phoenix表的时候会同步删除hbase表

2.建视图的时候如果hbase表不存在,会报错。视图是能查询数据,删除phoenix视图的时候不会删除hbase的表

在创建表的时候,表名默认会自动变成大写,如果想要表名/字段名保持小写,需要通过""括起来,后续在查询/插入/删除数据/删除表的时候,表名/字段必须也要用""括起来保持小写

### 5.1.3  删除表

drop table 表名

### 5.1.4 数据操作

​			1.插入/修改数据: upsert into 表名(字段名,..) values(值,..)
​			2.查询: select ... from .. where .. group by ..
​			3.删除数据: delete from 表名 where ..

## 5.2 二级索引

原因: 
hbase通过rowkey查询的时候可以通过元数据中region的startkey与stopkey对比知道rowkey对应的数据在哪个region，region处于哪个regionserver。此时查询比较快。

hbase通过字段值来查询的时候,不能通过元数据知道数据处于哪个region中,所以需要全部扫描,此时效率比较低。此时可以通过二级索引提高查询效率

全局二级索引
			语法: create index 索引名 on 表(字段名,..) [include (字段名,..)]
			原理: 给表字段建索引之后,会生成一个索引表,索引表中rowkey=索引字段值原来的rowkey,所以后续根据索引字段查询数据的时候,实际上是查询的索引表,此时可以通过索引表的元数据知道字段值大概处于索引表哪些region中。

全局索引限制:
1、查询的时候如果select查询的字段不在索引表中,此时会查询原表,会全表扫描【可以将select查询的字段通过include包含起来,此时查询的时候是范围扫描】

2、如果建的多个字段的组合索引,查询的时候必须带上组合索引第一个字段,而且必须和其他查询条件是and的关系,除此之外其他查询都是全表扫描

**全局索引一般不常用**

本地二级索引【工作常用[工作中一般用es/mysql创建二级索引]】
语法: create local index 索引名 on 表(字段名,..)
原理: 生成索引之后,在原表中插入索引数据,索引数据的rowkey=__索引字段值.._原来的rowkey,后续查询的时候是扫描索引数据,然后根据索引数据得到原来的rowkey,再根据原来的rowkey查询详细数据。

# 6 hive整合

hbase整合hive之后,可以在hive中通过sql语句操作Hbase数据。

内部表:
		在创建hive表的时候,会同步在hbase中也创建表[如果hbase表存在会报错]
		删除hive表的时候,会同步删除hbase的表

语法:
		CREATE TABLE 表名(字段名 类型,..) 
		STORED BY 'org.apache.hadoop.hive.hbase.HBaseStorageHandler'
		WITH SERDEPROPERTIES ("hbase.columns.mapping" = ":key,列簇名:列限定符,..") --映射hive字段与hbase字段[:key代表hbaserowkey]
		TBLPROPERTIES ("hbase.table.name" = "hbase表名");

外部表:【其实就是与hbase已经存在的表建立映射】
		在创建hive表的时候,此时要求hbase的表已经存在[如果hbase表不存在会报错]
		删除hive表的时候,hbase表不会删除
		CREATE external TABLE 表名(字段名 类型,..) 
		STORED BY 'org.apache.hadoop.hive.hbase.HBaseStorageHandler'
		WITH SERDEPROPERTIES ("hbase.columns.mapping" = ":key,列簇名:列限定符,..") --映射hive字段与hbase字段[:key代表hbaserowkey]
		TBLPROPERTIES ("hbase.table.name" = "hbase表名");
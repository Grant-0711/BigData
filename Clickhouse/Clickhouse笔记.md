**尚硅谷大数据之实时数仓****(Flink版)**

**Clickhouse数据库**

 (作者：尚硅谷大数据研发部)

 

版本：V1.0.0

# **第1章** **C****lick****house****简介**

ClickHouse 是俄罗斯的Yandex于2016年开源的列式存储数据库（DBMS），使用C++语言编写，主要用于在线分析处理查询（OLAP），能够使用SQL查询实时生成分析数据报告。 

 

Ø OLAP场景的关键特征

\1. 大多数是读请求 

\2. 数据总是以相当大的批(> 1000 rows)进行写入 

\3. 不修改已添加的数据 

\4. 每次查询都从数据库中读取大量的行，但是同时又仅需要少量的列 

\5. 宽表，即每个表包含着大量的列 

\6. 较少的查询(通常每台服务器每秒数百个查询或更少) 

\7. 对于简单查询，允许延迟大约50毫秒 

\8. 列中的数据相对较小： 数字和短字符串(例如，每个URL 60个字节) 

\9. 处理单个查询时需要高吞吐量（每个服务器每秒高达数十亿行） 

\10. 事务不是必须的 

\11. 对数据一致性要求低 

\12. 每一个查询除了一个大表外都很小 

\13. 查询结果明显小于源数据，换句话说，数据被过滤或聚合后能够被盛放在单台服务器的内存中

# **第2章** **C****lickhouse****的特点**

## **2.1** **列式存储**

| Id   | Name | Age  |
| ---- | ---- | ---- |
| 1    | 张三 | 18   |
| 2    | 李四 | 22   |
| 3    | 王五 | 34   |

Ø 采用行式存储时，数据在磁盘上的组织结构为：

| 1    | 张三 | 18   | 2    | 李四 | 22   | 3    | 王五 | 34   |
| ---- | ---- | ---- | ---- | ---- | ---- | ---- | ---- | ---- |
|      |      |      |      |      |      |      |      |      |

好处是想查某个人所有的属性时，可以通过一次磁盘查找加顺序读取就可以。但是当想查所有人的年龄时，需要不停的查找，或者全表扫描才行，遍历的很多数据都是不需要的。

 

Ø 而采用列式存储时，数据在磁盘上的组织结构为：

| 1    | 2    | 3    | 张三 | 李四 | 王五 | 18   | 22   | 34   |
| ---- | ---- | ---- | ---- | ---- | ---- | ---- | ---- | ---- |
|      |      |      |      |      |      |      |      |      |

​	这时想查所有人的年龄只需把年龄那一列拿出来就可以了

 

**Ø** **列式储存的好处：**

\1. 对于列的聚合，计数，求和等统计操作原因优于行式存储。

\2. 由于某一列的数据类型都是相同的，针对于数据存储更容易进行数据压缩，每一列选择更优的数据压缩算法，大大提高了数据的压缩比重。

\3. 由于数据压缩比更好，一方面节省了磁盘空间，另一方面对于cache也有了更大的发挥空间。

## **2.2** **DBMS功能**

Ø ClickHouse支持基于SQL的声明式查询语言，该语言大部分情况下是与SQL标准兼容的。 

Ø 支持的查询包括 GROUP BY，ORDER BY，IN，JOIN以及非相关子查询。 

**Ø** **不支持窗口函数和相关子查询。**

## **2.3** **多样化引擎**

​	clickhouse和mysql类似，把表级的存储引擎插件化，根据表的不同需求可以设定不同的存储引擎。

​	目前包括**合并树**、日志、接口和其他四大类20多种引擎

 

## **2.4** **高吞吐写入能力**

​	ClickHouse采用类LSM Tree的结构，数据写入后定期在后台Compaction。

 

​	通过类LSM tree的结构，ClickHouse在数据导入时全部是顺序append写，写入后数据段不可更改，在后台compaction时也是多个段merge sort后顺序写回磁盘。

 

​	顺序写的特性，充分利用了磁盘的吞吐能力，即便在HDD上也有着优异的写入性能。

 

​	官方公开benchmark测试显示能够达到50MB-200MB/s的写入吞吐能力，按照每行100Byte估算，大约相当于50W-200W条/s的写入速度。

## **2.5** **数据分区和线程并行**

​	ClickHouse将数据划分为多个partition，每个partition再进一步划分为多个index granularity(粒度)，然后通过多个CPU核心分别处理其中的一部分来实现并行数据处理。

 

​	在这种设计下，单条Query就能利用整机所有CPU。极致的并行处理能力，极大的降低了查询延时。

 

​	所以，clickhouse即使对于大量数据的查询也能够化整为零平行处理。

​	

​	但是有一个弊端就是对于单条查询使用多cpu，就不利于同时并发多条查询。所以对于高qps的查询业务，clickhouse并不是强项。

## **2.6** **一些限制**

\1. 没有完整的事务支持。

\2. 缺少高频率，低延迟的修改或删除已存在数据的能力。仅能用于批量删除或修改数据，但这符合 GDPR。

\3. 稀疏索引使得ClickHouse不适合通过其键检索单行的点查询。

## **2.7** **哪些公司在使用click****house**

<https://clickhouse.tech/docs/zh/introduction/adopters/>

 

# **第3章** **安装单机版click****house**

## **3.1** **准备工作**

### **3.2.1** **CentOS取消打开文件数限制**

sudo vim /etc/security/limits.conf

 

添加以下内容, 如果已经添加过, 则修改

 

\* soft nofile 65536 

\* hard nofile 65536 

\* soft nproc 131072 

\* hard nproc 131072

 

### **3.2.2** **CentOS取消SELINUX**

sudo vim /etc/sysconfig/selinux

 

SELINUX=disabled

### **3.2.3** **关闭防火墙**

如果已经关闭, 跳过该步骤

Ø 查看防火墙状态

sudo firewall-cmd --state

 

Ø 关闭防火墙

sudo systemctl stop firewalld

 

Ø 关闭开机自启动(因为防火墙是服务, 所以开启会自启, 需要关闭)

sudo systemctl disable firewalld

 

## **3.2** **单机安装**

### **3.2.4** **安装依赖的工具**

sudo yum -y install yum-utils initscripts

### **3.2.5** **使用****yum****安装(需要网络)**

sudo rpm --import https://repo.clickhouse.tech/CLICKHOUSE-KEY.GPG

sudo yum-config-manager --add-repo https://repo.clickhouse.tech/rpm/stable/x86_64

sudo yum -y install clickhouse-server clickhouse-client

### **3.2.6** **使用****rmp****离线安装(不需要网络)**

注意: 使用yum和使用rmp二选一

Ø 准备离线安装包

下载地址: <https://repo.yandex.ru/clickhouse/rpm/stable/x86_64/>

Ø 需要下面3个安装包

clickhouse-client-20.6.4.44-2.noarch.rpm   

clickhouse-common-static-20.6.4.44-2.x86_64.rpm 

clickhouse-server-20.6.4.44-2.noarch.rpm

 

Ø 使用rpm安装

sudo rpm -ivh clickhouse-common-static-20.6.4.44-2.x86_64.rpm

sudo rpm -ivh clickhouse-client-20.6.4.44-2.noarch.rpm

sudo rpm -ivh clickhouse-server-20.6.4.44-2.noarch.rpm

 

### **3.2.7** **修改配置文件**

sudo vim /etc/clickhouse-server/config.xml

 

<listen_host>::</listen_host>

允许: 来自任何地方的客户度连接当前服务器.(ipv4和ipv6都可以)

### **3.2.8** **启动****ClickhouseServer**

sudo systemctl start clickhouse-server

### **3.2.9** **启动****ClickhouseClient**

需要先source /etc/profile

Ø 连接本机服务器的9000端口

clickhouse-client -m

 

Ø 连接远程服务器

clickhouse-client --host=hadoop102 -m

### **3.2.10** **关闭开机自启动**

sudo systemctl disable clickhouse-server

# **第4章** **数据类型**

主要介绍一些与其他数据库不同的数据类型

 

参考官方文档: <https://clickhouse.tech/docs/en/sql-reference/data-types/>

 

## **4.1** **整数类型**

Ø Int Ranges 

Int8 - [-128 : 127]

Int16 - [-32768 : 32767]

Int32 - [-2147483648 : 2147483647]

Int64 - [-9223372036854775808 : 9223372036854775807]

 

Ø Uint Ranges 

UInt8 - [0 : 255]

UInt16 - [0 : 65535]

UInt32 - [0 : 4294967295]

UInt64 - [0 : 18446744073709551615]

注意: 这些类型的是严格区分大小写的

## **4.2** **浮点型**

Float32 - float

Float64 - double

 

尽量使用整形, 因为浮点型计算会有精度问题.

hadoop102 :) select 1-0.9;

 

SELECT 1 - 0.9

 

┌───────minus(1, 0.9)─┐

│ 0.09999999999999998 │

 

Ø 浮点型中的: NaN和inf

hadoop102 :) select 1/0;

 

SELECT 1 / 0

 

┌─divide(1, 0)─┐

│          inf │

└──────────────┘

 

hadoop102 :) select 0/0;

 

SELECT 0 / 0

 

┌─divide(0, 0)─┐

│          nan │

└──────────────┘

 

## **4.3** **Decimal类型**

浮点数精度不够, Decimal可以替代浮点型

Decimal32(s)，相当于Decimal(9-s,s)

Decimal64(s)，相当于Decimal(18-s,s)

Decimal128(s)，相当于Decimal(38-s,s)

说明:

\1. s 表示小数位数

## **4.4** **Boolean****类型**

**没有单独的类型来存储布尔值**。可以使用 UInt8 类型，取值限制为 0 或 1

## **4.5** **字符串**

\1. String  (varchar)

​	字符串可以任意长度的。它可以包含任意的字节集，包含空字节。

 

\2. Fixedstring(N) (char(n))

固定长度 N 的字符串，N 必须是严格的正自然数。当服务端读取长度小于 N 的字符串时候，通过在字符串末尾添加空字节来达到 N 字节长度。 当服务端读取长度大于 N 的字符串时候，将返回错误消息。

与String相比，极少会使用FixedString，因为使用起来不是很方便。

## **4.6** **枚举类型**

包括 Enum8 和 Enum16 类型。Enum 保存 'string'= integer 的对应关系。

Enum8 用 'String'= Int8 对描述。

Enum16 用 'String'= Int16 对描述

 

案例:

Ø 建表

CREATE TABLE t_enum

(

​    `x` Enum('hello' = 1, 'world' = 2)

)

ENGINE = TinyLog

 

Ø 插入数据

INSERT INTO t_enum VALUES ('hello'), ('world'), ('hello')     // ok

 

INSERT INTO t_enum values('a')   // error   Unknown element 'a' for type Enum8('hello' = 1, 'world' = 2)

 

Ø 使用场景：

对一些状态、类型的字段算是一种空间优化，也算是一种数据约束。但是实际使用中往往因为一些数据内容的变化增加一定的维护成本，甚至是数据丢失问题。所以谨慎使用。

## **4.7** **时间类型**

目前clickhouse 有三种时间类型

 

\1. Date 接受 **年****-****月****-****日** 的字符串比如 ‘2019-12-16’

\2. Datetime 接受 **年****-****月****-****日 时****:****分****:****秒** 的字符串比如 ‘2019-12-16 20:50:10’

\3. Datetime64 接受 **年****-****月****-****日 时****:****分****:****秒****.****亚秒** 的字符串比如 ‘2019-12-16 20:50:10.66’

 

## **4.8** **数组**

Array(T)：由 T 类型元素组成的数组, T 可以是任意类型，包含数组类型。 

 

但不推荐使用多维数组，ClickHouse 对多维数组的支持有限。例如，不能在 MergeTree 表中存储多维数组。

 

**创建方式:**

\1. 使用Array函数

hadoop102 :) SELECT array(1, 2) AS x, toTypeName(x) ;

 

SELECT

​    [1, 2] AS x,

​    toTypeName(x)

 

┌─x─────┬─toTypeName(array(1, 2))─┐

│ [1,2] │ Array(UInt8)            │

└───────┴─────────────────────────┘

\2. 使用 [ ] 语法 索引从1开始

每个元素有两个索引: 基于1的和基于-1

hadoop102 :) SELECT array(1, 2000) AS x, x[2] ;

 

 

## **4.9** **其他数据类型**

参考官网: <https://clickhouse.tech/docs/en/sql-reference/data-types/>

# **第5章** **表引擎**

## **5.1** **表引擎的作用**

​	表引擎是clickhouse的一大特色。可以说， 表引擎决定了如何存储数据。

包括：

\1. 数据的存储方式和位置，写到哪里以及从哪里读取数据

\2. 支持哪些查询以及如何支持。

\3. 并发数据访问。

\4. 索引的使用（如果存在）

\5. 是否可以执行多线程请求

\6. 数据复制参数。

 

## **5.2** **如何使用表引擎**

​	表引擎的使用方式就是必须显示在创建表时定义该表使用的引擎，以及引擎使用的相关参数。

如：

create table t_tinylog ( id String, name String) **engine=TinyLog**;

 

**注意:**引擎的名称大小写敏感

 

## **5.3** **常用引擎**

### **5.3.1** **TinyLog**

​	以列文件的形式保存在磁盘上，不支持索引，没有并发控制。一般保存少量数据的小表，生产环境上作用有限。可以用于平时**练习测试**用。

### **5.3.2** **Memory**

​	内存引擎，数据以未压缩的原始形式直接保存在内存当中，服务器重启数据就会消失。读写操作不会相互阻塞，不支持索引。简单查询下有非常非常高的性能表现（超过10G/s）。

 

​	一般用到它的地方不多，除了用来测试，就是在需要非常高的性能，同时数据量又不太大（上限大概 1 亿行）的场景。

create table t_memory(id Int16, name String) engine=Memory;

insert into t_memory values(1, 'lisi');

 

重启服务器查询数据已经没有了.

### **5.3.3** **MergeTree**

​	Clickhouse 中最强大的表引擎当属 MergeTree （合并树）引擎及该系列（*MergeTree）中的其他引擎。

​	地位可以相当于innodb之于Mysql。 而且基于MergeTree，还衍生除了很多小弟，也是非常有特色的引擎。

 

**Ø** **建表语句**

create table t_order_mt(

​    id UInt32,

​    sku_id String,

​    total_amount Decimal(16,2),

​    create_time  Datetime

 ) engine=**MergeTree**

 **partition by** toYYYYMMDD(create_time)

 **primary key** (id)

 **order** **by** (id,sku_id)

 

MergeTree其实还有很多参数(绝大多数用默认值即可)，但是上面3个标红参数是更加重要的，也涉及了关于MergeTree的很多概念。

 

**Ø** **插入多条数据**

insert into  t_order_mt

values(101,'sku_001',1000.00,'2020-06-01 12:00:00') ,

(102,'sku_002',2000.00,'2020-06-01 11:00:00'),

(102,'sku_004',2500.00,'2020-06-01 12:00:00'),

(102,'sku_002',2000.00,'2020-06-01 13:00:00')

(102,'sku_002',12000.00,'2020-06-01 13:00:00')

(102,'sku_002',600.00,'2020-06-02 12:00:00')

 

**5.3.3.1** **P****artition** **by** **分区**

**Ø** **作用**： 

​	学过hive的应该都不陌生，分区的目的主要是减少扫描的范围，优化查询速度。如果不填： 只会使用一个分区。

 

**Ø** **分区目录**： 

​	MergeTree 是以列文件+索引文件+表定义文件组成的，但是如果设定了分区那么这些文件就会保存到不同的分区目录中。

 

**Ø** **并行：**

​	分区后，面对涉及跨分区的查询统计，clickhouse会以分区为单位并行处理。

 

**Ø** **数据写入与分区合并：**

  任何一个批次的数据写入都会产生一个**临时分区**，不会纳入任何一个已有的分区。写入后的某个时刻（大概10-15分钟后），clickhouse会自动执行合并操作（等不及也可以手动通过optimize执行），把临时分区的数据，合并到已有分区中。

optimize table xxxx [final]

**5.3.3.2** **primary key主键**

Ø clickhouse中的主键，和其他数据库不太一样，它只提供了数据的**一级索引**，但是却**不是唯一约束**。这就意味着是可以存在相同primary key的数据的。

 

Ø 主键的设定主要依据是查询语句中的where 条件。

根据条件通过对主键进行某种形式的二分查找，能够定位到对应的index granularity,避免了全包扫描。

 

Ø index granularity： 

直接翻译的话就是索引粒度，指在稀疏索引中两个相邻索引对应数据的间隔。clickhouse中的MergeTree默认是8192。官方不建议修改这个值，除非该列存在大量重复值，比如在一个分区中几万行才有一个不同数据。

 

Ø 稀疏索引

稀疏索引的好处就是可以用很少的索引数据，定位更多的数据，代价就是只能定位到索引粒度的第一行，然后再进行进行一点扫描。

 

由于稀疏索引比较少, 所以理论上可以完全加载到内存中, 从提高查询速度

 

![img](file:///C:\Users\LoveKobe\AppData\Local\Temp\ksohtml19416\wps1.jpg) 

**5.3.3.3** **order by**

Ø order by 设定了**分区内**的数据按照哪些字段顺序进行有序保存。

 

Ø order by是MergeTree中唯一一个必填项，甚至比primary key 还重要，因为当用户不设置主键的情况，很多处理会依照order by的字段进行处理（比如后面会讲的去重和汇总）。

 

Ø 要求：主键必须是order by字段的前缀字段。

比如order by 字段是 (id,sku_id)  那么主键必须是id 或者(id,sku_id)

**5.3.3.4** **数据TTL**

TTL即Time To Live，MergeTree提供了可以管理数据或者列的生命周期的功能

 

Ø 列级别的TTL

针对某列数据过期

 

create table t_order_mt3(

​    id UInt32,

​    sku_id String,

​    total_amount Decimal(16,2)  TTL create_time+interval 10 SECOND,

​    create_time  Datetime 

 ) engine =MergeTree

partition by toYYYYMMDD(create_time)

primary key (id)

order by (id, sku_id)

 

insert into  t_order_mt3

values(106,'sku_001',1000.00,'2021-01-16 10:58:30') ,

(107,'sku_002',2000.00,'2020-06-12 22:52:30'),

(110,'sku_003',600.00,'2021-01-17 12:00:00')

 

Ø 表级别的TTL

针对整张表数据过期

alter table t_order_mt3 MODIFY TTL create_time + INTERVAL 20 SECOND;

 

Ø 判断时间

涉及判断的字段必须是Date或者Datetime类型，推荐使用分区的日期字段。

能够使用的时间周期：

\- SECOND

\- MINUTE

\- HOUR

\- DAY

\- WEEK

\- MONTH

\- QUARTER

\- YEAR 

 

### **5.3.4** **ReplacingMergeTree**

​	ReplacingMergeTree是MergeTree的一个变种，它存储特性完全继承MergeTree，只是多了一个**去重**的功能。

 

​	尽管MergeTree可以设置主键，但是primary key其实没有唯一约束的功能。如果你想处理掉重复的数据，可以借助这个ReplacingMergeTree。

 

Ø 什么样的数据是重复

order by字段相同认为重复

 

Ø 去重时机：

数据的去重只会在合并的过程中出现。合并会在未知的时间在后台进行，所以你无法预先作出计划。有一些数据可能仍未被处理。即使使用optimize 也不能保证一定会去重

 

Ø 去重范围：

如果表经过了分区，去重只会在分区内部进行去重，不能执行跨分区的去重。

 

所以ReplacingMergeTree能力有限， ReplacingMergeTree 适用于在后台清除重复的数据以节省空间，但是它**不保证没有重复的数据出现**。

 

**Ø** **建表**

create table t_order_rmt(

​    id UInt32,

​    sku_id String,

​    total_amount Decimal(16,2) ,

​    create_time  Datetime 

) engine =ReplacingMergeTree(create_time)

partition by toYYYYMMDD(create_time)

primary key (id)

order by (id, sku_id);

 

**关于版本:** Type UInt*, Date or DateTime. 

**Ø** **插入数据**

insert into  t_order_rmt

values(101,'sku_001',1000.00,'2020-06-01 12:00:00') ,

(102,'sku_002',2000.00,'2020-06-01 11:00:00'),

(102,'sku_004',2500.00,'2020-06-01 12:00:00'),

(102,'sku_002',2000.00,'2020-06-01 13:00:00')

(102,'sku_002',12000.00,'2020-06-01 13:00:00')

(102,'sku_002',600.00,'2020-06-02 12:00:00')

 

![img](file:///C:\Users\LoveKobe\AppData\Local\Temp\ksohtml19416\wps2.jpg) 

 

**Ø** **执行合并**

optimize table t_order_rmt final;

 

![img](file:///C:\Users\LoveKobe\AppData\Local\Temp\ksohtml19416\wps3.jpg) 

 

**该引擎使用场景:**

​	ReplacingMergeTree 适用于在后台清除重复的数据以节省空间，但是它不保证没有重复的数据出现。

 

### **5.3.5** **SummingMergeTree**

​	对于不查询明细，只关心以维度进行汇总聚合结果的场景。如果只使用普通的MergeTree的话，无论是存储空间的开销，还是查询时临时聚合的开销都比较大。

​	Clickhouse 为了这种场景，提供了一种能够“预聚合”的引擎: SummingMergeTree.

 

Ø 建表

create table t_order_smt(

​    id UInt32,

​    sku_id String,

​    total_amount Decimal(16,2) ,

​    create_time  Datetime 

 ) engine =SummingMergeTree(total_amount)

 partition by toYYYYMMDD(create_time)

   primary key (id)

   order by (id,sku_id )

 

Ø 插入数据

insert into  t_order_smt

values(101,'sku_001',1000.00,'2020-06-01 12:00:00') ,

(102,'sku_002',2000.00,'2020-06-01 11:00:00'),

(102,'sku_004',2500.00,'2020-06-01 12:00:00'),

(102,'sku_002',2000.00,'2020-06-01 13:00:00')

(102,'sku_002',12000.00,'2020-06-01 13:00:00')

(102,'sku_002',600.00,'2020-06-02 12:00:00')

 

![img](file:///C:\Users\LoveKobe\AppData\Local\Temp\ksohtml19416\wps4.jpg) 

 

Ø optimize: 会自动进行预聚合

![img](file:///C:\Users\LoveKobe\AppData\Local\Temp\ksohtml19416\wps5.jpg) 

 

Ø 结论:

\1. 以SummingMergeTree(字段)中指定的列作为汇总数据列。可以填写多列必须数字列，如果不填，以所有非维度列, 非主键且为数字列的字段为汇总数据列\

\2. 以order by 的列为准，作为维度列(group by …)

\3. 其他的列保留第一行。

\4. 不在一个分区的数据不会被预聚合。

\5. 聚合发生的时机不确定

\6. 查询的时候仍然需要sql聚合语句

 

 

# **第6章** **S****ql操作**

## **6.1** **insert**

Ø insert values

INSERT INTO [db.]table [(c1, c2, c3)] VALUES (v11, v12, v13), (v21, v22, v23), ...

 

Ø insert select

INSERT INTO [db.]table [(c1, c2, c3)] SELECT ...

 

## **6.2** **update****和delete**

​	ClickHouse提供了Delete 和Update的能力，这类操作被称为Mutation查询，它可以看做Alter 的一种。

 

​	虽然可以实现修改和删除，但是和一般的OLTP数据库不一样，**M****utation****语句是一种很****“重”的操作，而且不支持事务**。

 

​	“重”的原因主要是每次修改或者删除都会导致放弃目标数据的原有分区，重建新分区。所以尽量做批量的变更，不要进行频繁小数据的操作。

 

Ø 删除操作

alter table t_order_smt delete where sku_id ='sku_001';

Ø 修改操作

alter table t_order_smt 

update total_amount=toDecimal32(2000.00,2) 

where id =102;

 

​	由于操作比较“重”，所以 Mutation语句分两步执行，同步执行的部分其实只是进行新增数据新增分区和并把旧分区打上逻辑上的失效标记。直到触发分区合并的时候，才会删除旧数据释放磁盘空间。

 

## **6.3** **查询操作**

clickhouse基本上与标准SQL 差别不大。

 

**Ø** **支持如下操作:**

WITH clause

FROM clause

SAMPLE clause

JOIN clause

PREWHERE clause

WHERE clause

GROUP BY clause

LIMIT BY clause

HAVING clause

SELECT clause

DISTINCT clause

LIMIT clause

UNION ALL clause

INTO OUTFILE clause

FORMAT clause

 

**Ø** **不支持**

不支持窗口函数。

不支持自定义函数。

 

**Ø** **g****roup by** **子句特殊说明:**

GROUP BY 操作增加了 with rollup\with cube\with totals 用来计算小计和总计。

**1.** **插入数据**

insert into  t_order_mt

values(101,'sku_001',1000.00,'2020-06-01 12:00:00') ,

(102,'sku_002',2000.00,'2020-06-01 12:00:00'),

(103,'sku_004',2500.00,'2020-06-01 12:00:00'),

(104,'sku_002',2000.00,'2020-06-01 12:00:00')

(105,'sku_003',600.00,'2020-06-02 12:00:00'),

(106,'sku_001',1000.00,'2020-06-04 12:00:00'),

(107,'sku_002',2000.00,'2020-06-04 12:00:00'),

(108,'sku_004',2500.00,'2020-06-04 12:00:00'),

(109,'sku_002',2000.00,'2020-06-04 12:00:00'),

(110,'sku_003',600.00,'2020-06-01 12:00:00')

![img](file:///C:\Users\LoveKobe\AppData\Local\Temp\ksohtml19416\wps6.jpg) 

**2.** **with** **rollup:** **从右至左去掉维度进行小计**

select id , sku_id,sum(total_amount) from  t_order_mt group by id,sku_id with rollup;

![img](file:///C:\Users\LoveKobe\AppData\Local\Temp\ksohtml19416\wps7.jpg) 

**3.** **with** **cube:** **各种维度组合进行聚合**

select id , sku_id,sum(total_amount) from  t_order_mt group by id,sku_id with cube;

 

![img](file:///C:\Users\LoveKobe\AppData\Local\Temp\ksohtml19416\wps8.jpg) 

**4.** **with** **taotals:** **仅仅多了一个总计**

select id , sku_id,sum(total_amount) from  t_order_mt group by id,sku_id with totals;

![img](file:///C:\Users\LoveKobe\AppData\Local\Temp\ksohtml19416\wps9.jpg) 

## **6.4** **a****l****ter****操作**

同mysql的修改字段基本一致

 

Ø 新增字段

alter table tableName  add column  newcolname  String after col1;

 

Ø 修改字段类型

alter table tableName  modify column  newcolname  String    ；

 

Ø 删除字段

alter table tableName  drop column  newcolname   ;

## **6.5** **导出数据**

clickhouse-client  --query    "select toHour(create_time) hr  ,count(*) from test1.order_wide where dt='2020-06-23'  group by hr" --format CSVWithNames> ~/rs1.csv

 

 

支持的数据格式: <https://clickhouse.tech/docs/v19.14/en/interfaces/formats/>

# **第7章** **复本(高可用)**

​	副本的目的主要是保障数据的**高可用性**，即使一台clickhouse节点宕机，那么也可以从其他服务器获得相同的数据。

​	

​	clickhouse的副本严重依赖zookeeper, 用于通知副本server状态变更

 

​	副本是表级别的，不是整个服务器级的。所以，服务器里可以同时有复本表和非复本表。

 

## **7.1** **复本写入流程**

![img](file:///C:\Users\LoveKobe\AppData\Local\Temp\ksohtml19416\wps10.jpg) 

## **7.2** **配置规划**

| **hadoo****162** | **hadoop****163** | **hadoop****164** |
| ---------------- | ----------------- | ----------------- |
| zookeeper        | zookeeper         | zookeeper         |
| clickhouse       | clickhouse        |                   |

## **7.3** **在hadoop****163****安装click****house**

[参考第3章](#_安装单机版clickhouse)

 

## **7.4** **创建配置文件:** **metrika.xml**

**Ø** **分别在****hadoop****102****和hadoop****103****创建配置文件****metrika.xml,** **配置zookeeper地址**

sudo vim /etc/clickhouse-server/config.d/metrika.xml

 

<?xml version="1.0"?>

<yandex>

  <zookeeper-servers>

​     <node index="1">

​         <host>hadoop102</host>

​         <port>2181</port>

​     </node>

​     <node index="2">

​         <host>hadoop103</host>

​         <port>2181</port>

​     </node>

<node index="3">

​         <host>hadoop104</host>

​         <port>2181</port>

​     </node>

 

  </zookeeper-servers>

</yandex>

 

**Ø** **告诉clickhouse** **刚刚创建的配置文件的地址**

在hadoop102和hadoop103添加如下配置

sudo vim /etc/clickhouse-server/config.xml

 

找到<zookeeper>节点, 在下面添加如下内容

<include_from>/etc/clickhouse-server/config.d/metrika.xml</include_from>

![img](file:///C:\Users\LoveKobe\AppData\Local\Temp\ksohtml19416\wps11.jpg) 

 

## **7.5** **分别在hadoop****102****和hadoop****103****建表**

clickhouse的复本是**表级别**的. 有些语句不会自动产生复本, 有些语句会自动产生复本

 

Ø 对于 INSERT 和 ALTER 语句操作数据会在压缩的情况下被复制

Ø 而 CREATE，DROP，ATTACH，DETACH 和 RENAME 语句只会在单个服务器上执行，不会被复制

 

所以建表的时候, 需要在2个节点上分别手动建表

 

**Ø** **在hadoop****102****建表**

create table rep_t_order_mt2020 (

​    id UInt32,

​    sku_id String,

​    total_amount Decimal(16,2),

​    create_time  Datetime

) engine =ReplicatedMergeTree('/clickhouse/tables/01/rep_t_order_mt2020','rep_hadoop102')

partition by toYYYYMMDD(create_time)

primary key (id)

order by (id,sku_id);

 

**Ø** **在hadoop****103****上建表**

create table rep_t_order_mt2020 (

​    id UInt32,

​    sku_id String,

​    total_amount Decimal(16,2),

​    create_time  Datetime

) engine =ReplicatedMergeTree('/clickhouse/tables/01/rep_t_order_mt2020','rep_hadoop103')

partition by toYYYYMMDD(create_time)

primary key (id)

order by (id,sku_id);

 

Ø 说明

ReplicatedMergeTree('/clickhouse/tables/01/rep_t_order_mt2020','rep_hadoop103')

​	

​	参数1: 该表在zookeeper中的路径.  

​	/clickhouse/tables/{shard}/{table_name}  通常写法,  

​	shard表示表的分片编号, 一般用01,02,03…表示

​	table_name 一般和表明保持一致就行

​	参数2: 在zookeeper中的复本名.  相同的表, 复本名不能相同

 

Ø 在hadoop102上插入数据

insert into  rep_t_order_mt2020

values(101,'sku_001',1000.00,'2020-06-01 12:00:00') ,

(102,'sku_002',2000.00,'2020-06-01 12:00:00'),

(103,'sku_004',2500.00,'2020-06-01 12:00:00'),

(104,'sku_002',2000.00,'2020-06-01 12:00:00'),

(105,'sku_003',600.00,'2020-06-02 12:00:00')

 

Ø 分别在hadoop102和hadoop103查询

![img](file:///C:\Users\LoveKobe\AppData\Local\Temp\ksohtml19416\wps12.jpg) 

![img](file:///C:\Users\LoveKobe\AppData\Local\Temp\ksohtml19416\wps13.jpg) 

# **第8章** **分片集群(高并发)**

​	复本虽然能够提高数据的可用性，降低丢失风险，但是对数据的横向扩容没有解决。每台机子实际上必须容纳全量数据。

 

​	要解决数据水平切分的问题，需要引入分片的概念。

 

​	通过分片把一份完整的数据进行切分，不同的分片分布到不同的节点上。在通过Distributed表引擎把数据拼接起来一同使用。

 

​	Distributed表引擎本身不存储数据，有点类似于MyCat之于MySql，成为一种中间件，通过分布式逻辑表来写入、分发、路由来操作多台节点不同分片的分布式数据。

 

## **8.1** **读写原理**

![img](file:///C:\Users\LoveKobe\AppData\Local\Temp\ksohtml19416\wps14.jpg) 

![img](file:///C:\Users\LoveKobe\AppData\Local\Temp\ksohtml19416\wps15.jpg) 

## **8.2** **分片集群规划**

规划一个即分片, 有复本的集群.    

![img](file:///C:\Users\LoveKobe\AppData\Local\Temp\ksohtml19416\wps16.jpg) 

| **hadoop****162** | **hadoop****163** | **hadoop****164** |
| ----------------- | ----------------- | ----------------- |
| distribute        |                   |                   |
| shard1  replica1  | shard1  replica2  | shard2  replica1  |

 

**说明:**

\1. shard1 一共两个复本(hadoop102, hadoop103)

\2. shard2 只有一个复本(hadoop104)

## **8.3** **配置分片集群**

Ø 在hadoop102, 打开配置文件:

sudo vim /etc/clickhouse-server/config.d/metrika.xml

 

<?xml version="1.0"?>

<yandex>

​    <clickhouse_remote_servers>

​    <gmall_cluster> <!-- 集群名称--> 

​    <shard>         <!--集群的第一个分片-->

​        <internal_replication>true</internal_replication>

​        <replica>    <!-- 该分片的第一个副本 -->

​            <host>hadoop102</host>

​            <port>9000</port>

​        </replica>

​        <replica>    <!-- 该分片的第二个副本-->

​            <host>hadoop103</host>

​            <port>9000</port>

​        </replica>

​    </shard>

 

​    <shard>  <!--集群的第二个分片-->

​        <internal_replication>true</internal_replication>

​        <replica>    <!-- 该分片的第一个副本-->

​            <host>hadoop104</host>

​            <port>9000</port>

​        </replica>

​    </shard>

​     

​    </gmall_cluster>

​     

​    </clickhouse_remote_servers>

​     

​    <zookeeper-servers>

​        <node index="1">

​            <host>hadoop102</host>

​            <port>2181</port>

​        </node>

 

​        <node index="2">

​            <host>hadoop103</host>

​            <port>2181</port>

​        </node>

​        <node index="3">

​            <host>hadoop104</host>

​            <port>2181</port>

​        </node>

​    </zookeeper-servers>

​     

​	<!-- 宏: 将来建表的时候, 可以从这里自动读取, 每个机器上的建表语句就可以一样了 相当于变量 -->

​    <macros>

​        <shard>01</shard>   <!-- 不同机器放的分片索引不一样,  hadoop103,hadoop104需要更改 -->

​        <replica>hadoop102</replica>  <!-- 不同机器放的副本数不一样, hadoop103,hadoop104需要更改, 以主机命名比较方便-->

​    </macros>

</yandex>

 

**Ø** **分发****metrika.xml****到hadoop****103,hadoop104**

\1. 不要忘记先在hadoop104安装clickhouse

\2. 分发/etc/clickhouse-server/config.xml

\3. 分发/etc/clickhouse-server/config.d/metrika.xml

 

**Ø** **hadoop103** **的宏**

​	<!-- 宏: 将来建表的时候, 可以从这里自动读取, 每个机器上的建表语句就可以一样了 相当于变量 -->

​    <macros>

​        <shard>01</shard>   <!-- 不同机器放的分片索引不一样,  hadoop103,hadoop104需要更改 -->

​        <replica>hadoop103</replica>  <!-- 不同机器放的副本数不一样, hadoop103,hadoop104需要更改, 以主机命名比较方便-->

​    </macros>

</yandex>

 

**Ø** **hadoop****104****的宏**

​	<!-- 宏: 将来建表的时候, 可以从这里自动读取, 每个机器上的建表语句就可以一样了 相当于变量 -->

​    <macros>

​        <shard>02</shard>   <!-- 不同机器放的分片索引不一样,  hadoop103,hadoop104需要更改 -->

​        <replica>hadoop104</replica>  <!-- 不同机器放的副本数不一样, hadoop103,hadoop104需要更改, 以主机命名比较方便-->

​    </macros>

</yandex>

 

## **8.4** **3个节点创建数据库**

分别在hadoop102,hadoop103,hadoop104上创建数据库gmall

create database gmall;

## **8.5** **任意节点创建****本地表**

选择**任意一节点**创建本地表, 会自动同步到其他节点

本地表只负责存储自己的切片数据!

create table st_order_mt_gmall on cluster gmall_cluster (

​    id UInt32,

​    sku_id String,

​    total_amount Decimal(16,2),

​    create_time  Datetime

) engine =ReplicatedMergeTree('/clickhouse/tables/gmall/{shard}/st_order_mt_gmall','{replica}')

partition by toYYYYMMDD(create_time)

primary key (id)

order by (id,sku_id);

## **8.6** **创建分布式表****st_order_mt_gmall_all**

在hadoop162创建分布式表

create table st_order_mt_gmall_all on cluster gmall_cluster

(

​    id UInt32,

​    sku_id String,

​    total_amount Decimal(16,2),

​    create_time  Datetime

)engine = Distributed(gmall_cluster,gmall, st_order_mt_gmall,hiveHash(sku_id));

 

## **8.7** **通过分布式表添加数据**

insert into  st_order_mt_gmall_all 

values(201,'sku_001',1000.00,'2020-06-01 12:00:00') ,

(202,'sku_002',2000.00,'2020-06-01 12:00:00'),

(203,'sku_004',2500.00,'2020-06-01 12:00:00'),

(204,'sku_002',2000.00,'2020-06-01 12:00:00')

(205,'sku_003',600.00,'2020-06-02 12:00:00')

 

## **8.8** **查询数据**

**Ø** **通过分布式表查询**

select * from st_order_mt_gmall_all;

 

可以查询到所有数据!

![img](file:///C:\Users\LoveKobe\AppData\Local\Temp\ksohtml19416\wps17.jpg) 

 

**Ø** **通过本地表查询**

select * from st_order_mt_gmall;

​	只能查到当前节点的分片数据

![img](file:///C:\Users\LoveKobe\AppData\Local\Temp\ksohtml19416\wps18.jpg) 

 

![img](file:///C:\Users\LoveKobe\AppData\Local\Temp\ksohtml19416\wps19.jpg) 

![img](file:///C:\Users\LoveKobe\AppData\Local\Temp\ksohtml19416\wps20.jpg) 
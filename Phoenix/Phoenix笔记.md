盐表

1.Phoenix创建盐表

官网地址：http://phoenix.apache.org/salted.html
创建一个盐表：

    CREATE TABLE test.test_salt 
    (id VARCHAR PRIMARY KEY, 
    name VARCHAR, a
    ge INTEGER, 
    address VARCHAR) 
    SALT_BUCKETS = 20;

创建后，Region Count = 20

如果通过Hbase的console创建，语句为（注意大小写）

create 'TEST:TEST_SALT2', 'f1', SPLITS => ['10', '20', '30', '40']

Region Count = 5

默认情况下，对salted table创建二级索引，二级索引表会随同源表切进行Salted切分，SALT_BUCKETS与源表保持一致。当然，在创建二级索引表的时候也可以自定义SALT_BUCKETS的数量，phoenix没有强制它的数量必须跟源表保持一致。按照开发经验，在phoenix中，SALT_BUCKETS 设置的数量与HBase的 region保持一致。

2.盐表的基础知识

Phoenix Salted Table是phoenix为了防止hbase表rowkey设计为自增序列而引发热点region读和热点region写而采取的一种表设计手段。通过在创建表的时候指定SALT_BUCKETS来实现pre-split(预分割)。如下表示创建表的时候将表预分割到20个region里面。

3.盐表的实现原理

hoenix Salted Table的实现原理是在将一个散列取余后的byte值插入到 rowkey的第一个字节里，并通过定义每个region的start key 和 end key 将数据分割到不同的region，以此来防止自增序列引入的热点问题，从而达到平衡HBase集群的读写性能的目的。
salted byte的计算方式大致如下：hash(rowkey) % SALT_BUCKETS，SALT_BUCKETS的取值为1到256。
默认下salted byte将作为每个region的start key 及 end key，以此分割数据到不同的region，这样能做到具有相同salted byte的数据能够位于同一个region里面。

4.盐表的本质

Salting能够通过预分区(pre-splitting)有助于数据均匀的落在region上，从而显著提升写性能，然而在读的时候，数据的有序性被破坏，会影响一定的性能。官网描述为：

    Using salted table with pre-split would help uniformly distribute write workload across all the region servers, thus improves the write performance. Our own performance evaluation shows that a salted table achieves 80% write throughput increases over non-salted table.
     
    Reading from salted table can also reap benefits from the more uniform distribution of data. Our performance evaluation shows much improved read performances on read queries with salted table over non-salted table when we focus our query on a subset of the data.

Salting 翻译成中文是加盐的意思，本质是在hbase中，rowkey的byte数组的第一个字节位置设定一个系统生成的byte值，这个byte值是由主键生成rowkey的byte数组做一个哈希算法，计算得来的。Salting之后可以把数据分布到不同的region上，这样有利于phoenix并发的读写操作。
Salted table可以自动在每一个rowkey前面加上一个字节，这样对于一段连续的rowkeys，它们在表中实际存储时，就被自动地分布到不同的region中去了。当指定要读写该段区间内的数据时，也就避免了读写操作都集中在同一个region上。
简而言之，如果我们用Phoenix创建了一个saltedtable，那么向该表中写入数据时，原始的rowkey的前面会被自动地加上一个byte（不同的rowkey会被分配不同的byte），使得连续的rowkeys也能被均匀地分布到多个regions。

6.例子

插入数据

    UPSERT INTO TEST.TEST_SALT VALUES('1', 'test01', 22, 'PEK');
     
    UPSERT INTO TEST.TEST_SALT VALUES('2', 'test02', 18, 'SHA');

登录Phoenix的console，查询插入的数据

    [hadoop@hadoop002 bin]$ ./sqlline.py hadoop002:2181
    Setting property: [incremental, false]
    Setting property: [isolation, TRANSACTION_READ_COMMITTED]
    issuing: !connect jdbc:phoenix:hadoop002:2181 none none org.apache.phoenix.jdbc.PhoenixDriver
    Connecting to jdbc:phoenix:hadoop002:2181
    SLF4J: Class path contains multiple SLF4J bindings.
    SLF4J: Found binding in [jar:file:/home/hadoop/app/apache-phoenix-4.14.0-cdh5.14.2-bin/phoenix-4.14.0-cdh5.14.2-client.jar!/org/slf4j/impl/StaticLoggerBinder.class]
    SLF4J: Found binding in [jar:file:/home/hadoop/app/hadoop-2.6.0-cdh5.15.1/share/hadoop/common/lib/slf4j-log4j12-1.7.5.jar!/org/slf4j/impl/StaticLoggerBinder.class]
    SLF4J: See http://www.slf4j.org/codes.html#multiple_bindings for an explanation.
    SLF4J: Actual binding is of type [org.slf4j.impl.Log4jLoggerFactory]
    20/04/04 12:10:23 WARN util.NativeCodeLoader: Unable to load native-hadoop library for your platform... using builtin-java classes where applicable
    Connected to: Phoenix (version 4.14)
    Driver: PhoenixEmbeddedDriver (version 4.14)
    Autocommit status: true
    Transaction isolation: TRANSACTION_READ_COMMITTED
    Building list of tables and columns for tab-completion (set fastconnect to true to skip)...
    143/143 (100%) Done
    Done
    sqlline version 1.2.0
    0: jdbc:phoenix:hadoop002:2181> SELECT * FROM TEST.TEST_SALT;
    +-----+---------+------+----------+
    | ID  |  NAME   | AGE  | ADDRESS  |
    +-----+---------+------+----------+
    | 1   | test01  | 22   | PEK      |
    | 2   | test02  | 18   | SHA      |
    +-----+---------+------+----------+
    2 rows selected (0.137 seconds)
    0: jdbc:phoenix:hadoop002:2181> 

通过Hbase shell 的console，查看插入的数据

    hbase(main):009:0> scan 'TEST:TEST_SALT'
    ROW                                         COLUMN+CELL                                                                                                                 
     \x001                                      column=0:\x00\x00\x00\x00, timestamp=1585973304350, value=x                                                                 
     \x001                                      column=0:\x80\x0B, timestamp=1585973304350, value=test01                                                                    
     \x001                                      column=0:\x80\x0C, timestamp=1585973304350, value=\x80\x00\x00\x16                                                          
     \x001                                      column=0:\x80\x0D, timestamp=1585973304350, value=PEK                                                                       
     \x012                                      column=0:\x00\x00\x00\x00, timestamp=1585973330076, value=x                                                                 
     \x012                                      column=0:\x80\x0B, timestamp=1585973330076, value=test02                                                                    
     \x012                                      column=0:\x80\x0C, timestamp=1585973330076, value=\x80\x00\x00\x12                                                          
     \x012                                      column=0:\x80\x0D, timestamp=1585973330076, value=SHA                                                                       
    2 row(s) in 0.1480 seconds
     
    hbase(main):010:0> 

可以看到，phoenix是在写入数据的时候做了处理，在每条rowkey前面加了一个Byte，这里显示为了16进制。也正是因为添加了一个Byte，所以SALT_BUCKETS的值范围在必须再1 ~ 256之间。在插入数据的时候会计算一个byte字段并将这个字节插入到rowkey的首位置上；而在读取数据的API里面也相应地进行了处理，跳过(skip)第一个字节从而读取到正确的rowkey(注意只有salted table需要这么处理)，所以只能通过phoenix接口来获取数据已确保拿到正确的rowkey。

7.注意事项

在使用SALT_BUCKETS的时候需要注意以下三点：
创建salted table后，应该使用Phoenix SQL来读写数据，而不要混合使用Phoenix SQL和HBase API；
如果通过Phoenix创建了一个salted table，那么只有通过Phoenix SQL插入数据才能使得被插入的原始rowkey前面被自动加上一个byte，通过HBase shell 插入数据无法prefix原始的rowkey。
在使用盐表的时，前期规划如果region的数量为20个，SALT_BUCKETS 设置为 20，后期业务发展region的数量变换后，没有灵活的方式重置SALT_BUCKETS， 只能将表备份后drop，重新创建后再同步数据。

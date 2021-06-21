# Maxwell初始化功能

Mysql中已有的旧数据导入到Kafka中

 Canal无能为力, Maxwell提供了一个初始化功能, 可以满足我们的需求：

```shell
bin/maxwell-bootstrap --user maxwell  \
--password aaaaaa \
--host hadoop162  \
--database gmall2021 \
--table user_info \
--client_id maxwell_1
```

Maxwell-bootstrap不具备将数据直接导入kafka或者hbase的能力，通过--client_id指定将数据交给哪个maxwell进程处理，在maxwell的conf.properties中配置

# conf.properties实例

```shell
# tl;dr config
log_level=info

producer=kafka
kafka.bootstrap.servers=hadoop107:9092,hadoop108:9092,hadoop109:9092
kafka_topic=ods_db
# 按照主键的hash进行分区, 如果不设置是按照数据库分区
producer_partition_by=primary_key

# mysql login info
host=hadoop107
user=maxwell
password=aaaaaa
# 排除掉不想监控的数据库
filter=exclude:gmall2021_realtime.*
# 初始化维度表数据的时候使用
client_id=maxwell_1 
```


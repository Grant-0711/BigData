# 配置密码规则

```mysql
Mysql> set global validate_password_policy=0;
mysql> set global validate_password_length=4;
mysql> FLUSH PRIVILEGES;
```

# 设置用户操作其他数据库权限

```sql
GRANT  SELECT ,REPLICATION SLAVE , REPLICATION CLIENT  ON  *.*  TO 'maxwell'@'%';
```


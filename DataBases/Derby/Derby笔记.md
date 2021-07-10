### 1. Derby 介绍

纯绿色、轻巧、内存占用小

Apache Derby是一个完全用 java 编写的数据库，Derby是一个Open source的产品。

Apache Derby非常小巧，核心部分derby.jar只有2M，既可以做为单独的数据库服务器使用，也可以内嵌在应用程序中使用。

官网下载地址：<http://db.apache.org/derby/derby_downloads.html>

官网文档地址:  <https://builds.apache.org/job/Derby-docs/lastSuccessfulBuild/artifact/trunk/out/getstart/index.html>



### 2. Derby 操作和 Java 访问 

a.创建数据库，并且进行连接(存在则连接，不存在创建后连接)

```shell
--内嵌模式
connect 'jdbc:derby:dedb;user=db_user1;password=111111;create=true'; 

--服务器模式
connect 'jdbc:derby://127.0.0.1:1527/debryDB;user=db_user1;password=111111;create=true';
```

b.新建系统用户表

```sql
create table t_user(uuid varchar(32), name varchar(10), age int, address varchar(40));
```

c.插入一些测试数据

```sql
insert into t_user values('B82A6C5244244B9BB226EF31D5CBE508', 'Miachel', 20, 'street 1');
insert into t_user values('B82A6C5244244B9BB226EF31D5CBE509', 'Andrew', 35, 'street 1');
insert into t_user values('B82A6C5244244B9BB226EF31D5CBE510', 'Orson', 47, 'street 1');
insert into t_user values('B82A6C5244244B9BB226EF31D5CBE511', 'Rambo', 19, 'street 1');
```

注意：操作 Derby 需要使用 ij 工具(和 oracle 的 plus 差不多)，CMD 下面输入 ij 即可进入 ij 模式；

创建数据库的路径取决于你 CMD 的路径,如 C:\Users\Administrator> 下，创建的 Derby 数据库就在该目录下面；

如果你对 sql 比较熟悉的话，操作 derby 没有任何问题。

e.在 Java 程序中使用 Derby

```java
import java.sql.*;

public class DerbyTest {
    private static String driver = "org.apache.derby.jdbc.EmbeddedDriver";
    private static String protocol = "jdbc:derby:";
    String dbName = "E:\\Users\\Workspaces\\Derby\\dedb";

    public static void loadDriver() {
        try {
            Class.forName(driver).newInstance();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void getDataFromDerby() {
        try {
            Connection conn = DriverManager.getConnection(protocol + dbName + ";user=root;password=root;create=true");
            Statement statement = conn.createStatement();
            ResultSet resultSet = statement.executeQuery("select * from t_user");
            while (resultSet.next()) {
                System.out.println(resultSet.getString(1));
                System.out.println(resultSet.getString(2));
            }
            conn.close();
            statement.close();
            resultSet.close();
        } catch (Exception e1) {
            e1.printStackTrace();
        }
    }

    public static void main(String[] args) {
        DerbyTest derbyTest = new DerbyTest();
        loadDriver();
        derbyTest.getDataFromDerby();
    }
}
```


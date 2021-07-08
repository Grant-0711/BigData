# MyBatis

## 1 MyBatis简介

Mybatis是一个java编写的轻量级的半自动的ORM映射的Dao层框架

### ORM

ORM（object relational mapping）: 对象关系映射

​		对象：  面向对象语言中的对象

​		关系：  数据库中的表

​		映射： 一一对应

​		

Java程序中：  java是面向对象的语言，数据应该是封装在对象中。

举例： 存储用户信息

```
public class User{
   private String name;
   private Integer age;}
new User("jack",20);
```

在数据库中建表时，表结构需要和存储的数据结构对应！

```
create table user(
  name	varchar(20),
  age  int)
```

写入数据库：  将java对象的指定属性的值，对应要写入表的某个列！

读取数据库： 将指定记录的每一列的值，封装为Java对象的指定属性。将表中的每一行封装为一个Java对象！

### Dao

Dao(database access object): 数据库访问对象。这个对象提供对数据库增删改查的方法！

一般情况下，我们需要在java程序中，通过某些方法将数据写入到数据库！

方法必须依附于类存在，这个类实例化的对象称为Dao!

举例： 需要将User对象写入到数据库

```
public class UserDao{

   public void deleteUser(User user){
    xxxx}
    public void addUser(User user){
    xxxx }
   public void updateUser(User user){
    xxxx}}
完成添加用户时：
new UserDao.addUser(new User())
```

已经接触过原生JDBC的方式。

### JDBC的弊端

SQL夹在Java代码块里，耦合度高导致硬编码内伤，维护不易。

而实际开发需求中sql是有变化，频繁修改的情况多见。

Hibernate（全自动全映射的ORM框架）的弊端，自动生产的多长、难、复杂SQL，不容易做特殊优化。 

## 2 Mybatis的最大优点

Java代码与SQL分层解耦

原是apache的一个开源项目iBatis, 2010年这个项目由apache software foundation 迁移到了google code，

随着开发团队转投Google Code旗下，ibatis3.x正式更名为Mybatis ，代码于2013年11月迁移到Github（下载地址：https://github.com/mybatis/mybatis-3/releases）。

iBATIS提供的持久层框架包括SQL Maps和Data Access Objects（DAO）

MyBatis消除了几乎所有的JDBC代码和参数手工设置以及对结果集的检索封装。MyBatis可以使用**简单的XML或注解用于配置和原始映射**，将接口和Java的POJO（Plain Old Java Objects，普通的Java对象）映射成数据库中的记录.

本质是一个将SQL语句map到JAVA POJO的框架。

 

## 3  准备工作

①建库建表

```sql
CREATE TABLE tbl_employee(
  id INT(11) PRIMARY KEY AUTO_INCREMENT,
  last_name VARCHAR(255),
  gender VARCHAR(10),
  email VARCHAR(255)
);
INSERT INTO tbl_employee(last_name,gender,email) VALUES('Tom','male','Tom@163.com');

INSERT INTO tbl_employee(last_name,gender,email) VALUES('Jack','male','Jack@163.com');

INSERT INTO tbl_employee(last_name,gender,email) VALUES('Marry','female','Tom@163.com');
```

②POJO对象：Employee类

③建工程，导jar包

④加入log4j的配置文件

⑤加入mybatis的配置文件

```xml
<?xml version="1.0" encoding="UTF-8" ?>
<!DOCTYPE configuration
  PUBLIC "-//mybatis.org//DTD Config 3.0//EN"
  "http://mybatis.org/dtd/mybatis-3-config.dtd">
<configuration>
	<!-- environmets标签里可以配置多个environment标签 -->
	<environments default="development">
		<!-- 一个environmet就代表一个数据库运行环境 -->
		<environment id="development">
			<!-- transactionManager：事务管理器 -->
			<transactionManager type="JDBC" />
			<!--dataSource：数据源，需要配置用户名，密码，驱动，连接 -->
			<dataSource type="POOLED">
				<property name="driver" value="com.mysql.jdbc.Driver" />
				<property name="url" value="jdbc:mysql://localhost:3306/1115_mybatis" />
				<property name="username" value="root" />
				<property name="password" value="1234" />
			</dataSource>
		</environment>
	</environments>
	<mappers>
		<mapper resource="helloworld.xml" />
		<mapper resource="EmployeeMapper.xml"/>
		
		<!-- 注册接口类型 -->
		<mapper class="com.atguigu.mybatis.mapper.EmployeeAnnotationMapper"/>
	</mappers> 
</configuration>
```

⑥配置数据库连接信息

```xml
<property name=*"driver"* value=*"com.mysql.jdbc.Driver"*/>
<property name=*"url"* value=*"jdbc:mysql://localhost:3306/mybatis"*/>
<property name=*"username"* value=*"root"*/>
<property name=*"passw ord"* value=*"1234"*/>
```

##  4 Helloworld

​	mybatis的优点在于java代码和sql语句相分离。

​	sql语句编写在xxxMapper文件中，同时注意需要在全局配置文件中注册!

```xml
<?xml version="1.0" encoding="UTF-8" ?>
<!DOCTYPE mapper
  PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN"
  "http://mybatis.org/dtd/mybatis-3-mapper.dtd">
<mapper namespace="helloworld">
	
  <select id="selectEmployee" resultType="com.atguigu.mybatis.bean.Employee">
    select id,last_name lastName,gender,email from tbl_employee where id=#{id}
  </select>
</mapper>
```

```java
@Test
public void test() throws IOException{
		//从当前的类路径获取mybatis的配置文件
		String resource = "mybatis_config.xml";
		//使用一个流读取配置文件
		InputStream inputStream = Resources.getResourceAsStream(resource);
		//根据配置文件，创建一个SqlSessionFactory
SqlSessionFactory	sqlSessionFactory = new SqlSessionFactoryBuilder().build(inputStream);
SqlSession session = sqlSessionFactory.openSession();
		
		try{
			Object emp = session.selectOne("helloworld.selectEmployee", "1");
			
			System.out.println(emp);
		}finally{
				session.close();}}
```

## 4  接口式编程

使用原生API进行开发有以下不便之处：

​		①我们更倾向于使用Dao---DaoImpl来分层解耦

​		②方法的返回值，是Object类型，不能直接使用，需要强转

​		③方法的入参无法进行严格的检查

​	因此，在后续的mybatis版本中，推荐使用接口式编程开发！

​	要求：

①编写接口，mybatis习惯上以mapper作为接口文件的命名

```java
public interface EmployeeMapper {
	public Employee getEmployeeById(Integer id);}
```

②编写sql语句的配置文件，并与接口中的方法进行绑定

```xml
<?xml version="1.0" encoding="UTF-8" ?>
<!DOCTYPE mapper
  PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN"
  "http://mybatis.org/dtd/mybatis-3-mapper.dtd">
<mapper namespace="com.atguigu.mybatis.mapper.EmployeeMapper">

  <select id="getEmployeeById" 
  resultType="com.atguigu.mybatis.bean.Employee" >
    select id,last_name lastName,gender,email from tbl_employee where id=#{id}
  </select>
```

要求：

namespace:必须和接口的全类名一致

​	sql 的id： 必须和对应的方法名一致

​	select 标签中，返回值类型及参数类型也必须和对应的方法一致，parameterType参数类型也可以不写，mybatis会根据 typeHandler自动判断

③通过sqlsession的getMapper()方法获取接口的代理对象，执行其方法

```java
@Test
	public void testMapper(){
		
		SqlSession sqlSession = sqlSessionFactory.openSession();
		
		try{
			//执行数据库的CRUD
			EmployeeMapper mapper = sqlSession.getMapper(EmployeeMapper.class);
			
			System.out.println(mapper.getClass().getName());//com.sun.proxy.$Proxy3
			
			Employee employee = mapper.getEmployeeById(1);
			
			System.out.println(employee);
			
		}finally{
			sqlSession.close();}}
```

## 5   CRUD 

### 接口式编程流程 

 XXXMapper.java---->XXXMapper.xml---->在全局配置文件中注册---->接口式调用

①xxxMapper.java文件

```java
public interface EmployeeMapper {
  public Employee getEmployeeById(Integer id);
  public void  addEmployee(Employee emp); 
  public void deleteEmployee(Integer id);
  public void updateEmployeeEmail(Employee emp);
  public List<Employee> getAll();}
```

②xxxMapper.xml文件

```xml
<mapper namespace="com.atguigu.mapper.EmployeeMapper">  
<select id="getEmployeeById" resultType="com.atguigu.bean.Employee">
      SELECT `id`,`last_name` lastName,`gender`,`email` FROM `tbl_employee` WHERE `id`=#{id}
</select> 
<insert id="addEmployee" parameterType="com.atguigu.bean.Employee">
         INSERT INTO `tbl_employee`(`last_name`,`gender`,`email`) VALUES(#{lastName},#{gender},#{email})
</insert>
<delete id="deleteEmployee" parameterType="int">
        DELETE FROM `tbl_employee` WHERE id=#{id}
</delete>
<update id="updateEmployeeEmail" parameterType="com.atguigu.bean.Employee">
       UPDATE `tbl_employee` SET `email`=#{email} WHERE `id`=#{id}
</update>
<select id="getAll" resultType="com.atguigu.bean.Employee">
       SELECT `id`,`last_name` lastName,`gender`,`email` FROM `tbl_employee` </select> 
```

③在全局配置文件中注册xxxMapper.xml配置文件

```xml
<mappers>
    <mapper resource="com/atguigu/bean/EmployeeMapper.xml" />
</mappers>
```

④调用，以添加为例

```java
@Test
public void testAddEmployee() throws IOException {
    SqlSession sqlSession = sqlSessionFactory.openSession();
    try{
           EmployeeMapper mapper = sqlSession.getMapper(EmployeeMapper.class);    
           Employee employee=new Employee(null, "Jackie", "male", "Jackie@qq.com"); 
           mapper.addEmployee(employee);        
           sqlSession.commit();
    }finally{   
     sqlSession.close();} }
```

##  6  Sql Session 的细节 

①sqlSession 代表和数据库的一次会话。

sqlSession不是线程安全的，不能被共享！

因此它的范围最好是一个方法对应自己的sqlSession，或一次请求，创建一个sqlSession。

②SqlSession在每次方法执行完成之后，必须保证关闭，因此我们常常在方法的finally语句块中，执行其close()方法，将其关闭。
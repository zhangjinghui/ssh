# Struts2+Spring+Hibernate 框架整合笔记  

---

1. 整合 Spring  
> 1. 导入 jar 包  
> 2. 在 web.xml 中配置 spring 容器  
> 3. 创建 applicationContext.xml 文件，添加命名空间 aop、beans、context、tx  

2. 整合 Hibernate  
> 1. 导入 jar 包  
> 2. 创建 db.properties ，用于配置连接池属性  
> 3. 创建 hibernate.cfg.xml ，用于配置 数据库方言、显示及格式化 sql、自动生成数据表、二级缓存等相关的基本属性的设置  
> 4. 创建相应的持久化类及其映射文件  
> 5. 在 applicationContext.xml 中配置 连接池、sessionFactory、HibernateTransactionManager、事务切面等。  
> 6. 部署项目，并开启服务器，查看是否自动生成对应数据表  

3. 整合 Struts2  
> 1. 导入 jar 包，以及 spring 管理 action 的 jar包。如遇 jar 包冲突，则保留最新版本的 jar 包  
> 2. 在 web.xml 文件中配置 struts2 的核心过滤器   
> 3. 创建相应的 Action，创建 applicationContext-beans.xml 用于配置 actions、service、dao、entities等各层级中的 bean，配置 action 的 bean 时，不要忘记设置 scope 属性为 prototype  
> 4. 导入并修改 struts.xml 文件，配置 action 的 class 属性，不再是全类名，而使用 bean 的 id 代替  

## 项目层级结构  
![struct.png](https://ooo.0o0.ooo/2017/06/16/59431f606b3da.png)
## 相关配置文件  
web.xml
```xml
<?xml version="1.0" encoding="UTF-8"?>
<web-app xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
	xmlns="http://java.sun.com/xml/ns/javaee"
	xsi:schemaLocation="http://java.sun.com/xml/ns/javaee http://java.sun.com/xml/ns/javaee/web-app_3_0.xsd"
	version="3.0">

	<context-param>
		<param-name>contextConfigLocation</param-name>
		<param-value>classpath:applicationContext*.xml</param-value>
	</context-param>

	<listener>
		<listener-class>org.springframework.web.context.ContextLoaderListener</listener-class>
	</listener>

	<filter>
		<filter-name>struts2</filter-name>
		<filter-class>org.apache.struts2.dispatcher.ng.filter.StrutsPrepareAndExecuteFilter</filter-class>
	</filter>

	<filter-mapping>
		<filter-name>struts2</filter-name>
		<url-pattern>/*</url-pattern>
	</filter-mapping>
</web-app>
```
db.properties  
```properties
jdbc.user=root
jdbc.password=zhangjinghui
jdbc.driverClass=com.mysql.jdbc.Driver
jdbc.jdbcUrl=jdbc:mysql:///s2sh

jdbc.initialPoolSize=5
jdbc.maxPoolSize=10
#...
```
hibernate.cfg.xml
```xml
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE hibernate-configuration PUBLIC
		"-//Hibernate/Hibernate Configuration DTD 3.0//EN"
		"http://hibernate.sourceforge.net/hibernate-configuration-3.0.dtd">
<hibernate-configuration>
	<session-factory>
		<property name="hibernate.dialect">org.hibernate.dialect.MySQL5InnoDBDialect</property>
		<property name="hibernate.show_sql">true</property>
		<property name="hibernate.format_sql">true</property>
		<property name="hibernate.hbm2ddl.auto">update</property>
	</session-factory>
</hibernate-configuration>
```
applicationContext.xml
```xml
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
	xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:aop="http://www.springframework.org/schema/aop"
	xmlns:tx="http://www.springframework.org/schema/tx" xmlns:context="http://www.springframework.org/schema/context"
	xsi:schemaLocation="http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans.xsd
		http://www.springframework.org/schema/context http://www.springframework.org/schema/context/spring-context-4.3.xsd
		http://www.springframework.org/schema/aop http://www.springframework.org/schema/aop/spring-aop-4.3.xsd
		http://www.springframework.org/schema/tx http://www.springframework.org/schema/tx/spring-tx-4.3.xsd">

	<context:property-placeholder location="classpath:db.properties" />

	<bean id="dataSource" class="com.mchange.v2.c3p0.ComboPooledDataSource">
		<property name="user" value="${jdbc.user}"></property>
		<property name="password" value="${jdbc.password}"></property>
		<property name="driverClass" value="${jdbc.driverClass}"></property>
		<property name="jdbcUrl" value="${jdbc.jdbcUrl}"></property>

		<property name="initialPoolSize" value="${jdbc.initialPoolSize}"></property>
		<property name="maxPoolSize" value="${jdbc.maxPoolSize}"></property>
	</bean>

	<bean id="sessionFactory"
		class="org.springframework.orm.hibernate4.LocalSessionFactoryBean">
		<property name="dataSource" ref="dataSource"></property>
		<property name="configLocations" value="classpath:hibernate.cfg.xml"></property>
		<property name="mappingLocations" value="classpath:xin/zhangjinghui/ssh/entities/*.hbm.xml"></property>
	</bean>

	<bean id="transactionManager"
		class="org.springframework.orm.hibernate4.HibernateTransactionManager">
		<property name="sessionFactory" ref="sessionFactory"></property>
	</bean>

	<tx:advice id="txAdvice" transaction-manager="transactionManager">
		<tx:attributes>
			<tx:method name="get*" read-only="true" />
			<tx:method name="*" />
		</tx:attributes>
	</tx:advice>

	<aop:config>
		<aop:pointcut expression="execution(* xin.zhangjinghui.ssh.service.*.*(..))"
			id="txPointcut" />
		<aop:advisor advice-ref="txAdvice" pointcut-ref="txPointcut" />
	</aop:config>
</beans>
```
struts.xml
```xml
<?xml version="1.0" encoding="UTF-8" ?>
<!DOCTYPE struts PUBLIC
	"-//Apache Software Foundation//DTD Struts Configuration 2.3//EN"
	"http://struts.apache.org/dtds/struts-2.3.dtd">

<struts>

	<constant name="struts.enable.DynamicMethodInvocation" value="false" />
	<constant name="struts.devMode" value="true" />

	<package name="default" namespace="/" extends="struts-default">
		<action name="user-*" class="userAction" method="{1}">
			<result name="list">/WEB-INF/views/list.jsp</result>
		</action>

	</package>
</struts>
```

## 项目示例  
[ssh](https://github.com/zhangjinghui/ssh)

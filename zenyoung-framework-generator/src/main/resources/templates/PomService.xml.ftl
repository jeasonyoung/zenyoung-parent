<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
	xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
	<modelVersion>4.0.0</modelVersion>

	<artifactId>zy-${serverName}-service</artifactId>
    <version>${'$'}{${serverName}-service.version}</version>
    <packaging>jar</packaging>

    <parent>
       <groupId>top.zenyoung</groupId>
       <artifactId>zy-${serverName}</artifactId>
       <version>1.0.0-SNAPSHOT</version>
       <relativePath>../pom.xml</relativePath>
    </parent>

    <dependencies>
        <!-- 模块依赖 -->
        <#if isProvideServer=true>
        <dependency>
            <groupId>top.zenyoung</groupId>
            <artifactId>zy-${serverName}-api</artifactId>
        </dependency>
        </#if>
        <dependency>
            <groupId>top.zenyoung</groupId>
            <artifactId>zy-${serverName}-common</artifactId>
        </dependency>
        <!-- 框架依赖 -->
         <dependency>
            <groupId>top.zenyoung</groupId>
            <#if hasMicro=true>
            <artifactId>zenyoung-framework-micro</artifactId>
            <#else>
            <artifactId>zenyoung-framework-boot</artifactId>
            </#if>
         </dependency>
        <#if hasOrm=true>
        <dependency>
            <groupId>top.zenyoung</groupId>
            <artifactId>zenyoung-framework-orm</artifactId>
        </dependency>
        </#if>
    </dependencies>

    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
            </plugin>
        </plugins>
    </build>
</project>
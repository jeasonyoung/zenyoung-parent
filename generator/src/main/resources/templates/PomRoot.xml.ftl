<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
	xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
	<modelVersion>4.0.0</modelVersion>

	<artifactId>zy-${serverName}</artifactId>
    <version>1.0.0-SNAPSHOT</version>
    <packaging>pom</packaging>

    <parent>
        <groupId>top.zenyoung</groupId>
        <artifactId>zenyoung-parent</artifactId>
        <version>3.0.0</version>
        <relativePath/>
    </parent>

    <modules>
        <#if hasProvideServer=true>
        <module>zy-${serverName}-api</module>
        </#if>
        <module>zy-${serverName}-common</module>
        <module>zy-${serverName}-service</module>
    </modules>

    <properties>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
        <project.reporting.outputEncoding>UTF-8</project.reporting.outputEncoding>

        <#if hasProvideServer=true>
        <${serverName}-api.version>1.0.0-SNAPSHOT</${serverName}-api.version>
        </#if>
        <${serverName}-common.version>1.0.0-SNAPSHOT</${serverName}-common.version>
    	<${serverName}-service.version>1.0.0-SNAPSHOT</${serverName}-service.version>
    </properties>

    <dependencyManagement>
        <dependencies>
            <#if hasProvideServer=true>
            <dependency>
                <groupId>top.zenyoung</groupId>
                <artifactId>zy-${serverName}-api</artifactId>
                <version>${'$'}{${serverName}-api.version}</version>
            </dependency>
            </#if>
            <dependency>
                <groupId>top.zenyoung</groupId>
                <artifactId>zy-${serverName}-common</artifactId>
                <version>${'$'}{${serverName}-common.version}</version>
            </dependency>
        </dependencies>
    </dependencyManagement>

    <dependencies>
        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
        </dependency>
    </dependencies>

    <build>
        <resources>
            <resource>
                <directory>src/main/java</directory>
                <includes>
                    <include>**/*.class</include>
                    <include>**/*.xml</include>
                </includes>
                <filtering>false</filtering>
            </resource>
            <resource>
                <directory>src/main/resources</directory>
                <filtering>false</filtering>
                <includes>
                    <include>*.properties</include>
                    <include>*.xml</include>
                    <include>static/**</include>
                    <include>i18n/**</include>
                    <include>templates/**</include>
                </includes>
            </resource>
        </resources>
    </build>
</project>
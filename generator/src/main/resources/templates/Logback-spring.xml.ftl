<?xml version="1.0" encoding="UTF-8"?>
<configuration debug="false" scan="true" scanPeriod="10 seconds">
    <include resource="org/springframework/boot/logging/logback/defaults.xml"/>
    <springProperty source="spring.application.name" name="appName" scope="context"/>
    <springProperty source="spring.profiles.active" name="profileName" scope="context"/>
    <!-- -->
    <contextName>logback</contextName>
    <!-- 定义日志文件存储地址 -->
    <property name="LOG_HOME" value="./logs"/>

    <!-- 控制台输出 -->
    <appender name="console" class="ch.qos.logback.core.ConsoleAppender">
        <filter class="ch.qos.logback.classic.filter.ThresholdFilter">
            <level>debug</level>
        </filter>
        <encoder>
            <pattern>${'$'}{CONSOLE_LOG_PATTERN}</pattern>
            <charset>UTF-8</charset>
        </encoder>
    </appender>

    <!-- 文件输出-debug -->
    <appender name="debug_file" class="ch.qos.logback.core.rolling.RollingFileAppender">
        <file>${'$'}{LOG_HOME}/${'$'}{appName}-${'$'}{profileName}-debug.log</file>
        <encoder>
            <pattern>${'$'}{FILE_LOG_PATTERN}</pattern>
            <charset>UTF-8</charset>
        </encoder>
        <rollingPolicy class="ch.qos.logback.core.rolling.SizeAndTimeBasedRollingPolicy">
            <fileNamePattern>${'$'}{LOG_HOME}/${'$'}{appName}-${'$'}{profileName}-debug.log-%d{yyyy-MM-dd}-%i</fileNamePattern>
            <maxFileSize>20MB</maxFileSize>
            <maxHistory>2</maxHistory>
            <totalSizeCap>200MB</totalSizeCap>
        </rollingPolicy>
        <filter class="ch.qos.logback.classic.filter.LevelFilter">
            <level>DEBUG</level>
            <onMatch>ACCEPT</onMatch>
            <onMismatch>DENY</onMismatch>
        </filter>
    </appender>
    <appender name="debug_async_file" class="ch.qos.logback.classic.AsyncAppender">
        <appender-ref ref="debug_file"/>
    </appender>

    <!-- 文件输出-info -->
    <appender name="info_file" class="ch.qos.logback.core.rolling.RollingFileAppender">
        <file>${'$'}{LOG_HOME}/${'$'}{appName}-${'$'}{profileName}-info.log</file>
        <encoder>
            <pattern>${FILE_LOG_PATTERN}</pattern>
            <charset>UTF-8</charset>
        </encoder>
        <rollingPolicy class="ch.qos.logback.core.rolling.SizeAndTimeBasedRollingPolicy">
            <fileNamePattern>${'$'}{LOG_HOME}/${'$'}{appName}-${'$'}{profileName}-info.log-%d{yyyy-MM-dd}-%i</fileNamePattern>
            <maxFileSize>20MB</maxFileSize>
            <maxHistory>2</maxHistory>
            <totalSizeCap>200MB</totalSizeCap>
        </rollingPolicy>
        <filter class="ch.qos.logback.classic.filter.LevelFilter">
            <level>INFO</level>
            <onMatch>ACCEPT</onMatch>
            <onMismatch>DENY</onMismatch>
        </filter>
    </appender>
    <appender name="info_async_file" class="ch.qos.logback.classic.AsyncAppender">
        <appender-ref ref="info_file"/>
    </appender>

    <!-- 文件输出-warn -->
    <appender name="warn_file" class="ch.qos.logback.core.rolling.RollingFileAppender">
        <file>${'$'}{LOG_HOME}/${'$'}{appName}-${'$'}{profileName}-warn.log</file>
        <encoder>
            <pattern>${'$'}{FILE_LOG_PATTERN}</pattern>
            <charset>UTF-8</charset>
        </encoder>
        <rollingPolicy class="ch.qos.logback.core.rolling.SizeAndTimeBasedRollingPolicy">
            <fileNamePattern>${'$'}{LOG_HOME}/${'$'}{appName}-${'$'}{profileName}-warn.log-%d{yyyy-MM-dd}-%i</fileNamePattern>
            <maxFileSize>20MB</maxFileSize>
            <maxHistory>2</maxHistory>
            <totalSizeCap>200MB</totalSizeCap>
        </rollingPolicy>
        <filter class="ch.qos.logback.classic.filter.LevelFilter">
            <level>WARN</level>
            <onMatch>ACCEPT</onMatch>
            <onMismatch>DENY</onMismatch>
        </filter>
    </appender>
    <appender name="warn_async_file" class="ch.qos.logback.classic.AsyncAppender">
        <appender-ref ref="warn_file"/>
    </appender>

    <!-- 文件输出-error -->
    <appender name="error_file" class="ch.qos.logback.core.rolling.RollingFileAppender">
        <file>${'$'}{LOG_HOME}/${'$'}{appName}-${'$'}{profileName}-error.log</file>
        <encoder>
            <pattern>${'$'}{FILE_LOG_PATTERN}</pattern>
            <charset>UTF-8</charset>
        </encoder>
        <rollingPolicy class="ch.qos.logback.core.rolling.SizeAndTimeBasedRollingPolicy">
            <fileNamePattern>${'$'}{LOG_HOME}/${'$'}{appName}-${'$'}{profileName}-error.log-%d{yyyy-MM-dd}-%i</fileNamePattern>
            <maxFileSize>20MB</maxFileSize>
            <maxHistory>2</maxHistory>
            <totalSizeCap>200MB</totalSizeCap>
        </rollingPolicy>
        <filter class="ch.qos.logback.classic.filter.LevelFilter">
            <level>ERROR</level>
            <onMatch>ACCEPT</onMatch>
            <onMismatch>DENY</onMismatch>
        </filter>
    </appender>
    <appender name="error_async_file" class="ch.qos.logback.classic.AsyncAppender">
        <appender-ref ref="error_file"/>
    </appender>

    <!-- 日志输出级别 -->
    <!-- 开发环境/测试环境 -->
    <springProfile name="dev,test">
        <root level="debug">
            <appender-ref ref="console"/>
            <appender-ref ref="debug_async_file"/>
            <appender-ref ref="info_async_file"/>
            <appender-ref ref="warn_async_file"/>
            <appender-ref ref="error_async_file"/>
        </root>
    </springProfile>

    <!-- 生成环境 -->
    <springProfile name="prod">
        <root level="info">
            <appender-ref ref="info_async_file"/>
            <appender-ref ref="warn_async_file"/>
            <appender-ref ref="error_async_file"/>
        </root>
    </springProfile>
</configuration>
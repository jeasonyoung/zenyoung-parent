#spring
spring:
  profiles:
    active: ${'$'}{spring_profiles_active:dev}
  application:
    name: zy-${serverName}-service
  lifecycle:
    timeout-per-shutdown-phase: 10s
  mvc:
    format:
      date: yyyy-MM-dd
      time: HH:mm:ss
      datetime: yyyy-MM-dd HH:mm:ss
  jackson:
    date-format: yyyy-MM-dd HH:mm:ss
    time-zone: GMT+8
    default-property-inclusion: NON_NULL
#log
logging:
  level:
    org.springframework.scheduling: 'INFO'
    org.apache.http: 'debug'
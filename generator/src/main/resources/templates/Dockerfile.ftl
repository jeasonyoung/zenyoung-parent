FROM amazoncorretto:8u332-alpine3.15-jre
RUN /bin/cp /usr/share/zoneinfo/Asia/Shanghai /etc/localtime && echo 'Asia/Shanghai' >/etc/timezone
COPY ./target/${serverName}-service*.jar /opt/${serverName}-1.0.0.jar
ADD entrypoint.sh /bin/
ENTRYPOINT ["/bin/entrypoint.sh"]
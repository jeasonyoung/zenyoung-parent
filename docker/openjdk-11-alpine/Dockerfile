FROM alpine:3.9
MAINTAINER yangyong "young1982@foxmail.com"
RUN echo "https://mirrors.aliyun.com/alpine/v3.9/main" >> /etc/apk/repositories && \
    echo "https://mirrors.aliyun.com/alpine/v3.9/community" >> /etc/apk/repositories && \
    echo "https://mirrors.aliyun.com/alpine/edge/testing" >> /etc/apk/repositories && \
    apk update && \
    apk --no-cache add openjdk11-jdk --repository="http://mirrors.aliyun.com/alpine/edge/community" && \
    apk --no-cache add tzdata && \
    rm -rf /tmp/* /var/cache/apk/*
ENV TZ=Asia/Shanghai
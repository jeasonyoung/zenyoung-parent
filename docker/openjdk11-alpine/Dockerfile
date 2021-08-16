FROM alpine:3.9
MAINTAINER yangyong "young1982@foxmail.com"
#拷贝字体文件
COPY ./Powerline.ttf /usr/share/fonts/Powerline.ttf
#下载安装程序
RUN echo "https://mirrors.aliyun.com/alpine/v3.9/main" >> /etc/apk/repositories && \
    echo "https://mirrors.aliyun.com/alpine/v3.9/community" >> /etc/apk/repositories && \
    echo "https://mirrors.aliyun.com/alpine/edge/testing" >> /etc/apk/repositories && \
    apk update && \
    apk --no-cache add openjdk11-jdk --repository="http://mirrors.aliyun.com/alpine/edge/community" && \
    apk --no-cache add tzdata && \
    apk --no-cache add ttf-dejavu fontconfig && \
    rm -rf /tmp/* /var/cache/apk/*
#环境变量
ENV TZ=Asia/Shanghai
ENV LANG=en_US.UTF-8
ENV LANGUAGE=en_US:en
ENV LC_ALL=en_US.UTF-8
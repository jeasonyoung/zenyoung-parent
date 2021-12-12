FROM alpine:latest
MAINTAINER yangyong "young1982@foxmail.com"
#支持中文字体(微软雅黑/宋体)
COPY ./fonts/*.* /usr/share/fonts/
#时区/语言
ENV TZ=Asia/Chongqing LANG=en_US.UTF-8 LANGUAGE=en_US:en LC_ALL=en_US.UTF-8
#国内镜像/更新/时区/字体
RUN sed -i 's/dl-cdn.alpinelinux.org/mirrors.aliyun.com/g' /etc/apk/repositories \
    && apk update \
    && apk add --no-cache -U tzdata \
    && cp /usr/share/zoneinfo/${TZ} /etc/localtime \
    #清理
    && rm -rf /var/cache/apk/*
#!/bin/bash
#
java -Xmx${'$'}{xmx:-3072}m -Xms${'$'}{xms:-3072}m -Xmn${'$'}{xmn:-1024}m -XX:MetaspaceSize=256m -XX:MaxMetaspaceSize=256m -jar  /opt/${serverName}-1.0.0.jar

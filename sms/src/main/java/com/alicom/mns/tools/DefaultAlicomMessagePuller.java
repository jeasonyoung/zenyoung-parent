package com.alicom.mns.tools;

import com.aliyun.mns.client.CloudQueue;
import com.aliyun.mns.model.Message;
import com.aliyuncs.exceptions.ClientException;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.CollectionUtils;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * MNS Puller
 *
 * @author aliyun
 */
@Slf4j
public class DefaultAlicomMessagePuller {
    private MessageListener messageListener;
    private boolean isRunning = false;
    @Getter
    @Setter
    private Integer sleepMillsWhenNoData = 3000;
    @Getter
    private Integer consumeMinThreadSize = 6;
    @Getter
    private Integer consumeMaxThreadSize = 16;
    @Getter
    private Integer pullMsgThreadSize = 1;
    @Getter
    private Integer threadQueueSize = 200;
    private boolean debugLogOpen = false;

    public void startReceiveMsg(final String accessKeyId, final String accessKeySecret, final String messageType,
                                final String queueName, final MessageListener messageListener) {
        final TokenGetterForAlicom tokenGetterForAlicom = new TokenGetterForAlicom(accessKeyId, accessKeySecret);
        this.startReceiveMsg(tokenGetterForAlicom, messageType, queueName, messageListener);
    }

    public void startReceiveMsg(final TokenGetterForAlicom tokenGetter, final String messageType,
                                final String queueName, final MessageListener messageListener) {
        tokenGetter.init(null);
        this.messageListener = messageListener;
        this.isRunning = true;
        final PullMessageTask task = new PullMessageTask(this, tokenGetter, messageType, queueName);
        for (int i = 0; i < this.pullMsgThreadSize; ++i) {
            final Thread thread = new Thread(task, "PullMessageTask-thread-" + i);
            thread.start();
        }
    }

    public void startReceiveMsgWithRegion(final TokenGetterForAlicom tokenGetter, final RegionEnum regionEnum,
                                          final String messageType, final String queueName, MessageListener messageListener) {
        tokenGetter.init(regionEnum);
        this.messageListener = messageListener;
        this.isRunning = true;
        final PullMessageTask task = new PullMessageTask(this, tokenGetter, messageType, queueName);
        for (int i = 0; i < this.pullMsgThreadSize; ++i) {
            final Thread thread = new Thread(task, "PullMessageTask-thread-" + i);
            thread.start();
        }
    }

    public void startReceiveMsgForVpc(final TokenGetterForAlicom tokenGetter, final RegionEnum regionEnum, final String messageType,
                                      final String queueName, final MessageListener messageListener) throws ClientException {
        tokenGetter.initForVpc(regionEnum.getRegionId());
        this.messageListener = messageListener;
        this.isRunning = true;
        final PullMessageTask task = new PullMessageTask(this, tokenGetter, messageType, queueName);
        for (int i = 0; i < this.pullMsgThreadSize; ++i) {
            final Thread thread = new Thread(task, "PullMessageTask-thread-" + i);
            thread.start();
        }
    }

    public void startReceiveMsgForPartnerUser(final TokenGetterForAlicom tokenGetter, final RegionEnum regionEnum, final Long ownerId,
                                              final String messageType, final String queueName, final MessageListener messageListener) {
        tokenGetter.setOwnerId(ownerId);
        tokenGetter.init(regionEnum);
        this.messageListener = messageListener;
        this.isRunning = true;
        final PullMessageTask task = new PullMessageTask(this, tokenGetter, messageType, queueName);
        for (int i = 0; i < this.pullMsgThreadSize; ++i) {
            final Thread thread = new Thread(task, "PullMessageTask-thread-" + i);
            thread.start();
        }
    }

    public void stop() {
        this.isRunning = false;
    }

    public void openDebugLog(final boolean debugLogOpen) {
        this.debugLogOpen = debugLogOpen;
    }

    public void setConsumeMinThreadSize(final Integer consumeMinThreadSize) {
        if (consumeMinThreadSize != null && consumeMinThreadSize > 0) {
            this.consumeMinThreadSize = consumeMinThreadSize;
        }
    }

    public void setConsumeMaxThreadSize(final Integer consumeMaxThreadSize) {
        if (consumeMaxThreadSize != null && consumeMaxThreadSize > 0) {
            this.consumeMaxThreadSize = consumeMaxThreadSize;
        }
    }

    public void setPullMsgThreadSize(final Integer pullMsgThreadSize) {
        if (pullMsgThreadSize != null && pullMsgThreadSize > 1) {
            this.pullMsgThreadSize = pullMsgThreadSize;
        }
    }

    public void setThreadQueueSize(final Integer threadQueueSize) {
        if (threadQueueSize != null && threadQueueSize > 0 && threadQueueSize < 20) {
            this.threadQueueSize = threadQueueSize;
        }
    }

    @RequiredArgsConstructor
    private static class PullMessageTask implements Runnable {
        private static final ThreadLocal<DateFormat> LOCAL_FORMAT = ThreadLocal.withInitial(
                () -> new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        );

        private final DefaultAlicomMessagePuller puller;
        private final TokenGetterForAlicom tokenGetterForAlicom;
        private final String messageType;
        private final String queueName;

        @Override
        public void run() {
            final ExecutorService cachedThreadPool = new ThreadPoolExecutor(puller.consumeMinThreadSize,
                    puller.consumeMaxThreadSize, 30L, TimeUnit.MINUTES,
                    new ArrayBlockingQueue<>(puller.threadQueueSize)
            );
            while (puller.isRunning) {
                try {
                    final TokenForAlicom tokenObject = this.tokenGetterForAlicom.getTokenByMessageType(this.messageType, this.queueName);
                    final CloudQueue queue = tokenObject.getQueue();
                    if (puller.debugLogOpen) {
                        final DateFormat format = LOCAL_FORMAT.get();
                        log.warn(Thread.currentThread().getName() + "-popStart at ," + format.format(new Date()));
                    }
                    final List<Message> popMsg = queue.batchPopMessage(16);
                    if (puller.debugLogOpen) {
                        final DateFormat formatx = LOCAL_FORMAT.get();
                        log.warn(Thread.currentThread().getName() + "-popDone at ," + formatx.format(new Date()) + " msgSize=" + (popMsg == null ? 0 : popMsg.size()));
                    }
                    if (!CollectionUtils.isEmpty(popMsg)) {
                        for (final Message message : popMsg) {
                            cachedThreadPool.execute(() -> {
                                if (puller.debugLogOpen) {
                                    final DateFormat format = LOCAL_FORMAT.get();
                                    log.warn(message.getMessageId() + ",receive," + format.format(new Date()));
                                }
                                final boolean dealResult = puller.messageListener.dealMessage(message);
                                if (puller.debugLogOpen) {
                                    final DateFormat formatx = LOCAL_FORMAT.get();
                                    log.warn(message.getMessageId() + ",consumeResult" + dealResult + "," + formatx.format(new Date()));
                                }
                                if (dealResult) {
                                    queue.deleteMessage(message.getReceiptHandle());
                                }
                            });
                        }
                    }
                } catch (Exception e) {
                    log.error("PullMessageTask_execute_error,messageType:" + this.messageType + ",queueName:" + this.queueName, e);
                    try {
                        Thread.sleep(puller.sleepMillsWhenNoData);
                    } catch (InterruptedException ex) {
                        Thread.currentThread().interrupt();
                        log.error("PullMessageTask_execute_error,messageType:" + this.messageType + ",queueName:" + this.queueName, ex);
                    }
                }
            }
        }
    }
}

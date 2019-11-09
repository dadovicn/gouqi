package com.cjyw.gouqi.core.report.mq;

import com.cjyw.gouqi.util.config.EnvConfig;
import com.rabbitmq.client.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class TargetReport extends AbstractNotify {
    private static final Logger log = LoggerFactory.getLogger(AbstractNotify.class);
    private static Channel c = getAlarmChannel();
    // fixme 参数非必传
    public static void notifyCloud(EnvConfig.RabbitConfig config, String msg) {
        if (c != null) {
            if (config != null) {
                try {
                    if(c.isOpen()) {
                        log.debug("目标推送成功: {}", msg);
                        c.basicPublish(config.radarQueueExchange, config.radarQueueRoutingKey, null, msg.getBytes());
                    } else {
                        log.error("连接丢失");
                    }
                } catch (IOException e) {
                    log.error("离线消息推送失败: IO异常");
                }
            } else {
                log.error("rabbit MQ 配置未加载");
            }
        } else {
            log.error("rabbitMQ 离线通知 channel 为空");
        }
    }
}

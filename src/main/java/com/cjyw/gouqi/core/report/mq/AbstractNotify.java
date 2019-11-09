package com.cjyw.gouqi.core.report.mq;

import com.cjyw.gouqi.util.config.EnvConfig;
import com.cjyw.gouqi.util.config.PropertiesSource;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

public class AbstractNotify {
    private static final Logger log = LoggerFactory.getLogger(AbstractNotify.class);
    private static Connection connection = null;
    /** 故障告警通道 */
    private static Channel channel = null;
    private static EnvConfig env;

    static {
        env = PropertiesSource.INSTANCE.getConfig();
        try {
            ConnectionFactory factory = new ConnectionFactory();
            factory.setHost(env.rabbitConfig.host);
            factory.setPort(Integer.valueOf(env.rabbitConfig.port));
            factory.setUsername(env.rabbitConfig.userName);
            factory.setPassword(env.rabbitConfig.password);
            factory.setAutomaticRecoveryEnabled(true);
            factory.setNetworkRecoveryInterval(30); // 设置断线重连
            factory.setTopologyRecoveryEnabled(false);  // 不重新声明交换器/队列等
            connection  = factory.newConnection();
            channel = connection.createChannel();
            channel.addShutdownListener(cause -> cause.printStackTrace());
            channel.exchangeDeclare(env.rabbitConfig.radarQueueExchange,"direct", true, false, false, null );
            channel.queueDeclare(env.rabbitConfig.radarQueueName, true, false, false, null);
            channel.queueBind(env.rabbitConfig.radarQueueName, env.rabbitConfig.radarQueueExchange, env.rabbitConfig.radarQueueRoutingKey);
            log.debug("rabbit状态 --> 初始化 RabbitMQ 成功");
        } catch (TimeoutException e) {
            log.error("rabbit状态 --> 初始化 RabbitMQ 超时");
        } catch (IOException e) {
            e.printStackTrace();
            log.error("rabbit状态 --> 初始化 RabbitMQ 出现 IO 异常");
        }
    }

    public static Channel getAlarmChannel() {
        return channel;
    }
}

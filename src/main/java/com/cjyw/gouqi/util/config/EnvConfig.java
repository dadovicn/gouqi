package com.cjyw.gouqi.util.config;

public class EnvConfig {
    public RabbitConfig rabbitConfig;

    public static class RabbitConfig {
        public String host;
        public String port;
        public String userName;
        public String password;
        public String virtualHost;
        public String radarQueueName;
        public String radarQueueExchange;
        public String radarQueueRoutingKey;

        @Override
        public String toString() {
            return "RabbitConfig{" +
                    "host='" + host + '\'' +
                    ", port='" + port + '\'' +
                    ", userName='" + userName + '\'' +
                    ", password='" + password + '\'' +
                    ", virtualHost='" + virtualHost + '\'' +
                    ", radarQueueName='" + radarQueueName + '\'' +
                    ", radarQueueExchange='" + radarQueueExchange + '\'' +
                    ", radarQueueRoutingKey='" + radarQueueRoutingKey + '\'' +
                    '}';
        }
    }
}

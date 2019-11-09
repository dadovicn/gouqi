package com.cjyw.gouqi.fake;


import com.corundumstudio.socketio.Configuration;
import com.corundumstudio.socketio.SocketConfig;
import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.SocketIOServer;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.UUID;


/**
 * 模拟发射数据
 */
public class Fake {

    public static void test () throws IOException, InterruptedException {
        FileReader fr = new FileReader("/Users/duandongdong/daodao/open/gouqi/src/main/resources/ds/radar.txt");
        BufferedReader br=new BufferedReader(fr);
        String line = "";
        Thread.sleep(20000);
        while ((line=br.readLine())!=null) {
            String tmp =line.split("-->")[1];
            String canId = tmp.split(",")[0].split(":")[1];
            String con = tmp.split(",")[1].split(":")[1];
            String range = tmp.split(",")[2].split(":")[1];
            String radar = (canId + ": " + con + ":" + range);
            HashMap<UUID, SocketIOClient> userClient = ClientCache.getUserClient("dadovicn");
            userClient.forEach((uuid, socketIOClient) -> {
                socketIOClient.sendEvent("dadovicn", radar);
            });
            Thread.sleep(1000);
        }
        br.close();
        fr.close();
    }

    public static void init() throws IOException, InterruptedException {
        Configuration config = new Configuration();
        config.setPort(8080);
        SocketConfig socketConfig = new SocketConfig();
        socketConfig.setReuseAddress(true);
        socketConfig.setTcpNoDelay(true);
        socketConfig.setSoLinger(0);
        config.setSocketConfig(socketConfig);
        config.setHostname("localhost");
        SocketIOServer server = new SocketIOServer(config);
        server.addListeners(new EventListenner());
        server.start();
        System.out.println("启动正常");
        test();

    }
}

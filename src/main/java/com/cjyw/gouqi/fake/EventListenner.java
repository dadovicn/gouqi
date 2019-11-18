//package com.cjyw.gouqi.fake;
//
//import com.corundumstudio.socketio.AckRequest;
//import com.corundumstudio.socketio.SocketIOClient;
//import com.corundumstudio.socketio.annotation.OnConnect;
//import com.corundumstudio.socketio.annotation.OnDisconnect;
//import com.corundumstudio.socketio.annotation.OnEvent;
//
//import java.util.UUID;
//
///**
// * 事件监听器
// * @author dadovicn
// */
//public class EventListenner {
//
//    @OnConnect
//    public void onConnect(SocketIOClient client) {
//        String userId = client.getHandshakeData().getSingleUrlParam("userId");
//        UUID sessionId = client.getSessionId();
//        ClientCache.saveClient(userId,sessionId,client);
//        System.out.println("建立连接");
//    }
//
//    @OnDisconnect
//    public void onDisconnect(SocketIOClient client) {
//        String userId = client.getHandshakeData().getSingleUrlParam("userId");
//        ClientCache.deleteSessionClient(userId,client.getSessionId());
//        System.out.println("关闭连接");
//    }
//
//    //消息接收入口，当接收到消息后，查找发送目标客户端，并且向该客户端发送消息，且给自己发送消息
//    // 暂未使用
//    @OnEvent("messageevent")
//    public void onEvent(SocketIOClient client, AckRequest request) {
//    }
//}
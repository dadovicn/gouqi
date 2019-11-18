//package com.cjyw.gouqi.fake;
//
//import com.corundumstudio.socketio.SocketIOClient;
//import java.util.HashMap;
//import java.util.Map;
//import java.util.UUID;
//import java.util.concurrent.ConcurrentHashMap;
//
///**
// * 客户端缓存
// * @author dadovicn
// */
//public class ClientCache {
//
//    //本地缓存
//    private static Map<String, HashMap<UUID, SocketIOClient>> concurrentHashMap= new ConcurrentHashMap<>();
//
//    /**
//     * 存入本地缓存
//     * @param userId 用户ID
//     * @param sessionId 页面sessionID
//     * @param socketIOClient 页面对应的通道连接信息
//     */
//    public static void saveClient(String userId, UUID sessionId,SocketIOClient socketIOClient){
//        HashMap<UUID, SocketIOClient> sessionIdClientCache=concurrentHashMap.get(userId);
//        if(sessionIdClientCache==null){
//            sessionIdClientCache = new HashMap<>();
//        }
//        sessionIdClientCache.put(sessionId,socketIOClient);
//        concurrentHashMap.put(userId,sessionIdClientCache);
//    }
//
//    /**
//     * 根据用户ID获取所有通道信息
//     * @param userId
//     * @return
//     */
//    public static HashMap<UUID, SocketIOClient> getUserClient(String userId){
//        return concurrentHashMap.get(userId);
//    }
//
//    /**
//     * 根据用户ID及页面sessionID删除页面链接信息
//     * @param userId
//     * @param sessionId
//     */
//    public static void deleteSessionClient(String userId,UUID sessionId){
//        concurrentHashMap.get(userId).remove(sessionId);
//    }
//}
//
//

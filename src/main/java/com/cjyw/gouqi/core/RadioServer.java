package com.cjyw.gouqi.core;

import com.cjyw.gouqi.util.Convertor;
import com.google.common.primitives.Longs;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.FixedLengthFrameDecoder;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.AttributeKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Stream;

/**
 * 数据解析
 * @author dadovicn
 * @date   2019/3/11
 */
public class RadioServer {
    private static final Logger log = LoggerFactory.getLogger(RadioServer.class);
    private static Map<String,Boolean> clientStatusMap = new ConcurrentHashMap<>();
    private static final String id = "[usr-can-net-200-RadioServer]";
    private static final AttributeKey<String> CLIENT_ID = AttributeKey.valueOf("channel.clientId");
    private static AtomicLong a = new AtomicLong(0);
    private static AtomicLong now = new AtomicLong(System.currentTimeMillis());
    public void run(Integer port) throws Exception {
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        ServerBootstrap b = new ServerBootstrap();
        b.group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
                .option(ChannelOption.SO_BACKLOG, 1024)
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        ChannelPipeline pipeline = ch.pipeline();
                        pipeline.addFirst(new IdleStateHandler(5, 111110, 0));
                        pipeline.addLast(new FixedLengthFrameDecoder(13));
                        pipeline.addLast("SerialServer", new ChannelInboundHandlerAdapter() {
                            @Override
                            public void channelActive(ChannelHandlerContext ctx) throws Exception {
                                now.set(System.currentTimeMillis());
                                ctx.fireChannelActive();
                                String clientId = ctx.channel().remoteAddress().toString();
                                log.debug("{} 有客户端 {} 连入",id, clientId);
                                ctx.channel().attr(CLIENT_ID).setIfAbsent(clientId);
                                clientStatusMap.put((clientId), true);
                            }
                            @Override
                            public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
//                                log.info("收到消息: {} - {}", a.get(), msgToHex(msg));
                                ByteBuf byteBuf = (ByteBuf) msg;
                                parse(byteBuf);
                                // todo release bytebuf
                                byteBuf.release();
                                a.getAndAdd(1);
                            }
                            @Override
                            public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
                                clientStatusMap.put((ctx.channel().attr(CLIENT_ID).get()), serverIdle(ctx, evt, id));
                            }
                            @Override
                            public void channelInactive(ChannelHandlerContext ctx) throws Exception {
                                clientStatusMap.put((ctx.channel().attr(CLIENT_ID).get()), false);
                                log.debug("{} (usr-can-net-200 服务器): {} 瓦力下线", id, ctx.channel().remoteAddress());
                            }
                        });
                    }
                });

        ChannelFuture f = b.bind(port).sync();
        // 心里的话, 我想要带你回家
        // 在那深夜酒吧, 哪管他是真是假
        // 请你尽情摇摆, 忘记中意的他
        // 你是最迷人噶, 你知道吗
        log.debug("{} 启动成功", id);
    }

    protected boolean serverIdle(ChannelHandlerContext ctx, Object evt, String id) {
        if (evt instanceof IdleStateEvent) {
            IdleStateEvent e = (IdleStateEvent) evt;
            switch (e.state()) {
                case READER_IDLE:
                    // todo 客户端连接丢失
                    log.error("{} 与客户端 {} 之间的心跳丢失", id, ctx.channel().remoteAddress());
                    ctx.close();
                    return false;
                default:
                    return true;
            }
        }
        return true;
    }

    protected String msgToHex(Object msg) {
        ByteBuf byteBuf = (ByteBuf) msg;
        return ByteBufUtil.hexDump(byteBuf);
    }

    public void parse(ByteBuf byteBuf) {
        // 1. canId 解析
        byte[] r = new byte[13];
        byteBuf.getBytes(0, r);
        byte[] canIds = new byte[4];
        byteBuf.getBytes(1, canIds, 0, 4);
        log.debug("canId-HEX: {}", Convertor.bytesToHex(canIds));
        int canId = Convertor.bytesToInt(canIds, ByteOrder.BIG_ENDIAN);
        log.debug("canId-DEC: {}", canId);
        byte[] data = new byte[8];
        byteBuf.getBytes(5, data, 0, 8);
        List<Long> target = Longs.asList(Stream.of(Convertor.bytesToBinary(data).split("")).mapToLong(Long::parseLong).toArray());
        if(canId >= 1024 && canId <= 1087) {
            msgIndex1(target, canId, Convertor.bytesToHex(data));
        } else if(canId >= 1280 && canId <= 1343) {
            msgIndex2(target);
        }
    }

    private void msgIndex1(List<Long> t, int canId, String source) {
        // 轨迹状态 0: empty, 1: first detected 2: first detected 3: valid, 10: invalid
        List<Long> track = new ArrayList<Long>() {{ addAll(t.subList(0, 4)); }};
        Collections.reverse(track);
        Long trackValue = compute(track);
        // 帧号后四位
        log.debug("帧号后四位: {}", t.subList(4, 8));
        // 目标置信度
        List<Long> confidence =  t.subList(8, 15);
        Collections.reverse(confidence);
        Long confidenceValue = compute(confidence);

        log.debug("预留: {}", t.subList(15, 15)); // 预留

        Long angleValue = val(t.subList(16, 24), t.subList(29, 32));
        String angleStr = String.format("%.2f", Double.valueOf(angleValue * 0.1d - 102.4d));
        log.debug("目标水平角度: {}, 原始值: {}", angleStr, angleValue);

        Long rangeValue = val(t.subList(24, 29), t.subList(32, 40));
        String rangeStr = String.format("%.2f", Double.valueOf(rangeValue) * 0.05);
        log.debug("目标-{}-相对距离: {}, 原始值: {}", canId, rangeStr , rangeValue);

        long powerValue = val(t.subList(40, 48), t.subList(54, 56));
        String powerStr = String.format("%.2f", Double.valueOf(powerValue) * 0.1d);
        log.debug("目标强度: {}, 原始值: {}", powerStr, powerValue);

        long rateValue = val(t.subList(48, 54), t.subList(56, 64));
        String rateStr = String.format("%.2f", Double.valueOf(rateValue) * 0.02d - 163.84d);
        if(trackValue.intValue() == 3 && confidenceValue.intValue() > 30 && (Double.valueOf(rateValue) * 0.02d - 163.84d) > 15) {
            log.info("| {}=======================================================================================>>>>|", a.get());
            log.info("|= 位图({}个): {}", t.size(), t);
            log.info("|= 原始字节hex: {}", source);
            log.info("|= 目标轨迹状态值: {}, canId: {}, 置信度: {}, 水平角度: {}", trackValue, canId, confidenceValue, angleStr);
            log.info("|= 目标-{}-相对径向速度: {}, 原始值: {} || 目标相对距离: {}, 原始值: {}", canId, rateStr, rateValue, rangeStr, rangeValue);
            log.info("|<<<<=======================================================================================|");
        }
    }

    private long compute(List<Long> res) {
        return Long.valueOf(String.valueOf(res.stream().reduce((x,y) -> Long.valueOf(x + String.valueOf(y))).get()), 2);
    }

    private long val(List<Long> big, List<Long> end) {
        Collections.reverse(big);
        Collections.reverse(end);
        List<Long> sort = new ArrayList<Long>() {{
            addAll(big);
            addAll(end);
        }};
        return compute(sort);
    }

    private void msgIndex2(List<Long> t) {
        log.debug("检测到index2: -> ");
        log.debug("帧号后四位: {}", t.subList(4, 7)); // 帧号后四位
        log.debug("预留: {}", t.subList(0, 3)); // 预留
        log.debug("预留: {}", t.subList(8, 21)); // 预留
        log.debug("目标更新模式: {}", t.subList(22, 23)); // 目标更新模式
        log.debug("预留: {}", t.subList(24, 31)); // 预留
        log.debug("预留: {}", t.subList(35, 39)); // 预留
        log.debug("目标俯仰角度-1: {}", t.subList(32, 34)); // 目标俯仰角度
        log.debug("目标俯仰角度-2: {}", t.subList(42, 47)); //
        log.debug("目标相对径向加速度-1: {}", t.subList(40, 41)); // 目标相对径向加速度
        log.debug("目标相对径向加速度-2: {}", t.subList(48, 55)); //
        log.debug("目标相对横向速度: {}", t.subList(56, 63)); // 目标相对横向速度
    }

}

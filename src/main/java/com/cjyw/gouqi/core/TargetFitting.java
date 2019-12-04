package com.cjyw.gouqi.core;

import com.alibaba.fastjson.JSON;
import com.cjyw.gouqi.core.report.mq.TargetReport;
import com.cjyw.gouqi.entity.Target;
import com.cjyw.gouqi.util.config.PropertiesSource;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static java.util.Map.*;

/**
 * 目标拟合算法
 */
public class TargetFitting {
    private static final double  ANGLE_SPLIT = 0d;
    private static final Logger log = LoggerFactory.getLogger(TargetFitting.class);
    private static AtomicInteger cartId = new AtomicInteger(0);


    /** <k, range> */
    private static Cache<Integer, Double> fitMap = CacheBuilder.newBuilder()
            .maximumSize(32).expireAfterAccess(1000, TimeUnit.MILLISECONDS).build();

    /**
     * 目标跟踪
     * @param cur 当前目标
     */
    public static void trace(Target cur) {
        if(Math.abs(cur.getAngleVal()) > 5) {
            if(cur.getAngleVal() < 0) {
                cur.setAngleVal(-1.5);
            } else {
                cur.setAngleVal(1.5);
            }
        }
        if(cur.getTraceVal().intValue() == 1 && cur.getRangeVal() < 60) {
            // 目标出现

            if(fitMap.size() == 0) {
                cartId.set(0);
                fitMap.put(cartId.addAndGet(1), cur.getRangeVal());
                // 第一个目标
            } else {
                if(checkTargetCart(cur.getRangeVal())) {
                    // 新目标出现
                    if(fitMap.asMap().size() >= 2) {
                        launderTarget();
                    }

                    log.info("新目标出现: ", cartId.toString() );
                    fitMap.put(cartId.addAndGet(1), cur.getRangeVal());
                    if(fitMap.asMap().size() >= 2) {
                        launderTarget();
                    }
                } else {
                    // 已有目标, 更新目标
                    log.info("先找目标, 然后更新, 更新模式");
                    updatePosition(cur.getRangeVal(), cur);
                }
            }



        }
        if(cur.getTraceVal().intValue() == 3) {
            // 更新距离
            // 已经被跟踪的目标
            updatePosition(cur.getRangeVal(), cur);
            updatePosition(cur.getRangeVal(), cur);
        }
    }



    /**
     * 检查大车目标
     * @param range
     * @return
     */
    public static boolean checkTargetCart(Double range) {
        Optional<Entry<Integer, Double>> lastCur = fitMap.asMap().entrySet().stream().reduce((x, y) -> x.getValue() < y.getValue() ? x: y);
        if(lastCur.isPresent()) {
            if((range -lastCur.get().getValue()) > 20) {
                return range < lastCur.get().getValue();
            }
        }
        return false;
    }

    /**
     * 检查是否为新目标
     * @return true: 新目标, false 已有目标
     */
    public static boolean checkTarget(Double range, Cache<Integer, Double> cache) {
        Optional<Entry<Integer, Double>> lastCur = cache.asMap().entrySet().stream().reduce((x, y) -> x.getValue() < y.getValue() ? x: y);
        if(lastCur.isPresent()) {
            return range < lastCur.get().getValue();
        }
        return false;
    }

    /**
     * 找到 k , 并更新 range
     * @param range
     * @param cur
     * @return
     */
    public static void updatePosition(Double range, Target cur) {
        // 先找是属于哪边的车

        // 规约开始
        if(range > 150.0) {
            // 直接丢掉
            return;
        }
        Optional<Entry<Integer, Double>> max = fitMap.asMap().entrySet().stream().reduce((x, y) -> x.getValue() < y.getValue() ? y: x);
        if(max.isPresent()) {
            double maxRange = max.get().getValue();
            int pos = max.get().getKey();
            if(range > maxRange) {
                cur.setCanId(pos);
                log.info(cur.toString());
                fitMap.put(pos, range);
                TargetReport.notifyCloud(PropertiesSource.INSTANCE.getConfig().rabbitConfig, JSON.toJSONString(cur));
            }
        }
        fitMap.put(0, 0d);
        fitMap.asMap().entrySet().stream().reduce((x, y) -> {
            if (x.getValue() < range && range < y.getValue()) {
                // 更新目标位置
                if(x.getKey() != 0) {
                    fitMap.put(x.getKey(), range);
                    cur.setCanId(x.getKey());
                    log.info(cur.toString());
                    TargetReport.notifyCloud(PropertiesSource.INSTANCE.getConfig().rabbitConfig, JSON.toJSONString(cur));
                }
            }
            return y;
        });
        fitMap.invalidate(0);
    }

    /**
     * 目标清洗: 移除假目标
     */
    public static void launderTarget() {
        List<Integer> mqp = new ArrayList<>(10);
        log.info("移除目标: {}", mqp.toString());
        if(fitMap.asMap().size() > 2) {
            fitMap.asMap().entrySet().stream().reduce((x, y) -> {
                if(y.getValue() - x.getValue() < 5) {
                    mqp.add(x.getKey());
                }
                return y;
            });
        }
        mqp.forEach(i -> {
            fitMap.invalidate(i);
        });
    }



    /**
     * todo 滤波
     * @param cur 当前目标
     */
    public static boolean filter(Target cur) {
        return true;
    }

    /**
     * @deprecated 样本不行(不够), 无法区分
     * true: 行车道, false: 超车道
     * todo 每个雷达都要计算出最佳 {x}
     * @param angle 水平角度°
     * @return {@code true} if angle greater than {x} is {@code 0}, otherwise {@code false}
     *
     */
    public static boolean splitByAngle(double angle) {
        return angle > ANGLE_SPLIT;
    }
}